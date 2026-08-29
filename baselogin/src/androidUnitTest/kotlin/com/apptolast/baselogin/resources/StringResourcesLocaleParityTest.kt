package com.apptolast.baselogin.resources

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Spec 012 — the library speaks the same nine languages its consumer apps do.
 *
 * Lives in `androidUnitTest` and not in `commonTest` on purpose: it inspects the resource *tree* on
 * disk, and `commonTest` also compiles for iOS, where there is no filesystem API. It reads nothing
 * but the plain text under `commonMain/composeResources`, so it needs no device and no Firebase
 * config, and runs with the documented `./gradlew :baselogin:testDebugUnitTest`.
 *
 * (`src/androidHostTest/` is *not* the JVM test source set in this build — it is a template leftover
 * that nothing compiles. The registered one is `src/androidUnitTest/`.)
 *
 * The point of the test is not the translations this spec adds but the ones that come later: the
 * invariants below break silently every time someone adds a key to the base and forgets the other
 * eight files.
 *
 * Covers AC-01 (nine locales, the same 92 keys), AC-02 (no escaped apostrophe) and AC-03 (the two
 * placeholders of the base survive translation).
 */
class StringResourcesLocaleParityTest {

    @Test
    fun `AC-01 the resource tree declares exactly the nine locales the consumers maintain`() {
        // Given the :baselogin resource tree
        // When the values* directories are listed
        val present = composeResources.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values") }
            .map { it.name }
            .toSortedSet()

        // Then there are nine, and every one of them carries a strings.xml
        assertEquals(EXPECTED_LOCALES.toSortedSet(), present, "unexpected set of locale directories")
        present.forEach { locale ->
            assertTrue(stringsFile(locale).isFile, "$locale has no strings.xml")
        }
    }

    @Test
    fun `AC-01 every locale declares the same keys as the base`() {
        // Given the base locale as the reference
        val baseKeys = keysOf(BASE_LOCALE)
        assertEquals(EXPECTED_KEY_COUNT, baseKeys.size, "the base no longer declares $EXPECTED_KEY_COUNT keys")
        assertEquals(baseKeys.size, baseKeys.toSet().size, "the base declares a key twice")

        // When the keys of each locale are compared against it
        // Then no locale is missing a key, and none declares one the base does not have
        translatedLocales().forEach { locale ->
            val keys = keysOf(locale)
            assertEquals(keys.size, keys.toSet().size, "$locale declares a key twice")
            assertEquals(
                emptyList(),
                (baseKeys - keys.toSet()).sorted(),
                "$locale is missing keys — a half-translated locale is worse than an absent one",
            )
            assertEquals(emptyList(), (keys - baseKeys.toSet()).sorted(), "$locale declares keys the base does not")
        }
    }

    @Test
    fun `AC-02 no locale escapes the apostrophe`() {
        // Given every strings.xml of the module
        // When the escaped sequence is counted in the raw text
        // Then the count is zero: Compose Resources does not unescape it and the user reads the backslash
        EXPECTED_LOCALES.forEach { locale ->
            val escaped = stringsFile(locale).readText().occurrencesOf(ESCAPED_APOSTROPHE)
            assertEquals(0, escaped, "$locale escapes the apostrophe $escaped time(s) — write it raw")
        }
    }

    @Test
    fun `AC-03 the placeholders of the base survive in every locale`() {
        // Given the base keys that carry a placeholder
        val baseValues = valuesOf(BASE_LOCALE)
        val placeholderKeys = baseValues.filterValues { it.contains(PLACEHOLDER) }.keys.sorted()
        assertEquals(EXPECTED_PLACEHOLDER_KEYS, placeholderKeys, "the set of keys carrying a placeholder changed")

        // When their translations are read
        // Then each one still carries exactly one, or the text breaks at runtime and not at compile time
        translatedLocales().forEach { locale ->
            val values = valuesOf(locale)
            placeholderKeys.forEach { key ->
                val count = values.getValue(key).occurrencesOf(PLACEHOLDER)
                assertEquals(1, count, "$locale/$key carries $count placeholders, expected exactly 1")
            }
        }
    }

    private fun translatedLocales() = EXPECTED_LOCALES.filter { it != BASE_LOCALE }

    private fun stringsFile(locale: String) = File(composeResources, "$locale/strings.xml")

    private fun entriesOf(locale: String) = STRING_ENTRY.findAll(stringsFile(locale).readText())

    private fun keysOf(locale: String) = entriesOf(locale).map { it.groupValues[1] }.toList()

    private fun valuesOf(locale: String) = entriesOf(locale).associate { it.groupValues[1] to it.groupValues[2] }

    private fun String.occurrencesOf(token: String) = split(token).size - 1

    private companion object {
        const val BASE_LOCALE = "values"

        val EXPECTED_LOCALES = listOf(
            BASE_LOCALE,
            "values-es",
            "values-fr",
            "values-it",
            "values-pt",
            "values-de",
            "values-nl",
            "values-pl",
            "values-ro",
        )

        const val EXPECTED_KEY_COUNT = 92

        val EXPECTED_PLACEHOLDER_KEYS = listOf(
            "magic_link_screen_success_description",
            "phone_auth_screen_otp_description",
        )

        const val ESCAPED_APOSTROPHE = "\\'"

        const val PLACEHOLDER = "%1\$s"

        val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)

        /**
         * The unit test working directory is not guaranteed to be the module directory, so the
         * resource root is found by walking up from wherever the JVM was started.
         */
        val composeResources: File by lazy {
            val start = File(System.getProperty("user.dir")).absoluteFile
            generateSequence(start) { it.parentFile }
                .flatMap {
                    sequenceOf(
                        File(it, "src/commonMain/composeResources"),
                        File(it, "baselogin/src/commonMain/composeResources"),
                    )
                }
                .firstOrNull(File::isDirectory)
                ?: error("could not locate baselogin/src/commonMain/composeResources starting from $start")
        }
    }
}
