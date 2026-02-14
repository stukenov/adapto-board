package com.playoutedge.server.views.onboarding

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.settings.TIMEZONES
import kotlinx.html.*

/**
 * Onboarding wizard main view with improved UX.
 */
fun HTML.onboardingWizardView(
    session: AdminClaims,
    state: WizardState
) {
    adminLayout(title = "Getting Started", userName = session.displayName, currentPath = "/admin/onboarding") {
        pageHeader(
            title = "Getting Started",
            subtitle = "Set up your Playout Edge system in a few simple steps"
        ) {
            a(href = "/admin", classes = "btn btn-ghost") { +"Skip for now" }
        }

        // Progress bar
        div("card mb-4") {
            div("card-body") {
                div("wizard-progress") {
                    WizardStep.entries.forEach { step ->
                        val stepClass = when {
                            step in state.completedSteps -> "completed"
                            step == state.currentStep -> "active"
                            step in state.skippedSteps -> "skipped"
                            else -> ""
                        }
                        div("wizard-step $stepClass") {
                            div("step-number") {
                                if (step in state.completedSteps) {
                                    +"✓"
                                } else {
                                    +"${step.order}"
                                }
                            }
                            div("step-title") { +step.title }
                        }
                    }
                }
                div("progress mt-3") {
                    div("progress-bar") {
                        style = "width: ${state.progress}%"
                    }
                }
                div("progress-label mt-2") {
                    span("text-muted") { +"${state.progress}% complete" }
                }
            }
        }

        // Current step content
        when (state.currentStep) {
            WizardStep.TENANT_SETTINGS -> tenantSettingsStep(session)
            WizardStep.CONTENT_POLICIES -> contentPoliciesStep()
            WizardStep.FIRST_ASSET -> firstAssetStep()
            WizardStep.FIRST_CHANNEL -> firstChannelStep(state)
            WizardStep.FIRST_DEVICE -> firstDeviceStep(state)
            WizardStep.VERIFY -> verifyStep(state)
        }
    }
}

private fun MAIN.tenantSettingsStep(session: AdminClaims) {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 1" }
                h2 { +"Organization Settings" }
            }
        }
        div("card-body") {
            p("lead") { +"Let's set up your basic organization settings." }

            form(action = "/admin/onboarding/step/tenant-settings", method = FormMethod.post) {
                div("form-group") {
                    label {
                        htmlFor = "name"
                        +"Organization Name"
                    }
                    input(type = InputType.text, classes = "form-control form-control-lg") {
                        id = "name"
                        name = "name"
                        placeholder = "Your Company Name"
                        required = true
                    }
                }

                div("form-row") {
                    div("form-group col-6") {
                        label {
                            htmlFor = "timezone"
                            +"Timezone"
                        }
                        select("form-control") {
                            id = "timezone"
                            name = "timezone"
                            TIMEZONES.forEach { (id, label) ->
                                option {
                                    value = id
                                    if (id == "UTC") selected = true
                                    +label
                                }
                            }
                        }
                        small("form-helper") { +"Used for scheduling and reporting." }
                    }

                    div("form-group col-6") {
                        label {
                            htmlFor = "offlineThreshold"
                            +"Offline Threshold"
                        }
                        div("input-group") {
                            input(type = InputType.number, classes = "form-control") {
                                id = "offlineThreshold"
                                name = "offlineThreshold"
                                value = "5"
                                min = "2"
                                max = "10"
                            }
                            span("input-group-text") { +"minutes" }
                        }
                        small("form-helper") { +"Time before a device is marked offline." }
                    }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                        +"Continue"
                    }
                    a(href = "/admin/onboarding/skip/tenant-settings", classes = "btn btn-ghost") {
                        +"Skip this step"
                    }
                }
            }
        }
    }
}

private fun MAIN.contentPoliciesStep() {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 2" }
                h2 { +"Content Policies" }
            }
        }
        div("card-body") {
            p("lead") { +"Define what media formats are allowed. These are the recommended defaults." }

            form(action = "/admin/onboarding/step/content-policies", method = FormMethod.post) {
                div("form-row") {
                    div("form-group col-6") {
                        label { +"Allowed Video Codecs" }
                        div("checkbox-group") {
                            AVAILABLE_CODECS.forEach { codec ->
                                div("checkbox") {
                                    label {
                                        input(type = InputType.checkBox) {
                                            name = "codecs"
                                            value = codec
                                            if (codec in listOf("H.264", "H.265")) checked = true
                                        }
                                        +" $codec"
                                    }
                                }
                            }
                        }
                    }

                    div("form-group col-6") {
                        label { +"Allowed Containers" }
                        div("checkbox-group") {
                            AVAILABLE_CONTAINERS.forEach { container ->
                                div("checkbox") {
                                    label {
                                        input(type = InputType.checkBox) {
                                            name = "containers"
                                            value = container
                                            if (container in listOf("MP4", "MOV", "MKV")) checked = true
                                        }
                                        +" $container"
                                    }
                                }
                            }
                        }
                    }
                }

                div("form-row") {
                    div("form-group col-4") {
                        label {
                            htmlFor = "maxBitrate"
                            +"Maximum Bitrate"
                        }
                        div("input-group") {
                            input(type = InputType.number, classes = "form-control") {
                                id = "maxBitrate"
                                name = "maxBitrate"
                                value = "50"
                                min = "10"
                                max = "100"
                            }
                            span("input-group-text") { +"Mbps" }
                        }
                    }

                    div("form-group col-4") {
                        label {
                            htmlFor = "maxResolution"
                            +"Maximum Resolution"
                        }
                        select("form-control") {
                            id = "maxResolution"
                            name = "maxResolution"
                            AVAILABLE_RESOLUTIONS.forEach { res ->
                                option {
                                    value = res
                                    if (res == "4K") selected = true
                                    +res
                                }
                            }
                        }
                    }

                    div("form-group col-4") {
                        label {
                            htmlFor = "maxFileSize"
                            +"Maximum File Size"
                        }
                        div("input-group") {
                            input(type = InputType.number, classes = "form-control") {
                                id = "maxFileSize"
                                name = "maxFileSize"
                                value = "500"
                                min = "50"
                                max = "2000"
                            }
                            span("input-group-text") { +"MB" }
                        }
                    }
                }

                div("form-actions") {
                    a(href = "/admin/onboarding?step=TENANT_SETTINGS", classes = "btn btn-secondary") {
                        +"← Previous"
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                        +"Continue"
                    }
                    a(href = "/admin/onboarding/skip/content-policies", classes = "btn btn-ghost") {
                        +"Skip this step"
                    }
                }
            }
        }
    }
}

private fun MAIN.firstAssetStep() {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 3" }
                h2 { +"Upload Your First Content" }
            }
        }
        div("card-body") {
            p("lead") { +"Upload a video or image to get started. You can use one of our sample files." }

            div("form-actions mb-4") {
                a(href = "/admin/onboarding?step=CONTENT_POLICIES", classes = "btn btn-secondary") {
                    +"← Previous"
                }
            }

            form(action = "/admin/onboarding/step/first-asset", method = FormMethod.post, encType = FormEncType.multipartFormData) {
                div("upload-zone mb-4") {
                    div("upload-zone-content") {
                        div("upload-icon") { +"📁" }
                        div("form-group") {
                            label {
                                htmlFor = "file"
                                +"Choose a file"
                            }
                            input(type = InputType.file, classes = "form-control") {
                                id = "file"
                                name = "file"
                                accept = "video/*,image/*"
                            }
                        }
                        p("text-muted") { +"Supported: MP4, MOV, MKV, JPG, PNG" }
                    }
                }

                div("divider") {
                    span { +"or" }
                }

                div("form-group") {
                    label {
                        htmlFor = "sample"
                        +"Use a sample file"
                    }
                    select("form-control") {
                        id = "sample"
                        name = "sample"
                        option { value = ""; +"Choose a sample..." }
                        option { value = "demo-video"; +"Demo Video (30 seconds, 1080p)" }
                        option { value = "demo-image"; +"Demo Image (1920x1080)" }
                    }
                    small("form-helper") { +"Perfect for testing your setup." }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                        +"Upload & Continue"
                    }
                    a(href = "/admin/onboarding/skip/first-asset", classes = "btn btn-ghost") {
                        +"Skip this step"
                    }
                }
            }
        }
    }
}

private fun MAIN.firstChannelStep(state: WizardState) {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 4" }
                h2 { +"Create Your First Channel" }
            }
        }
        div("card-body") {
            p("lead") { +"A channel is a playlist that plays on your displays. Let's create one." }

            form(action = "/admin/onboarding/step/first-channel", method = FormMethod.post) {
                div("form-group") {
                    label {
                        htmlFor = "name"
                        +"Channel Name"
                    }
                    input(type = InputType.text, classes = "form-control form-control-lg") {
                        id = "name"
                        name = "name"
                        placeholder = "e.g., Lobby Display, Conference Room A"
                        required = true
                    }
                    small("form-helper") { +"Choose a descriptive name for your channel." }
                }

                if (state.createdAssetId != null) {
                    div("info-box mb-4") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "addAsset"
                                    checked = true
                                }
                                +" Add the uploaded asset to this channel"
                            }
                        }
                        small("form-helper") { +"The content you uploaded will be added to the channel's schedule." }
                    }
                }

                div("form-actions") {
                    a(href = "/admin/onboarding?step=FIRST_ASSET", classes = "btn btn-secondary") {
                        +"← Previous"
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                        +"Create Channel"
                    }
                    a(href = "/admin/onboarding/skip/first-channel", classes = "btn btn-ghost") {
                        +"Skip this step"
                    }
                }
            }
        }
    }
}

private fun MAIN.firstDeviceStep(state: WizardState) {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 5" }
                h2 { +"Connect Your First Device" }
            }
        }
        div("card-body") {
            p("lead") { +"Install the Playout Edge app on your Android TV device and enter this code:" }

            if (state.enrollCode != null) {
                div("enroll-code-display mb-4") {
                    div("code-box code-box-lg") {
                        +state.enrollCode
                    }
                    div("code-info mt-2") {
                        span("badge badge-warning") { +"Expires in 15 minutes" }
                    }
                }

                div("qr-code-container mb-4") {
                    div("qr-code") {
                        img(src = "https://api.qrserver.com/v1/create-qr-code/?data=${state.enrollCode}&size=200x200") {
                            alt = "QR Code"
                            width = "200"
                            height = "200"
                        }
                    }
                    p("text-muted text-center") { +"Scan with your device camera" }
                }
            }

            div("instructions-card mb-4") {
                h4 { +"Setup Instructions" }
                ol("instructions-list") {
                    li {
                        span("instruction-number") { +"1" }
                        span("instruction-text") { +"On your Android TV, open the Playout Edge app" }
                    }
                    li {
                        span("instruction-number") { +"2" }
                        span("instruction-text") { +"Select 'Enroll Device'" }
                    }
                    li {
                        span("instruction-number") { +"3" }
                        span("instruction-text") { +"Enter the code above or scan the QR code" }
                    }
                    li {
                        span("instruction-number") { +"4" }
                        span("instruction-text") { +"The device will automatically connect" }
                    }
                }
            }

            form(action = "/admin/onboarding/step/first-device", method = FormMethod.post) {
                if (state.createdChannelId != null) {
                    div("info-box mb-4") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "bindToChannel"
                                    checked = true
                                }
                                +" Automatically bind device to the created channel"
                            }
                        }
                        small("form-helper") { +"The device will start playing your channel's content immediately after enrollment." }
                    }
                }

                div("form-actions") {
                    a(href = "/admin/onboarding?step=FIRST_CHANNEL", classes = "btn btn-secondary") {
                        +"← Previous"
                    }
                    if (state.enrollCode == null) {
                        button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                            +"Generate Enroll Code"
                        }
                    } else {
                        button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                            name = "action"
                            value = "check"
                            +"Check Connection"
                        }
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            name = "action"
                            value = "regenerate"
                            +"Generate New Code"
                        }
                    }
                    a(href = "/admin/onboarding/skip/first-device", classes = "btn btn-ghost") {
                        +"Skip this step"
                    }
                }
            }
        }
    }
}

private fun MAIN.verifyStep(state: WizardState) {
    div("card") {
        div("card-header") {
            div("step-indicator") {
                span("step-badge") { +"Step 6" }
                h2 { +"Verify Setup" }
            }
        }
        div("card-body") {
            if (state.isComplete) {
                div("success-banner mb-4") {
                    div("success-icon") { +"🎉" }
                    h3 { +"Congratulations!" }
                    p("lead") { +"Your Playout Edge setup is complete. Your content should now be playing on your device." }
                }
            } else {
                alertBox("You've skipped some steps. You can complete them later from the respective sections.", "info")
            }

            div("checklist-card") {
                h4 { +"Setup Checklist" }
                ul("checklist-items") {
                    WizardStep.entries.filter { it != WizardStep.VERIFY }.forEach { step ->
                        val statusClass = when {
                            step in state.completedSteps -> "completed"
                            step in state.skippedSteps -> "skipped"
                            else -> "pending"
                        }
                        li(statusClass) {
                            span("check-icon") {
                                +when {
                                    step in state.completedSteps -> "✓"
                                    step in state.skippedSteps -> "—"
                                    else -> "○"
                                }
                            }
                            span("step-title") { +step.title }
                            if (step in state.completedSteps) {
                                span("status-badge badge badge-success badge-plain") { +"Done" }
                            } else if (step in state.skippedSteps) {
                                span("status-badge badge badge-gray badge-plain") { +"Skipped" }
                            }
                        }
                    }
                }
            }

            div("form-actions mt-4") {
                a(href = "/admin", classes = "btn btn-primary btn-lg") {
                    +"Go to Dashboard"
                }
                if (!state.isComplete) {
                    val nextStep = state.nextStep()
                    if (nextStep != null) {
                        a(href = "/admin/onboarding?step=${nextStep.name}", classes = "btn btn-secondary") {
                            +"Continue Setup"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Onboarding complete celebration with improved UX.
 */
fun HTML.onboardingCompleteView(session: AdminClaims) {
    adminLayout(title = "Setup Complete", userName = session.displayName, currentPath = "/admin/onboarding") {
        div("celebration-container") {
            div("celebration-icon") { +"🎉" }
            h1 { +"You're All Set!" }
            p("lead") { +"Your Playout Edge system is configured and ready to go." }

            div("stats-grid mt-4 mb-4") {
                div("stat-card") {
                    div("stat-icon") { +"🎬" }
                    div("stat-content") {
                        div("stat-value") { +"1" }
                        div("stat-label") { +"Asset Uploaded" }
                    }
                }
                div("stat-card") {
                    div("stat-icon") { +"📺" }
                    div("stat-content") {
                        div("stat-value") { +"1" }
                        div("stat-label") { +"Channel Created" }
                    }
                }
                div("stat-card") {
                    div("stat-icon") { +"📱" }
                    div("stat-content") {
                        div("stat-value") { +"1" }
                        div("stat-label") { +"Device Connected" }
                    }
                }
            }

            div("next-steps card") {
                div("card-header") {
                    h3 { +"What's Next?" }
                }
                div("card-body") {
                    div("next-steps-grid") {
                        a(href = "/admin/assets", classes = "next-step-item") {
                            span("next-step-icon") { +"📁" }
                            span("next-step-title") { +"Upload more content" }
                            span("next-step-description") { +"Build your media library" }
                        }
                        a(href = "/admin/channels", classes = "next-step-item") {
                            span("next-step-icon") { +"📅" }
                            span("next-step-title") { +"Create schedules" }
                            span("next-step-description") { +"Automate playback" }
                        }
                        a(href = "/admin/devices", classes = "next-step-item") {
                            span("next-step-icon") { +"📺" }
                            span("next-step-title") { +"Add more devices" }
                            span("next-step-description") { +"Expand your network" }
                        }
                        a(href = "/admin/overlay", classes = "next-step-item") {
                            span("next-step-icon") { +"🎨" }
                            span("next-step-title") { +"Set up overlays" }
                            span("next-step-description") { +"Dynamic content" }
                        }
                    }
                }
            }

            a(href = "/admin", classes = "btn btn-primary btn-lg mt-4") {
                +"Go to Dashboard"
            }
        }
    }
}
