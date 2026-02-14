package com.playoutedge.server.views.settings

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*

fun HTML.profileView(
    session: AdminClaims,
    error: String? = null,
    success: String? = null,
    forceChange: Boolean = false
) {
    adminLayout(
        title = "Profile",
        userName = session.displayName,
        userEmail = null,
        userRole = session.roleLabel,
        currentPath = "/admin/settings"
    ) {
        pageHeader(
            title = if (forceChange) "Change Your Password" else "Profile Settings",
            subtitle = if (forceChange) "You must change your password before continuing." else "Manage your account settings"
        )

        if (error != null) { alertBox(error, "error") }
        if (success != null) { alertBox(success, "success") }

        if (!forceChange) {
            div("card mb-4") {
                div("card-header") { h3 { +"Avatar" } }
                div("card-body") {
                    div("d-flex align-items-center") {
                        style = "display: flex; align-items: center; gap: 1rem;"
                        div("avatar-circle") {
                            style = "width: 64px; height: 64px; border-radius: 50%; background: var(--primary-color, #4f46e5); color: white; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 600;"
                            val initials = session.displayName.split(" ")
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .ifEmpty { "?" }
                            +initials
                        }
                        div {
                            p("font-medium") { +session.displayName }
                            small("text-muted") { +"Avatar is generated from your initials. Custom avatar uploads are not yet supported." }
                        }
                    }
                }
            }
        }

        div("card") {
            div("card-header") { h3 { +"Change Password" } }
            div("card-body") {
                form(action = "/admin/settings/profile/change-password", method = FormMethod.post) {
                    if (!forceChange) {
                        div("form-group") {
                            label { htmlFor = "currentPassword"; +"Current Password" }
                            input(type = InputType.password, name = "currentPassword", classes = "form-control") {
                                id = "currentPassword"; required = true
                            }
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "newPassword"; +"New Password" }
                        input(type = InputType.password, name = "newPassword", classes = "form-control") {
                            id = "newPassword"; required = true; minLength = "8"
                            attributes["oninput"] = "updatePasswordStrength(this.value)"
                        }
                        small("form-helper") { +"Must be at least 8 characters, include uppercase, lowercase, and a number." }
                        div("password-strength") {
                            id = "password-strength"
                            div("strength-meter") {
                                div("strength-bar") { id = "strength-bar" }
                            }
                            span("strength-text") { id = "strength-text" }
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "confirmPassword"; +"Confirm New Password" }
                        input(type = InputType.password, name = "confirmPassword", classes = "form-control") {
                            id = "confirmPassword"; required = true
                        }
                    }
                    if (forceChange) {
                        hiddenInput(name = "force") { value = "true" }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Change Password"
                    }
                }
            }
        }
    }
}
