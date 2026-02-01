package com.playoutedge.server.routes.admin

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.CreateChannelRequest
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.onboarding.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// In-memory wizard state (would be persisted in production)
private val wizardStates = ConcurrentHashMap<UUID, WizardState>()

/**
 * Admin onboarding wizard routes.
 */
fun Route.adminOnboardingRoutes(
    assetRepository: AssetRepository,
    channelRepository: ChannelRepository,
    deviceRepository: DeviceRepository
) {
    route("/admin/onboarding") {
        // GET /admin/onboarding - Wizard main view
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = session.tenantId

            // Get or create wizard state
            val state = wizardStates.getOrPut(tenantId) {
                WizardState(
                    currentStep = WizardStep.TENANT_SETTINGS,
                    completedSteps = emptySet(),
                    skippedSteps = emptySet()
                )
            }

            // Check if step override requested
            val stepParam = call.request.queryParameters["step"]
            val requestedStep = stepParam?.let { runCatching { WizardStep.valueOf(it) }.getOrNull() }

            val effectiveState = if (requestedStep != null) {
                state.copy(currentStep = requestedStep)
            } else {
                state
            }

            call.respondHtml {
                onboardingWizardView(session, effectiveState)
            }
        }

        // POST /admin/onboarding/step/tenant-settings
        post("/step/tenant-settings") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = session.tenantId
            val params = call.receiveParameters()

            // Would save settings to tenant
            // val name = params["name"]
            // val timezone = params["timezone"]
            // val offlineThreshold = params["offlineThreshold"]?.toIntOrNull() ?: 5

            // Update wizard state
            val state = wizardStates.getOrPut(tenantId) {
                WizardState(WizardStep.TENANT_SETTINGS, emptySet(), emptySet())
            }
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.TENANT_SETTINGS,
                currentStep = WizardStep.CONTENT_POLICIES
            )
            wizardStates[tenantId] = newState

            call.respondRedirect("/admin/onboarding")
        }

        // POST /admin/onboarding/step/content-policies
        post("/step/content-policies") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = session.tenantId
            val params = call.receiveParameters()

            // Would save content policies
            // val codecs = params.getAll("codecs") ?: listOf("H.264")
            // val containers = params.getAll("containers") ?: listOf("MP4")
            // val maxBitrate = params["maxBitrate"]?.toIntOrNull() ?: 50
            // val maxResolution = params["maxResolution"] ?: "4K"
            // val maxFileSize = params["maxFileSize"]?.toIntOrNull() ?: 500

            // Update wizard state
            val state = wizardStates[tenantId] ?: WizardState(WizardStep.CONTENT_POLICIES, emptySet(), emptySet())
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.CONTENT_POLICIES,
                currentStep = WizardStep.FIRST_ASSET
            )
            wizardStates[tenantId] = newState

            call.respondRedirect("/admin/onboarding")
        }

        // POST /admin/onboarding/step/first-asset
        post("/step/first-asset") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = session.tenantId
            val params = call.receiveParameters()

            // In production, would handle file upload or sample creation
            val sample = params["sample"]
            val assetId = if (sample != null && sample.isNotBlank()) {
                // Would create sample asset
                UUID.randomUUID() // Placeholder
            } else {
                // Would handle file upload
                UUID.randomUUID() // Placeholder
            }

            // Update wizard state
            val state = wizardStates[tenantId] ?: WizardState(WizardStep.FIRST_ASSET, emptySet(), emptySet())
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.FIRST_ASSET,
                currentStep = WizardStep.FIRST_CHANNEL,
                createdAssetId = assetId
            )
            wizardStates[tenantId] = newState

            call.respondRedirect("/admin/onboarding")
        }

        // POST /admin/onboarding/step/first-channel
        post("/step/first-channel") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()

            val name = params["name"] ?: "My First Channel"
            val addAsset = params["addAsset"] != null

            // Create channel
            val channel = channelRepository.create(tenantId, CreateChannelRequest(name = name))
            val channelId = channel.id.value

            // Would add asset to schedule if requested
            val state = wizardStates[session.tenantId] ?: WizardState(WizardStep.FIRST_CHANNEL, emptySet(), emptySet())
            if (addAsset && state.createdAssetId != null) {
                // Would create schedule with asset
            }

            // Update wizard state
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.FIRST_CHANNEL,
                currentStep = WizardStep.FIRST_DEVICE,
                createdChannelId = channelId
            )
            wizardStates[session.tenantId] = newState

            call.respondRedirect("/admin/onboarding")
        }

        // POST /admin/onboarding/step/first-device
        post("/step/first-device") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()
            val action = params["action"] ?: "generate"

            val state = wizardStates[session.tenantId] ?: WizardState(WizardStep.FIRST_DEVICE, emptySet(), emptySet())

            when (action) {
                "generate", "regenerate" -> {
                    // Generate new enroll code
                    val enrollCode = generateEnrollCode()

                    // Would save to device repository
                    // deviceRepository.createEnrollCode(tenantId, enrollCode, ...)

                    val newState = state.copy(
                        enrollCode = enrollCode
                    )
                    wizardStates[session.tenantId] = newState
                }

                "check" -> {
                    // Check if device enrolled
                    val devices = deviceRepository.findAll(tenantId)
                    if (devices.isNotEmpty()) {
                        // Device found, mark complete
                        val newState = state.copy(
                            completedSteps = state.completedSteps + WizardStep.FIRST_DEVICE,
                            currentStep = WizardStep.VERIFY
                        )
                        wizardStates[session.tenantId] = newState
                    }
                }
            }

            call.respondRedirect("/admin/onboarding")
        }

        // Skip routes
        get("/skip/{step}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = session.tenantId
            val stepName = call.parameters["step"]
            val step = stepName?.let { runCatching { WizardStep.valueOf(it.uppercase().replace("-", "_")) }.getOrNull() }

            if (step != null) {
                val state = wizardStates[tenantId] ?: WizardState(step, emptySet(), emptySet())
                val nextStep = WizardStep.entries
                    .filter { it.order > step.order }
                    .minByOrNull { it.order }
                    ?: WizardStep.VERIFY

                val newState = state.copy(
                    skippedSteps = state.skippedSteps + step,
                    currentStep = nextStep
                )
                wizardStates[tenantId] = newState
            }

            call.respondRedirect("/admin/onboarding")
        }

        // GET /admin/onboarding/complete - Celebration view
        get("/complete") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            call.respondHtml {
                onboardingCompleteView(session)
            }
        }

        // POST /admin/onboarding/seed - Seed sample data
        post("/seed") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)

            // Would create sample data:
            // - Sample assets (demo video, demo image)
            // - Sample channel with schedule
            // - Sample overlay profile

            call.respondRedirect("/admin/onboarding/complete")
        }
    }
}

private fun generateEnrollCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}
