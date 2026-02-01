package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Overlay profiles list view.
 */
fun HTML.overlayProfilesListView(
    session: AdminClaims,
    profiles: List<OverlayProfileItem>
) {
    adminLayout(title = "Overlay Profiles", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Overlay Profiles" }
            a(href = "/admin/overlay/profiles/new", classes = "btn btn-primary") {
                +"New Profile"
            }
        }

        div("card") {
            if (profiles.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"🎨" }
                    p("empty-state-title") { +"No overlay profiles" }
                    p("empty-state-text") { +"Create your first overlay profile to add dynamic content to your channels." }
                    a(href = "/admin/overlay/profiles/new", classes = "btn btn-primary") {
                        +"Create Profile"
                    }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Widget Types" }
                            th { +"Bindings" }
                            th { +"Created" }
                            th { +"Actions" }
                        }
                    }
                    tbody {
                        profiles.forEach { profile ->
                            tr {
                                td {
                                    a(href = "/admin/overlay/profiles/${profile.id}") {
                                        +profile.name
                                    }
                                }
                                td {
                                    if (profile.widgetTypes.isEmpty()) {
                                        span("text-muted") { +"—" }
                                    } else {
                                        profile.widgetTypes.forEach { type ->
                                            span("badge badge-gray mr-1") { +type }
                                        }
                                    }
                                }
                                td {
                                    span("badge badge-info") { +"${profile.bindingsCount}" }
                                }
                                td { +profile.createdAt.toString().substringBefore("T") }
                                td {
                                    a(href = "/admin/overlay/profiles/${profile.id}/edit", classes = "btn btn-sm btn-secondary") {
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

/**
 * New overlay profile form.
 */
fun HTML.newOverlayProfileView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "New Overlay Profile", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/overlay/profiles", classes = "link") { +"← Back to Profiles" }
                h1("page-title") { +"New Overlay Profile" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-danger") { +error }
                }

                form(action = "/admin/overlay/profiles", method = FormMethod.post) {
                    div("form-group") {
                        label { +"Profile Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "name"
                            required = true
                            placeholder = "e.g., Main Ticker"
                        }
                    }

                    div("form-group") {
                        label { +"Template" }
                        select("form-control") {
                            name = "template"
                            OverlayTemplate.entries.forEach { template ->
                                option {
                                    value = template.name
                                    +"${template.displayName} - ${template.description}"
                                }
                            }
                        }
                    }

                    div("form-group") {
                        label { +"Definition JSON (optional)" }
                        textArea(classes = "form-control") {
                            name = "definitionJson"
                            rows = "10"
                            placeholder = """{"widgets": []}"""
                        }
                        small("text-muted") { +"Leave empty to use template defaults" }
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
 * Overlay profile detail view.
 */
fun HTML.overlayProfileDetailView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    bindings: List<OverlayBindingItem>
) {
    adminLayout(title = profile.name, userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/overlay/profiles", classes = "link") { +"← Back to Profiles" }
                h1("page-title") { +profile.name }
            }
            div("btn-group") {
                a(href = "/admin/overlay/profiles/${profile.id}/edit", classes = "btn btn-secondary") {
                    +"Edit"
                }
                form(action = "/admin/overlay/profiles/${profile.id}/delete", method = FormMethod.post, classes = "inline") {
                    button(type = ButtonType.submit, classes = "btn btn-danger") {
                        attributes["onclick"] = "return confirm('Delete this profile?')"
                        +"Delete"
                    }
                }
            }
        }

        // Profile definition
        div("card mb-4") {
            div("card-header") { +"Definition" }
            div("card-body") {
                pre("code-block") {
                    +profile.definitionJson
                }
            }
        }

        // Bindings using this profile
        div("card") {
            div("card-header") {
                +"Channel Bindings"
            }
            if (bindings.isEmpty()) {
                div("card-body") {
                    p("text-muted") { +"No channels are using this profile yet." }
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
                                    a(href = "/admin/channels/${binding.channelId}") {
                                        +binding.channelName
                                    }
                                }
                                td {
                                    span("badge badge-gray") {
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
 * Edit overlay profile view.
 */
fun HTML.editOverlayProfileView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    error: String? = null
) {
    adminLayout(title = "Edit ${profile.name}", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/overlay/profiles/${profile.id}", classes = "link") { +"← Back to Profile" }
                h1("page-title") { +"Edit Profile" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-danger") { +error }
                }

                form(action = "/admin/overlay/profiles/${profile.id}", method = FormMethod.post) {
                    div("form-group") {
                        label { +"Profile Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "name"
                            required = true
                            value = profile.name
                        }
                    }

                    div("form-group") {
                        label { +"Definition JSON" }
                        textArea(classes = "form-control") {
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
