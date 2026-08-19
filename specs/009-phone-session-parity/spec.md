# Spec 009: Una sesión de teléfono igual a las demás

> Rama: `feature/009-phone-session-parity` · Proyecto: `BaseLogin` (`:baselogin`)
> Estado: implementado · Sale de `feature/008-demo-bundle-id`.
> Sin ticket FLE: sale de la auditoría de iOS.

## Contexto y objetivo

Entrar por SMS deja al usuario con una sesión distinta según la plataforma:

| | Android | iOS |
|---|---|---|
| `userId` | ✅ | ✅ |
| `email`, `displayName`, `photoUrl`, `isEmailVerified` | ✅ | ❌ siempre nulo o falso |

`Platform.android.kt` construye la sesión leyendo el `FirebaseUser`; `PhoneAuthProviderIOS` solo
recibe de Swift el `uid` y fabrica `UserSession(userId, email = null)`. Cualquier pantalla que pinte
el email o compruebe `isEmailVerified` se comporta distinto en cada plataforma para el mismo usuario,
y la que se equivoca es iOS.

Debajo hay un problema mayor: los dos puntos de entrada del teléfono son funciones `expect` de nivel
superior, así que **todo el flujo de teléfono es intesteable desde `commonTest`** — el mismo agujero
que FLE-90 cerró para el resto del provider y que dejó abierto aquí.

## Alcance

**Dentro:**

- Puerto `PhoneAuthPort` sobre `sendPhoneVerificationCode` y `verifyPhoneCode`.
- `verifyPhoneOtp` lee el usuario del gateway en vez de fiarse de lo que arme la plataforma.
- Tests del flujo de teléfono, que no existían.

**Fuera:**

- Cambiar cómo firma cada plataforma. Sigue siendo el SDK nativo: Android necesita una Activity e
  iOS registro de APNs, y eso no cabe detrás del gateway.
- Tocar `PhoneAuthProviderIOS` ni `Platform.android.kt`. El arreglo va en el sitio común, para que
  valga para las dos plataformas y para cualquier futura.
- La verificación con APNs y el fallback de reCAPTCHA, que ya entraron en el spec 004.

## Diseño

### Leer, no confiar

```kotlin
when (val result = phoneAuth.verifyCode(verificationId, otpCode)) {
    is AuthResult.Success -> runAuth { gateway.currentUser?.toSuccess() ?: result }
    else -> result
}
```

Cuando la plataforma dice que ha firmado, el usuario **ya está en Firebase**, así que la fuente de
verdad de sus datos es el gateway y no lo que cada `actual` haya conseguido rellenar. Es el mismo
patrón que ya usa `SocialTokenResult.PlatformHandled`, donde Swift firma y Kotlin vuelve a leer la
sesión.

El `?: result` mantiene el resultado de la plataforma si el gateway no ve usuario: preferible a
inventar un fallo cuando la plataforma acaba de decir que todo fue bien.

### El puerto

Tercer caso del mismo patrón (`SocialTokenProvider`, `SocialTokenRevoker`): una función `expect` no se
puede falsear, así que se envuelve. Con default en el constructor, así que nadie que construya
`FirebaseAuthProvider` a mano tiene que cambiar nada.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: La sesión de teléfono trae los mismos campos que las demás
  Given una plataforma que devuelve solo el uid
   And  un usuario en Firebase con email, nombre y email verificado
  When  se verifica el OTP
  Then  la sesión devuelta trae email, nombre y el flag de verificado

Scenario [AC-02]: El código llega a la plataforma tal cual
  Given un verificationId y un código
  When  se verifica el OTP
  Then  la plataforma los recibe sin tocar

Scenario [AC-03]: Un OTP rechazado se propaga
  Given una plataforma que devuelve Failure(InvalidCredentials)
  When  se verifica el OTP
  Then  el resultado es ese mismo fallo, sin convertirse en éxito

Scenario [AC-04]: El timeout configurado llega a la plataforma
  Given phoneAuthConfig.timeoutSeconds = 90
  When  se pide el código
  Then  la plataforma recibe 90
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | `009 verifyPhoneOtp returns the full user, not the platform stub` | **sí** |
| AC-02 | `009 verifyPhoneOtp forwards the code to the platform` | no — el puerto es refactor sin cambio de comportamiento; queda como guardia |
| AC-03 | `009 a rejected otp is propagated untouched` | no — guardia de la rama `else` |
| AC-04 | `009 sendPhoneOtp forwards the timeout to the platform` | no — guardia |

> Solo AC-01 cambia comportamiento y solo AC-01 nació rojo. Los otros tres son cobertura que antes
> era imposible escribir, y se apuntan como lo que son en vez de como TDD que no fue.

## Notas no funcionales

**API pública que se añade**: `PhoneAuthPort`, `PlatformPhoneAuthPort` y un quinto parámetro **con
default** en `FirebaseAuthProvider`. Nada se cambia ni se borra.

**Cambio de comportamiento visible**: en iOS, una sesión de teléfono pasa a traer email y nombre si el
usuario los tiene en Firebase. Es lo que un consumidor ya esperaba —Android lo hacía—, así que se
considera arreglo y no ruptura.
