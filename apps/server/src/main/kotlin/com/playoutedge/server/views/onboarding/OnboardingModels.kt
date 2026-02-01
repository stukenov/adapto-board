package com.playoutedge.server.views.onboarding

import java.util.UUID

/**
 * Wizard step.
 */
enum class WizardStep(val order: Int, val title: String, val description: String) {
    TENANT_SETTINGS(1, "Organization", "Set your timezone and preferences"),
    CONTENT_POLICIES(2, "Content Rules", "Define allowed media formats"),
    FIRST_ASSET(3, "Upload Content", "Add your first video or image"),
    FIRST_CHANNEL(4, "Create Channel", "Set up your first broadcast channel"),
    FIRST_DEVICE(5, "Connect Device", "Pair your first display"),
    VERIFY(6, "Verify", "Confirm everything is working")
}

/**
 * Wizard state for tracking progress.
 */
data class WizardState(
    val currentStep: WizardStep,
    val completedSteps: Set<WizardStep>,
    val skippedSteps: Set<WizardStep>,
    val createdAssetId: UUID? = null,
    val createdChannelId: UUID? = null,
    val enrollCode: String? = null
) {
    val isComplete: Boolean
        get() = completedSteps.containsAll(WizardStep.entries.filter { it != WizardStep.VERIFY })

    val progress: Int
        get() = (completedSteps.size * 100) / WizardStep.entries.size

    fun nextStep(): WizardStep? {
        val remaining = WizardStep.entries
            .filter { it !in completedSteps && it !in skippedSteps }
            .sortedBy { it.order }
        return remaining.firstOrNull()
    }
}

/**
 * Content policy defaults.
 */
data class ContentPolicyDefaults(
    val allowedCodecs: List<String> = listOf("H.264", "H.265"),
    val allowedContainers: List<String> = listOf("MP4", "MOV", "MKV"),
    val maxBitrateMbps: Int = 50,
    val maxResolution: String = "4K",
    val maxFileSizeMb: Int = 500
)

/**
 * Available codecs.
 */
val AVAILABLE_CODECS = listOf("H.264", "H.265", "VP8", "VP9", "AV1")

/**
 * Available containers.
 */
val AVAILABLE_CONTAINERS = listOf("MP4", "MOV", "MKV", "WebM", "AVI")

/**
 * Available resolutions.
 */
val AVAILABLE_RESOLUTIONS = listOf("1080p", "4K", "8K")
