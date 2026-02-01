package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.dangerItem
import com.playoutedge.server.views.dangerZone
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * New channel form view with improved UX.
 */
fun HTML.newChannelView(
    session: AdminClaims,
    error: String? = null
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
                        form(action = "/admin/channels/${channel.id}/delete", method = FormMethod.post) {
                            button(type = ButtonType.submit, classes = "btn btn-danger") {
                                attributes["onclick"] = "return confirm('Are you sure you want to delete this channel? This action cannot be undone.')"
                                +"Delete Channel"
                            }
                        }
                    }
                }
            }
        }
    }
}
