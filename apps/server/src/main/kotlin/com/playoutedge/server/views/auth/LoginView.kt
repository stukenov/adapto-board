package com.playoutedge.server.views.auth

import com.playoutedge.server.views.authLayout
import kotlinx.html.*

/**
 * Login page view.
 */
fun HTML.loginView(
    error: String? = null,
    email: String? = null,
    next: String? = null
) {
    authLayout("Login") {
        div("auth-container") {
            div("auth-card") {
                div("auth-header") {
                    h1 { +"Playout Edge" }
                    p { +"Sign in to your account" }
                }

                if (error != null) {
                    div("alert alert-error") {
                        +error
                    }
                }

                form(action = "/admin/login", method = FormMethod.post, classes = "auth-form") {
                    if (next != null) {
                        hiddenInput(name = "next") { value = next }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "email"
                            +"Email"
                        }
                        input(type = InputType.email, name = "email", classes = "form-control") {
                            id = "email"
                            placeholder = "you@example.com"
                            required = true
                            autoFocus = true
                            if (email != null) {
                                value = email
                            }
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "password"
                            +"Password"
                        }
                        input(type = InputType.password, name = "password", classes = "form-control") {
                            id = "password"
                            placeholder = "••••••••"
                            required = true
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-primary btn-block") {
                        +"Sign In"
                    }
                }

                div("auth-footer") {
                    a(href = "/admin/forgot-password") { +"Forgot password?" }
                }
            }
        }
    }
}
