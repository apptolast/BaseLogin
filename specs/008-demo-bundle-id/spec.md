# Spec 008: Un bundle id estable para el demo de iOS

> Rama: `feature/008-demo-bundle-id` · Proyecto: `BaseLogin` (`iosApp/` + `composeApp/`)
> Estado: implementado · Sale de `feature/007-apple-scopes-ios`.
> Sin ticket FLE: sale de la auditoría de iOS.

## Contexto y objetivo

El demo declara su identidad así:

```
PRODUCT_BUNDLE_IDENTIFIER=com.apptolast.login.Login$(TEAM_ID)
```

Viene de la plantilla de KMP, que añade el team id para que cada desarrollador tenga un bundle id
único y no choquen al firmar. Para un ejemplo de usar y tirar está bien. Aquí no, porque este bundle
id tiene que coincidir con tres cosas a la vez:

1. la app de iOS registrada en Firebase (`GoogleService-Info.plist`),
2. el cliente OAuth de iOS que emitió el reversed client id del `Info.plist`,
3. `MagicLinkConfig.iosBundleId`, que es lo que Firebase pone en el enlace para reabrir la app.

Con el sufijo, **poner `TEAM_ID` en local cambia la identidad de la app** y rompe las tres en
silencio. Y aun sin ponerlo hay una discrepancia real hoy: el bundle es `com.apptolast.login.Login` y
el magic link dice `com.apptolast.login`, así que el enlace no reabre la app.

## Alcance

**Dentro:**

- Quitar `$(TEAM_ID)` del bundle id.
- Alinear `MagicLinkConfig.iosBundleId` con el bundle id real.

**Fuera:**

- Cambiar el bundle id a otro valor. Ver la decisión de abajo.
- Montar los Universal Links de `apptolast.com` (fichero `apple-app-site-association`, capacidad
  *Associated Domains*). Sin eso el magic link sigue sin volver a la app, pero es infraestructura del
  dominio, no de este repo.
- Tocar el `applicationId` de Android, que ya es coherente.

## Decisión: se conserva `com.apptolast.login.Login`

Los dos candidatos eran ese y `com.apptolast.login`, que es el `applicationId` de Android y lo que
decía el magic link. Se conserva el primero **porque es el que está en el árbol hoy** con `TEAM_ID`
vacío, y por tanto el único que sabemos que casa con lo que hay registrado en la consola de Firebase
y en el cliente OAuth de iOS. Cambiar la identidad de la app a partir de una corazonada rompería
Google Sign-In sin avisar, y el `GoogleService-Info.plist` está en `.gitignore`, así que no se puede
comprobar desde aquí.

**Si al validar en Mac resulta que la app de iOS en Firebase está registrada como
`com.apptolast.login`, el arreglo es mover las dos líneas —`Config.xcconfig` y `MainViewController`—
a ese valor.** Es una línea en cada sitio.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: La identidad de la app no depende del desarrollador
  Given un desarrollador que rellena TEAM_ID en Config.xcconfig
  When  compila el demo
  Then  el bundle id sigue siendo com.apptolast.login.Login
   And  la firma sigue usando su team id

Scenario [AC-02]: El magic link apunta a la app que existe
  Given la configuración del demo
  When  se compara MagicLinkConfig.iosBundleId con PRODUCT_BUNDLE_IDENTIFIER
  Then  son la misma cadena

Scenario [AC-03]: El team id sigue sin estar en el repositorio
  Given project.pbxproj y Config.xcconfig
  When  se busca un team id concreto
  Then  no aparece ninguno: DEVELOPMENT_TEAM sigue leyendo ${TEAM_ID}
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — inspección de `Config.xcconfig` | n/a — configuración de build |
| AC-02 | *(no unitario)* — comparación de las dos líneas | n/a |
| AC-03 | *(no unitario)* — `grep -rn "DEVELOPMENT_TEAM" iosApp/` | n/a |

> Configuración de build: no hay nada que un test unitario pueda afirmar. Lo verifica el build y el
> smoke, igual que el spec 001.

## Notas no funcionales

**Riesgo**: si la consola de Firebase tuviera registrada otra cadena, el demo dejaría de autenticar.
Se conserva el valor actual precisamente para no correrlo, y el arreglo está escrito arriba.

**Lo que sigue faltando para el magic link**: los Universal Links del dominio. Este ticket solo quita
la discrepancia del bundle id.
