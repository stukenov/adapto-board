package com.playoutedge.auth

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

@DisplayName("PasswordService")
class PasswordServiceTest {

    private val passwordService = PasswordService(costFactor = 10) // Lower cost for faster tests

    @Test
    @DisplayName("should hash password")
    fun `hash should return bcrypt hash`() {
        val password = "testPassword123"
        val hash = passwordService.hash(password)

        assertNotNull(hash)
        assertTrue(hash.startsWith("\$2a\$"))
        assertNotEquals(password, hash)
    }

    @Test
    @DisplayName("should verify correct password")
    fun `verify should return true for correct password`() {
        val password = "correctPassword"
        val hash = passwordService.hash(password)

        assertTrue(passwordService.verify(password, hash))
    }

    @Test
    @DisplayName("should reject incorrect password")
    fun `verify should return false for incorrect password`() {
        val password = "correctPassword"
        val wrongPassword = "wrongPassword"
        val hash = passwordService.hash(password)

        assertFalse(passwordService.verify(wrongPassword, hash))
    }

    @Test
    @DisplayName("should generate unique hashes for same password")
    fun `hash should generate different hashes for same password`() {
        val password = "samePassword"
        val hash1 = passwordService.hash(password)
        val hash2 = passwordService.hash(password)

        assertNotEquals(hash1, hash2)
        assertTrue(passwordService.verify(password, hash1))
        assertTrue(passwordService.verify(password, hash2))
    }

    @Test
    @DisplayName("should handle empty password")
    fun `hash should work with empty password`() {
        val password = ""
        val hash = passwordService.hash(password)

        assertTrue(passwordService.verify(password, hash))
        assertFalse(passwordService.verify("notempty", hash))
    }

    @Test
    @DisplayName("should handle special characters")
    fun `hash should work with special characters`() {
        val password = "p@ssw0rd!#\$%^&*()_+-=[]{}|;':\",./<>?"
        val hash = passwordService.hash(password)

        assertTrue(passwordService.verify(password, hash))
    }

    @Test
    @DisplayName("should handle unicode characters")
    fun `hash should work with unicode characters`() {
        val password = "пароль密码パスワード"
        val hash = passwordService.hash(password)

        assertTrue(passwordService.verify(password, hash))
    }
}
