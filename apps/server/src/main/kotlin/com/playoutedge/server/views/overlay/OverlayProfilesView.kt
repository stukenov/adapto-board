package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Overlay profiles list view with improved UX.
 */
fun HTML.overlayProfilesListView(
    session: AdminClaims,
    profiles: List<OverlayProfileItem>
) {
    adminLayout(title = "Overlay Profiles", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Overlay Profiles",
            subtitle = "Manage dynamic content templates for your displays"
        ) {
            a(href = "/admin/overlay/profiles/new", classes = "btn btn-primary") {
                +"+ New Profile"
            }
        }

        // Navigation tabs
        div("tabs mb-4") {
            a(href = "/admin/overlay/profiles", classes = "tab active") { +"Profiles" }
            a(href = "/admin/overlay/bindings", classes = "tab") { +"Bindings" }
        }

        div("card") {
            if (profiles.isEmpty()) {
                emptyState(
                    icon = "🎨",
                    title = "No overlay profiles",
                    description = "Create your first overlay profile to add dynamic content like tickers, tables, and KPIs to your channels.",
                    actionHref = "/admin/overlay/profiles/new",
                    actionLabel = "Create Profile"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Widget Types" }
                            th { +"Bindings" }
                            th { +"Created" }
                            th { }
                        }
                    }
                    tbody {
                        profiles.forEach { profile ->
                            tr {
                                td {
                                    a(href = "/admin/overlay/profiles/${profile.id}", classes = "font-medium") {
                                        +profile.name
                                    }
                                }
                                td {
                                    if (profile.widgetTypes.isEmpty()) {
                                        span("text-muted") { +"No widgets" }
                                    } else {
                                        profile.widgetTypes.take(3).forEach { type ->
                                            span("badge badge-gray badge-plain mr-1") { +type }
                                        }
                                        if (profile.widgetTypes.size > 3) {
                                            span("text-muted text-sm") { +"+${profile.widgetTypes.size - 3}" }
                                        }
                                    }
                                }
                                td {
                                    if (profile.bindingsCount > 0) {
                                        span("badge badge-info badge-plain") { +"${profile.bindingsCount}" }
                                    } else {
                                        span("text-muted") { +"—" }
                                    }
                                }
                                td {
                                    span("text-muted") {
                                        +profile.createdAt.toString().substringBefore("T")
                                    }
                                }
                                td {
                                    div("table-actions") {
                                        a(href = "/admin/overlay/profiles/${profile.id}", classes = "btn btn-secondary btn-sm") {
                                            +"View"
                                        }
                                        a(href = "/admin/overlay/profiles/${profile.id}/edit", classes = "btn btn-ghost btn-sm") {
                                            +"Edit"
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
 * New overlay profile form with improved UX.
 */
fun HTML.newOverlayProfileView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "New Overlay Profile", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "New Overlay Profile",
            subtitle = "Create a template for dynamic content",
            backHref = "/admin/overlay/profiles",
            backLabel = "Back to Profiles"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/overlay/profiles", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Profile Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            required = true
                            placeholder = "e.g., Main Ticker, Queue Display"
                        }
                        small("form-helper") {
                            +"Choose a descriptive name for this overlay profile."
                        }
                    }

                    div("form-group") {
                        label { +"Template" }
                        input(type = InputType.hidden) {
                            id = "template"
                            name = "template"
                            value = OverlayTemplate.entries.first().name
                        }
                        div("template-gallery") {
                            OverlayTemplate.entries.forEach { template ->
                                val templateIcon = when (template) {
                                    OverlayTemplate.TICKER -> "📰"
                                    OverlayTemplate.KPI_TILES -> "📊"
                                    OverlayTemplate.QUEUE_TABLE -> "📋"
                                    OverlayTemplate.QR_CARD -> "📱"
                                }
                                div("template-card") {
                                    attributes["data-template"] = template.name
                                    if (template == OverlayTemplate.entries.first()) {
                                        classes = classes + " selected"
                                    }
                                    div("template-card-icon") { +templateIcon }
                                    div("template-card-name") { +template.displayName }
                                    div("template-card-desc") { +template.description }
                                }
                            }
                        }
                        small("form-helper") {
                            +"Select a template to start with."
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "definitionJson"
                            +"Definition JSON"
                        }
                        textArea(classes = "form-control code-editor") {
                            id = "definitionJson"
                            name = "definitionJson"
                            rows = "10"
                            placeholder = """{"widgets": []}"""
                        }
                        small("form-helper") {
                            +"Leave empty to use template defaults. Advanced users can customize the JSON directly."
                        }
                    }

                    // Template gallery selection script
                    script {
                        unsafe {
                            +"""
(function() {
    var cards = document.querySelectorAll('.template-card');
    var input = document.getElementById('template');
    cards.forEach(function(card) {
        card.addEventListener('click', function() {
            cards.forEach(function(c) { c.classList.remove('selected'); });
            card.classList.add('selected');
            input.value = card.dataset.template;
        });
    });
})();
"""
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create Profile"
                        }
                        a(href = "/admin/overlay/profiles", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay profile detail view with improved layout.
 */
fun HTML.overlayProfileDetailView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    bindings: List<OverlayBindingItem>
) {
    adminLayout(title = profile.name, userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = profile.name,
            backHref = "/admin/overlay/profiles",
            backLabel = "Back to Profiles"
        ) {
            a(href = "/admin/overlay/profiles/${profile.id}/edit", classes = "btn btn-secondary") {
                +"Edit"
            }
            form(action = "/admin/overlay/profiles/${profile.id}/delete", method = FormMethod.post, classes = "inline") {
                button(type = ButtonType.submit, classes = "btn btn-danger") {
                    attributes["onclick"] = "return confirm('Delete this profile? This will also remove all bindings.')"
                    +"Delete"
                }
            }
        }

        // Profile definition
        div("card mb-4") {
            div("card-header") {
                h3 { +"Definition" }
            }
            div("card-body") {
                pre("code-block code-editor") {
                    code {
                        +profile.definitionJson
                    }
                }
            }
        }

        // Bindings using this profile
        div("card") {
            div("card-header") {
                h3 { +"Channel Bindings" }
                if (bindings.isNotEmpty()) {
                    span("badge badge-gray badge-plain") { +"${bindings.size}" }
                }
            }
            if (bindings.isEmpty()) {
                div("card-body") {
                    emptyState(
                        icon = "🔗",
                        title = "No bindings yet",
                        description = "Connect this profile to a channel to start displaying dynamic content.",
                        actionHref = "/admin/overlay/bindings/new?profile=${profile.id}",
                        actionLabel = "Create Binding"
                    )
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Channel" }
                            th { +"Source" }
                            th { +"Status" }
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
                                    span("badge badge-gray badge-plain") {
                                        +binding.sourceType.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${bindingStatusBadge(binding.status)}") {
                                        +binding.status.name.lowercase()
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
 * Edit overlay profile view with improved UX.
 */
fun HTML.editOverlayProfileView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    error: String? = null
) {
    adminLayout(title = "Edit ${profile.name}", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Edit Profile",
            subtitle = profile.name,
            backHref = "/admin/overlay/profiles/${profile.id}",
            backLabel = "Back to Profile"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/overlay/profiles/${profile.id}", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Profile Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            required = true
                            value = profile.name
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "definitionJson"
                            +"Definition JSON"
                        }
                        textArea(classes = "form-control") {
                            id = "definitionJson"
                            name = "definitionJson"
                            rows = "15"
                            +profile.definitionJson
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Changes"
                        }
                        a(href = "/admin/overlay/profiles/${profile.id}", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

private fun bindingStatusBadge(status: com.playoutedge.domain.enums.BindingStatus): String = when (status) {
    com.playoutedge.domain.enums.BindingStatus.ACTIVE -> "success"
    com.playoutedge.domain.enums.BindingStatus.PAUSED -> "warning"
}
