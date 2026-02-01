package com.playoutedge.server.views.onboarding

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.settings.TIMEZONES
import kotlinx.html.*

/**
 * Onboarding wizard main view.
 */
fun HTML.onboardingWizardView(
    session: AdminClaims,
    state: WizardState
) {
    adminLayout(title = "Getting Started", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Getting Started" }
            a(href = "/admin", classes = "link") { +"Skip for now →" }
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
                            div("step-number") { +"${step.order}" }
                            div("step-title") { +step.title }
                        }
                    }
                }
                div("progress mt-3") {
                    div("progress-bar") {
                        style = "width: ${state.progress}%"
                    }
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
            h2 { +"Step 1: Organization Settings" }
        }
        div("card-body") {
            p { +"Let's set up your basic organization settings." }

            form(action = "/admin/onboarding/step/tenant-settings", method = FormMethod.post) {
                div("form-group") {
                    label { +"Organization Name" }
                    input(type = InputType.text, classes = "form-control") {
                        name = "name"
                        placeholder = "Your Company Name"
                        required = true
                    }
                }

                div("form-group") {
                    label { +"Timezone" }
                    select("form-control") {
                        name = "timezone"
                        TIMEZONES.forEach { tz ->
                            option {
                                value = tz
                                if (tz == "UTC") selected = true
                                +tz
                            }
                        }
                    }
                    small("text-muted") { +"Used for scheduling and reporting" }
                }

                div("form-group") {
                    label { +"Offline Threshold (minutes)" }
                    input(type = InputType.number, classes = "form-control") {
                        name = "offlineThreshold"
                        value = "5"
                        min = "2"
                        max = "10"
                    }
                    small("text-muted") { +"Time before a device is marked offline" }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Continue"
                    }
                    a(href = "/admin/onboarding/skip/tenant-settings", classes = "btn btn-secondary") {
                        +"Skip"
                    }
                }
            }
        }
    }
}

private fun MAIN.contentPoliciesStep() {
    div("card") {
        div("card-header") {
            h2 { +"Step 2: Content Policies" }
        }
        div("card-body") {
            p { +"Define what media formats are allowed. These are the recommended defaults." }

            form(action = "/admin/onboarding/step/content-policies", method = FormMethod.post) {
                div("form-group") {
                    label { +"Allowed Video Codecs" }
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

                div("form-group") {
                    label { +"Allowed Containers" }
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

                div("form-group") {
                    label { +"Maximum Bitrate (Mbps)" }
                    input(type = InputType.number, classes = "form-control") {
                        name = "maxBitrate"
                        value = "50"
                        min = "10"
                        max = "100"
                    }
                }

                div("form-group") {
                    label { +"Maximum Resolution" }
                    select("form-control") {
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

                div("form-group") {
                    label { +"Maximum File Size (MB)" }
                    input(type = InputType.number, classes = "form-control") {
                        name = "maxFileSize"
                        value = "500"
                        min = "50"
                        max = "2000"
                    }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Continue"
                    }
                    a(href = "/admin/onboarding/skip/content-policies", classes = "btn btn-secondary") {
                        +"Skip"
                    }
                }
            }
        }
    }
}

private fun MAIN.firstAssetStep() {
    div("card") {
        div("card-header") {
            h2 { +"Step 3: Upload Your First Content" }
        }
        div("card-body") {
            p { +"Upload a video or image to get started. You can use one of our sample files." }

            div("upload-zone mb-4") {
                form(action = "/admin/onboarding/step/first-asset", method = FormMethod.post, encType = FormEncType.multipartFormData) {
                    div("form-group") {
                        label { +"Choose a file" }
                        input(type = InputType.file, classes = "form-control") {
                            name = "file"
                            accept = "video/*,image/*"
                        }
                    }

                    div("form-group") {
                        label { +"Or use a sample" }
                        select("form-control") {
                            name = "sample"
                            option { value = ""; +"Choose a sample..." }
                            option { value = "demo-video"; +"Demo Video (30s, 1080p)" }
                            option { value = "demo-image"; +"Demo Image (1920x1080)" }
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Upload & Continue"
                    }
                }
            }

            a(href = "/admin/onboarding/skip/first-asset", classes = "link") {
                +"Skip for now"
            }
        }
    }
}

private fun MAIN.firstChannelStep(state: WizardState) {
    div("card") {
        div("card-header") {
            h2 { +"Step 4: Create Your First Channel" }
        }
        div("card-body") {
            p { +"A channel is a playlist that plays on your displays. Let's create one." }

            form(action = "/admin/onboarding/step/first-channel", method = FormMethod.post) {
                div("form-group") {
                    label { +"Channel Name" }
                    input(type = InputType.text, classes = "form-control") {
                        name = "name"
                        placeholder = "e.g., Lobby Display"
                        required = true
                    }
                }

                if (state.createdAssetId != null) {
                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "addAsset"
                                    checked = true
                                }
                                +" Add the uploaded asset to this channel"
                            }
                        }
                    }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Create Channel"
                    }
                    a(href = "/admin/onboarding/skip/first-channel", classes = "btn btn-secondary") {
                        +"Skip"
                    }
                }
            }
        }
    }
}

private fun MAIN.firstDeviceStep(state: WizardState) {
    div("card") {
        div("card-header") {
            h2 { +"Step 5: Connect Your First Device" }
        }
        div("card-body") {
            p { +"Install the Playout Edge app on your Android TV device and enter this code:" }

            if (state.enrollCode != null) {
                div("enroll-code-display mb-4") {
                    div("code-box") {
                        +state.enrollCode
                    }
                    p("text-muted mt-2") { +"This code expires in 15 minutes" }
                }

                div("qr-code mb-4") {
                    // Would show QR code here
                    div("qr-placeholder") {
                        +"[QR Code]"
                    }
                }
            }

            div("instructions mb-4") {
                h4 { +"Instructions" }
                ol {
                    li { +"On your Android TV, open the Playout Edge app" }
                    li { +"Select 'Enroll Device'" }
                    li { +"Enter the code above or scan the QR code" }
                    li { +"The device will automatically connect" }
                }
            }

            form(action = "/admin/onboarding/step/first-device", method = FormMethod.post) {
                if (state.createdChannelId != null) {
                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "bindToChannel"
                                    checked = true
                                }
                                +" Automatically bind device to the created channel"
                            }
                        }
                    }
                }

                div("form-actions") {
                    if (state.enrollCode == null) {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Generate Enroll Code"
                        }
                    } else {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
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
                    a(href = "/admin/onboarding/skip/first-device", classes = "btn btn-secondary") {
                        +"Skip"
                    }
                }
            }
        }
    }
}

private fun MAIN.verifyStep(state: WizardState) {
    div("card") {
        div("card-header") {
            h2 { +"Step 6: Verify Setup" }
        }
        div("card-body") {
            if (state.isComplete) {
                div("success-state mb-4") {
                    div("success-icon") { +"🎉" }
                    h3 { +"Congratulations!" }
                    p { +"Your Playout Edge setup is complete. Your content should now be playing on your device." }
                }
            } else {
                div("info-state mb-4") {
                    p { +"You've skipped some steps. You can complete them later from the respective sections." }
                }
            }

            div("checklist") {
                h4 { +"Setup Checklist" }
                ul("checklist-items") {
                    WizardStep.entries.filter { it != WizardStep.VERIFY }.forEach { step ->
                        li(if (step in state.completedSteps) "completed" else if (step in state.skippedSteps) "skipped" else "") {
                            span("check-icon") {
                                +if (step in state.completedSteps) "✓" else if (step in state.skippedSteps) "—" else "○"
                            }
                            +step.title
                        }
                    }
                }
            }

            div("form-actions mt-4") {
                a(href = "/admin", classes = "btn btn-primary") {
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
 * Onboarding complete celebration.
 */
fun HTML.onboardingCompleteView(session: AdminClaims) {
    adminLayout(title = "Setup Complete", userName = "Admin") {
        div("celebration-container") {
            div("celebration-icon") { +"🎉" }
            h1 { +"You're All Set!" }
            p("lead") { +"Your Playout Edge system is configured and ready to go." }

            div("stats-grid mt-4 mb-4") {
                div("stat-card") {
                    div("stat-value") { +"1" }
                    div("stat-label") { +"Asset Uploaded" }
                }
                div("stat-card") {
                    div("stat-value") { +"1" }
                    div("stat-label") { +"Channel Created" }
                }
                div("stat-card") {
                    div("stat-value") { +"1" }
                    div("stat-label") { +"Device Connected" }
                }
            }

            div("next-steps card") {
                div("card-header") { +"What's Next?" }
                div("card-body") {
                    ul {
                        li {
                            a(href = "/admin/assets") { +"Upload more content" }
                            +" to build your media library"
                        }
                        li {
                            a(href = "/admin/channels") { +"Create schedules" }
                            +" to automate playback"
                        }
                        li {
                            a(href = "/admin/devices") { +"Add more devices" }
                            +" to expand your network"
                        }
                        li {
                            a(href = "/admin/overlay") { +"Set up overlays" }
                            +" for dynamic content"
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
