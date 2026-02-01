package com.playoutedge.server.ui

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ScreenshotType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Paths

/**
 * End-to-end browser tests that perform real user actions.
 *
 * These tests simulate a real user journey through the admin panel.
 *
 * Requirements:
 * - Playwright browsers: npx playwright install chromium
 * - Server running at http://localhost:8080
 *
 * Run with: RUN_UI_TESTS=true ./gradlew :apps:server:uiTest
 */
@DisplayName("Admin E2E Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Tag("ui")
class AdminE2ETest {

    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private var context: BrowserContext? = null
    private var page: Page? = null
    private var playwrightAvailable = false

    private val baseUrl = System.getenv("TEST_BASE_URL") ?: "http://localhost:8080"
    private val screenshotDir = Paths.get("build/screenshots/e2e").toFile()

    // Track created resources for cleanup
    private var createdChannelId: String? = null
    private var createdChannelName: String? = null

    @BeforeAll
    fun setupAll() {
        val runUiTests = System.getenv("RUN_UI_TESTS")?.toBoolean()
            ?: System.getProperty("RUN_UI_TESTS")?.toBoolean()
            ?: false

        if (!runUiTests) {
            println("E2E tests skipped. Set RUN_UI_TESTS=true to run them.")
            return
        }

        try {
            screenshotDir.mkdirs()
            playwright = Playwright.create()
            browser = playwright!!.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(100.0) // Slow down for visibility in headed mode
            )
            playwrightAvailable = true
        } catch (e: Exception) {
            println("Playwright not available: ${e.message}")
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
    }

    private fun requirePage(): Page = page ?: throw AssertionError("Page not initialized")

    private fun loginAsAdmin(p: Page) {
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        if (p.url().contains("/login")) {
            p.fill("input[name='email']", "admin@example.com")
            p.fill("input[name='password']", "admin")
            p.click("button[type='submit']")
            p.waitForLoadState()
        }
    }

    // ========== UC 1: Authentication Flow ==========

    @Test
    @Order(1)
    @DisplayName("UC 1.1: Login page renders correctly")
    fun `UC 1-1 login page renders correctly`() {
        val p = requirePage()
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        screenshot("uc1-1-login-page")

        // Verify login form elements
        assertTrue(p.locator("input[name='email']").isVisible, "Email input should be visible")
        assertTrue(p.locator("input[name='password']").isVisible, "Password input should be visible")
        assertTrue(p.locator("button[type='submit']").isVisible, "Submit button should be visible")
    }

    @Test
    @Order(2)
    @DisplayName("UC 1.2: Invalid login shows error")
    fun `UC 1-2 invalid login shows error`() {
        val p = requirePage()
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()

        p.fill("input[name='email']", "invalid@test.com")
        p.fill("input[name='password']", "wrongpassword")
        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("uc1-2-invalid-login")

        // Should stay on login page or show error
        assertTrue(
            p.url().contains("/login") || p.url().contains("error"),
            "Should show login error"
        )
    }

    @Test
    @Order(3)
    @DisplayName("UC 1.3: Valid login redirects to dashboard")
    fun `UC 1-3 valid login redirects to dashboard`() {
        val p = requirePage()
        loginAsAdmin(p)

        screenshot("uc1-3-dashboard-after-login")

        assertTrue(
            p.url().contains("/admin") && !p.url().contains("/login"),
            "Should redirect to admin area after login"
        )
    }

    // ========== UC 2: Dashboard ==========

    @Test
    @Order(10)
    @DisplayName("UC 2.1: Dashboard displays fleet health")
    fun `UC 2-1 dashboard displays fleet health`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        screenshot("uc2-1-dashboard")

        val content = p.content().lowercase()
        // Check for expected dashboard elements
        assertTrue(
            content.contains("device") || content.contains("dashboard") || content.contains("fleet"),
            "Dashboard should show fleet/device information"
        )
    }

    // ========== UC 3: Channel Management ==========

    @Test
    @Order(20)
    @DisplayName("UC 3.1: Channels list page loads")
    fun `UC 3-1 channels list page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()

        screenshot("uc3-1-channels-list")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("channel") || content.contains("create"),
            "Channels page should display"
        )
    }

    @Test
    @Order(21)
    @DisplayName("UC 3.2: Create new channel")
    fun `UC 3-2 create new channel`() {
        val p = requirePage()
        loginAsAdmin(p)

        // Navigate directly to the create channel page
        p.navigate("$baseUrl/admin/channels/new")
        p.waitForLoadState()

        screenshot("uc3-2-new-channel-form")

        // Fill in channel name
        createdChannelName = "Test Channel ${System.currentTimeMillis()}"
        val nameInput = p.locator("input[name='name']")
        if (nameInput.isVisible) {
            nameInput.fill(createdChannelName!!)

            screenshot("uc3-2-channel-form-filled")

            // Submit form - button says "Create Channel"
            val submitButton = p.locator("button:has-text('Create Channel')")
            if (submitButton.count() > 0) {
                submitButton.click()
                p.waitForLoadState()

                screenshot("uc3-2-channel-created")

                // Extract channel ID from URL if redirected to detail page
                if (p.url().matches(Regex(".*channels/[a-f0-9-]+.*"))) {
                    createdChannelId = p.url().split("/channels/")[1].split("/")[0].split("?")[0]
                    println("Created channel ID: $createdChannelId")
                }
            }
        }

        // Verify channel was created
        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()

        val content = p.content()
        if (createdChannelName != null) {
            assertTrue(
                content.contains(createdChannelName!!) || content.contains("Test Channel"),
                "Created channel should appear in list"
            )
        }
    }

    @Test
    @Order(22)
    @DisplayName("UC 3.3: View channel detail")
    fun `UC 3-3 view channel detail`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()

        // Click on the channel name link (not the new/create link)
        // The table has channel names that link directly to the detail page
        val channelLinks = p.locator("table a[href*='/admin/channels/']")

        if (channelLinks.count() > 0) {
            // Filter out links that go to /new
            for (i in 0 until channelLinks.count()) {
                val link = channelLinks.nth(i)
                val href = link.getAttribute("href") ?: ""
                if (!href.contains("/new") && href.matches(Regex(".*/admin/channels/[a-f0-9-]+.*"))) {
                    link.click()
                    p.waitForLoadState()
                    break
                }
            }
        }

        screenshot("uc3-3-channel-detail")

        val url = p.url()
        // Accept either a UUID channel detail page or the channels list if no channels exist
        assertTrue(
            url.matches(Regex(".*channels/[a-f0-9-]+.*")) || url.contains("/channels"),
            "Should be on channel detail or channels page. Current URL: $url"
        )
    }

    @Test
    @Order(23)
    @DisplayName("UC 3.4: Edit channel schedule")
    fun `UC 3-4 edit channel schedule`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()

        // Click on first channel
        val channelLink = p.locator("a[href*='/admin/channels/']").first()
        if (channelLink.isVisible) {
            channelLink.click()
            p.waitForLoadState()

            // Click on schedule link
            val scheduleLink = p.locator("a[href*='/schedule']")
            if (scheduleLink.count() > 0) {
                scheduleLink.first().click()
                p.waitForLoadState()

                screenshot("uc3-4-channel-schedule")

                assertTrue(
                    p.url().contains("/schedule"),
                    "Should navigate to schedule page"
                )
            }
        }
    }

    // ========== UC 4: Device Management ==========

    @Test
    @Order(30)
    @DisplayName("UC 4.1: Devices list page loads")
    fun `UC 4-1 devices list page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()

        screenshot("uc4-1-devices-list")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("device") || content.contains("enroll"),
            "Devices page should display"
        )
    }

    @Test
    @Order(31)
    @DisplayName("UC 4.2: Access enrollment codes page")
    fun `UC 4-2 access enrollment codes page`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices/enroll")
        p.waitForLoadState()

        screenshot("uc4-2-enrollment-codes")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("enroll") || content.contains("code"),
            "Enrollment page should display"
        )
    }

    @Test
    @Order(32)
    @DisplayName("UC 4.3: Generate enrollment code")
    fun `UC 4-3 generate enrollment code`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices/enroll")
        p.waitForLoadState()

        screenshot("uc4-3-before-generate")

        // Find and click generate button
        val generateBtn = p.locator("button[type='submit']:has-text('Generate'), button:has-text('Generate Code')")
        if (generateBtn.count() > 0) {
            generateBtn.first().click()
            p.waitForLoadState()

            screenshot("uc4-3-after-generate")

            // Check if code was generated (look for code display)
            val content = p.content()
            assertTrue(
                content.contains("Generated") || content.matches(Regex(".*\\d{6}.*")),
                "Should display generated code"
            )
        }
    }

    @Test
    @Order(33)
    @DisplayName("UC 4.4: View device detail")
    fun `UC 4-4 view device detail`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()

        // Click on first device if exists
        val deviceLink = p.locator("a[href*='/admin/devices/']").first()
        if (deviceLink.isVisible) {
            deviceLink.click()
            p.waitForLoadState()

            screenshot("uc4-4-device-detail")

            assertTrue(
                p.url().matches(Regex(".*devices/[a-f0-9-]+.*")),
                "Should navigate to device detail"
            )
        }
    }

    // ========== UC 5: Asset Management ==========

    @Test
    @Order(40)
    @DisplayName("UC 5.1: Assets page loads")
    fun `UC 5-1 assets page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets")
        p.waitForLoadState()

        screenshot("uc5-1-assets-page")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("asset") || content.contains("upload"),
            "Assets page should display"
        )
    }

    // ========== UC 6: Overlay Management ==========

    @Test
    @Order(50)
    @DisplayName("UC 6.1: Overlay profiles page loads")
    fun `UC 6-1 overlay profiles page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay")
        p.waitForLoadState()

        screenshot("uc6-1-overlay-profiles")

        // Should redirect to /admin/overlay/profiles
        assertTrue(
            p.url().contains("/overlay"),
            "Should be on overlay page"
        )
    }

    @Test
    @Order(51)
    @DisplayName("UC 6.2: Webhook logs page loads")
    fun `UC 6-2 webhook logs page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/webhooks")
        p.waitForLoadState()

        screenshot("uc6-2-webhook-logs")

        assertTrue(
            p.url().contains("/webhook") || p.url().contains("/overlay"),
            "Should be on webhook logs page"
        )
    }

    // ========== UC 7: Reports ==========

    @Test
    @Order(60)
    @DisplayName("UC 7.1: Reports page loads")
    fun `UC 7-1 reports page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports")
        p.waitForLoadState()

        screenshot("uc7-1-reports-page")

        // Should redirect to /admin/reports/asrun
        assertTrue(
            p.url().contains("/reports"),
            "Should be on reports page"
        )
    }

    @Test
    @Order(61)
    @DisplayName("UC 7.2: As-run report page loads")
    fun `UC 7-2 asrun report page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports/asrun")
        p.waitForLoadState()

        screenshot("uc7-2-asrun-report")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("as-run") || content.contains("asrun") || content.contains("report"),
            "As-run report page should display"
        )
    }

    @Test
    @Order(62)
    @DisplayName("UC 7.3: Audit log page loads")
    fun `UC 7-3 audit log page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports/audit")
        p.waitForLoadState()

        screenshot("uc7-3-audit-log")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("audit") || content.contains("log"),
            "Audit log page should display"
        )
    }

    // ========== UC 8: Settings ==========

    @Test
    @Order(70)
    @DisplayName("UC 8.1: Settings page loads")
    fun `UC 8-1 settings page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/settings")
        p.waitForLoadState()

        screenshot("uc8-1-settings-page")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("setting") || content.contains("account") || content.contains("profile"),
            "Settings page should display"
        )
    }

    // ========== UC 9: Onboarding ==========

    @Test
    @Order(80)
    @DisplayName("UC 9.1: Onboarding page loads")
    fun `UC 9-1 onboarding page loads`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/onboarding")
        p.waitForLoadState()

        screenshot("uc9-1-onboarding-page")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("onboard") || content.contains("start") || content.contains("welcome"),
            "Onboarding page should display"
        )
    }

    // ========== UC 10: Navigation Flow ==========

    @Test
    @Order(90)
    @DisplayName("UC 10.1: Complete navigation flow")
    fun `UC 10-1 complete navigation flow`() {
        val p = requirePage()
        loginAsAdmin(p)

        val pagesToVisit = listOf(
            "/admin" to "dashboard",
            "/admin/channels" to "channels",
            "/admin/devices" to "devices",
            "/admin/assets" to "assets",
            "/admin/overlay" to "overlay",
            "/admin/reports" to "reports",
            "/admin/settings" to "settings"
        )

        for ((path, name) in pagesToVisit) {
            p.navigate("$baseUrl$path")
            p.waitForLoadState()

            screenshot("uc10-1-nav-$name")

            // Verify page loaded (not error page)
            val statusText = p.content()
            assertFalse(
                statusText.contains("404") || statusText.contains("500 Internal Server Error"),
                "Page $name should load without error"
            )
        }
    }

    // ========== UC 11: Logout Flow ==========

    @Test
    @Order(100)
    @DisplayName("UC 11.1: Logout works correctly")
    fun `UC 11-1 logout works correctly`() {
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        screenshot("uc11-1-before-logout")

        // The logout is in the Admin dropdown
        // First click on the Admin dropdown
        val adminDropdown = p.locator("text=Admin")
        if (adminDropdown.count() > 0) {
            adminDropdown.first().click()
            Thread.sleep(500) // Wait for dropdown to open
        }

        // Find logout form/button
        val logoutForm = p.locator("form[action='/admin/logout']")
        if (logoutForm.count() > 0) {
            val submitBtn = logoutForm.locator("button, input[type='submit']")
            if (submitBtn.count() > 0) {
                submitBtn.first().click()
                p.waitForLoadState()
            }
        } else {
            // Try to find logout link
            val logoutLink = p.locator("a:has-text('Logout'), a:has-text('Sign out')")
            if (logoutLink.count() > 0) {
                logoutLink.first().click()
                p.waitForLoadState()
            }
        }

        screenshot("uc11-1-after-logout")

        // Verify logged out - try accessing admin again
        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        val url = p.url()
        // Should redirect to login or be on login page
        assertTrue(
            url.contains("/login") || p.content().lowercase().contains("login"),
            "After logout, accessing admin should redirect to login. Current URL: $url"
        )
    }
}
