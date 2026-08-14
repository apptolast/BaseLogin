# Spec 010: Higiene del demo de iOS

> Rama: `feature/010-ios-build-hygiene` · Proyecto: `BaseLogin`
> Estado: implementado · Sale de `feature/009-phone-session-parity`. Último de la tanda.
> Sin ticket FLE: recoge los restos de la auditoría de iOS que no daban para ticket propio.

## Contexto y objetivo

Cuatro cosas pequeñas, ninguna urgente, todas de las que hacen perder una tarde a quien llega nuevo:

1. **El demo no se puede construir desde un clon limpio.** `google-services.json` y
   `GoogleService-Info.plist` están en `.gitignore` y **no están en el árbol**. El primero rompe
   `:composeApp:assembleDebug` con un error claro; el segundo deja compilar y la app **casca al
   arrancar** en `FirebaseApp.configure()`. Nada lo documenta. (Se descubrió al ejecutar
   `assembleDebug` en el spec 008: hasta entonces solo se habían corrido tareas de `:custom-login`,
   que no necesitan ninguno de los dos.)
2. **Código muerto en la API pública.** `GoogleSignInProviderIOS.onSignInResult` no lo llama nadie —
   el resultado viaja en el completion del `signInHandler` — y `getTopViewController()` además usa
   `UIApplication.windows`, deprecado en iOS 15 e indiferente a qué escena está en primer plano.
3. **El handler de Google del demo se quedó atrás.** Coge `connectedScenes.first`, que puede ser una
   escena en segundo plano, y registra con `print`. Los tres coordinadores escritos en los specs
   003–005 hacen ambas cosas bien, así que ahora desentona justo en el fichero que un consumidor
   abre primero.
4. **El enlazado de `ComposeApp.framework` es implícito** desde FLE-91. Ver la decisión de abajo.

## Alcance

**Dentro:** documentar los dos ficheros que faltan, deprecar el código muerto, alinear el handler de
Google.

**Fuera:**

- Borrar el código muerto. Es API pública de una librería que los consumidores pinean por SHA:
  se deprecia con `WARNING` y se borra cuando se sepa que nadie la usa.
- Añadir el target `iosX64`. El README documenta «Arm64 + Simulator Arm64» como decisión tomada, no
  como deuda; si hace falta simulador Intel, es su propio ticket.
- Tocar `project.pbxproj`. Ver abajo.

## Decisión: el `pbxproj` no se toca

La auditoría anotó que las configuraciones del target no declaran `FRAMEWORK_SEARCH_PATHS` ni
`OTHER_LDFLAGS = -framework ComposeApp`. Antes de FLE-91 los aportaban los xcconfig generados por
CocoaPods; la migración no los repuso y hoy `import ComposeApp` se resuelve de forma implícita —
funcionó en el smoke de FLE-91 y sigue funcionando.

**No se añaden a ciegas.** El valor correcto depende de dónde deje exactamente el framework la tarea
`embedAndSignAppleFrameworkForXcode`, y eso no se puede comprobar desde Windows: los targets de Apple
no compilan aquí. Escribir una ruta plausible en un fichero de build que no puedo compilar es peor
que dejarlo como está, porque parece un arreglo.

Lo que hay que hacer en el Mac, en dos comandos:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp -showBuildSettings \
  | grep -E "FRAMEWORK_SEARCH_PATHS|OTHER_LDFLAGS|BUILT_PRODUCTS_DIR"
find composeApp/build -name "ComposeApp.framework" -maxdepth 6
```

Con eso se sabe qué ruta declarar, y entonces es un ticket de dos líneas.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: Quien clona sabe qué le falta
  Given un clon limpio del repositorio
  When  se lee CLAUDE.md antes de construir el demo
  Then  dice qué dos ficheros hay que descargar de Firebase y dónde van
   And  dice qué pasa exactamente si falta cada uno

Scenario [AC-02]: El código muerto avisa sin romper
  Given un consumidor que llama a onSignInResult o getTopViewController
  When  compila contra esta versión
  Then  sigue compilando, con un warning que explica qué usar en su lugar

Scenario [AC-03]: El demo presenta desde la escena correcta
  Given una app con más de una escena conectada
  When  el usuario inicia sesión con Google
  Then  se presenta desde la que está en primer plano

Scenario [AC-04]: El demo registra como los demás coordinadores
  Given el handler de Google del demo
  When  falla o cancela
  Then  usa NSLog con formato "%@" y no print
```

## Trazabilidad

| AC | Test(s) | ¿Rojo antes? |
|----|---------|--------------|
| AC-01 | *(no unitario)* — lectura de `CLAUDE.md` | n/a — documentación |
| AC-02 | *(no unitario)* — `ktlintCheck` + inspección de las anotaciones | n/a |
| AC-03 | *(no unitario)* — inspección del `AppDelegate` | n/a |
| AC-04 | *(no unitario)* — `grep -n "print(" iosApp/` | n/a |

> Documentación, deprecaciones y Swift: nada que un test unitario pueda afirmar. Se dice, en vez de
> rellenar la tabla.

## Notas no funcionales

**API pública**: dos deprecaciones a nivel `WARNING`. Nada se borra, nadie deja de compilar.

**Pendiente de Mac**: la decisión del `pbxproj`, con los dos comandos escritos arriba.
