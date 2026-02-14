package com.playoutedge.server.views.auth

import com.playoutedge.server.views.authLayout
import kotlinx.html.*

fun HTML.forgotPasswordFormView(error: String? = null, success: String? = null) {
    authLayout("Forgot Password") {
        div("auth-container") {
            div("auth-card") {
                div("auth-logo") { +"P" }
                div("auth-header") {
                    h1 { +"Forgot Password" }
                    p { +"Enter your email address and we'll send you a reset link." }
                }
                if (error != null) {
                    div("alert alert-error") {
                        div("alert-content") { +error }
                    }
                }
                if (success != null) {
                    div("alert alert-success") {
                        div("alert-content") { +success }
                    }
                }
                form(action = "/admin/forgot-password", method = FormMethod.post, classes = "auth-form") {
                    div("form-group") {
                        label { htmlFor = "email"; +"Email Address" }
                        input(type = InputType.email, name = "email", classes = "form-control") {
                            id = "email"; placeholder = "you@example.com"; required = true; autoFocus = true
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-block btn-lg") {
                        +"Send Reset Link"
                    }
                }
                div("auth-footer") {
                    a(href = "/admin/login") { +"Back to Sign In" }
                }
            }
        }
    }
}

fun HTML.resetPasswordView(token: String, error: String? = null) {
    authLayout("Reset Password") {
        div("auth-container") {
            div("auth-card") {
                div("auth-logo") { +"P" }
                div("auth-header") {
                    h1 { +"Set New Password" }
                    p { +"Enter your new password below." }
                }
                if (error != null) {
                    div("alert alert-error") {
                        div("alert-content") { +error }
                    }
                }
                form(action = "/admin/reset-password", method = FormMethod.post, classes = "auth-form") {
                    hiddenInput(name = "token") { value = token }
                    div("form-group") {
                        label { htmlFor = "password"; +"New Password" }
                        input(type = InputType.password, name = "password", classes = "form-control") {
                            id = "password"; placeholder = "Enter new password"; required = true
                            minLength = "8"
                            attributes["oninput"] = "updatePasswordStrength(this.value)"
                        }
                        div("password-strength") {
                            id = "password-strength"
                            div("strength-meter") {
                                div("strength-bar") { id = "strength-bar" }
                            }
                            span("strength-text") { id = "strength-text" }
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "confirmPassword"; +"Confirm Password" }
                        input(type = InputType.password, name = "confirmPassword", classes = "form-control") {
                            id = "confirmPassword"; placeholder = "Confirm new password"; required = true
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary btn-block btn-lg") {
                        +"Reset Password"
                    }
                }
                div("auth-footer") {
                    a(href = "/admin/login") { +"Back to Sign In" }
                }
            }
        }
    }
}

fun HTML.resetPasswordSuccessView() {
    authLayout("Password Reset") {
        div("auth-container") {
            div("auth-card") {
                div("auth-logo") { +"P" }
                div("auth-header") {
                    h1 { +"Password Reset Successfully" }
                    p { +"Your password has been changed. You can now sign in." }
                }
                a(href = "/admin/login", classes = "btn btn-primary btn-block btn-lg") {
                    +"Sign In"
                }
            }
        }
    }
}
