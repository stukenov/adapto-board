package com.playoutedge.server.ui

import com.microsoft.playwright.*
import com.microsoft.playwright.options.ScreenshotType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.nio.file.Paths

/**
 * UI tests for the public landing pages and signup flow.
 *
 * These tests require:
 * 1. Playwright browsers installed: npx playwright install chromium
 * 2. The server running at http://localhost:8080 (or TEST_BASE_URL env var)
 *
 * Run with: RUN_UI_TESTS=true ./gradlew :apps:server:uiTest --tests "*PublicPagesUITest"
 *
 * Screenshots are saved to: build/screenshots/public/
 */
@DisplayName("Public Pages & Signup UI Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Tag("ui")
class PublicPagesUITest {

    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private var context: BrowserContext? = null
    private var page: Page? = null
    private var playwrightAvailable = false

    private val baseUrl = System.getenv("TEST_BASE_URL") ?: "http://localhost:8080"
    private val screenshotDir = Paths.get("build/screenshots/public").toFile()

    // Track signup data for cleanup
    private val testEmail = "uitest-${System.currentTimeMillis()}@example.com"
    private val testOrgName = "UI Test Org ${System.currentTimeMillis()}"
    private val testPassword = "TestPass123"

    @BeforeAll
    fun setupAll() {
        val runUiTests = System.getenv("RUN_UI_TESTS")?.toBoolean()
            ?: System.getProperty("RUN_UI_TESTS")?.toBoolean()
            ?: false

        if (!runUiTests) {
            println("UI tests skipped. Set RUN_UI_TESTS=true to run them.")
            return
        }

        try {
            screenshotDir.mkdirs()
            playwright = Playwright.create()
            browser = playwright!!.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(true)
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
            Browser.NewContextOptions().setViewportSize(1920, 1080)
        )
        page = context!!.newPage()
    }

    @AfterEach
    fun teardown() {
        context?.close()
    }

    private fun screenshot(name: String): String {
        val path = Paths.get(screenshotDir.path, "$name.png")
        page?.screenshot(
            Page.ScreenshotOptions()
                .setPath(path)
                .setFullPage(true)
                .setType(ScreenshotType.PNG)
        )
        println("Screenshot saved: $path")
        return path.toString()
    }

    private fun requirePage(): Page = page ?: throw AssertionError("Page not initialized")

    // ==================== Landing Page ====================

    @Test
    @Order(1)
    @DisplayName("Landing page should load with hero section")
    fun `landing page should load`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()
        screenshot("01-landing-page")

        // Title
        val title = p.title()
        assertTrue(title.contains("Playout Edge"), "Title should contain 'Playout Edge', got: $title")

        // Hero section
        val hero = p.locator(".hero")
        assertTrue(hero.isVisible, "Hero section should be visible")

        val heroH1 = p.locator(".hero h1")
        assertTrue(heroH1.isVisible, "Hero heading should be visible")
        val heroText = heroH1.textContent()
        assertTrue(heroText.isNotBlank(), "Hero heading should have text")

        // CTA buttons
        val ctaButtons = p.locator(".hero-actions .btn")
        assertTrue(ctaButtons.count() >= 2, "Hero should have at least 2 CTA buttons")
    }

    @Test
    @Order(2)
    @DisplayName("Landing page navigation should have all links")
    fun `landing page navigation`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        // Public nav
        val nav = p.locator(".public-nav")
        assertTrue(nav.isVisible, "Public navigation should be visible")

        // Nav links
        val featuresLink = p.locator(".public-nav-links a[href='/features']")
        assertTrue(featuresLink.isVisible, "Features link should be visible")

        val pricingLink = p.locator(".public-nav-links a[href='/pricing']")
        assertTrue(pricingLink.isVisible, "Pricing link should be visible")

        val faqLink = p.locator(".public-nav-links a[href='/faq']")
        assertTrue(faqLink.isVisible, "FAQ link should be visible")

        val contactLink = p.locator(".public-nav-links a[href='/contact']")
        assertTrue(contactLink.isVisible, "Contact link should be visible")

        // Auth buttons
        val signInBtn = p.locator(".public-nav-actions a[href='/admin/login']")
        assertTrue(signInBtn.isVisible, "Sign In button should be visible")

        val signUpBtn = p.locator(".public-nav-actions a[href='/signup']")
        assertTrue(signUpBtn.isVisible, "Sign Up button should be visible")

        screenshot("02-landing-nav")
    }

    @Test
    @Order(3)
    @DisplayName("Landing page should have feature cards")
    fun `landing page feature cards`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        val featureCards = p.locator(".feature-card")
        assertTrue(featureCards.count() >= 3, "Should have at least 3 feature cards, got ${featureCards.count()}")

        // Each card should have icon, title, description
        for (i in 0 until featureCards.count()) {
            val card = featureCards.nth(i)
            assertTrue(card.locator("h3").isVisible, "Feature card $i should have a title")
            assertTrue(card.locator("p").isVisible, "Feature card $i should have a description")
        }

        screenshot("03-landing-features")
    }

    @Test
    @Order(4)
    @DisplayName("Landing page should have footer")
    fun `landing page footer`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        val footer = p.locator(".public-footer")
        assertTrue(footer.isVisible, "Footer should be visible")

        val footerSections = p.locator(".footer-section")
        assertTrue(footerSections.count() >= 3, "Footer should have at least 3 sections")

        screenshot("04-landing-footer")
    }

    // ==================== Features Page ====================

    @Test
    @Order(10)
    @DisplayName("Features page should load with 6 feature cards")
    fun `features page should load`() {
        val p = requirePage()
        p.navigate("$baseUrl/features")
        p.waitForLoadState()
        screenshot("10-features-page")

        val title = p.title()
        assertTrue(title.contains("Features"), "Title should contain 'Features', got: $title")

        val header = p.locator(".section-header h1")
        assertTrue(header.isVisible, "Section header should be visible")
        assertEquals("Features", header.textContent().trim())

        val featureCards = p.locator(".feature-card")
        assertEquals(6, featureCards.count(), "Should have 6 feature cards")
    }

    @Test
    @Order(11)
    @DisplayName("Navigate to Features from landing via nav link")
    fun `navigate to features from landing`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        p.click(".public-nav-links a[href='/features']")
        p.waitForLoadState()

        assertTrue(p.url().endsWith("/features"), "Should navigate to /features, got: ${p.url()}")
        screenshot("11-features-via-nav")
    }

    // ==================== Pricing Page ====================

    @Test
    @Order(20)
    @DisplayName("Pricing page should show 3 plans")
    fun `pricing page should load`() {
        val p = requirePage()
        p.navigate("$baseUrl/pricing")
        p.waitForLoadState()
        screenshot("20-pricing-page")

        val title = p.title()
        assertTrue(title.contains("Pricing"), "Title should contain 'Pricing'")

        val pricingCards = p.locator(".pricing-card")
        assertEquals(3, pricingCards.count(), "Should have 3 pricing cards")

        // Check highlighted card (Premium)
        val highlighted = p.locator(".pricing-card-highlighted")
        assertEquals(1, highlighted.count(), "Should have 1 highlighted pricing card")

        // Check badge
        val badge = p.locator(".pricing-badge")
        assertTrue(badge.isVisible, "Popular badge should be visible")
        assertTrue(badge.textContent().contains("Popular", ignoreCase = true), "Badge should say 'Most Popular'")
    }

    @Test
    @Order(21)
    @DisplayName("Pricing cards should have correct structure")
    fun `pricing cards structure`() {
        val p = requirePage()
        p.navigate("$baseUrl/pricing")
        p.waitForLoadState()

        val cards = p.locator(".pricing-card")
        for (i in 0 until cards.count()) {
            val card = cards.nth(i)
            assertTrue(card.locator(".pricing-name").isVisible, "Card $i should have a plan name")
            assertTrue(card.locator(".pricing-amount").isVisible, "Card $i should have a price")
            assertTrue(card.locator(".pricing-features").isVisible, "Card $i should have features list")
            assertTrue(card.locator(".btn").isVisible, "Card $i should have a CTA button")
        }

        screenshot("21-pricing-structure")
    }

    @Test
    @Order(22)
    @DisplayName("Navigate to Pricing from landing via nav link")
    fun `navigate to pricing from landing`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        p.click(".public-nav-links a[href='/pricing']")
        p.waitForLoadState()

        assertTrue(p.url().endsWith("/pricing"), "Should navigate to /pricing")
        screenshot("22-pricing-via-nav")
    }

    // ==================== FAQ Page ====================

    @Test
    @Order(30)
    @DisplayName("FAQ page should show accordion items")
    fun `faq page should load`() {
        val p = requirePage()
        p.navigate("$baseUrl/faq")
        p.waitForLoadState()
        screenshot("30-faq-page")

        val title = p.title()
        assertTrue(title.contains("FAQ"), "Title should contain 'FAQ'")

        val faqItems = p.locator(".faq-item")
        assertTrue(faqItems.count() >= 5, "Should have at least 5 FAQ items, got ${faqItems.count()}")
    }

    @Test
    @Order(31)
    @DisplayName("FAQ accordion should expand/collapse on click")
    fun `faq accordion interaction`() {
        val p = requirePage()
        p.navigate("$baseUrl/faq")
        p.waitForLoadState()

        val firstItem = p.locator(".faq-item").first()
        val summary = firstItem.locator("summary")
        val answer = firstItem.locator("p")

        // Initially collapsed — <details> hides content
        screenshot("31-faq-collapsed")

        // Click to expand
        summary.click()
        Thread.sleep(300)
        screenshot("32-faq-expanded")

        // Answer should be visible after click
        assertTrue(answer.isVisible, "FAQ answer should be visible after expanding")

        // Click again to collapse
        summary.click()
        Thread.sleep(300)
        screenshot("33-faq-collapsed-again")
    }

    // ==================== Contact Page ====================

    @Test
    @Order(40)
    @DisplayName("Contact page should show 3 contact cards")
    fun `contact page should load`() {
        val p = requirePage()
        p.navigate("$baseUrl/contact")
        p.waitForLoadState()
        screenshot("40-contact-page")

        val title = p.title()
        assertTrue(title.contains("Contact"), "Title should contain 'Contact'")

        val contactCards = p.locator(".contact-card")
        assertEquals(3, contactCards.count(), "Should have 3 contact cards")

        // Each card should have email link
        for (i in 0 until contactCards.count()) {
            val card = contactCards.nth(i)
            assertTrue(card.locator("h3").isVisible, "Contact card $i should have a heading")
            val emailLink = card.locator("a[href^='mailto:']")
            assertTrue(emailLink.count() > 0, "Contact card $i should have an email link")
        }
    }

    // ==================== Signup Page ====================

    @Test
    @Order(50)
    @DisplayName("Signup page should display registration form")
    fun `signup page should load`() {
        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()
        screenshot("50-signup-page")

        val title = p.title()
        assertTrue(title.contains("Sign Up"), "Title should contain 'Sign Up', got: $title")

        // Form fields
        assertTrue(p.locator("input[name='orgName']").isVisible, "Org name input should be visible")
        assertTrue(p.locator("input[name='email']").isVisible, "Email input should be visible")
        assertTrue(p.locator("input[name='displayName']").isVisible, "Display name input should be visible")
        assertTrue(p.locator("input[name='password']").isVisible, "Password input should be visible")
        assertTrue(p.locator("input[name='confirmPassword']").isVisible, "Confirm password input should be visible")
        assertTrue(p.locator("button[type='submit']").isVisible, "Submit button should be visible")

        // Sign in link
        val signInLink = p.locator("a[href='/admin/login']")
        assertTrue(signInLink.count() > 0, "Should have a link to sign in")
    }

    @Test
    @Order(51)
    @DisplayName("Navigate to Signup from landing CTA")
    fun `navigate to signup from landing cta`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        // Click the "Get Started Free" or "Sign Up" CTA in hero
        val ctaBtn = p.locator(".hero-actions a[href='/signup']")
        assertTrue(ctaBtn.isVisible, "Hero CTA to signup should be visible")
        ctaBtn.click()
        p.waitForLoadState()

        assertTrue(p.url().contains("/signup"), "Should navigate to /signup, got: ${p.url()}")
        screenshot("51-signup-via-cta")
    }

    @Test
    @Order(52)
    @DisplayName("Navigate to Signup from nav button")
    fun `navigate to signup from nav`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        p.click(".public-nav-actions a[href='/signup']")
        p.waitForLoadState()

        assertTrue(p.url().contains("/signup"), "Should navigate to /signup")
        screenshot("52-signup-via-nav")
    }

    @Test
    @Order(53)
    @DisplayName("Signup with mismatched passwords should show error")
    fun `signup password mismatch`() {
        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()

        p.fill("input[name='orgName']", "Test Org")
        p.fill("input[name='email']", "mismatch@example.com")
        p.fill("input[name='displayName']", "Test User")
        p.fill("input[name='password']", "TestPass123")
        p.fill("input[name='confirmPassword']", "DifferentPass456")

        screenshot("53-signup-filled-mismatch")

        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("54-signup-mismatch-error")

        // Should redirect back with error and preserved fields
        assertTrue(p.url().contains("error="), "URL should contain error parameter")
        assertTrue(p.url().contains("Passwords"), "Error should mention passwords")
    }

    @Test
    @Order(54)
    @DisplayName("Signup with short org name should show error")
    fun `signup short org name`() {
        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()

        // Remove HTML5 validation to test server-side validation
        p.evaluate("document.querySelector('form').setAttribute('novalidate', '')")
        p.evaluate("document.querySelector('input[name=orgName]').removeAttribute('minLength')")

        p.fill("input[name='orgName']", "AB")
        p.fill("input[name='email']", "short-org@example.com")
        p.fill("input[name='displayName']", "Test User")
        p.fill("input[name='password']", "TestPass123")
        p.fill("input[name='confirmPassword']", "TestPass123")

        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("55-signup-short-org-error")

        assertTrue(p.url().contains("error="), "Should show error for short org name")
    }

    @Test
    @Order(55)
    @DisplayName("Signup with weak password should show error")
    fun `signup weak password`() {
        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()

        // Remove HTML5 validation to test server-side validation
        p.evaluate("document.querySelector('form').setAttribute('novalidate', '')")
        p.evaluate("document.querySelector('input[name=password]').removeAttribute('minLength')")
        p.evaluate("document.querySelector('input[name=confirmPassword]').removeAttribute('minLength')")

        p.fill("input[name='orgName']", "Test Org Valid")
        p.fill("input[name='email']", "weak-pass@example.com")
        p.fill("input[name='displayName']", "Test User")
        p.fill("input[name='password']", "nope")
        p.fill("input[name='confirmPassword']", "nope")

        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("56-signup-weak-password-error")

        assertTrue(p.url().contains("error="), "Should show error for weak password")
    }

    @Test
    @Order(60)
    @DisplayName("Successful signup should redirect to onboarding")
    fun `signup success flow`() {
        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()

        p.fill("input[name='orgName']", testOrgName)
        p.fill("input[name='email']", testEmail)
        p.fill("input[name='displayName']", "UI Test User")
        p.fill("input[name='password']", testPassword)
        p.fill("input[name='confirmPassword']", testPassword)

        screenshot("60-signup-filled-valid")

        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("61-signup-success-redirect")

        // Should redirect to admin onboarding
        val url = p.url()
        assertTrue(
            url.contains("/admin/onboarding") || url.contains("/admin"),
            "Should redirect to admin area after signup. Got: $url"
        )

        // Should have admin_session cookie set
        val cookies = context!!.cookies()
        val sessionCookie = cookies.find { it.name == "admin_session" }
        assertNotNull(sessionCookie, "admin_session cookie should be set after signup")
    }

    @Test
    @Order(61)
    @DisplayName("Duplicate email signup should show error")
    fun `signup duplicate email`() {
        val p = requirePage()
        // Clear cookies to simulate fresh session
        context!!.clearCookies()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()

        // Try to register with the same email again
        p.fill("input[name='orgName']", "Another Org")
        p.fill("input[name='email']", testEmail)
        p.fill("input[name='displayName']", "Duplicate User")
        p.fill("input[name='password']", testPassword)
        p.fill("input[name='confirmPassword']", testPassword)

        p.click("button[type='submit']")
        p.waitForLoadState()

        screenshot("62-signup-duplicate-email")

        assertTrue(p.url().contains("error="), "Should show error for duplicate email")
        assertTrue(
            p.url().contains("already") || p.url().contains("exists"),
            "Error should mention email already exists"
        )
    }

    // ==================== Cross-page Navigation ====================

    @Test
    @Order(70)
    @DisplayName("Full navigation flow: Landing -> Features -> Pricing -> FAQ -> Contact -> Signup")
    fun `full navigation flow`() {
        val p = requirePage()

        // Landing
        p.navigate(baseUrl)
        p.waitForLoadState()
        assertTrue(p.locator(".hero").isVisible, "Landing hero should be visible")
        screenshot("70-nav-landing")

        // Features
        p.click(".public-nav-links a[href='/features']")
        p.waitForLoadState()
        assertTrue(p.url().endsWith("/features"), "Should be on /features")
        assertTrue(p.locator(".feature-card").count() == 6, "Should have 6 feature cards")
        screenshot("71-nav-features")

        // Pricing
        p.click(".public-nav-links a[href='/pricing']")
        p.waitForLoadState()
        assertTrue(p.url().endsWith("/pricing"), "Should be on /pricing")
        assertTrue(p.locator(".pricing-card").count() == 3, "Should have 3 pricing cards")
        screenshot("72-nav-pricing")

        // FAQ
        p.click(".public-nav-links a[href='/faq']")
        p.waitForLoadState()
        assertTrue(p.url().endsWith("/faq"), "Should be on /faq")
        assertTrue(p.locator(".faq-item").count() >= 5, "Should have FAQ items")
        screenshot("73-nav-faq")

        // Contact
        p.click(".public-nav-links a[href='/contact']")
        p.waitForLoadState()
        assertTrue(p.url().endsWith("/contact"), "Should be on /contact")
        assertTrue(p.locator(".contact-card").count() == 3, "Should have 3 contact cards")
        screenshot("74-nav-contact")

        // Signup
        p.click(".public-nav-actions a[href='/signup']")
        p.waitForLoadState()
        assertTrue(p.url().contains("/signup"), "Should be on /signup")
        assertTrue(p.locator("input[name='orgName']").isVisible, "Signup form should be visible")
        screenshot("75-nav-signup")
    }

    @Test
    @Order(71)
    @DisplayName("Sign In link navigates to admin login")
    fun `sign in link navigates to login`() {
        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()

        p.click(".public-nav-actions a[href='/admin/login']")
        p.waitForLoadState()

        assertTrue(p.url().contains("/admin/login"), "Should navigate to /admin/login")
        assertTrue(p.locator("input[name='email']").isVisible, "Login form should be visible")
        screenshot("76-sign-in-link")
    }

    @Test
    @Order(72)
    @DisplayName("Pricing Enterprise CTA links to contact page")
    fun `pricing enterprise links to contact`() {
        val p = requirePage()
        p.navigate("$baseUrl/pricing")
        p.waitForLoadState()

        // Find the Enterprise card's CTA (last card)
        val enterpriseCta = p.locator(".pricing-card:last-child .btn")
        assertTrue(enterpriseCta.isVisible, "Enterprise CTA should be visible")
        enterpriseCta.click()
        p.waitForLoadState()

        assertTrue(p.url().endsWith("/contact"), "Enterprise CTA should go to /contact")
        screenshot("77-enterprise-to-contact")
    }

    @Test
    @Order(73)
    @DisplayName("Pricing Basic/Premium CTAs link to signup page")
    fun `pricing plans link to signup`() {
        val p = requirePage()
        p.navigate("$baseUrl/pricing")
        p.waitForLoadState()

        // First card (Basic) CTA
        val basicCta = p.locator(".pricing-card:first-child .btn")
        assertTrue(basicCta.isVisible, "Basic CTA should be visible")
        basicCta.click()
        p.waitForLoadState()

        assertTrue(p.url().contains("/signup"), "Basic CTA should go to /signup")
        screenshot("78-basic-to-signup")
    }

    // ==================== Mobile Viewport ====================

    @Test
    @Order(80)
    @DisplayName("Landing page should be responsive on mobile")
    fun `landing page mobile responsive`() {
        context?.close()
        context = browser!!.newContext(
            Browser.NewContextOptions().setViewportSize(375, 812)
        )
        page = context!!.newPage()

        val p = requirePage()
        p.navigate(baseUrl)
        p.waitForLoadState()
        screenshot("80-mobile-landing")

        // Hero should still be visible
        assertTrue(p.locator(".hero").isVisible, "Hero should be visible on mobile")

        // Feature cards should stack
        val featureCards = p.locator(".feature-card")
        assertTrue(featureCards.count() >= 3, "Feature cards should still exist on mobile")
    }

    @Test
    @Order(81)
    @DisplayName("Signup page should work on mobile")
    fun `signup page mobile`() {
        context?.close()
        context = browser!!.newContext(
            Browser.NewContextOptions().setViewportSize(375, 812)
        )
        page = context!!.newPage()

        val p = requirePage()
        p.navigate("$baseUrl/signup")
        p.waitForLoadState()
        screenshot("81-mobile-signup")

        // All form fields should be visible
        assertTrue(p.locator("input[name='orgName']").isVisible, "Org name should be visible on mobile")
        assertTrue(p.locator("input[name='email']").isVisible, "Email should be visible on mobile")
        assertTrue(p.locator("button[type='submit']").isVisible, "Submit should be visible on mobile")
    }
}
