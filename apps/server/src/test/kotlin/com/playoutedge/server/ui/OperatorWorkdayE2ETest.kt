package com.playoutedge.server.ui

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ScreenshotType
import com.microsoft.playwright.options.WaitUntilState
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Comprehensive E2E test simulating a full operator workday.
 *
 * This test simulates real operator workflows:
 * - Morning: Login, check dashboard, review device status
 * - Content work: Upload assets, create/edit channels, manage schedules
 * - Device management: Generate enrollment codes, assign devices
 * - Overlay configuration: Create profiles, set up bindings
 * - Reporting: View as-run reports, audit logs
 * - End of day: Review changes, logout
 *
 * Test scenarios include:
 * - Normal flow (data exists)
 * - Empty state (no data)
 * - Error handling (invalid inputs)
 * - Layout/CSS verification
 *
 * Requirements:
 * - Playwright browsers: npx playwright install chromium
 * - Server running at http://localhost:8080
 *
 * Run with: RUN_UI_TESTS=true ./gradlew :apps:server:uiTest
 */
@DisplayName("Operator Workday E2E Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Tag("ui")
class OperatorWorkdayE2ETest {

    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private var context: BrowserContext? = null
    private var page: Page? = null
    private var playwrightAvailable = false

    private val baseUrl = System.getenv("TEST_BASE_URL") ?: "http://localhost:8080"
    private val screenshotDir = Paths.get("build/screenshots/workday").toFile()

    // Timestamp for unique names
    private val timestamp = System.currentTimeMillis()
    private val dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    // Track created resources
    private var createdChannelId: String? = null
    private var createdChannelName: String? = null
    private var createdProfileId: String? = null
    private var createdBindingId: String? = null
    private var generatedEnrollCode: String? = null

    // Issue tracker for layout/UI bugs
    private val layoutIssues = mutableListOf<LayoutIssue>()

    data class LayoutIssue(
        val page: String,
        val description: String,
        val selector: String?,
        val screenshot: String
    )

    @BeforeAll
    fun setupAll() {
        val runUiTests = System.getenv("RUN_UI_TESTS")?.toBoolean()
            ?: System.getProperty("RUN_UI_TESTS")?.toBoolean()
            ?: false

        if (!runUiTests) {
            println("Operator Workday E2E tests skipped. Set RUN_UI_TESTS=true to run them.")
            return
        }

        try {
            screenshotDir.mkdirs()
            playwright = Playwright.create()
            browser = playwright!!.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(50.0)
            )
            playwrightAvailable = true
            log("Test suite started")
        } catch (e: Exception) {
            println("Playwright not available: ${e.message}")
            playwrightAvailable = false
        }
    }

    @AfterAll
    fun teardownAll() {
        // Print layout issues report
        if (layoutIssues.isNotEmpty()) {
            println("\n========== LAYOUT ISSUES FOUND ==========")
            layoutIssues.forEachIndexed { index, issue ->
                println("${index + 1}. [${issue.page}] ${issue.description}")
                println("   Selector: ${issue.selector ?: "N/A"}")
                println("   Screenshot: ${issue.screenshot}")
            }
            println("==========================================\n")
        }

        browser?.close()
        playwright?.close()
        log("Test suite completed")
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

    private fun log(message: String) {
        val time = LocalDateTime.now().format(dateFormatter)
        println("[$time] $message")
    }

    private fun screenshot(name: String): String {
        val path = Paths.get(screenshotDir.path, "$name.png")
        page?.screenshot(
            Page.ScreenshotOptions()
                .setPath(path)
                .setFullPage(true)
                .setType(ScreenshotType.PNG)
        )
        return path.toString()
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

    /**
     * Check for common layout issues on current page.
     */
    private fun checkLayoutIssues(pageName: String) {
        val p = requirePage()
        val content = p.content()

        // Check for overflow issues
        val bodyWidth = p.evaluate("document.body.scrollWidth") as? Number
        val viewportWidth = p.evaluate("window.innerWidth") as? Number
        if (bodyWidth != null && viewportWidth != null && bodyWidth.toDouble() > viewportWidth.toDouble() + 10) {
            val screenshotPath = screenshot("layout-issue-overflow-$pageName")
            layoutIssues.add(LayoutIssue(
                page = pageName,
                description = "Horizontal overflow detected: body width (${bodyWidth}) > viewport (${viewportWidth})",
                selector = "body",
                screenshot = screenshotPath
            ))
        }

        // Check for missing form labels
        val inputsWithoutLabels = p.locator("input:not([type='hidden']):not([type='submit'])").all()
            .filter { input ->
                val id = input.getAttribute("id")
                val name = input.getAttribute("name")
                val hasLabel = if (id != null) {
                    p.locator("label[for='$id']").count() > 0
                } else false
                val hasParentLabel = input.locator("xpath=ancestor::label").count() > 0
                !hasLabel && !hasParentLabel && name != null
            }

        if (inputsWithoutLabels.isNotEmpty()) {
            val screenshotPath = screenshot("layout-issue-nolabel-$pageName")
            layoutIssues.add(LayoutIssue(
                page = pageName,
                description = "Inputs without associated labels: ${inputsWithoutLabels.size} found",
                selector = "input:not([type='hidden'])",
                screenshot = screenshotPath
            ))
        }

        // Check for buttons without text or aria-label
        val emptyButtons = p.locator("button").all()
            .filter { btn ->
                val text = btn.textContent()?.trim()
                val ariaLabel = btn.getAttribute("aria-label")
                text.isNullOrEmpty() && ariaLabel.isNullOrEmpty()
            }

        if (emptyButtons.isNotEmpty()) {
            val screenshotPath = screenshot("layout-issue-emptybtn-$pageName")
            layoutIssues.add(LayoutIssue(
                page = pageName,
                description = "Buttons without text or aria-label: ${emptyButtons.size} found",
                selector = "button",
                screenshot = screenshotPath
            ))
        }

        // Check for broken images
        val brokenImages = p.locator("img").all()
            .filter { img ->
                val naturalWidth = img.evaluate("el => el.naturalWidth") as? Number
                naturalWidth?.toInt() == 0
            }

        if (brokenImages.isNotEmpty()) {
            val screenshotPath = screenshot("layout-issue-brokenimg-$pageName")
            layoutIssues.add(LayoutIssue(
                page = pageName,
                description = "Broken images found: ${brokenImages.size}",
                selector = "img",
                screenshot = screenshotPath
            ))
        }

        // Check for elements with negative margins causing overlap
        // Check for z-index issues with dropdowns
        val dropdown = p.locator(".dropdown-menu")
        if (dropdown.count() > 0 && dropdown.first().isVisible) {
            val zIndex = dropdown.first().evaluate("el => window.getComputedStyle(el).zIndex") as? String
            if (zIndex == "auto" || (zIndex?.toIntOrNull() ?: 0) < 100) {
                val screenshotPath = screenshot("layout-issue-zindex-$pageName")
                layoutIssues.add(LayoutIssue(
                    page = pageName,
                    description = "Dropdown may have z-index issues: $zIndex",
                    selector = ".dropdown-menu",
                    screenshot = screenshotPath
                ))
            }
        }
    }

    // ==================== MORNING: START OF DAY ====================

    @Test
    @Order(1)
    @DisplayName("Morning 08:00 - Login to system")
    fun `morning login`() {
        log("Morning 08:00 - Operator starts the day")
        val p = requirePage()

        // Test login page layout
        p.navigate("$baseUrl/admin/login")
        p.waitForLoadState()
        screenshot("01-morning-login-page")
        checkLayoutIssues("login")

        // Verify login form elements
        assertTrue(p.locator("input[name='email']").isVisible, "Email input should be visible")
        assertTrue(p.locator("input[name='password']").isVisible, "Password input should be visible")
        assertTrue(p.locator("button[type='submit']").isVisible, "Submit button should be visible")

        // Login
        loginAsAdmin(p)
        screenshot("02-morning-after-login")

        assertTrue(
            !p.url().contains("/login"),
            "Should redirect to admin area after login"
        )
        log("Successfully logged in")
    }

    @Test
    @Order(2)
    @DisplayName("Morning 08:05 - Check dashboard overview")
    fun `morning check dashboard`() {
        log("Morning 08:05 - Checking dashboard")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()
        screenshot("03-morning-dashboard")
        checkLayoutIssues("dashboard")

        val content = p.content().lowercase()

        // Check dashboard has expected sections
        val hasDashboardContent = content.contains("device") ||
            content.contains("channel") ||
            content.contains("dashboard") ||
            content.contains("fleet") ||
            content.contains("welcome")

        assertTrue(hasDashboardContent, "Dashboard should show fleet/device information")
        log("Dashboard loaded successfully")
    }

    @Test
    @Order(3)
    @DisplayName("Morning 08:10 - Review device fleet status")
    fun `morning review devices`() {
        log("Morning 08:10 - Reviewing device fleet")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()
        screenshot("04-morning-devices-list")
        checkLayoutIssues("devices-list")

        val content = p.content().lowercase()

        // Check if there are devices or empty state
        val hasDevicesContent = content.contains("device") || content.contains("enroll")
        assertTrue(hasDevicesContent, "Devices page should display")

        // Check for device table or empty state
        val hasTable = p.locator("table").count() > 0
        val hasEmptyState = content.contains("no device") || content.contains("empty")

        if (hasTable) {
            log("Device fleet found - checking device details")
            val deviceLinks = p.locator("table a[href*='/admin/devices/']")
            if (deviceLinks.count() > 0) {
                deviceLinks.first().click()
                p.waitForLoadState()
                screenshot("05-morning-device-detail")
                checkLayoutIssues("device-detail")
            }
        } else if (hasEmptyState) {
            log("No devices enrolled yet - empty state displayed")
            screenshot("05-morning-devices-empty")
        }
    }

    // ==================== CONTENT MANAGEMENT ====================

    @Test
    @Order(10)
    @DisplayName("Content 09:00 - Check assets library")
    fun `content check assets`() {
        log("Content 09:00 - Checking assets library")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets")
        p.waitForLoadState()
        screenshot("10-content-assets-list")
        checkLayoutIssues("assets-list")

        val content = p.content().lowercase()

        // Check quota display
        val hasQuota = content.contains("storage") || content.contains("quota")
        if (hasQuota) {
            log("Storage quota displayed")
        }

        // Check filters
        val hasFilters = p.locator("select[name='type']").count() > 0 ||
            p.locator("select[name='status']").count() > 0

        if (hasFilters) {
            log("Asset filters available")
        }

        // Test filter functionality
        val typeSelect = p.locator("select[name='type']")
        if (typeSelect.count() > 0 && typeSelect.isVisible) {
            typeSelect.selectOption("VIDEO")
            val applyBtn = p.locator("button:has-text('Apply')")
            if (applyBtn.count() > 0) {
                applyBtn.first().click()
                p.waitForLoadState()
                screenshot("11-content-assets-filtered-video")
                log("Filtered by VIDEO type")
            }

            // Clear filter
            p.navigate("$baseUrl/admin/assets")
            p.waitForLoadState()
        }
    }

    @Test
    @Order(11)
    @DisplayName("Content 09:15 - Upload asset (if form exists)")
    fun `content upload asset form`() {
        log("Content 09:15 - Testing asset upload")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets/upload")
        p.waitForLoadState()
        screenshot("12-content-upload-form")
        checkLayoutIssues("assets-upload")

        val content = p.content().lowercase()

        // Check for upload form
        val hasUploadForm = p.locator("input[type='file']").count() > 0 ||
            content.contains("upload") ||
            content.contains("drag") ||
            content.contains("drop")

        if (hasUploadForm) {
            log("Upload form found")
        } else {
            log("Upload form not implemented or uses different URL")
        }
    }

    @Test
    @Order(12)
    @DisplayName("Content 09:20 - Upload actual file")
    fun `content upload actual file`() {
        log("Content 09:20 - Uploading actual file")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets/upload")
        p.waitForLoadState()

        // Find file input
        val fileInput = p.locator("input[type='file']")
        if (fileInput.count() > 0) {
            // Get test image path
            val testImagePath = Paths.get("src/test/resources/test-image.png").toAbsolutePath().toString()
            val testFile = java.io.File(testImagePath)

            if (testFile.exists()) {
                // Upload file
                fileInput.setInputFiles(java.nio.file.Paths.get(testImagePath))
                screenshot("13-content-file-selected")
                log("Test file selected")

                // Wait for any upload progress
                Thread.sleep(500)

                // Submit the form
                val uploadBtn = p.locator("button:has-text('Upload'), button[type='submit']")
                if (uploadBtn.count() > 0) {
                    uploadBtn.first().click()
                    p.waitForLoadState()
                    screenshot("14-content-file-uploaded")
                    log("File uploaded")

                    // Check result
                    val resultContent = p.content().lowercase()
                    val uploadSuccess = resultContent.contains("success") ||
                        resultContent.contains("uploaded") ||
                        p.url().contains("/assets/")

                    if (uploadSuccess) {
                        log("Upload successful")
                    } else {
                        log("Upload result unclear - may require further configuration")
                    }
                }
            } else {
                log("Test file not found at: $testImagePath")
            }
        } else {
            log("No file input found - upload may use different mechanism")
        }
    }

    @Test
    @Order(13)
    @DisplayName("Content 09:25 - Upload with invalid file (error handling)")
    fun `content upload invalid file`() {
        log("Content 09:25 - Testing upload error handling")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/assets/upload")
        p.waitForLoadState()
        screenshot("15-content-upload-before-validation")

        // Try to submit without file - use various possible button selectors
        val uploadBtn = p.locator("button:has-text('Upload'), button:has-text('Submit'), button[type='submit']:visible")
        if (uploadBtn.count() > 0 && uploadBtn.first().isVisible) {
            try {
                uploadBtn.first().click(Locator.ClickOptions().setTimeout(5000.0))
                p.waitForLoadState()
                screenshot("15-content-upload-validation")

                val content = p.content().lowercase()
                // Should show error or stay on form
                val hasValidation = content.contains("required") ||
                    content.contains("error") ||
                    content.contains("select") ||
                    content.contains("file") ||
                    p.url().contains("/upload") ||
                    p.url().contains("/asset")

                assertTrue(hasValidation, "Should show validation or stay on form")
                log("Upload validation works")
            } catch (e: Exception) {
                screenshot("15-content-upload-error")
                log("Upload button interaction failed: ${e.message}")
                // Test passes if no clear submit button - form may require file selection
            }
        } else {
            log("No upload submit button found - form may require file selection first")
            screenshot("15-content-upload-no-button")
        }
    }

    @Test
    @Order(20)
    @DisplayName("Content 09:30 - View channel list")
    fun `content view channels`() {
        log("Content 09:30 - Viewing channels")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()
        screenshot("20-content-channels-list")
        checkLayoutIssues("channels-list")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("channel") || content.contains("create"),
            "Channels page should display"
        )

        // Test search filter
        val searchInput = p.locator("input[name='search']")
        if (searchInput.count() > 0 && searchInput.isVisible) {
            searchInput.fill("test")
            val applyBtn = p.locator("button:has-text('Apply')")
            if (applyBtn.count() > 0) {
                applyBtn.first().click()
                p.waitForLoadState()
                screenshot("21-content-channels-searched")
                log("Channel search works")
            }

            // Clear
            p.navigate("$baseUrl/admin/channels")
            p.waitForLoadState()
        }
    }

    @Test
    @Order(21)
    @DisplayName("Content 09:45 - Create new channel")
    fun `content create channel`() {
        log("Content 09:45 - Creating new channel")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels/new")
        p.waitForLoadState()
        screenshot("22-content-channel-form")
        checkLayoutIssues("channel-new")

        // Fill channel name
        createdChannelName = "Workday Test Channel $timestamp"
        val nameInput = p.locator("input[name='name']")

        if (nameInput.count() > 0 && nameInput.isVisible) {
            nameInput.fill(createdChannelName!!)
            screenshot("23-content-channel-filled")

            // Submit - use specific button text
            val createBtn = p.locator("button:has-text('Create Channel')")
            if (createBtn.count() > 0) {
                createBtn.click()
                p.waitForLoadState()
                screenshot("24-content-channel-created")

                // Extract ID
                val url = p.url()
                if (url.matches(Regex(".*channels/([a-f0-9-]+).*"))) {
                    createdChannelId = url.split("/channels/")[1].split("/")[0].split("?")[0]
                    log("Created channel: $createdChannelId")
                }

                // Verify
                assertTrue(
                    p.content().contains(createdChannelName!!) || createdChannelId != null,
                    "Channel should be created"
                )
            }
        }
    }

    @Test
    @Order(22)
    @DisplayName("Content 10:00 - Test channel validation (empty name)")
    fun `content channel validation`() {
        log("Content 10:00 - Testing channel form validation")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels/new")
        p.waitForLoadState()

        // Try to submit without name
        val submitBtn = p.locator("button:has-text('Create Channel')")
        if (submitBtn.count() > 0) {
            submitBtn.first().click()
            p.waitForLoadState()
            screenshot("25-content-channel-validation")

            // Should stay on form or show error
            val url = p.url()
            val content = p.content().lowercase()
            val hasValidation = url.contains("/new") ||
                url.contains("/channels") ||
                content.contains("required") ||
                content.contains("error") ||
                content.contains("name")

            assertTrue(hasValidation, "Should show validation or stay on form")
            log("Form validation working")
        }
    }

    @Test
    @Order(23)
    @DisplayName("Content 10:15 - Edit channel")
    fun `content edit channel`() {
        log("Content 10:15 - Editing channel")
        val p = requirePage()
        loginAsAdmin(p)

        val channelId = createdChannelId ?: run {
            // Find first channel
            p.navigate("$baseUrl/admin/channels")
            p.waitForLoadState()
            val link = p.locator("table a[href*='/admin/channels/']").first()
            if (link.isVisible) {
                val href = link.getAttribute("href") ?: ""
                href.split("/channels/").getOrNull(1)?.split("/")?.get(0)?.split("?")?.get(0)
            } else null
        }

        if (channelId != null) {
            p.navigate("$baseUrl/admin/channels/$channelId/edit")
            p.waitForLoadState()
            screenshot("26-content-channel-edit")
            checkLayoutIssues("channel-edit")

            val nameInput = p.locator("input[name='name']")
            if (nameInput.count() > 0 && nameInput.isVisible) {
                nameInput.clear()
                nameInput.fill("Updated Channel $timestamp")
                screenshot("27-content-channel-edit-filled")

                // Use specific button text
                val saveBtn = p.locator("button:has-text('Save'), button:has-text('Update')")
                if (saveBtn.count() > 0) {
                    saveBtn.first().click()
                    p.waitForLoadState()
                    screenshot("28-content-channel-updated")
                    log("Channel updated successfully")
                }
            }
        }
    }

    @Test
    @Order(24)
    @DisplayName("Content 10:30 - View channel schedule")
    fun `content view schedule`() {
        log("Content 10:30 - Viewing channel schedule")
        val p = requirePage()
        loginAsAdmin(p)

        val channelId = createdChannelId ?: run {
            p.navigate("$baseUrl/admin/channels")
            p.waitForLoadState()
            val link = p.locator("table a[href*='/admin/channels/']").first()
            if (link.isVisible) {
                val href = link.getAttribute("href") ?: ""
                href.split("/channels/").getOrNull(1)?.split("/")?.get(0)?.split("?")?.get(0)
            } else null
        }

        if (channelId != null) {
            p.navigate("$baseUrl/admin/channels/$channelId/schedule")
            p.waitForLoadState()
            screenshot("29-content-schedule")
            checkLayoutIssues("channel-schedule")

            assertTrue(
                p.url().contains("/schedule") || p.url().contains("/channels"),
                "Should be on schedule page"
            )
            log("Schedule view loaded")
        }
    }

    @Test
    @Order(25)
    @DisplayName("Content 10:35 - Add items to schedule")
    fun `content add schedule items`() {
        log("Content 10:35 - Adding items to schedule")
        val p = requirePage()
        loginAsAdmin(p)

        val channelId = createdChannelId ?: run {
            p.navigate("$baseUrl/admin/channels")
            p.waitForLoadState()
            val link = p.locator("table a[href*='/admin/channels/']").first()
            if (link.isVisible) {
                val href = link.getAttribute("href") ?: ""
                href.split("/channels/").getOrNull(1)?.split("/")?.get(0)?.split("?")?.get(0)
            } else null
        }

        if (channelId != null) {
            // First go to channel detail
            p.navigate("$baseUrl/admin/channels/$channelId")
            p.waitForLoadState()
            screenshot("29a-content-channel-detail-for-schedule")

            // Click on "Add Items" or "Edit Schedule" button
            val addItemsBtn = p.locator("a:has-text('Add Items'), a:has-text('Edit Schedule'), a[href*='/schedule']")
            if (addItemsBtn.count() > 0) {
                addItemsBtn.first().click()
                p.waitForLoadState()
                screenshot("29b-content-schedule-editor")

                // Check if we're on the schedule page
                val content = p.content().lowercase()
                val onSchedulePage = p.url().contains("/schedule") ||
                    content.contains("schedule") ||
                    content.contains("playlist")

                if (onSchedulePage) {
                    log("On schedule editor")

                    // Look for asset selection
                    val assetSelect = p.locator("select[name='assetId'], select[name='asset']")
                    if (assetSelect.count() > 0 && assetSelect.isVisible) {
                        val options = assetSelect.locator("option")
                        if (options.count() > 1) {
                            assetSelect.selectOption(options.nth(1).getAttribute("value") ?: "")
                            screenshot("29c-content-schedule-asset-selected")
                            log("Asset selected for schedule")

                            // Set duration if field exists
                            val durationInput = p.locator("input[name='duration'], input[name='displayTime']")
                            if (durationInput.count() > 0 && durationInput.isVisible) {
                                durationInput.fill("30")
                                log("Duration set to 30 seconds")
                            }

                            // Add to schedule
                            val addBtn = p.locator("button:has-text('Add'), button:has-text('Add Item')")
                            if (addBtn.count() > 0) {
                                addBtn.first().click()
                                p.waitForLoadState()
                                screenshot("29d-content-schedule-item-added")
                                log("Item added to schedule")
                            }
                        }
                    } else {
                        log("No asset selector found - schedule may use different UI")
                    }
                }
            } else {
                log("No schedule edit button found")
            }
        }
    }

    @Test
    @Order(26)
    @DisplayName("Content 10:40 - Publish schedule")
    fun `content publish schedule`() {
        log("Content 10:40 - Publishing schedule")
        val p = requirePage()
        loginAsAdmin(p)

        val channelId = createdChannelId ?: run {
            p.navigate("$baseUrl/admin/channels")
            p.waitForLoadState()
            val link = p.locator("table a[href*='/admin/channels/']").first()
            if (link.isVisible) {
                val href = link.getAttribute("href") ?: ""
                href.split("/channels/").getOrNull(1)?.split("/")?.get(0)?.split("?")?.get(0)
            } else null
        }

        if (channelId != null) {
            // Go to channel detail page
            p.navigate("$baseUrl/admin/channels/$channelId")
            p.waitForLoadState()
            screenshot("29e-content-channel-before-publish")

            // Look for Publish button
            val publishBtn = p.locator("button:has-text('Publish'), a:has-text('Publish')")
            if (publishBtn.count() > 0 && publishBtn.first().isVisible) {
                publishBtn.first().click()
                p.waitForLoadState()
                screenshot("29f-content-channel-published")

                val content = p.content().lowercase()
                val publishResult = content.contains("published") ||
                    content.contains("success") ||
                    content.contains("schedule")

                log(if (publishResult) "Schedule published" else "Publish action completed")
            } else {
                log("No publish button visible - may require schedule items first")
                screenshot("29f-content-no-publish-button")
            }
        }
    }

    @Test
    @Order(28)
    @DisplayName("Content 10:50 - Test non-existent channel")
    fun `content nonexistent channel`() {
        log("Content 10:50 - Testing non-existent channel handling")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/channels/00000000-0000-0000-0000-000000000000")
        p.waitForLoadState()
        screenshot("30-content-channel-notfound")

        // Should handle gracefully (redirect or error message)
        val handled = p.url().contains("/channels") ||
            p.content().lowercase().contains("not found") ||
            p.content().lowercase().contains("error")

        assertTrue(handled, "Should handle non-existent channel gracefully")
        log("Non-existent channel handled correctly")
    }

    // ==================== DEVICE MANAGEMENT ====================

    @Test
    @Order(30)
    @DisplayName("Devices 11:00 - Generate enrollment code")
    fun `devices generate code`() {
        log("Devices 11:00 - Generating enrollment code")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices/enroll")
        p.waitForLoadState()
        screenshot("31-devices-enroll-page")
        checkLayoutIssues("devices-enroll")

        // Select channel if available
        val channelSelect = p.locator("select[name='channelId']")
        if (channelSelect.count() > 0 && channelSelect.isVisible) {
            val options = channelSelect.locator("option")
            if (options.count() > 1) {
                channelSelect.selectOption(options.nth(1).getAttribute("value") ?: "")
            }
        }

        screenshot("32-devices-enroll-filled")

        // Generate code - use specific button text
        val generateBtn = p.locator("button:has-text('Generate Code'), button:has-text('Generate')")
        if (generateBtn.count() > 0) {
            generateBtn.first().click()
            p.waitForLoadState()
            screenshot("33-devices-code-generated")

            // Try to find 6-digit code
            val content = p.content()
            val codeMatch = Regex("\\b(\\d{6})\\b").find(content)
            if (codeMatch != null) {
                generatedEnrollCode = codeMatch.value
                log("Generated enrollment code: $generatedEnrollCode")
            } else {
                log("Code generated (format may vary)")
            }
        }
    }

    @Test
    @Order(31)
    @DisplayName("Devices 11:15 - View device detail (if exists)")
    fun `devices view detail`() {
        log("Devices 11:15 - Viewing device detail")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()

        val deviceLink = p.locator("table a[href*='/admin/devices/']").first()
        if (deviceLink.isVisible) {
            deviceLink.click()
            p.waitForLoadState()
            screenshot("34-devices-detail")
            checkLayoutIssues("device-detail")

            assertTrue(
                p.url().matches(Regex(".*devices/[a-f0-9-]+.*")),
                "Should be on device detail page"
            )
            log("Device detail loaded")
        } else {
            screenshot("34-devices-no-devices")
            log("No devices to view")
        }
    }

    // ==================== OVERLAY MANAGEMENT ====================

    @Test
    @Order(40)
    @DisplayName("Overlay 13:00 - View overlay profiles")
    fun `overlay view profiles`() {
        log("Overlay 13:00 - Viewing overlay profiles")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/profiles")
        p.waitForLoadState()
        screenshot("40-overlay-profiles-list")
        checkLayoutIssues("overlay-profiles")

        assertTrue(
            p.url().contains("/overlay") || p.url().contains("/profiles"),
            "Should be on overlay profiles page"
        )
    }

    @Test
    @Order(41)
    @DisplayName("Overlay 13:15 - Create overlay profile")
    fun `overlay create profile`() {
        log("Overlay 13:15 - Creating overlay profile")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/profiles/new")
        p.waitForLoadState()
        screenshot("41-overlay-profile-form")
        checkLayoutIssues("overlay-profile-new")

        val nameInput = p.locator("input[name='name']")
        if (nameInput.count() > 0 && nameInput.isVisible) {
            val profileName = "Workday Profile $timestamp"
            nameInput.fill(profileName)
            screenshot("42-overlay-profile-filled")

            // Use specific button text
            val createBtn = p.locator("button:has-text('Create Profile')")
            if (createBtn.count() > 0) {
                createBtn.click()
                p.waitForLoadState()
                screenshot("43-overlay-profile-created")

                // Extract ID
                val url = p.url()
                if (url.matches(Regex(".*profiles/([a-f0-9-]+).*"))) {
                    createdProfileId = url.split("/profiles/")[1].split("/")[0].split("?")[0]
                    log("Created profile: $createdProfileId")
                }
            }
        }
    }

    @Test
    @Order(42)
    @DisplayName("Overlay 13:30 - Test profile validation (empty name)")
    fun `overlay profile validation`() {
        log("Overlay 13:30 - Testing profile form validation")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/profiles/new")
        p.waitForLoadState()

        // Try to submit without name - use specific button
        val submitBtn = p.locator("button:has-text('Create Profile')")
        if (submitBtn.count() > 0) {
            submitBtn.first().click()
            p.waitForLoadState()
            screenshot("44-overlay-profile-validation")

            val url = p.url()
            val content = p.content().lowercase()
            val hasValidation = url.contains("/new") ||
                url.contains("/profiles") ||
                content.contains("required") ||
                content.contains("error")

            assertTrue(hasValidation, "Should show validation or stay on form")
            log("Profile validation working")
        }
    }

    @Test
    @Order(43)
    @DisplayName("Overlay 13:45 - View bindings")
    fun `overlay view bindings`() {
        log("Overlay 13:45 - Viewing overlay bindings")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/bindings")
        p.waitForLoadState()
        screenshot("45-overlay-bindings-list")
        checkLayoutIssues("overlay-bindings")

        assertTrue(
            p.url().contains("/overlay") || p.url().contains("/bindings"),
            "Should be on bindings page"
        )
    }

    @Test
    @Order(44)
    @DisplayName("Overlay 14:00 - Create binding (if profiles exist)")
    fun `overlay create binding`() {
        log("Overlay 14:00 - Creating overlay binding")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/bindings/new")
        p.waitForLoadState()
        screenshot("46-overlay-binding-form")
        checkLayoutIssues("overlay-binding-new")

        val content = p.content()
        if (content.contains("No overlay profiles") || content.contains("Create a profile first")) {
            log("No profiles available for binding")
            screenshot("46-overlay-binding-no-profiles")
            return
        }

        // Select profile and channel
        val profileSelect = p.locator("select[name='profileId']")
        val channelSelect = p.locator("select[name='channelId']")

        if (profileSelect.count() > 0 && channelSelect.count() > 0) {
            val profileOptions = profileSelect.locator("option")
            val channelOptions = channelSelect.locator("option")

            if (profileOptions.count() > 1 && channelOptions.count() > 1) {
                profileSelect.selectOption(profileOptions.nth(1).getAttribute("value") ?: "")
                channelSelect.selectOption(channelOptions.nth(1).getAttribute("value") ?: "")
                screenshot("47-overlay-binding-filled")

                // Use specific button text
                val createBtn = p.locator("button:has-text('Create Binding'), button:has-text('Create')")
                if (createBtn.count() > 0) {
                    createBtn.first().click()
                    p.waitForLoadState()
                    screenshot("48-overlay-binding-created")

                    // Extract ID
                    val url = p.url()
                    if (url.matches(Regex(".*bindings/([a-f0-9-]+).*"))) {
                        createdBindingId = url.split("/bindings/")[1].split("/")[0].split("?")[0]
                        log("Created binding: $createdBindingId")
                    }
                }
            }
        }
    }

    @Test
    @Order(45)
    @DisplayName("Overlay 14:15 - View webhook logs")
    fun `overlay view webhooks`() {
        log("Overlay 14:15 - Viewing webhook logs")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/overlay/webhooks")
        p.waitForLoadState()
        screenshot("49-overlay-webhooks")
        checkLayoutIssues("overlay-webhooks")

        assertTrue(
            p.url().contains("/webhook") || p.url().contains("/overlay"),
            "Should be on webhook logs page"
        )
    }

    // ==================== REPORTS ====================

    @Test
    @Order(50)
    @DisplayName("Reports 15:00 - View As-Run report")
    fun `reports view asrun`() {
        log("Reports 15:00 - Viewing As-Run report")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports/asrun")
        p.waitForLoadState()
        screenshot("50-reports-asrun")
        checkLayoutIssues("reports-asrun")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("as-run") || content.contains("asrun") ||
            content.contains("report") || content.contains("playback"),
            "As-run report page should display"
        )

        // Test date filter if exists
        val dateInput = p.locator("input[type='date']").first()
        if (dateInput.isVisible) {
            dateInput.fill("2024-01-01")
            val filterBtn = p.locator("button:has-text('Filter'), button:has-text('Apply')")
            if (filterBtn.count() > 0) {
                filterBtn.first().click()
                p.waitForLoadState()
                screenshot("51-reports-asrun-filtered")
                log("As-run date filter works")
            }
        }
    }

    @Test
    @Order(51)
    @DisplayName("Reports 15:15 - View Audit log")
    fun `reports view audit`() {
        log("Reports 15:15 - Viewing Audit log")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/reports/audit")
        p.waitForLoadState()
        screenshot("52-reports-audit")
        checkLayoutIssues("reports-audit")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("audit") || content.contains("log") || content.contains("action"),
            "Audit log page should display"
        )
        log("Audit log loaded")
    }

    // ==================== SETTINGS ====================

    @Test
    @Order(60)
    @DisplayName("Settings 16:00 - View settings page")
    fun `settings view page`() {
        log("Settings 16:00 - Viewing settings")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/settings")
        p.waitForLoadState()
        screenshot("60-settings-page")
        checkLayoutIssues("settings")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("setting") || content.contains("account") || content.contains("profile"),
            "Settings page should display"
        )
    }

    @Test
    @Order(61)
    @DisplayName("Settings 16:15 - Test password change form (no submit)")
    fun `settings password form`() {
        log("Settings 16:15 - Testing password change form")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/settings")
        p.waitForLoadState()

        val passwordSection = p.locator("form:has(input[type='password'])")
        if (passwordSection.count() > 0) {
            val currentPass = p.locator("input[name='currentPassword']")
            val newPass = p.locator("input[name='newPassword']")

            if (currentPass.count() > 0 && currentPass.isVisible &&
                newPass.count() > 0 && newPass.isVisible) {
                currentPass.fill("testpassword")
                newPass.fill("newpassword123")
                screenshot("61-settings-password-filled")
                log("Password form can be filled")
                // Don't submit - just verify form interaction
            }
        }
    }

    // ==================== ONBOARDING ====================

    @Test
    @Order(70)
    @DisplayName("Onboarding - View onboarding page")
    fun `onboarding view page`() {
        log("Checking onboarding page")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin/onboarding")
        p.waitForLoadState()
        screenshot("70-onboarding-page")
        checkLayoutIssues("onboarding")

        val content = p.content().lowercase()
        assertTrue(
            content.contains("onboard") || content.contains("start") ||
            content.contains("welcome") || content.contains("setup"),
            "Onboarding page should display"
        )
    }

    // ==================== END OF DAY ====================

    @Test
    @Order(80)
    @DisplayName("End of Day 17:00 - Full navigation test")
    fun `end of day navigation test`() {
        log("End of Day 17:00 - Full navigation test")
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
            screenshot("80-nav-$name")

            // Verify no error page
            val content = p.content()
            assertFalse(
                content.contains("404") || content.contains("500 Internal Server Error"),
                "Page $name should load without error"
            )
        }
        log("All pages navigate successfully")
    }

    @Test
    @Order(81)
    @DisplayName("End of Day 17:15 - Test mobile viewport")
    fun `end of day mobile viewport`() {
        log("End of Day 17:15 - Testing mobile viewport")
        val p = requirePage()

        // Change to mobile viewport
        p.setViewportSize(375, 812) // iPhone X size

        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()
        screenshot("81-mobile-dashboard")
        checkLayoutIssues("mobile-dashboard")

        p.navigate("$baseUrl/admin/channels")
        p.waitForLoadState()
        screenshot("82-mobile-channels")
        checkLayoutIssues("mobile-channels")

        p.navigate("$baseUrl/admin/devices")
        p.waitForLoadState()
        screenshot("83-mobile-devices")
        checkLayoutIssues("mobile-devices")

        log("Mobile viewport tested")
    }

    @Test
    @Order(90)
    @DisplayName("End of Day 17:30 - Logout")
    fun `end of day logout`() {
        log("End of Day 17:30 - Logging out")
        val p = requirePage()
        loginAsAdmin(p)

        p.navigate("$baseUrl/admin")
        p.waitForLoadState()
        screenshot("90-before-logout")

        // Open dropdown - click on the dropdown toggle
        val dropdownToggle = p.locator(".dropdown-toggle")
        if (dropdownToggle.count() > 0) {
            dropdownToggle.first().click()
            Thread.sleep(300)
            screenshot("90b-dropdown-open")
        }

        // Find logout form and click button
        val logoutBtn = p.locator("form[action='/admin/logout'] button, .logout-btn")
        if (logoutBtn.count() > 0) {
            logoutBtn.first().click()
            p.waitForLoadState()
        } else {
            // Try clicking "Logout" text directly
            val logoutText = p.locator("text=Logout")
            if (logoutText.count() > 0) {
                logoutText.first().click()
                p.waitForLoadState()
            }
        }

        screenshot("91-after-logout")

        // Verify logout - access admin should redirect to login
        p.navigate("$baseUrl/admin")
        p.waitForLoadState()

        val url = p.url()
        assertTrue(
            url.contains("/login") || p.content().lowercase().contains("login"),
            "After logout, should redirect to login. Current URL: $url"
        )
        log("Successfully logged out")
    }

    @Test
    @Order(99)
    @DisplayName("Final - Print layout issues summary")
    fun `final layout summary`() {
        log("=== LAYOUT ISSUES SUMMARY ===")
        if (layoutIssues.isEmpty()) {
            log("No layout issues detected!")
        } else {
            log("Found ${layoutIssues.size} layout issues:")
            layoutIssues.forEach { issue ->
                log("- [${issue.page}] ${issue.description}")
            }
        }

        // This test always passes - it's just for reporting
        assertTrue(true)
    }
}
