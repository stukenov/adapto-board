package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

/**
 * Overlay bindings list view with improved UX.
 */
fun HTML.overlayBindingsListView(
    session: AdminClaims,
    bindings: List<OverlayBindingItem>,
    profiles: List<OverlayProfileItem>,
    channels: List<ChannelOption>
) {
    adminLayout(title = "Overlay Bindings", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Overlay Bindings",
            subtitle = "Connect profiles to channels for dynamic content"
        ) {
            a(href = "/admin/overlay/bindings/new", classes = "btn btn-primary") {
                +"+ New Binding"
            }
        }

        // Navigation tabs
        div("tabs mb-4") {
            a(href = "/admin/overlay/profiles", classes = "tab") { +"Profiles" }
            a(href = "/admin/overlay/bindings", classes = "tab active") { +"Bindings" }
        }

        div("card") {
            if (bindings.isEmpty()) {
                emptyState(
                    icon = "🔗",
                    title = "No overlay bindings",
                    description = "Connect overlay profiles to channels to display dynamic content like tickers, tables, and KPIs.",
                    actionHref = "/admin/overlay/bindings/new",
                    actionLabel = "Create Binding"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Channel" }
                            th { +"Profile" }
                            th { +"Source" }
                            th { +"Status" }
                            th { }
                        }
                    }
                    tbody {
                        bindings.forEach { binding ->
                            tr {
                                td {
                                    a(href = "/admin/channels/${binding.channelId}", classes = "font-medium") {
                                        +binding.channelName
                                    }
                                }
                                td {
                                    a(href = "/admin/overlay/profiles/${binding.profileId}") {
                                        +binding.profileName
                                    }
                                }
                                td {
                                    span("badge badge-${sourceTypeBadge(binding.sourceType)} badge-plain") {
                                        +binding.sourceType.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${bindingStatusBadge(binding.status)}") {
                                        +binding.status.name.lowercase()
                                    }
                                }
                                td {
                                    div("table-actions") {
                                        a(href = "/admin/overlay/bindings/${binding.id}", classes = "btn btn-secondary btn-sm") {
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
}

/**
 * New binding form with improved UX.
 */
fun HTML.newOverlayBindingView(
    session: AdminClaims,
    profiles: List<OverlayProfileItem>,
    channels: List<ChannelOption>,
    preselectedProfileId: UUID? = null,
    error: String? = null
) {
    adminLayout(title = "New Overlay Binding", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "New Overlay Binding",
            subtitle = "Connect a profile to a channel",
            backHref = "/admin/overlay/bindings",
            backLabel = "Back to Bindings"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                if (profiles.isEmpty()) {
                    alertBox("No overlay profiles exist.", "warning")
                    div("mt-3") {
                        a(href = "/admin/overlay/profiles/new", classes = "btn btn-primary") {
                            +"Create a Profile First"
                        }
                    }
                } else if (channels.isEmpty()) {
                    alertBox("No channels exist.", "warning")
                    div("mt-3") {
                        a(href = "/admin/channels/new", classes = "btn btn-primary") {
                            +"Create a Channel First"
                        }
                    }
                } else {
                    form(action = "/admin/overlay/bindings", method = FormMethod.post) {
                        div("form-group") {
                            label {
                                htmlFor = "channelId"
                                +"Channel"
                            }
                            select("form-control") {
                                id = "channelId"
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
                            small("form-helper") {
                                +"The channel where the overlay will be displayed."
                            }
                        }

                        div("form-group") {
                            label {
                                htmlFor = "profileId"
                                +"Overlay Profile"
                            }
                            select("form-control") {
                                id = "profileId"
                                name = "profileId"
                                required = true
                                option {
                                    value = ""
                                    +"Select a profile..."
                                }
                                profiles.forEach { profile ->
                                    option {
                                        value = profile.id.toString()
                                        if (profile.id == preselectedProfileId) selected = true
                                        +profile.name
                                    }
                                }
                            }
                            small("form-helper") {
                                +"The profile defines the overlay layout and widgets."
                            }
                        }

                        div("form-group") {
                            label {
                                htmlFor = "sourceType"
                                +"Data Source"
                            }
                            select("form-control") {
                                id = "sourceType"
                                name = "sourceType"
                                required = true
                                OverlaySourceType.entries.forEach { type ->
                                    option {
                                        value = type.name
                                        +when (type) {
                                            OverlaySourceType.MANUAL -> "Manual - Edit data directly in the admin panel"
                                            OverlaySourceType.REST_PULL -> "REST Pull - Periodically fetch from an external API"
                                            OverlaySourceType.WEBHOOK -> "Webhook - Receive real-time data via webhook"
                                        }
                                    }
                                }
                            }
                            small("form-helper") {
                                +"How the overlay data will be populated."
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
 * Binding detail view with improved layout.
 */
fun HTML.overlayBindingDetailView(
    session: AdminClaims,
    binding: OverlayBindingDetail,
    webhookLogs: List<WebhookLogItem>
) {
    adminLayout(title = "Binding: ${binding.channelName}", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "${binding.channelName} Overlay",
            subtitle = "Profile: ${binding.profileName}",
            backHref = "/admin/overlay/bindings",
            backLabel = "Back to Bindings"
        ) {
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
                    attributes["onclick"] = "return confirm('Delete this binding? This action cannot be undone.')"
                    +"Delete"
                }
            }
        }

        // Status cards
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-icon") { +"📡" }
                div("stat-content") {
                    div("stat-value") {
                        span("badge badge-${bindingStatusBadge(binding.status)} badge-lg") {
                            +binding.status.name.lowercase()
                        }
                    }
                    div("stat-label") { +"Status" }
                }
            }
            div("stat-card") {
                div("stat-icon") { +"🔌" }
                div("stat-content") {
                    div("stat-value") { +binding.sourceType.name.replace("_", " ") }
                    div("stat-label") { +"Source Type" }
                }
            }
            div("stat-card") {
                div("stat-icon") { +"🎨" }
                div("stat-content") {
                    div("stat-value") {
                        a(href = "/admin/overlay/profiles/${binding.profileId}") {
                            +binding.profileName
                        }
                    }
                    div("stat-label") { +"Profile" }
                }
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
        div("card-header") {
            h3 { +"Manual Editor" }
        }
        div("card-body") {
            form(action = "/admin/overlay/bindings/${binding.id}/state", method = FormMethod.post) {
                div("form-group") {
                    label {
                        htmlFor = "stateJson"
                        +"Overlay State JSON"
                    }
                    textArea(classes = "form-control code-editor") {
                        id = "stateJson"
                        name = "stateJson"
                        rows = "12"
                        placeholder = """{"ticker": {"text": "Breaking news..."}}"""
                        +binding.sourceConfigJson
                    }
                    small("form-helper") {
                        +"Edit the JSON data that will be displayed on the overlay. Changes are pushed to devices in real-time."
                    }
                }
                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Send to Devices"
                    }
                }
            }
        }
    }
}

private fun MAIN.restConnectorCard(binding: OverlayBindingDetail) {
    div("card mb-4") {
        div("card-header") {
            h3 { +"REST Connector Configuration" }
        }
        div("card-body") {
            form(action = "/admin/overlay/bindings/${binding.id}/rest-config", method = FormMethod.post) {
                div("form-row") {
                    div("form-group col-8") {
                        label {
                            htmlFor = "url"
                            +"API URL"
                        }
                        input(type = InputType.url, classes = "form-control") {
                            id = "url"
                            name = "url"
                            placeholder = "https://api.example.com/data"
                        }
                        small("form-helper") {
                            +"The endpoint to fetch overlay data from."
                        }
                    }

                    div("form-group col-4") {
                        label {
                            htmlFor = "intervalSeconds"
                            +"Polling Interval"
                        }
                        div("input-group") {
                            input(type = InputType.number, classes = "form-control") {
                                id = "intervalSeconds"
                                name = "intervalSeconds"
                                min = "10"
                                max = "3600"
                                value = "60"
                            }
                            span("input-group-text") { +"seconds" }
                        }
                    }
                }

                div("form-row") {
                    div("form-group col-6") {
                        label {
                            htmlFor = "authType"
                            +"Authentication"
                        }
                        select("form-control") {
                            id = "authType"
                            name = "authType"
                            option { value = "none"; +"None" }
                            option { value = "bearer"; +"Bearer Token" }
                            option { value = "basic"; +"Basic Auth" }
                            option { value = "header"; +"Custom Header" }
                        }
                    }

                    div("form-group col-6") {
                        label {
                            htmlFor = "authValue"
                            +"Auth Value"
                        }
                        input(type = InputType.password, classes = "form-control") {
                            id = "authValue"
                            name = "authValue"
                            placeholder = "Token or credentials"
                        }
                    }
                }

                div("form-group") {
                    label {
                        htmlFor = "mappingPreset"
                        +"Mapping Preset"
                    }
                    select("form-control") {
                        id = "mappingPreset"
                        name = "mappingPreset"
                        option { value = "raw"; +"Raw (no transformation)" }
                        option { value = "queue"; +"Queue Table" }
                        option { value = "kpi"; +"KPI Tiles" }
                        option { value = "ticker"; +"Ticker" }
                    }
                    small("form-helper") {
                        +"Transform the API response to match the overlay widget format."
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
        div("card-header") {
            h3 { +"Current Configuration" }
        }
        div("card-body") {
            pre("code-block") {
                +binding.sourceConfigJson
            }
        }
    }
}

private fun MAIN.webhookConfigCard(binding: OverlayBindingDetail, webhookLogs: List<WebhookLogItem>) {
    div("card mb-4") {
        div("card-header") {
            h3 { +"Webhook Configuration" }
        }
        div("card-body") {
            div("form-group") {
                label { +"Webhook URL" }
                div("input-group") {
                    input(type = InputType.text, classes = "form-control") {
                        readonly = true
                        value = binding.webhookUrl ?: "Not configured"
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        attributes["onclick"] = "navigator.clipboard.writeText('${binding.webhookUrl ?: ""}'); this.textContent = 'Copied!'; setTimeout(() => this.textContent = 'Copy', 2000)"
                        +"Copy"
                    }
                }
                small("form-helper") {
                    +"Send POST requests to this URL to update the overlay."
                }
            }

            div("form-group") {
                label { +"Signing Secret" }
                div("input-group") {
                    input(type = InputType.password, classes = "form-control") {
                        id = "webhookSecret"
                        readonly = true
                        value = binding.webhookSecret ?: ""
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        attributes["onclick"] = "const inp = document.getElementById('webhookSecret'); inp.type = inp.type === 'password' ? 'text' : 'password'; this.textContent = inp.type === 'password' ? 'Show' : 'Hide'"
                        +"Show"
                    }
                }
                small("form-helper") {
                    +"Use this secret to sign webhook payloads for security."
                }
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
        div("card-header") {
            h3 { +"Recent Webhook Calls" }
            if (webhookLogs.isNotEmpty()) {
                span("badge badge-gray badge-plain") { +"${webhookLogs.size}" }
            }
        }
        if (webhookLogs.isEmpty()) {
            div("card-body") {
                emptyState(
                    icon = "📥",
                    title = "No webhook calls yet",
                    description = "Webhook calls will appear here once you start sending data."
                )
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
                            td {
                                span("text-muted") {
                                    +log.createdAt.toString().replace("T", " ").substringBeforeLast(":")
                                }
                            }
                            td {
                                span("badge badge-${if (log.isSuccess) "success" else "danger"}") {
                                    +log.statusCode.toString()
                                }
                            }
                            td { +"${log.latencyMs}ms" }
                            td { +"${log.payloadSize}B" }
                            td {
                                log.error?.let {
                                    span("text-danger") { +it }
                                } ?: span("text-muted") { +"—" }
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
