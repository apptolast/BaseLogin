# Spec 001: Migrar la integración iOS de CocoaPods a Swift Package Manager (FLE-91)

> Rama: `feature/001-cocoapods-to-spm` · Proyecto: `BaseLogin` · Estado: draft
> Primer spec del repo. Le sigue el 002 (puerto de Firebase Auth, FLE-90).
> El spec es el mecanismo anti-deriva: debe ser autosuficiente (releíble al inicio de cada fase).

## Contexto y objetivo

Directriz del usuario: **cero CocoaPods en ningún proyecto de la flota**. Fledge ya está en SPM; este
repo es el que queda.

Conviene ser preciso sobre dónde están los pods, porque cambia el argumento:

- **La librería publicada `:custom-login` no usa pods.** No declara ninguno y no importa ninguna clase
  de pod — `grep "^import cocoapods"` sobre sus fuentes devuelve vacío. Sus providers de iOS usan solo
  handlers de Swift y `platform.UIKit`, que son las platform libs estándar de Kotlin/Native.
- Los pods viven en **`composeApp`** (bloque `cocoapods {}` con `FirebaseCore`, `FirebaseAuth`,
  `GoogleSignIn ~> 9.0`) y en **`iosApp/`** (`Podfile`, `Podfile.lock`, `Pods/`, `.xcworkspace`,
  `composeApp.podspec`), es decir, en las **apps de demostración**.

Por tanto **los pods no se filtran hoy a Fledge**, que consume solo el artefacto Maven por JitPack.
Esto no arregla ningún bug de Fledge.

El motivo real por el que sí merece la pena es otro, y es medible:

| | BaseLogin (pods) | Fledge (SPM) |
|---|---|---|
| FirebaseAuth / FirebaseCore | **12.7.0** (`Podfile.lock`) | **11.8.1** (`Package.resolved`) |
| Deployment target iOS | **26.1** | **18.2** |
| Sistema de dependencias nativas | CocoaPods | SPM |

Son **majors distintos** del SDK de Firebase para iOS. Consecuencia: *«funciona en la demo de
BaseLogin» no demuestra «funciona en Fledge»*. La demo existe precisamente para validar la librería
como la integra un consumidor, y hoy no lo hace — valida una integración que ningún consumidor usa.
Es el mismo riesgo D-3 que ya apareció en el spec 002 de Fledge, visto desde el otro lado.

Además, el cinterop de GitLive apunta a una versión concreta del SDK de Firebase para iOS; mezclar
majors es justo lo que produce símbolos ausentes en el enlazado.

## Alcance

**Dentro:**

- Quitar el plugin `kotlin.cocoapods` y el bloque `cocoapods {}` de `composeApp`, y declarar el
  framework con `binaries.framework { … export(project(":custom-login")) }`.
- Borrar `Podfile`, `Podfile.lock`, `composeApp.podspec` y el `.xcworkspace`; pasar a trabajar sobre
  `iosApp.xcodeproj`. (`Pods/` ya está en `.gitignore`, así que no está versionado.)
- Limpiar el `project.pbxproj` de CocoaPods: 30 referencias, incluidas las tres fases
  `[CP] Check Pods Manifest.lock`, `[CP] Embed Pods Frameworks` y `[CP] Copy Pods Resources`, el
  `Pods_iosApp.framework` y los `baseConfigurationReference` que apuntan a los xcconfig de Pods.
- Añadir por SPM los paquetes y productos que la demo necesita, con la **misma regla de versión que
  Fledge**.
- Añadir la build phase «Compile Kotlin Framework» que hoy aportaba el podspec.
- Versionar `Package.resolved` y actualizar `CLAUDE.md`.

**Fuera:**

- Tocar el código de `:custom-login` — la librería no cambia en este ticket.
- El puerto de `FirebaseAuthGateway`, el `displayName` de Apple y el `signOut` social: **FLE-90**,
  spec 002.
- Cambiar la UI, los flujos de auth o los modelos de dominio.
- Migrar Android: `google-services` y las dependencias de Android se quedan como están.
- Retirar el `google-services.json` / `GoogleService-Info.plist` de la demo en favor de
  `FirebaseOptions` explícitas, como hace Fledge. Es un cambio de enfoque distinto y no hace falta
  para quitar los pods.

## Conocimiento reutilizable

De **Fledge** (`apptolast/Fledge`, `iosApp/iosApp.xcodeproj`) — es la implementación de referencia,
está en producción y funciona:

*Se reutiliza tal cual:*

- La build phase de Kotlin es una `PBXShellScriptBuildPhase` llamada `Compile Kotlin Framework`,
  **la primera** de `buildPhases`, con `alwaysOutOfDate = 1` y este script, que respeta el bypass del
  IDE:

  ```sh
  if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED..."
    exit 0
  fi
  cd "$SRCROOT/.."
  ./gradlew :<módulo>:embedAndSignAppleFrameworkForXcode
  ```

- El paquete SPM se declara como `XCRemoteSwiftPackageReference` con
  `kind = upToNextMinorVersion; minimumVersion = 11.8.0`, y cada producto como
  `XCSwiftPackageProductDependency`.

*Se adapta:* Fledge enlaza solo `FirebaseCore` + `FirebaseFirestore`. Aquí hacen falta otros
productos (ver Diseño).

De **engram #106** (FLE-78): GitLive 2.5.0 hace su cinterop contra Firebase iOS **11.8.0**; mezclar
majors arriesga símbolos ausentes en el enlazado. Por eso Fledge quedó pineado a 11.8.x.

De **Fledge / FLE-78, aprendido a base de perder tiempo**: un `xcodebuild` verde **no** prueba que un
producto SPM esté enlazado si ningún símbolo suyo se referencia todavía — el klib de cinterop no entra
en el binario y el linker nunca pide el framework. Aquí sí se referencian (el Swift de la demo usa
`FirebaseApp.configure()`, `AppCheck` y `GIDSignIn`), así que el build **sí** es un oráculo válido.

## Diseño

### Qué necesita realmente el enlazado

Verificado leyendo los `import` del Swift de la demo y las dependencias de Kotlin:

| Producto | Paquete SPM | Por qué |
|---|---|---|
| `FirebaseCore` | `firebase-ios-sdk` | `FirebaseApp.configure()` en `iOSApp.swift`; también lo pide el cinterop de GitLive |
| `FirebaseAuth` | `firebase-ios-sdk` | Lo pide el cinterop de `dev.gitlive:firebase-auth` vía `-framework FirebaseAuth`, aunque el Swift no lo importe |
| `FirebaseAppCheck` | `firebase-ios-sdk` | `AppCheckDebugProviderFactory` en `iOSApp.swift` |
| `GoogleSignIn` | `google/GoogleSignIn-iOS` | `GIDSignIn`, `GIDConfiguration` en `iOSApp.swift` |

`GoogleSignIn ~> 9.0` en pods corresponde a la 9.x del paquete SPM `GoogleSignIn-iOS`.

### Lo que se pierde al quitar el podspec

`composeApp.podspec` no solo declaraba dependencias: su script era quien **compilaba el framework de
Kotlin**. Al quitarlo hay que añadir explícitamente la build phase, o el proyecto compilará contra un
framework obsoleto o inexistente. Es el fallo más probable de esta migración.

## Criterios de aceptación (Gherkin)

```gherkin
Scenario [AC-01]: No queda rastro de CocoaPods en el repositorio
  Given la migración completada
  When  se inspecciona el árbol de trabajo
  Then  no existen Podfile, Podfile.lock, composeApp.podspec ni ningún .xcworkspace
   And  ningún fichero de Gradle referencia el plugin kotlin.cocoapods
   And  project.pbxproj no contiene ninguna cadena "Pods" ni ninguna fase "[CP]"

Scenario [AC-02]: El framework de Kotlin se declara en Gradle
  Given composeApp sin el bloque cocoapods
  When  se inspecciona su configuración de targets iOS
  Then  cada target iOS declara binaries.framework con baseName "ComposeApp"
   And  el framework exporta el proyecto :custom-login

Scenario [AC-03]: Xcode compila el framework de Kotlin al construir
  Given el proyecto iosApp sin el script del podspec
  When  se inspeccionan las fases de compilación del target iosApp
  Then  la primera fase es un script llamado "Compile Kotlin Framework"
   And  invoca ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   And  respeta la variable OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED

Scenario [AC-04]: Las dependencias nativas vienen por SPM
  Given el proyecto iosApp
  When  se inspeccionan sus referencias de paquete
  Then  declara firebase-ios-sdk con la misma regla de versión que Fledge
   And  enlaza los productos FirebaseCore, FirebaseAuth y FirebaseAppCheck
   And  declara GoogleSignIn-iOS y enlaza su producto GoogleSignIn
   And  Package.resolved queda versionado en el repositorio

Scenario [AC-05]: La versión de Firebase para iOS coincide con la del consumidor
  Given Package.resolved resuelto
  When  se compara con el de Fledge
  Then  la versión resuelta de firebase-ios-sdk es la misma en ambos repositorios
   And  por tanto validar iOS aquí sí dice algo sobre el consumidor

Scenario [AC-06]: El proyecto iOS construye sin CocoaPods
  Given el árbol sin pods y con SPM configurado
  When  se ejecuta xcodebuild sobre el simulador con DerivedData limpio
  Then  el build termina en BUILD SUCCEEDED
   And  no se ha ejecutado pod install en ningún momento

Scenario [AC-07]: La firma de código vuelve a ser portable
  Given que el árbol local tenía DEVELOPMENT_TEAM fijado a un identificador concreto
  When  se inspecciona project.pbxproj
  Then  DEVELOPMENT_TEAM vuelve a leerse del placeholder ${TEAM_ID}
   And  Configuration/Config.xcconfig sigue siendo el origen de ese valor
   And  el fichero iosApp.entitlements con la capacidad Apple Sign-In se conserva y queda versionado

Scenario [AC-08]: Android no se ve afectado
  Given la migración, que solo toca la integración de iOS
  When  se ejecutan :composeApp:assembleDebug y :custom-login:testDebugUnitTest
  Then  ambos terminan en BUILD SUCCESSFUL

Scenario [AC-09]: La demo sigue autenticando en iOS
  Given la app de demostración instalada en el simulador
  When  se inicia sesión con Google y con Apple
  Then  ambos flujos completan y devuelven una sesión
   And  no hay fallos de enlazado por símbolos ausentes de Firebase
```

## Desglose de tareas (ligero)

- [ ] **T1** — Gradle: quitar el plugin `kotlin.cocoapods` de `composeApp`, de la raíz y del catálogo;
      sustituir el bloque `cocoapods {}` por `binaries.framework`. → **AC-01**, **AC-02**
- [ ] **T2** — Borrar `Podfile`, `Podfile.lock`, `composeApp.podspec` y el `.xcworkspace`.
      → **AC-01**
- [ ] **T3** — Limpiar `project.pbxproj`: las 3 fases `[CP]`, `Pods_iosApp.framework` y los
      `baseConfigurationReference` de Pods. Restaurar el `baseConfigurationReference` a
      `Configuration/Config.xcconfig`. → **AC-01**
- [ ] **T4** — Añadir la build phase «Compile Kotlin Framework» como primera fase. → **AC-03**
- [ ] **T5** 🚦 — Añadir los paquetes SPM y sus productos. **Paso manual del usuario en Xcode** si se
      confirma D-3. → **AC-04**, **AC-05**
- [ ] **T6** — Firma: restaurar `${TEAM_ID}` y `CODE_SIGN_STYLE = Automatic`; conservar y versionar
      `iosApp.entitlements`. → **AC-07**
- [ ] **T7** — Validación: `xcodebuild` con DerivedData limpio, `:composeApp:assembleDebug`,
      `:custom-login:testDebugUnitTest`. → **AC-06**, **AC-08**
- [ ] **T8** — Smoke manual de Google y Apple en el simulador. → **AC-09**
- [ ] **T9** — Actualizar `CLAUDE.md` (§Module Structure menciona «GoogleSignIn pod», §Build Commands
      dice «build/run from Xcode in /iosApp») y el `README` si documenta `pod install`.

## Notas no funcionales

**Plataformas**: solo iOS. Android no se toca (AC-08 lo verifica).

**No hay impacto de UI**: no se crea ni modifica ninguna pantalla, componente ni cadena.
**`/design-check` se prevé N/A**, a confirmar en su gate.

**Testabilidad**: esta feature **no añade tests automáticos**, y hay que decirlo claro en vez de
inventarlos. Es una migración de configuración de build: lo que la verifica es el propio build
(AC-01…AC-08) más un smoke manual (AC-09). Toda su trazabilidad es «n/a — verificable por build», así
que **`/test` será una fase vacía** y `/validate` no debe bloquear por ausencia de test rojo previo.

**Riesgo dominante**: olvidar la build phase de Kotlin. El podspec era quien compilaba el framework;
sin ella, Xcode enlazaría contra un `ComposeApp.framework` obsoleto o inexistente, y el síntoma
—«funciona hasta que cambias código Kotlin y no se refleja»— es confuso de diagnosticar.

**ktlint**: no está configurado en este repo (lo pide el gate `/validate`). Se aborda en el spec 002,
que sí toca Kotlin; esta feature apenas toca `.kts`.

> **Evidencia del smoke de AC-09 (2026-07-28) — parcial, y conviene decir qué parte.**
>
> Ejecutado sobre el simulador iPhone 17 / iOS 26.2, con la app instalada desde el build de SPM:
>
> | Comprobado | Resultado |
> |---|---|
> | La app arranca y el proceso sigue vivo | ✅ `UIKitApplication:com.apptolast.login.Login` |
> | `FirebaseCore` configura, enlazado por SPM | ✅ `[FirebaseCore][I-COR000001] Configuring the default app.` |
> | `FirebaseAppCheck` carga y alcanza su endpoint | ✅ `error=NoError(0) hostname=firebaseappcheck.googleapis.com` |
> | El framework de Kotlin carga y pinta | ✅ la pantalla de login se renderiza completa, con los ocho botones sociales |
> | Crash reports | ✅ ninguno |
>
> Esto prueba lo que la migración podía romper: que los frameworks nativos se enlazan y cargan en
> tiempo de ejecución, y que la build phase «Compile Kotlin Framework» produce un framework válido —
> si fallara, la app no pintaría nada.
>
> **Lo que NO queda probado**: completar un login real con Google o con Apple. Requiere credenciales y
> la interacción de una persona, así que sigue pendiente de ejecución manual. La UI muestra los
> botones, pero eso no demuestra que el flujo termine en sesión.

## Decisiones abiertas (🚦 Gate 1)

| # | Decisión | Propuesta |
|---|---|---|
| **D-1** ⚠️ | **Versión de GitLive**: BaseLogin declara 2.4.0; Fledge, 2.5.0. El cinterop de cada una apunta a una versión distinta del SDK de Firebase para iOS, así que la versión de GitLive determina a qué hay que pinear SPM. | **Subir BaseLogin a 2.5.0** y pinear SPM a 11.8.x, igual que Fledge. Es lo que hace que AC-05 signifique algo. Coste: un cambio de versión más en este ticket. Dato a favor: Fledge ya eleva el transitivo 2.4.0 → 2.5.0 por resolución de Gradle, así que el artefacto que se consume **ya es** 2.5.0; BaseLogin es el único que sigue construyendo contra 2.4.0. |
| **D-2** | **Deployment target**: `composeApp` declara 26.1 y Fledge 18.2. | Bajarlo para acercarlo a Fledge, o justificar la diferencia. 26.1 es muy restrictivo para una demo y no parece intencionado. **Decisión del usuario.** |
| **D-3** ⚠️ | **Cómo se editan los paquetes SPM en el `pbxproj`**: a mano o desde Xcode. | En Fledge se editó a mano el *borrado* de productos con éxito, pero **añadir** paquetes desde cero genera UUIDs y secciones nuevas; ahí la edición manual es frágil. Propuesta: el agente hace T1–T4 y T6 (borrado y limpieza, verificable con `plutil -lint`), y **el usuario añade los 2 paquetes SPM desde Xcode** (T5), como se hizo en FLE-78. |
| **D-4** | **Artefacto local de firma**: el árbol tenía `DEVELOPMENT_TEAM = 3NXH5U7C5A` y `CODE_SIGN_STYLE = Manual` sustituyendo al placeholder `${TEAM_ID}`, más un `iosApp.entitlements` nuevo. | Conservar el `entitlements` (la capacidad Apple Sign-In es necesaria y hoy falta en el repo) y **revertir el team id**, porque ata el repo a una cuenta de desarrollador concreta y `Configuration/Config.xcconfig` ya tiene `TEAM_ID=` como mecanismo previsto. Recogido en AC-07. |
| **D-5** | **`FirebaseAppCheck`** solo lo usa la demo, con `AppCheckDebugProviderFactory`. | Mantenerlo: quitarlo cambiaría el comportamiento de la demo, y eso no es objeto de este ticket. |

## Trazabilidad

| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | *(no unitario)* — inspección del árbol + grep de `Pods`, `[CP]` y `cocoapods` | n/a — verificable por build |
| AC-02 | *(no unitario)* — inspección de `composeApp/build.gradle.kts` | n/a — verificable por build |
| AC-03 | *(no unitario)* — inspección de `project.pbxproj` | n/a — verificable por build |
| AC-04 | *(no unitario)* — inspección de `project.pbxproj` y `Package.resolved` | n/a — verificable por build |
| AC-05 | *(no unitario)* — comparación de `Package.resolved` con el de Fledge | n/a — verificable por build |
| AC-06 | *(no unitario)* — `xcodebuild` con DerivedData limpio | n/a — verificable por build |
| AC-07 | *(no unitario)* — inspección de `project.pbxproj` y `git ls-files` del entitlements | n/a — verificable por build |
| AC-08 | *(no unitario)* — `:composeApp:assembleDebug`, `:custom-login:testDebugUnitTest` | n/a — verificable por build |
| AC-09 | *(no unitario)* — smoke manual en el simulador | n/a — **parcial**, ver evidencia abajo |

> Nota para `/validate`: **ningún AC de este spec es verificable con un test unitario**, y no se
> escriben tests de mentira para rellenar la tabla. Es una migración de configuración de build. El
> gate de trazabilidad no debe bloquear por ausencia de test rojo previo; cada fila indica el comando
> que la valida.
