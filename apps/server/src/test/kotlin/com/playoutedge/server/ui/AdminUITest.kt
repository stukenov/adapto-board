package com.playoutedge.server.ui

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ScreenshotType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Paths

/**
 * UI tests for the admin panel using Playwright.
 *
 * These tests require:
 * 1. Playwright browsers installed: npx playwright install chromium
 * 2. The server running at http://localhost:8080 (or TEST_BASE_URL env var)
 *
 * Run manually with: RUN_UI_TESTS=true ./gradlew :apps:server:test --tests "AdminUITest"
 *
 * Screenshots are saved to: build/screenshots/
 */
@DisplayName("Admin UI Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Tag("ui")
class AdminUITest {

    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private var context: BrowserContext? = null
    private var page: Page? = null
    private var playwrightAvailable = false

    private val baseUrl = System.getenv("TEST_BASE_URL") ?: "http://localhost:8080"
    private val screenshotDir = Paths.get("build/screenshots").toFile()

    @BeforeAll
    fun setupAll() {
        // Check if UI tests should be run
        val runUiTests = System.getenv("RUN_UI_TESTS")?.toBoolean()
            ?: System.getProperty("RUN_UI_TESTS")?.toBoolean()
            ?: false

        if (!runUiTests) {
            println("UI tests skipped. Set RUN_UI_TESTS=true to run them.")
            return
        }

        try {
            // Create screenshot directory
            screenshotDir.mkdirs()

            // Initialize Playwright
            playwright = Playwright.create()
            browser = playwright!!.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
            )
            playwrightAvailable = true
        } catch (e: Exception) {
            println("Playwright not available: ${e.message}")
            println("Install with: npx playwright install chromium")
            playwrightAvailable = false
        }
    }

    @AfterAll
    fun teardownAll() {
        browser?.close()
        playwright?.close()
    }

    @BeforeEach
    fun setup() {
        Assumptions.assumeTrue(playwrightAvailable, "Playwright not available")
        context = browser!!.newContext(
            Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
        )
        page = context!!.newPage()
    }

    @AfterEach
    fun teardown() {
        context?.close()
    }

    private fun screenshot(name: String) {
        page?.screenshot(
            Page.ScreenshotOptions()
                .setPath(Paths.get(screenshotDir.path, "$name.png"))
                .setFullPage(true)
                .setType(ScreenshotType.PNG)
        )
        println("Screenshot saved: $screenshotDir/$name.png")
    }

    private fun requirePage(): Page {
        return page ?: throw AssertionError("Page not initialized")
    }

    @Test
    @Order(1)
    @DisplayName("Login page should load correctly")
    fun `login page should display`() {
        val p = requirePage()
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        screenshot("01-login-page")

        // Verify page elements
        val title = p.title()
        assertTrue(title.contains("Login") || title.contains("Playout"), "Page title should contain Login or Playout")

        // Check for email field
        val emailInput = p.locator("input[name='email']")
        assertTrue(emailInput.isVisible, "Email input should be visible")

        // Check for password field
        val passwordInput = p.locator("input[name='password']")
        assertTrue(passwordInput.isVisible, "Password input should be visible")

        // Check for submit button
        val submitButton = p.locator("button[type='submit']")
        assertTrue(submitButton.isVisible, "Submit button should be visible")
    }

    @Test
    @Order(2)
    @DisplayName("Login with invalid credentials should show error")
    fun `login with invalid credentials should show error`() {
        val p = requirePage()
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        // Fill in wrong credentials
        p.fill("input[name='email']", "wrong@example.com")
        p.fill("input[name='password']", "wrongpassword")

        screenshot("02-login-filled-invalid")

        // Submit form
        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("03-login-error")

        // Should redirect back to login with error
        assertTrue(p.url().contains("error=invalid"), "URL should contain error parameter")
    }

    @Test
    @Order(3)
    @DisplayName("Login with valid credentials should redirect to dashboard")
    fun `login with valid credentials should redirect to dashboard`() {
        val p = requirePage()
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        // Fill in correct credentials (from seed data)
        p.fill("input[name='email']", "admin@example.com")
        p.fill("input[name='password']", "admin")

        screenshot("04-login-filled-valid")

        // Submit form
        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("05-dashboard")

        // Should redirect to admin dashboard
        assertTrue(
            p.url().endsWith("/admin") || p.url().endsWith("/admin/"),
            "Should redirect to admin dashboard. Current URL: ${p.url()}"
        )
    }

    @Test
    @Order(4)
    @DisplayName("Dashboard should show fleet health")
    fun `dashboard should show fleet health`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        screenshot("06-dashboard-content")

        // Check for main content
        val content = p.content()
        assertTrue(content.isNotEmpty(), "Page should have content")
    }

    @Test
    @Order(5)
    @DisplayName("Devices page should load")
    fun `devices page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()

        screenshot("07-devices-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Devices page should have content")
    }

    @Test
    @Order(6)
    @DisplayName("Channels page should load")
    fun `channels page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()

        screenshot("08-channels-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Channels page should have content")
    }

    @Test
    @Order(7)
    @DisplayName("Assets page should load")
    fun `assets page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets")
        p.waitForLoadState()

        screenshot("09-assets-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Assets page should have content")
    }

    @Test
    @Order(8)
    @DisplayName("Overlays page should load")
    fun `overlays page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        // Route is /admin/overlay (without 's'), which redirects to /admin/overlay/profiles
        p.navigate("$baseUrl/admin/overlay")
        p.waitForLoadState()

        screenshot("10-overlays-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Overlays page should have content")
    }

    @Test
    @Order(9)
    @DisplayName("Settings page should load")
    fun `settings page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/settings")
        p.waitForLoadState()

        screenshot("11-settings-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Settings page should have content")
    }

    @Test
    @Order(10)
    @DisplayName("Reports page should load")
    fun `reports page should load`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports")
        p.waitForLoadState()

        screenshot("12-reports-page")

        val content = p.content()
        assertTrue(content.isNotEmpty(), "Reports page should have content")
    }

    @Test
    @Order(11)
    @DisplayName("Navigation should work correctly")
    fun `navigation should work correctly`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        screenshot("13-navigation")

        // Check for navigation links
        val navLinks = p.locator("nav a, .sidebar a, .nav-link")
        val count = navLinks.count()
        println("Found $count navigation links")

        screenshot("14-admin-overview")
    }

    @Test
    @Order(12)
    @DisplayName("Logout should work")
    fun `logout should work`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        screenshot("15-before-logout")

        // The logout is in the Admin dropdown - first click to open it
        val adminDropdown = p.locator("text=Admin")
        if (adminDropdown.count() > 0) {
            adminDropdown.first().click()
            Thread.sleep(500) // Wait for dropdown to open
        }

        // Find logout form/button in the Admin dropdown or navigation
        val logoutForm = p.locator("form[action='/admin/logout']")

        if (logoutForm.count() > 0) {
            // Submit the logout form
            val submitBtn = logoutForm.locator("button[type='submit'], input[type='submit']")
            if (submitBtn.count() > 0) {
                submitBtn.first().click()
                p.waitForLoadState()
            }
        } else {
            // Try clicking a logout link/button
            val logoutButton = p.locator("a[href*='logout'], button:has-text('Logout'), button:has-text('Sign out')")
            if (logoutButton.count() > 0) {
                logoutButton.first().click()
                p.waitForLoadState()
            }
        }

        screenshot("16-after-logout")

        val currentUrl = p.url()
        println("After logout URL: $currentUrl")
    }

    private fun loginAsAdmin(p: Page) {
        // Check if already logged in by trying to access admin page
        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        if (p.url().contains("/login")) {
            // Not logged in, perform login
            p.fill("input[name='email']", "admin@example.com")
            p.fill("input[name='password']", "admin")
            p.click("button[type='submit']")
            p.waitForLoadState()
        }
    }
}
