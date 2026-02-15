package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.dangerItem
import com.playoutedge.server.views.dangerZone
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.components.hxButton
import com.playoutedge.server.views.components.BtnVariant
import kotlinx.html.*

/**
 * New channel form view with improved UX.
 */
fun HTML.newChannelView(
    session: AdminClaims,
    error: String? = null,
    groups: List<ChannelGroup> = emptyList()
) {
    adminLayout(title = "Create Channel", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Create Channel",
            subtitle = "Set up a new content playlist for your displays",
            backHref = "/admin/channels",
            backLabel = "Back to Channels"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/channels", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Channel Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            placeholder = "e.g., Lobby Display, Reception Screen"
                            required = true
                            autoFocus = true
                        }
                        small("form-helper") {
                            +"Choose a descriptive name to easily identify this channel."
                        }
                    }

                    // Schedule template selector
                    div("form-group") {
                        label { +"Schedule Template" }
                        div("template-options") {
                            style = "display:flex;gap:1rem;flex-wrap:wrap;"
                            label("day-checkbox") {
                                style = "display:flex;align-items:center;gap:0.5rem;padding:0.75rem 1rem;border:1px solid var(--border);border-radius:var(--radius);cursor:pointer;"
                                input(type = InputType.radio) {
                                    name = "template"
                                    value = "blank"
                                    checked = true
                                }
                                div {
                                    strong { +"Blank" }
                                    br
                                    small("text-muted") { +"Start with an empty schedule" }
                                }
                            }
                            label("day-checkbox") {
                                style = "display:flex;align-items:center;gap:0.5rem;padding:0.75rem 1rem;border:1px solid var(--border);border-radius:var(--radius);cursor:pointer;"
                                input(type = InputType.radio) {
                                    name = "template"
                                    value = "loop"
                                }
                                div {
                                    strong { +"24/7 Loop" }
                                    br
                                    small("text-muted") { +"All assets play continuously, all day" }
                                }
                            }
                            label("day-checkbox") {
                                style = "display:flex;align-items:center;gap:0.5rem;padding:0.75rem 1rem;border:1px solid var(--border);border-radius:var(--radius);cursor:pointer;"
                                input(type = InputType.radio) {
                                    name = "template"
                                    value = "timeslotted"
                                }
                                div {
                                    strong { +"Time-slotted" }
                                    br
                                    small("text-muted") { +"Different content at different times of day" }
                                }
                            }
                        }
                    }

                    // Channel group selector
                    if (groups.isNotEmpty()) {
                        div("form-group") {
                            label {
                                htmlFor = "groupId"
                                +"Channel Group"
                            }
                            select("form-control") {
                                id = "groupId"
                                name = "groupId"
                                option {
                                    value = ""
                                    +"No group"
                                }
                                groups.forEach { group ->
                                    option {
                                        value = group.id.toString()
                                        +group.name
                                    }
                                }
                            }
                            small("form-helper") {
                                +"Optionally assign this channel to a group for easier organization."
                            }
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create Channel"
                        }
                        a(href = "/admin/channels", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Edit channel form view with improved UX.
 */
fun HTML.editChannelView(
    session: AdminClaims,
    channel: ChannelDetail,
    error: String? = null
) {
    adminLayout(title = "Edit ${channel.name}", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Edit Channel",
            subtitle = channel.name,
            backHref = "/admin/channels/${channel.id}",
            backLabel = "Back to Channel"
        )

        // Edit form
        div("card mb-4") {
            div("card-header") {
                h3 { +"Channel Details" }
            }
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/channels/${channel.id}", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Channel Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            value = channel.name
                            placeholder = "Enter channel name"
                            required = true
                            autoFocus = true
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Changes"
                        }
                        a(href = "/admin/channels/${channel.id}", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }

        // Danger zone
        div("card") {
            div("card-header") {
                h3 { +"Danger Zone" }
            }
            div("card-body") {
                dangerZone {
                    dangerItem(
                        title = "Delete Channel",
                        description = "Permanently remove this channel and all its schedule items. Devices will be unassigned."
                    ) {
                        hxButton(
                            label = "Delete Channel",
                            variant = BtnVariant.DANGER,
                            hxPost = "/admin/channels/${channel.id}/delete",
                            target = "body",
                            swap = "none",
                            confirm = "Are you sure you want to delete this channel? This action cannot be undone."
                        )
                    }
                }
            }
        }
    }
}
