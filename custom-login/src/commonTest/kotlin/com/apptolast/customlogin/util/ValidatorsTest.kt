package com.apptolast.customlogin.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {

    // ── Email ──────────────────────────────────────────────────────────────

    @Test
    fun `valid emails return true`() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("user.name+tag@sub.domain.co"))
        assertTrue(Validators.isValidEmail("user_123@example.org"))
        assertTrue(Validators.isValidEmail("a@b.io"))
    }

    @Test
    fun `email without at-sign returns false`() {
        assertFalse(Validators.isValidEmail("userexample.com"))
    }

    @Test
    fun `email without domain returns false`() {
        assertFalse(Validators.isValidEmail("user@"))
    }

    @Test
    fun `blank email returns false`() {
        assertFalse(Validators.isValidEmail(""))
    }

    @Test
    fun `email with only spaces returns false`() {
        assertFalse(Validators.isValidEmail("   "))
    }

    // ── Password ───────────────────────────────────────────────────────────

    @Test
    fun `password with exactly 6 chars returns true`() {
        assertTrue(Validators.isValidPassword("abcdef"))
    }

    @Test
    fun `password with more than 6 chars returns true`() {
        assertTrue(Validators.isValidPassword("secureP@ssw0rd!"))
    }

    @Test
    fun `password with 5 chars returns false`() {
        assertFalse(Validators.isValidPassword("abcde"))
    }

    @Test
    fun `empty password returns false`() {
        assertFalse(Validators.isValidPassword(""))
    }
}
