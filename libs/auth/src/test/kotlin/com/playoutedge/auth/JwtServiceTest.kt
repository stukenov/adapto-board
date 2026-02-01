package com.playoutedge.auth

import com.playoutedge.domain.enums.UserRole
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days

@DisplayName("JwtService")
class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var config: AuthConfig

    @BeforeEach
    fun setup() {
        config = AuthConfig(
            jwtSecret = "test-secret-key-at-least-32-chars-long",
            jwtIssuer = "test-issuer",
            jwtAudience = "test-audience",
            adminAccessTokenTtl = 15.minutes,
            adminRefreshTokenTtl = 7.days,
            deviceAccessTokenTtl = 1.hours,
            deviceRefreshTokenTtl = 90.days,
            enrollCodeTtl = 30.minutes
        )
        jwtService = JwtService(config)
    }

    @Nested
    @DisplayName("Admin Token Generation")
    inner class AdminTokenGeneration {

        @Test
        @DisplayName("should generate valid admin access token")
        fun `generateAdminAccessToken should return valid JWT`() {
            val claims = AdminClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                role = UserRole.TENANT_ADMIN
            )

            val token = jwtService.generateAdminAccessToken(claims)

            assertNotNull(token)
            assertTrue(token.split(".").size == 3, "Token should have 3 parts")
        }

        @Test
        @DisplayName("should validate generated admin token")
        fun `validateToken should return AdminClaims for valid admin token`() {
            val originalClaims = AdminClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                role = UserRole.TENANT_ADMIN
            )

            val token = jwtService.generateAdminAccessToken(originalClaims)
            val validatedClaims = jwtService.validateToken(token)

            assertNotNull(validatedClaims)
            assertTrue(validatedClaims is AdminClaims)
            val adminClaims = validatedClaims as AdminClaims
            assertEquals(originalClaims.subject, adminClaims.subject)
            assertEquals(originalClaims.tenantId, adminClaims.tenantId)
            assertEquals(originalClaims.role, adminClaims.role)
        }

        @Test
        @DisplayName("should preserve all user roles in token")
        fun `generateAdminAccessToken should preserve role`() {
            UserRole.entries.forEach { role ->
                val claims = AdminClaims(
                    subject = UUID.randomUUID(),
                    tenantId = UUID.randomUUID(),
                    role = role
                )

                val token = jwtService.generateAdminAccessToken(claims)
                val validatedClaims = jwtService.validateToken(token) as AdminClaims

                assertEquals(role, validatedClaims.role)
            }
        }
    }

    @Nested
    @DisplayName("Device Token Generation")
    inner class DeviceTokenGeneration {

        @Test
        @DisplayName("should generate valid device access token")
        fun `generateDeviceAccessToken should return valid JWT`() {
            val claims = DeviceClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                channelId = UUID.randomUUID()
            )

            val token = jwtService.generateDeviceAccessToken(claims)

            assertNotNull(token)
            assertTrue(token.split(".").size == 3)
        }

        @Test
        @DisplayName("should validate generated device token")
        fun `validateToken should return DeviceClaims for valid device token`() {
            val originalClaims = DeviceClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                channelId = UUID.randomUUID()
            )

            val token = jwtService.generateDeviceAccessToken(originalClaims)
            val validatedClaims = jwtService.validateToken(token)

            assertNotNull(validatedClaims)
            assertTrue(validatedClaims is DeviceClaims)
            val deviceClaims = validatedClaims as DeviceClaims
            assertEquals(originalClaims.subject, deviceClaims.subject)
            assertEquals(originalClaims.tenantId, deviceClaims.tenantId)
            assertEquals(originalClaims.channelId, deviceClaims.channelId)
        }

        @Test
        @DisplayName("should handle null channelId")
        fun `generateDeviceAccessToken should handle null channelId`() {
            val originalClaims = DeviceClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                channelId = null
            )

            val token = jwtService.generateDeviceAccessToken(originalClaims)
            val validatedClaims = jwtService.validateToken(token) as DeviceClaims

            assertNull(validatedClaims.channelId)
        }
    }

    @Nested
    @DisplayName("Refresh Token Generation")
    inner class RefreshTokenGeneration {

        @Test
        @DisplayName("should generate unique admin refresh tokens")
        fun `generateAdminRefreshToken should generate unique tokens`() {
            val tokens = (1..10).map { jwtService.generateAdminRefreshToken() }.toSet()
            assertEquals(10, tokens.size, "All refresh tokens should be unique")
        }

        @Test
        @DisplayName("should generate unique device refresh tokens")
        fun `generateDeviceRefreshToken should generate unique tokens`() {
            val tokens = (1..10).map { jwtService.generateDeviceRefreshToken() }.toSet()
            assertEquals(10, tokens.size, "All refresh tokens should be unique")
        }

        @Test
        @DisplayName("should generate URL-safe refresh tokens")
        fun `generateAdminRefreshToken should be URL safe`() {
            val token = jwtService.generateAdminRefreshToken()
            assertTrue(token.matches(Regex("^[A-Za-z0-9_-]+$")))
        }
    }

    @Nested
    @DisplayName("Token Validation")
    inner class TokenValidation {

        @Test
        @DisplayName("should reject invalid token format")
        fun `validateToken should return null for invalid token`() {
            val invalidToken = "not-a-valid-token"
            assertNull(jwtService.validateToken(invalidToken))
        }

        @Test
        @DisplayName("should reject token with wrong signature")
        fun `validateToken should return null for tampered token`() {
            val claims = AdminClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                role = UserRole.OPERATOR
            )
            val token = jwtService.generateAdminAccessToken(claims)
            val tamperedToken = token.dropLast(5) + "xxxxx"

            assertNull(jwtService.validateToken(tamperedToken))
        }

        @Test
        @DisplayName("should reject token from different issuer")
        fun `validateToken should reject token from different service`() {
            val otherConfig = config.copy(jwtSecret = "different-secret-key-for-another-service")
            val otherService = JwtService(otherConfig)

            val claims = AdminClaims(
                subject = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                role = UserRole.OPERATOR
            )
            val tokenFromOther = otherService.generateAdminAccessToken(claims)

            assertNull(jwtService.validateToken(tokenFromOther))
        }

        @Test
        @DisplayName("should reject empty token")
        fun `validateToken should return null for empty token`() {
            assertNull(jwtService.validateToken(""))
        }

        @Test
        @DisplayName("should reject malformed JWT structure")
        fun `validateToken should return null for malformed structure`() {
            assertNull(jwtService.validateToken("header.payload"))
            assertNull(jwtService.validateToken("a.b.c.d"))
        }
    }
}
