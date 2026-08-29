# Spec 012: La librería habla los nueve idiomas que hablan sus apps

> Rama: `feature/012-strings-nine-locales` · Proyecto: `BaseLogin` (`:baselogin`, solo recursos)
> Estado: **especificado, sin implementar**. Sale de `develop`.
> Sin ticket FLE: nace de una revisión de la pantalla de login de Paparcar (29-08-2026).
> ⛔ **No se publica al terminar.** La release queda pendiente de la migración JitPack → Maven
> Central, que es decisión aparte y anterior. Ver "Notas no funcionales".

## Contexto y objetivo

La librería trae hoy **5 locales**, los cinco completos y sin huecos:

| Locale | Keys | | Locale | Keys |
|---|---|---|---|---|
| `values` (EN, base) | 92 | | `values-it` | 92 |
| `values-es` | 92 | | `values-pt` | 92 |
| `values-fr` | 92 | | | |

Su consumidor Paparcar mantiene **nueve**: EN, ES, IT, PT, FR, **DE, NL, PL, RO**. Los cuatro
últimos no existen aquí, así que Compose Resources cae al base y los sirve en inglés.

El resultado es una pantalla mestiza: un usuario con el móvil en alemán ve la app en alemán y la
autenticación en inglés. Y no es una esquina del producto — de las 92 keys, con una configuración
como la de Paparcar (sin teléfono, sin magic link, sin la pantalla de reauth) quedan **~74
alcanzables**, repartidas así:

| Grupo | Keys | Qué es |
|---|---|---|
| `auth_*` | 18 | **mensajes de error** — credenciales, red, email en uso, cuenta deshabilitada |
| `login_*` | 13 | pantalla de login y etiquetas de los botones de proveedor |
| `validation_*` | 9 | validación de formulario |
| `register_*` | 8 | alta de cuenta |
| `reset_*` / `forgot_*` | 12 | recuperar contraseña |
| `common_*` / `cd_*` / `divider_*` | 10 | acciones comunes, accesibilidad, separador "O" |
| `welcome_*` | 4 | pantalla de bienvenida (solo si el host la monta) |
| `phone_*` / `magic_*` / `reauth_*` | 18 | **no alcanzables** con la config de Paparcar |

Los 18 `auth_*` son los que más pesan: son lo que el usuario lee justo cuando algo le ha salido mal,
que es el peor momento para cambiarle de idioma.

**Objetivo:** que la superficie de autenticación esté completa en los mismos nueve idiomas que ya
mantienen las apps que la consumen.

## Alcance

**Dentro:**

- Cuatro ficheros nuevos en `baselogin/src/commonMain/composeResources/`: `values-de/strings.xml`,
  `values-nl/strings.xml`, `values-pl/strings.xml`, `values-ro/strings.xml`.
- **92 keys en cada uno**, las mismas exactamente, sin añadir ni renombrar ninguna.

**Fuera:**

- Tocar keys existentes, el inglés base o los cuatro locales que ya están.
- La app demo (`composeApp/`), que tiene sus propios strings y no se publica.
- Los `validation_*` que las ViewModels aún no consumen (usan inglés hardcodeado): se traducen como
  recurso, pero **este spec no cambia ese cableado** — es otro trabajo, y mezclarlos convertiría un
  cambio de recursos en un cambio de comportamiento.
- **Publicar**. Ni tag, ni JitPack, ni Maven Central.

## Invariantes, medidos antes de escribir nada

1. **Apóstrofos crudos, nunca `\'`.** Compose Resources —a diferencia de `android:strings`— **no
   desescapa** `\'`, y el usuario acaba leyendo la barra invertida. Paparcar lo sufrió en 87 textos.
   Medido hoy aquí con un script, no con `grep` (que cuenta mal esta secuencia): **0 escapados** en
   los cinco locales; los apóstrofos que hay son crudos (5 en EN, 10 en FR, 2 en IT). Los cuatro
   ficheros nuevos tienen que nacer así.
2. **Los dos `%1$s` del base sobreviven.** Son los únicos placeholders del fichero y una traducción
   que se los coma rompe el texto en tiempo de ejecución, no de compilación.
3. **Sin plurales.** El base no declara ninguno; PL y RO tienen reglas de plural más ricas que EN,
   así que si alguna frase las pidiera, se resuelve reformulando, no introduciendo `<plurals>` en un
   spec de traducción.
4. **Un locale a medias es peor que ausente.** Hoy DE cae limpiamente al inglés porque la carpeta no
   existe; una carpeta presente con keys ausentes es justo el caso que en Paparcar se trata como
   fatal. De ahí que el criterio sea 92/92, no "las importantes".

## Criterios de aceptación

```gherkin
Scenario [AC-01] Los nueve locales existen y están completos
  Given el módulo :baselogin
  When se listan los ficheros strings.xml de commonMain/composeResources
  Then hay nueve locales: base, es, fr, it, pt, de, nl, pl, ro
  And cada uno declara exactamente las mismas 92 keys que el base

Scenario [AC-02] Ninguna traducción reintroduce el apóstrofo escapado
  Given los cuatro ficheros nuevos
  When se cuenta la secuencia \' en cada uno
  Then el resultado es cero

Scenario [AC-03] Los placeholders sobreviven a la traducción
  Given las dos keys del base que contienen %1$s
  When se leen sus equivalentes en de, nl, pl y ro
  Then cada una contiene exactamente un %1$s

Scenario [AC-04] La app demo compila y arranca con un locale nuevo
  Given el demo de :composeApp instalado en un device en alemán
  When se abre la pantalla de login y se fuerza un error de credenciales
  Then el mensaje se lee en alemán, no en inglés
```

| AC | Verificación | Test |
|---|---|---|
| AC-01 | test unitario nuevo sobre el árbol de recursos, o script en CI | sí |
| AC-02 | mismo test, contando la secuencia | sí |
| AC-03 | mismo test | sí |
| AC-04 | *(no unitario)* — device con el locale cambiado | n/a |

> AC-01..03 sí son automatizables y **deberían serlo**: son exactamente la clase de invariante que se
> rompe sola con el tiempo, cada vez que alguien añade una key al base y se olvida de los otros ocho
> ficheros. Un test que compare los conjuntos de keys entre locales vale más que las traducciones que
> este spec añade, porque protege a todas las futuras.

## Notas no funcionales

**Compatibilidad**: **no rompe nada**. Solo añade recursos; ninguna key cambia de nombre, ningún
símbolo público se mueve. Los consumidores pinados a 1.1.0 siguen igual hasta que suban el pin.

**Consumidores**: Paparcar (los nueve locales, es quien lo pide) y **Fledge**, que se beneficia sin
tocar nada. Ninguno necesita cambios de código por este spec.

**Orden y publicación**: esto viaja en la misma release que ya tiene acumulada `develop` — el
renombrado `customlogin` → `baselogin` y `loginPresentationModule` público — es decir, la **2.0.0**.
Esa release **no sale por JitPack**: la decisión tomada es migrar a **Maven Central bajo
`com.apptolast`**, porque JitPack reescribe el grupo y con ello estropea el Gradle Module Metadata de
KMP (el mismo motivo por el que se movió el fork de kmp-maps). El gate de esa migración es un TXT en
`apptolast.com`, cuyo DNS está en Cloudflare. **Nada de esto entra en este spec**: aquí se traduce, se
deja verde y se para.

**Lo que arrastra la 2.0.0 río abajo, para tenerlo escrito**: cuando Paparcar suba el pin, cambian
todos sus imports `com.apptolast.customlogin.*` → `com.apptolast.baselogin.*`, y podrá borrar el
parche de Koin de su flavor mock (registra a mano `LoginViewModel` porque `presentationModule` era
`internal`; con `loginPresentationModule` público deja de hacer falta).

**Requisitos externos**: ninguno para traducir. Para publicar, los del párrafo anterior.
