package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*
import java.util.UUID

/**
 * Overlay bindings list view.
 */
fun HTML.overlayBindingsListView(
    session: AdminClaims,
    bindings: List<OverlayBindingItem>,
    profiles: List<OverlayProfileItem>,
    channels: List<ChannelOption>
) {
    adminLayout(title = "Overlay Bindings", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Overlay Bindings" }
            a(href = "/admin/overlay/bindings/new", classes = "btn btn-primary") {
                +"New Binding"
            }
        }

        div("card") {
            if (bindings.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"🔗" }
                    p("empty-state-title") { +"No overlay bindings" }
                    p("empty-state-text") { +"Connect overlay profiles to channels to display dynamic content." }
                    a(href = "/admin/overlay/bindings/new", classes = "btn btn-primary") {
                        +"Create Binding"
                    }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Channel" }
                            th { +"Profile" }
                            th { +"Source" }
                            th { +"Status" }
                            th { +"Actions" }
                        }
                    }
                    tbody {
                        bindings.forEach { binding ->
                            tr {
                                td {
                                    a(href = "/admin/channels/${binding.channelId}") {
                                        +binding.channelName
                                    }
                                }
                                td {
                                    a(href = "/admin/overlay/profiles/${binding.profileId}") {
                                        +binding.profileName
                                    }
                                }
                                td {
                                    span("badge badge-${sourceTypeBadge(binding.sourceType)}") {
                                        +binding.sourceType.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${bindingStatusBadge(binding.status)}") {
                                        +binding.status.name.lowercase()
                                    }
                                }
                                td {
                                    a(href = "/admin/overlay/bindings/${binding.id}", classes = "btn btn-sm btn-secondary") {
                                        +"View"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * New binding form.
 */
fun HTML.newOverlayBindingView(
    session: AdminClaims,
    profiles: List<OverlayProfileItem>,
    channels: List<ChannelOption>,
    error: String? = null
) {
    adminLayout(title = "New Overlay Binding", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/overlay/bindings", classes = "link") { +"← Back to Bindings" }
                h1("page-title") { +"New Overlay Binding" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-danger") { +error }
                }

                if (profiles.isEmpty()) {
                    div("alert alert-warning") {
                        +"No overlay profiles exist. "
                        a(href = "/admin/overlay/profiles/new") { +"Create a profile first." }
                    }
                } else if (channels.isEmpty()) {
                    div("alert alert-warning") {
                        +"No channels exist. "
                        a(href = "/admin/channels/new") { +"Create a channel first." }
                    }
                } else {
                    form(action = "/admin/overlay/bindings", method = FormMethod.post) {
                        div("form-group") {
                            label { +"Channel" }
                            select("form-control") {
                                name = "channelId"
                                required = true
                                option {
                                    value = ""
                                    +"Select a channel..."
                                }
                                channels.forEach { channel ->
                                    option {
                                        value = channel.id.toString()
                                        +channel.name
                                    }
                                }
                            }
                        }

                        div("form-group") {
                            label { +"Overlay Profile" }
                            select("form-control") {
                                name = "profileId"
                                required = true
                                option {
                                    value = ""
                                    +"Select a profile..."
                                }
                                profiles.forEach { profile ->
                                    option {
                                        value = profile.id.toString()
                                        +profile.name
                                    }
                                }
                            }
                        }

                        div("form-group") {
                            label { +"Data Source" }
                            select("form-control") {
                                name = "sourceType"
                                required = true
                                OverlaySourceType.entries.forEach { type ->
                                    option {
                                        value = type.name
                                        +when (type) {
                                            OverlaySourceType.MANUAL -> "Manual - Edit data directly"
                                            OverlaySourceType.REST_PULL -> "REST Pull - Fetch from external API"
                                            OverlaySourceType.WEBHOOK -> "Webhook - Receive data via webhook"
                                        }
                                    }
                                }
                            }
                        }

                        div("form-actions") {
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Create Binding"
                            }
                            a(href = "/admin/overlay/bindings", classes = "btn btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Binding detail view.
 */
fun HTML.overlayBindingDetailView(
    session: AdminClaims,
    binding: OverlayBindingDetail,
    webhookLogs: List<WebhookLogItem>
) {
    adminLayout(title = "Binding: ${binding.channelName}", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/overlay/bindings", classes = "link") { +"← Back to Bindings" }
                h1("page-title") { +"${binding.channelName} Overlay" }
            }
            div("btn-group") {
                if (binding.status == BindingStatus.ACTIVE) {
                    form(action = "/admin/overlay/bindings/${binding.id}/pause", method = FormMethod.post, classes = "inline") {
                        button(type = ButtonType.submit, classes = "btn btn-warning") { +"Pause" }
                    }
                } else {
                    form(action = "/admin/overlay/bindings/${binding.id}/activate", method = FormMethod.post, classes = "inline") {
                        button(type = ButtonType.submit, classes = "btn btn-success") { +"Activate" }
                    }
                }
                form(action = "/admin/overlay/bindings/${binding.id}/delete", method = FormMethod.post, classes = "inline") {
                    button(type = ButtonType.submit, classes = "btn btn-danger") {
                        attributes["onclick"] = "return confirm('Delete this binding?')"
                        +"Delete"
                    }
                }
            }
        }

        // Status card
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-value") {
                    span("badge badge-${bindingStatusBadge(binding.status)} badge-lg") {
                        +binding.status.name
                    }
                }
                div("stat-label") { +"Status" }
            }
            div("stat-card") {
                div("stat-value") { +binding.sourceType.name.replace("_", " ") }
                div("stat-label") { +"Source Type" }
            }
            div("stat-card") {
                div("stat-value") { +binding.profileName }
                div("stat-label") { +"Profile" }
            }
        }

        // Source configuration
        when (binding.sourceType) {
            OverlaySourceType.MANUAL -> manualEditorCard(binding)
            OverlaySourceType.REST_PULL -> restConnectorCard(binding)
            OverlaySourceType.WEBHOOK -> webhookConfigCard(binding, webhookLogs)
        }
    }
}

private fun MAIN.manualEditorCard(binding: OverlayBindingDetail) {
    div("card") {
        div("card-header") { +"Manual Editor" }
        div("card-body") {
            form(action = "/admin/overlay/bindings/${binding.id}/state", method = FormMethod.post) {
                div("form-group") {
                    label { +"Overlay State JSON" }
                    textArea(classes = "form-control") {
                        name = "stateJson"
                        rows = "10"
                        placeholder = """{"ticker": {"text": "Breaking news..."}}"""
                        +binding.sourceConfigJson
                    }
                    small("text-muted") { +"Edit the JSON data that will be displayed on the overlay" }
                }
                button(type = ButtonType.submit, classes = "btn btn-primary") {
                    +"Send to Devices"
                }
            }
        }
    }
}

private fun MAIN.restConnectorCard(binding: OverlayBindingDetail) {
    div("card mb-4") {
        div("card-header") { +"REST Connector Configuration" }
        div("card-body") {
            form(action = "/admin/overlay/bindings/${binding.id}/rest-config", method = FormMethod.post) {
                div("form-group") {
                    label { +"API URL" }
                    input(type = InputType.url, classes = "form-control") {
                        name = "url"
                        placeholder = "https://api.example.com/data"
                    }
                }

                div("form-group") {
                    label { +"Authentication" }
                    select("form-control") {
                        name = "authType"
                        option { value = "none"; +"None" }
                        option { value = "bearer"; +"Bearer Token" }
                        option { value = "basic"; +"Basic Auth" }
                        option { value = "header"; +"Custom Header" }
                    }
                }

                div("form-group") {
                    label { +"Auth Value" }
                    input(type = InputType.text, classes = "form-control") {
                        name = "authValue"
                        placeholder = "Token or credentials"
                    }
                }

                div("form-group") {
                    label { +"Polling Interval (seconds)" }
                    input(type = InputType.number, classes = "form-control") {
                        name = "intervalSeconds"
                        min = "10"
                        max = "3600"
                        value = "60"
                    }
                }

                div("form-group") {
                    label { +"Mapping Preset" }
                    select("form-control") {
                        name = "mappingPreset"
                        option { value = "raw"; +"Raw (no transformation)" }
                        option { value = "queue"; +"Queue Table" }
                        option { value = "kpi"; +"KPI Tiles" }
                        option { value = "ticker"; +"Ticker" }
                    }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Save Configuration"
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        +"Test Fetch"
                    }
                }
            }
        }
    }

    // Current config
    div("card") {
        div("card-header") { +"Current Configuration" }
        div("card-body") {
            pre("code-block") {
                +binding.sourceConfigJson
            }
        }
    }
}

private fun MAIN.webhookConfigCard(binding: OverlayBindingDetail, webhookLogs: List<WebhookLogItem>) {
    div("card mb-4") {
        div("card-header") { +"Webhook Configuration" }
        div("card-body") {
            div("form-group") {
                label { +"Webhook URL" }
                div("input-group") {
                    input(type = InputType.text, classes = "form-control") {
                        readonly = true
                        value = binding.webhookUrl ?: "Not configured"
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        attributes["onclick"] = "navigator.clipboard.writeText('${binding.webhookUrl ?: ""}')"
                        +"Copy"
                    }
                }
            }

            div("form-group") {
                label { +"Signing Secret" }
                div("input-group") {
                    input(type = InputType.password, classes = "form-control") {
                        readonly = true
                        value = binding.webhookSecret ?: ""
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        attributes["onclick"] = "this.previousElementSibling.type = this.previousElementSibling.type === 'password' ? 'text' : 'password'"
                        +"Show"
                    }
                }
                small("text-muted") { +"Use this secret to sign webhook payloads" }
            }

            div("form-group") {
                label { +"Example Payload" }
                pre("code-block") {
                    +"""{
  "type": "ticker",
  "data": {
    "text": "Your message here"
  }
}"""
                }
            }
        }
    }

    // Webhook logs
    div("card") {
        div("card-header") { +"Recent Webhook Calls" }
        if (webhookLogs.isEmpty()) {
            div("card-body") {
                p("text-muted") { +"No webhook calls received yet." }
            }
        } else {
            table("table") {
                thead {
                    tr {
                        th { +"Time" }
                        th { +"Status" }
                        th { +"Latency" }
                        th { +"Size" }
                        th { +"Error" }
                    }
                }
                tbody {
                    webhookLogs.forEach { log ->
                        tr {
                            td { +log.createdAt.toString() }
                            td {
                                span("badge badge-${if (log.isSuccess) "success" else "danger"}") {
                                    +log.statusCode.toString()
                                }
                            }
                            td { +"${log.latencyMs}ms" }
                            td { +"${log.payloadSize}B" }
                            td {
                                log.error?.let { +it } ?: span("text-muted") { +"—" }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sourceTypeBadge(type: OverlaySourceType): String = when (type) {
    OverlaySourceType.MANUAL -> "info"
    OverlaySourceType.REST_PULL -> "primary"
    OverlaySourceType.WEBHOOK -> "success"
}

private fun bindingStatusBadge(status: BindingStatus): String = when (status) {
    BindingStatus.ACTIVE -> "success"
    BindingStatus.PAUSED -> "warning"
}

/**
 * Channel option for dropdowns.
 */
data class ChannelOption(
    val id: UUID,
    val name: String
)
