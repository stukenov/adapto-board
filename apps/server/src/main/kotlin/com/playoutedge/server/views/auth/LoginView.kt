package com.playoutedge.server.views.auth

import com.playoutedge.server.views.authLayout
import kotlinx.html.*

/**
 * Login page view with improved UX.
 */
fun HTML.loginView(
    error: String? = null,
    email: String? = null,
    next: String? = null
) {
    authLayout("Login") {
        div("auth-container") {
            div("auth-card") {
                // Logo
                div("auth-logo") { +"P" }

                // Header
                div("auth-header") {
                    h1 { +"Welcome Back" }
                    p { +"Sign in to your Playout Edge account" }
                }

                // Error message
                if (error != null) {
                    div("alert alert-error") {
                        unsafe {
                            +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
                        }
                        div("alert-content") { +error }
                    }
                }

                // Login form
                form(action = "/admin/login", method = FormMethod.post, classes = "auth-form") {
                    if (next != null) {
                        hiddenInput(name = "next") { value = next }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "email"
                            +"Email Address"
                        }
                        input(type = InputType.email, name = "email", classes = "form-control") {
                            id = "email"
                            placeholder = "you@example.com"
                            required = true
                            autoFocus = true
                            attributes["autocomplete"] = "email"
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
                            placeholder = "Enter your password"
                            required = true
                            attributes["autocomplete"] = "current-password"
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-primary btn-block btn-lg") {
                        +"Sign In"
                    }
                }

                // Footer
                div("auth-footer") {
                    a(href = "/admin/forgot-password") { +"Forgot your password?" }
                }
            }
        }
    }
}

/**
 * Forgot password page view.
 */
fun HTML.forgotPasswordView(
    success: String? = null,
    error: String? = null,
    email: String? = null
) {
    authLayout("Reset Password") {
        div("auth-container") {
            div("auth-card") {
                // Logo
                div("auth-logo") { +"P" }

                // Header
                div("auth-header") {
                    h1 { +"Reset Password" }
                    p { +"Enter your email and we'll send you a reset link" }
                }

                // Success message
                if (success != null) {
                    div("alert alert-success") {
                        unsafe {
                            +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
                        }
                        div("alert-content") { +success }
                    }
                }

                // Error message
                if (error != null) {
                    div("alert alert-error") {
                        unsafe {
                            +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
                        }
                        div("alert-content") { +error }
                    }
                }

                // Form
                form(action = "/admin/forgot-password", method = FormMethod.post, classes = "auth-form") {
                    div("form-group") {
                        label {
                            htmlFor = "email"
                            +"Email Address"
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

                    button(type = ButtonType.submit, classes = "btn btn-primary btn-block") {
                        +"Send Reset Link"
                    }
                }

                // Footer
                div("auth-footer") {
                    a(href = "/admin/login") { +"Back to Sign In" }
                }
            }
        }
    }
}
