# Spec 007: Que `AppleSignInConfig.scopes` signifique algo en iOS

> Rama: `feature/007-apple-scopes-ios` · Proyecto: `BaseLogin` (`:custom-login` + demo)
> Estado: implementado · Sale de `feature/006-ios-social-signout`.
> Sin ticket FLE: sale de la auditoría de iOS.

## Contexto y objetivo

`AppleSignInConfig(scopes = listOf("email", "name"))` es configuración pública de la librería, y en
Android se usa de verdad: `Platform.android.kt` la lee para el flujo OAuth web. En iOS **no la lee
nadie**. El handler de Swift recibe un `String?` documentado como *«unused (reserved for future
config)»* y siempre vale `null`, así que los scopes están escritos a fuego en el host.

Un campo de configuración que en una plataforma hace algo y en la otra no es peor que no tenerlo:
quien lo cambie creerá que funciona en las dos.

## Alcance

**Dentro:**

- El primer parámetro del `signInHandler` de Apple pasa a llevar los scopes configurados,
  separados por comas.
- `AppleSignInProviderIOS.signIn(scopes: String? = null)`, con default para no romper a nadie.
- El demo los traduce a `ASAuthorization.Scope`.

**Fuera:**

- Hacer lo mismo con los cuatro providers de OAuth web. Sus `OAuthProviderConfig` tienen además
  `customParameters`, y un `String?` no da para eso: necesita su propio ticket y probablemente otra
  forma del seam.
- Cambiar los valores por defecto de `AppleSignInConfig`.

## Diseño

### Por qué se reutiliza el parámetro que ya existía

El seam `((String?, (String?) -> Unit) -> Unit)?` ya tenía ese hueco reservado. Llenarlo es
**aditivo**: un host integrado hoy lo ignora con `_` y sigue compilando y funcionando igual, porque
seguía pidiendo `[.fullName, .email]` a mano — que es exactamente el valor por defecto configurado.

Se envía como cadena separada por comas y no como lista porque una `List<String>` cruza a Swift como
`[Any]` y obliga al host a castear; una cadena es lo que ya viajaba por ese parámetro.

### El fallback no es "ningún scope"

Una lista vacía o irreconocible cae en `[.fullName, .email]`, nunca en pedir nada. Pedir cero scopes
significa que Apple no devuelve el nombre, y el nombre solo llega en la primerísima autorización: se
perdería para siempre. Un error de configuración no puede costar eso.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Los scopes configurados llegan a Apple
  Given AppleSignInConfig(scopes = listOf("email"))
  When  el usuario inicia sesión con Apple en iOS
  Then  el handler recibe "email"
   And  la petición pide solo .email

Scenario [AC-02]: Sin configuración se piden los dos
  Given un host sin appleSignInConfig, o con scopes vacíos
  When  el usuario inicia sesión con Apple
  Then  la petición pide .fullName y .email

Scenario [AC-03]: Un host ya integrado no cambia
  Given un host cuyo handler ignora el primer parámetro
  When  compila y ejecuta contra esta versión
  Then  sigue compilando y pidiendo lo que pedía

Scenario [AC-04]: Un scope desconocido no deja la petición vacía
  Given scopes = listOf("cumpleaños")
  When  el demo los traduce
  Then  cae al valor por defecto en vez de no pedir nada
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — el camino entero vive en `Platform.ios.kt` y en Swift, ninguno alcanzable desde `commonTest` | n/a |
| AC-02 | *(no unitario)* — inspección de `scopes(from:)` en el demo | n/a |
| AC-03 | *(no unitario)* — el default de `signIn(scopes:)` lo garantiza en compilación | n/a |
| AC-04 | *(no unitario)* — inspección de `scopes(from:)` | n/a |

> Ningún AC es unitario. Lo que se toca es el `actual` de iOS y el host de Swift, que es
> precisamente el otro lado de la frontera que `commonTest` no cruza. Se dice en vez de inventar un
> test que comprobara el `joinToString`.

## Notas no funcionales

**API pública**: `signIn()` gana un parámetro **con default**, así que no se rompe ninguna llamada
existente. La semántica del primer parámetro del handler pasa de "reservado" a "scopes", y eso se
documenta en el KDoc y en el README.

**Pendiente de Mac**: compilar el Swift y comprobar en el smoke que con `scopes = listOf("email")` la
hoja de Apple ya no ofrece compartir el nombre.
