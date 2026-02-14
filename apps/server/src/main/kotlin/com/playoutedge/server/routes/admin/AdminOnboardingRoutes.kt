package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.EnrollCodeStatus
import com.playoutedge.domain.enums.ScheduleState
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.EnrollCodeEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.*
import com.playoutedge.persistence.repositories.OnboardingStateData
import com.playoutedge.persistence.repositories.OnboardingStateRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.onboarding.*
import com.playoutedge.storage.AssetUploadService
import com.playoutedge.storage.UploadRequest
import com.playoutedge.storage.ValidationResult
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Helper to load WizardState from repository.
 */
private suspend fun loadWizardState(repo: OnboardingStateRepository, tenantId: UUID): WizardState {
    val data = repo.findByTenantId(tenantId) ?: return WizardState(
        currentStep = WizardStep.TENANT_SETTINGS,
        completedSteps = emptySet(),
        skippedSteps = emptySet()
    )
    return WizardState(
        currentStep = runCatching { WizardStep.valueOf(data.currentStep) }.getOrDefault(WizardStep.TENANT_SETTINGS),
        completedSteps = data.completedSteps.mapNotNull { runCatching { WizardStep.valueOf(it) }.getOrNull() }.toSet(),
        skippedSteps = data.skippedSteps.mapNotNull { runCatching { WizardStep.valueOf(it) }.getOrNull() }.toSet(),
        createdAssetId = data.createdAssetId,
        createdChannelId = data.createdChannelId,
        enrollCode = data.enrollCode
    )
}

/**
 * Helper to save WizardState to repository.
 */
private suspend fun saveWizardState(repo: OnboardingStateRepository, tenantId: UUID, state: WizardState) {
    repo.save(tenantId, OnboardingStateData(
        currentStep = state.currentStep.name,
        completedSteps = state.completedSteps.map { it.name }.toSet(),
        skippedSteps = state.skippedSteps.map { it.name }.toSet(),
        createdAssetId = state.createdAssetId,
        createdChannelId = state.createdChannelId,
        enrollCode = state.enrollCode
    ))
}

/**
 * Admin onboarding wizard routes.
 */
fun Route.adminOnboardingRoutes(
    assetRepository: AssetRepository,
    channelRepository: ChannelRepository,
    deviceRepository: DeviceRepository,
    tenantRepository: TenantRepository,
    assetUploadService: AssetUploadService,
    scheduleRepository: ScheduleRepository,
    onboardingStateRepository: OnboardingStateRepository
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
            val state = loadWizardState(onboardingStateRepository, tenantId)

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

            // Save tenant settings
            val name = params["name"]?.takeIf { it.isNotBlank() }
            if (name != null) {
                val tenant = tenantRepository.findById(tenantId)
                if (tenant != null) {
                    newSuspendedTransaction {
                        tenant.name = name
                    }
                }
            }

            // Update wizard state
            val state = loadWizardState(onboardingStateRepository, tenantId)
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.TENANT_SETTINGS,
                currentStep = WizardStep.CONTENT_POLICIES
            )
            saveWizardState(onboardingStateRepository, tenantId, newState)

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

            // Save content policies to onboarding state
            val codecs = params.getAll("codecs") ?: listOf("H.264")
            val containers = params.getAll("containers") ?: listOf("MP4")
            val maxBitrate = params["maxBitrate"]?.toIntOrNull() ?: 50
            val maxResolution = params["maxResolution"] ?: "4K"
            val maxFileSize = params["maxFileSize"]?.toIntOrNull() ?: 500
            val policiesJson = """{"codecs":${codecs.joinToString(",","[","]") { "\"$it\"" }},"containers":${containers.joinToString(",","[","]") { "\"$it\"" }},"maxBitrate":$maxBitrate,"maxResolution":"$maxResolution","maxFileSize":$maxFileSize}"""

            // Update wizard state with content policies
            val state = loadWizardState(onboardingStateRepository, tenantId)
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.CONTENT_POLICIES,
                currentStep = WizardStep.FIRST_ASSET
            )
            onboardingStateRepository.save(tenantId, OnboardingStateData(
                currentStep = newState.currentStep.name,
                completedSteps = newState.completedSteps.map { s -> s.name }.toSet(),
                skippedSteps = newState.skippedSteps.map { s -> s.name }.toSet(),
                createdAssetId = newState.createdAssetId,
                createdChannelId = newState.createdChannelId,
                enrollCode = newState.enrollCode,
                contentPolicies = policiesJson
            ))

            call.respondRedirect("/admin/onboarding")
        }

        // POST /admin/onboarding/step/first-asset
        post("/step/first-asset") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)

            // Handle multipart file upload
            val multipart = call.receiveMultipart()
            var filename: String? = null
            var mimeType: String? = null
            var tempFile: File? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        filename = part.originalFileName?.takeIf { it.isNotBlank() }
                        mimeType = part.contentType?.toString() ?: "application/octet-stream"
                        tempFile = File.createTempFile("onboarding-", ".tmp").also { tf ->
                            part.streamProvider().use { input ->
                                tf.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val assetId = try {
                if (filename != null && tempFile != null && tempFile!!.length() > 0) {
                    val id = UUID.randomUUID()
                    val uploadRequest = UploadRequest(
                        tenantId = tenantId.value,
                        assetId = id,
                        filename = filename!!,
                        mimeType = mimeType!!,
                        contentLength = tempFile!!.length()
                    )
                    val validation = assetUploadService.validateUpload(uploadRequest)
                    if (validation is ValidationResult.Valid) {
                        val uploadResult = assetUploadService.upload(uploadRequest, tempFile!!.inputStream())
                        if (uploadResult.validationError == null) {
                            val asset = assetRepository.create(tenantId, CreateAssetRequest(
                                type = validation.assetType,
                                name = filename!!,
                                mimeType = mimeType!!,
                                storageKey = uploadResult.storageKey,
                                fileSizeBytes = uploadResult.sizeBytes,
                                checksumSha256 = uploadResult.checksumSha256,
                                createdBy = session.subject
                            ))
                            assetRepository.update(tenantId, asset.id.value, UpdateAssetRequest(status = AssetStatus.READY))
                            asset.id.value
                        } else id
                    } else id
                } else {
                    UUID.randomUUID()
                }
            } finally {
                tempFile?.delete()
            }

            // Update wizard state
            val state = loadWizardState(onboardingStateRepository, session.tenantId)
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.FIRST_ASSET,
                currentStep = WizardStep.FIRST_CHANNEL,
                createdAssetId = assetId
            )
            saveWizardState(onboardingStateRepository, session.tenantId, newState)

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

            // Add asset to schedule if requested
            val state = loadWizardState(onboardingStateRepository, session.tenantId)
            if (addAsset && state.createdAssetId != null) {
                val version = scheduleRepository.createVersion(tenantId, channelId, session.subject)
                scheduleRepository.addItem(tenantId, version.id.value, CreateScheduleItemRequest(
                    assetId = state.createdAssetId,
                    orderIndex = 0
                ))
                scheduleRepository.updateVersionState(tenantId, version.id.value, ScheduleState.PUBLISHED)
            }

            // Update wizard state
            val newState = state.copy(
                completedSteps = state.completedSteps + WizardStep.FIRST_CHANNEL,
                currentStep = WizardStep.FIRST_DEVICE,
                createdChannelId = channelId
            )
            saveWizardState(onboardingStateRepository, session.tenantId, newState)

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

            val state = loadWizardState(onboardingStateRepository, session.tenantId)

            when (action) {
                "generate", "regenerate" -> {
                    // Generate new enroll code and persist
                    val enrollCode = generateEnrollCode()
                    val expiresAt = Clock.System.now().plus(24.hours)

                    newSuspendedTransaction {
                        EnrollCodeEntity.new {
                            this.tenant = TenantEntity[session.tenantId]
                            this.code = enrollCode
                            this.status = EnrollCodeStatus.PENDING
                            this.expiresAt = expiresAt
                            this.createdBy = UserEntity[session.subject]
                            this.createdAt = Clock.System.now()
                            this.channel = state.createdChannelId?.let {
                                com.playoutedge.persistence.entities.ChannelEntity.findById(it)
                            }
                        }
                    }

                    val newState = state.copy(
                        enrollCode = enrollCode
                    )
                    saveWizardState(onboardingStateRepository, session.tenantId, newState)
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
                        saveWizardState(onboardingStateRepository, session.tenantId, newState)
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
                val state = loadWizardState(onboardingStateRepository, tenantId)
                val nextStep = WizardStep.entries
                    .filter { it.order > step.order }
                    .minByOrNull { it.order }
                    ?: WizardStep.VERIFY

                val newState = state.copy(
                    skippedSteps = state.skippedSteps + step,
                    currentStep = nextStep
                )
                saveWizardState(onboardingStateRepository, tenantId, newState)
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

            // Create demo channel with schedule
            val demoChannel = channelRepository.create(tenantId, CreateChannelRequest(name = "Demo Channel"))
            val version = scheduleRepository.createVersion(tenantId, demoChannel.id.value, session.subject)

            // If there are existing assets, add the first one to the schedule
            val existingAssets = assetRepository.findAllActive(tenantId)
            if (existingAssets.isNotEmpty()) {
                scheduleRepository.addItem(tenantId, version.id.value, CreateScheduleItemRequest(
                    assetId = existingAssets.first().id.value,
                    orderIndex = 0
                ))
                scheduleRepository.updateVersionState(tenantId, version.id.value, ScheduleState.PUBLISHED)
            }

            call.respondRedirect("/admin/onboarding/complete")
        }
    }
}

private fun generateEnrollCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}
