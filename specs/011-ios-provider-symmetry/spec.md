# Spec 011: Simetría de los providers de iOS — `GoogleSignInProviderIOS` como `object`

> Rama: `feature/011-ios-provider-symmetry` · Proyecto: `BaseLogin` (`:custom-login` + demo + README)
> Estado: **implementado**. Sale de `feature/010-ios-build-hygiene`.
> Sin ticket FLE: cierra la causa de un hallazgo del spec 003.
> ⚠️ **Rompe a los hosts Swift ya integrados. Va en 2.0.0**, junto con el renombrado
> `custom-login` → `baselogin`, para que los consumidores migren una sola vez en vez de dos.

## Contexto y objetivo

El spec 003 corrigió `.companion` → `.shared` en el KDoc de los seis providers de iOS y en las siete
apariciones del README, porque los ejemplos que la librería publicaba **no compilaban**. Eso arregló
los síntomas y dejó la causa intacta.

La causa es que hay dos formas de provider donde debería haber una:

| Provider | Forma en Kotlin | Acceso desde Swift |
|---|---|---|
| `AppleSignInProviderIOS` | `object` | `.shared` |
| `GitHubSignInProviderIOS` | `object` | `.shared` |
| `MicrosoftSignInProviderIOS` | `object` | `.shared` |
| `TwitterSignInProviderIOS` | `object` | `.shared` |
| `FacebookSignInProviderIOS` | `object` | `.shared` |
| **`GoogleSignInProviderIOS`** | **`class` + `companion object`** | **`.companion` / `.Companion.shared`** |

Google es el único distinto, y por una razón que no tiene que ver con Swift: recibe
`GoogleSignInConfig` en el constructor. Como fue también el primero que existió, alguien copió su
bloque de ejemplo a los otros cinco y ahí nació la documentación falsa: en Google `.companion` era
correcto, en los demás nunca lo fue.

**La trampa sigue viva después del 003.** En esta rama base, el propio README usa las dos formas para
el mismo objeto con 24 líneas de diferencia:

```swift
README.md:609   GoogleSignInProviderIOS.companion.signInHandler = { clientId, completion in
README.md:633   GoogleSignInProviderIOS.Companion.shared.signOutHandler = {
```

Y el demo (`iosApp/iosApp/iOSApp.swift:75,80`) usa la segunda para las dos. Las dos compilan
—Kotlin/Native exporta un `companion object` de una clase por ambos caminos— y por eso nadie lo ha
visto: no es un fallo, es ruido que enseña al lector que hay dos contratos. El siguiente que copie el
bloque equivocado a un `object` repite exactamente el 003.

**Objetivo:** una sola manera de alcanzar cualquier provider de iOS desde Swift, `X.shared.…`, y que
el README no pueda volver a contradecirse.

## Alcance

**Dentro:**

- `GoogleSignInProviderIOS`: `class` + `companion object` → `object`. La `config` sale del
  constructor y entra en `signIn(config: GoogleSignInConfig)`, que es la forma que ya tiene
  `AppleSignInProviderIOS.signIn(scopes)`.
- `Platform.ios.kt:68`, único call site en Kotlin de todo el repo.
- README líneas 609 y 633, y el resto de la sección de Google, a la forma única.
- `iosApp/iosApp/iOSApp.swift:75,80`.
- `CLAUDE.md:94`, que hoy nombra el seam sin decir cómo se alcanza desde Swift.
- Decidir el destino de `getClientId()` y `getTopViewController()` — ver *Decisión abierta*.
- Declarar el break: la versión la fija la PR del renombrado (**2.0.0**); aquí va la nota explícita
  en el README, en una sección `Migrating to 2.0.0`.

**Fuera:**

- Los otros cinco providers. Ya son `object`; este spec los toma como referencia, no los toca.
- El formato del token social (`idToken|||accessToken|||…`). No cambia ni un separador.
- La lógica de sign-in. Es un cambio de **forma**, no de comportamiento: ni `PLATFORM_AUTH_HANDLED`,
  ni el `signOutHandler` del spec 006, ni la ruta de handler ausente cambian de semántica.
- `commonMain` y sus tests. Nada de esto cruza a código común.

## Diseño

### El cambio

```kotlin
// Antes
class GoogleSignInProviderIOS(private val config: GoogleSignInConfig) {
    companion object {
        var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null
        var signOutHandler: (() -> Unit)? = null
        private var pendingCallback: ((String?) -> Unit)? = null
        fun onSignInResult(idToken: String?) { … }
    }
    suspend fun signIn(): String? { … config.iosClientId ?: config.webClientId … }
}

// Después
object GoogleSignInProviderIOS {
    var signInHandler: ((String?, (String?) -> Unit) -> Unit)? = null
    var signOutHandler: (() -> Unit)? = null
    private var pendingCallback: ((String?) -> Unit)? = null
    fun onSignInResult(idToken: String?) { … }
    suspend fun signIn(config: GoogleSignInConfig): String? { … }
}
```

### Por qué esto no empeora el estado compartido

La objeción esperable a un `object` es que globaliza estado. Aquí no globaliza nada: `signInHandler`,
`signOutHandler` y `pendingCallback` **ya viven en el `companion object`**, es decir, ya son uno por
proceso. Lo que hay hoy es peor que global: es global disfrazado de instancia. `GoogleSignInProviderIOS(config)`
sugiere que dos instancias son independientes, y no lo son — comparten handler y `pendingCallback`, y
dos sign-in concurrentes se pisarían igual que ahora. El cambio no introduce el acoplamiento, lo hace
legible.

### Superficie del break

| Consumidor | Antes | Después |
|---|---|---|
| Swift (host) | `GoogleSignInProviderIOS.Companion.shared.signInHandler = …` | `GoogleSignInProviderIOS.shared.signInHandler = …` |
| Swift (host) | `GoogleSignInProviderIOS.Companion.shared.signOutHandler = …` | `GoogleSignInProviderIOS.shared.signOutHandler = …` |
| Kotlin (iosMain) | `GoogleSignInProviderIOS(config).signIn()` | `GoogleSignInProviderIOS.signIn(config)` |

Lo que hace este break aceptable es que **falla en compilación, no en runtime**. El host se entera al
bumpear el pin, no un mes después con un botón muerto — que es exactamente el modo de fallo que
arrastraban los specs 003 y 004.

Y como los consumidores pinean **SHA, no tag** (ver `CLAUDE.md`, *How this library is consumed*), no
es una publicación a ciegas: el break se coordina con el bump de Fledge en la misma tanda, un commit
aquí y dos líneas de Swift allí. La versión sube a 2.0.0 por honestidad semántica. Y sí hay quien
consume por versión: Paparcar pinea el tag `1.1.0`, no un SHA.

### Decisión (era abierta): `getClientId()` y `getTopViewController()`

Ambas son públicas y tienen **cero call sites** en este repo. `getClientId()` depende de la `config`,
así que al vaciar el constructor hay que hacer algo con ella; `getTopViewController()` no depende de
nada y sobrevive tal cual.

Esta es la única ventana barata para tocarlas: ya estamos rompiendo el tipo. **Resuelto así:**
`getClientId()` **desaparece** (quien la llamaría ya tiene la `config` en la mano) y
`getTopViewController()` **se queda** como función del `object`, con su `@Deprecated` del spec 010
intacto.

El riesgo del «consumidor Swift desconocido» se pudo medir en lugar de estimar: `getClientId()`
tiene **cero call sites** en toda la organización `apptolast`, y ni Fledge ni Paparcar mencionan
`GoogleSignInProviderIOS` en Kotlin ni en Swift. Quitarla no rompe a nadie hoy.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Un solo contrato desde Swift
  Given los seis providers de iOS de la librería
  When  se accede a cualquiera de ellos desde Swift
  Then  la forma es siempre X.shared.…
   And  ningún ejemplo del repo (KDoc, README, demo) usa .companion ni .Companion.shared

Scenario [AC-02]: La config viaja en signIn, no en el constructor
  Given getSocialIdToken(IdentityProvider.Google) en iOS
  When  resuelve GoogleSignInConfig desde Koin
  Then  la pasa a GoogleSignInProviderIOS.signIn(config)
   And  no se construye ninguna instancia del provider

Scenario [AC-03]: El comportamiento del login no se mueve
  Given un login de Google completado en iOS
  When  Swift devuelve los tokens
  Then  Kotlin recibe idToken|||accessToken|||<accessToken>, sin cambios
   And  con signInHandler sin asignar sigue devolviendo null tras un warning

Scenario [AC-04]: El signOut del spec 006 sigue en pie
  Given una sesión de Google abierta en iOS
  When  clearSocialSignInState() se ejecuta
  Then  invoca signOutHandler sobre el object
   And  el siguiente login de Google vuelve a ofrecer selector de cuenta

Scenario [AC-05]: README y demo dicen lo mismo
  Given los snippets de Google del README y los de iosApp/iosApp/iOSApp.swift
  When  se comparan
  Then  usan la misma forma de acceso, literalmente

Scenario [AC-06]: El break está declarado, no descubierto
  Given un host Swift escrito contra 1.1.0
  When  compila contra esta versión
  Then  falla en compilación con símbolo no resuelto
   And  el README declara el cambio como breaking de 2.0.0
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — `grep -rn "\.companion\.\|\.Companion\.shared" custom-login/src/iosMain iosApp README.md CLAUDE.md` debe salir vacío | n/a — el grep sale con 4 hits hoy |
| AC-02 | *(no unitario)* — `:custom-login:linkDebugFrameworkIosSimulatorArm64` + inspección de `Platform.ios.kt` | n/a en iosMain |
| AC-03 | `:custom-login:testDebugUnitTest` en verde sin tocar tests + smoke manual en Mac | n/a — el parseo vive en commonMain y no cambia |
| AC-04 | *(no unitario)* — smoke manual: login → logout → login y comprobar que aparece el selector | n/a |
| AC-05 | *(no unitario)* — diff manual de los dos snippets | n/a |
| AC-06 | *(no unitario)* — compilar el demo, que **es** el host de referencia | n/a |

> Como en los specs 003 y 004: ningún AC de este spec es verificable con un test unitario nuevo. Todo
> lo que cambia vive en `iosMain` o en Swift, donde `commonTest` no llega, y el comportamiento
> deliberadamente no se mueve. Se dice explícitamente en vez de inventar tests de relleno.

## Notas no funcionales

**Plataformas**: solo iOS. Android no ve nada de esto.

**Compatibilidad**: **BREAKING** para cualquier host Swift ya integrado — en teoría. Es la razón
de ser del bump a 1.2.0 y de que esto no se coló en el 003, que se declaró explícitamente
*«no cambiar la API pública»*.

**Orden**: depende del spec 006, que añade `signOutHandler` al mismo fichero. Debe implementarse
sobre `feature/010-ios-build-hygiene` o posterior; hacerlo antes obliga a rehacer el trabajo cuando
006 aterrice.

**Coordinación**: comprobado antes de implementar — **ningún consumidor toca esta API**. Ni Fledge ni
Paparcar nombran `GoogleSignInProviderIOS` en Kotlin ni en Swift, así que este spec por sí solo no
obliga a ningún cambio río abajo. Lo que sí se lo obliga es el renombrado del paquete que viaja en
la misma 2.0.0.

**Requisitos externos**: ninguno. No toca entitlements, consola de Firebase ni SPM.
