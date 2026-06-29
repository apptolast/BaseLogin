package com.apptolast.customlogin.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CountryCallingCodesTest {

    // ── Data integrity ────────────────────────────────────────────────────

    @Test
    fun `COMMON_COUNTRIES is not empty`() {
        assertTrue(COMMON_COUNTRIES.isNotEmpty())
    }

    @Test
    fun `all entries have non blank flag name and dialCode`() {
        COMMON_COUNTRIES.forEach { country ->
            assertTrue(country.flag.isNotBlank(), "flag is blank for ${country.name}")
            assertTrue(country.name.isNotBlank(), "name is blank for entry with flag ${country.flag}")
            assertTrue(country.dialCode.isNotBlank(), "dialCode is blank for ${country.name}")
        }
    }

    @Test
    fun `all dialCodes start with a plus sign`() {
        COMMON_COUNTRIES.forEach { country ->
            assertTrue(
                country.dialCode.startsWith("+"),
                "dialCode '${country.dialCode}' for ${country.name} does not start with '+'"
            )
        }
    }

    @Test
    fun `displayLabel combines flag and dialCode`() {
        COMMON_COUNTRIES.forEach { country ->
            val expected = "${country.flag} ${country.dialCode}"
            assertEquals(
                expected,
                country.displayLabel,
                "displayLabel mismatch for ${country.name}"
            )
        }
    }

    @Test
    fun `no country name is duplicated`() {
        val names = COMMON_COUNTRIES.map { it.name }
        val unique = names.toSet()
        assertEquals(
            unique.size, names.size,
            "Duplicate country names found: ${names.groupBy { it }.filter { it.value.size > 1 }.keys}"
        )
    }

    // ── Spot checks for well-known entries ────────────────────────────────

    @Test
    fun `United States entry exists with correct dialCode`() {
        val us = COMMON_COUNTRIES.find { it.name == "United States" }
        assertNotNull(us, "United States not found")
        assertEquals("+1", us.dialCode)
    }

    @Test
    fun `Spain entry exists with correct dialCode`() {
        val spain = COMMON_COUNTRIES.find { it.name == "Spain" }
        assertNotNull(spain, "Spain not found")
        assertEquals("+34", spain.dialCode)
    }

    @Test
    fun `Germany entry exists with correct dialCode`() {
        val germany = COMMON_COUNTRIES.find { it.name == "Germany" }
        assertNotNull(germany, "Germany not found")
        assertEquals("+49", germany.dialCode)
    }

    @Test
    fun `dialCode digits only contain valid characters`() {
        COMMON_COUNTRIES.forEach { country ->
            val digitsOnly = country.dialCode.removePrefix("+")
            assertFalse(
                digitsOnly.isBlank(),
                "dialCode has only '+' and no digits for ${country.name}"
            )
            assertTrue(
                digitsOnly.all { it.isDigit() },
                "dialCode '${country.dialCode}' for ${country.name} contains non-digit characters after '+'"
            )
        }
    }
}
