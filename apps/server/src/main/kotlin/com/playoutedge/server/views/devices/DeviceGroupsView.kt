package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*
import java.util.UUID

data class DeviceGroupViewItem(
    val id: UUID,
    val name: String,
    val deviceCount: Int
)

fun HTML.deviceGroupsView(
    session: AdminClaims,
    groups: List<DeviceGroupViewItem>
) {
    adminLayout(
        title = "Device Groups",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/devices"
    ) {
        breadcrumb("Devices" to "/admin/devices", "Groups" to null)
        pageHeader(title = "Device Groups", subtitle = "Organize devices into groups") {
            button(classes = "btn btn-primary") {
                attributes["onclick"] = "document.getElementById('create-group-form').style.display='block'"
                +"Create Group"
            }
        }

        // Create group form (hidden by default)
        div("card") {
            id = "create-group-form"
            style = "display:none"
            div("card-body") {
                form(action = "/admin/devices/groups", method = FormMethod.post) {
                    div("form-row") {
                        div("form-group") {
                            label { htmlFor = "groupName"; +"Group Name" }
                            input(type = InputType.text, name = "name", classes = "form-control") {
                                id = "groupName"; required = true; placeholder = "Enter group name"
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") { +"Create" }
                    }
                }
            }
        }

        if (groups.isEmpty()) {
            emptyState("📁", "No Groups", "Create groups to organize your devices.")
        } else {
            div("card") {
                div("card-body") {
                    div("table-responsive") {
                        table("table") {
                            thead {
                                tr {
                                    th { +"Name" }
                                    th { +"Devices" }
                                    th { +"Actions" }
                                }
                            }
                            tbody {
                                groups.forEach { group ->
                                    tr {
                                        td {
                                            a(href = "/admin/devices?group=${group.id}", classes = "font-medium") {
                                                +group.name
                                            }
                                        }
                                        td {
                                            span("badge badge-gray badge-plain") { +"${group.deviceCount}" }
                                        }
                                        td {
                                            div("table-actions") {
                                                a(href = "/admin/devices?group=${group.id}", classes = "btn btn-secondary btn-sm") {
                                                    +"View Devices"
                                                }
                                                form(action = "/admin/devices/groups/${group.id}/delete", method = FormMethod.post, classes = "inline-form") {
                                                    button(type = ButtonType.submit, classes = "btn btn-danger btn-sm") { +"Delete" }
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
    }
}
