package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.signupView(
    error: String? = null,
    orgName: String? = null,
    email: String? = null,
    displayName: String? = null
) {
    publicLayout("Sign Up", currentPath = "/signup") {
        section("section") {
            div("section-inner") {
                div("auth-container") {
                    div("auth-card") {
                        div("auth-header") {
                            h1 { +"Create Your Account" }
                            p { +"Start managing your digital signage in minutes." }
                        }

                        if (error != null) {
                            div("alert alert-error") {
                                unsafe {
                                    +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
                                }
                                div("alert-content") { +error }
                            }
                        }

                        form(action = "/signup", method = FormMethod.post, classes = "auth-form") {
                            div("form-group") {
                                label {
                                    htmlFor = "orgName"
                                    +"Organization Name"
                                }
                                input(type = InputType.text, name = "orgName", classes = "form-control") {
                                    id = "orgName"
                                    placeholder = "Your company or team name"
                                    required = true
                                    minLength = "3"
                                    maxLength = "100"
                                    if (orgName != null) value = orgName
                                }
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
                                    attributes["autocomplete"] = "email"
                                    if (email != null) value = email
                                }
                            }

                            div("form-group") {
                                label {
                                    htmlFor = "displayName"
                                    +"Your Name"
                                }
                                input(type = InputType.text, name = "displayName", classes = "form-control") {
                                    id = "displayName"
                                    placeholder = "John Doe"
                                    required = true
                                    if (displayName != null) value = displayName
                                }
                            }

                            div("form-group") {
                                label {
                                    htmlFor = "password"
                                    +"Password"
                                }
                                input(type = InputType.password, name = "password", classes = "form-control") {
                                    id = "password"
                                    placeholder = "At least 8 characters"
                                    required = true
                                    minLength = "8"
                                    attributes["autocomplete"] = "new-password"
                                }
                                small("form-hint") { +"Must contain at least one letter and one digit." }
                            }

                            div("form-group") {
                                label {
                                    htmlFor = "confirmPassword"
                                    +"Confirm Password"
                                }
                                input(type = InputType.password, name = "confirmPassword", classes = "form-control") {
                                    id = "confirmPassword"
                                    placeholder = "Repeat your password"
                                    required = true
                                    attributes["autocomplete"] = "new-password"
                                }
                            }

                            button(type = ButtonType.submit, classes = "btn btn-primary btn-block btn-lg") {
                                +"Create Account"
                            }
                        }

                        div("auth-footer") {
                            p {
                                +"Already have an account? "
                                a(href = "/admin/login") { +"Sign in" }
                            }
                        }
                    }
                }
            }
        }
    }
}
