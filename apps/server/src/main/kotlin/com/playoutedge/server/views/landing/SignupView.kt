package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.signupView(
    error: String? = null,
    orgName: String? = null,
    email: String? = null,
    displayName: String? = null
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"Sign Up - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%23d97706' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body {
        div("signup-page") {
            // Left brand panel
            div("signup-brand") {
                div("signup-brand-content") {
                    a(href = "/", classes = "signup-brand-logo") {
                        div("nav-brand-icon") { +"P" }
                        span { +"Playout Edge" }
                    }

                    div("signup-brand-text") {
                        h1 { +"Power your screens with intelligent signage" }
                        p { +"Создайте рабочее пространство для пилота и получите доступ к демо/онбордингу команды." }
                    }

                    ul("signup-trust-list") {
                        li("signup-trust-item") {
                            unsafe {
                                +"""<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="10" fill="rgba(255,255,255,0.2)"/><path d="M6 10l3 3 5-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>"""
                            }
                            span { +"Paid POC/Pilot onboarding" }
                        }
                        li("signup-trust-item") {
                            unsafe {
                                +"""<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="10" fill="rgba(255,255,255,0.2)"/><path d="M6 10l3 3 5-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>"""
                            }
                            span { +"Pilot scope and KPI aligned upfront" }
                        }
                        li("signup-trust-item") {
                            unsafe {
                                +"""<svg width="20" height="20" viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="10" fill="rgba(255,255,255,0.2)"/><path d="M6 10l3 3 5-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>"""
                            }
                            span { +"Template demo pack in 1–2 days" }
                        }
                    }
                }

                div("signup-testimonial") {
                    p("signup-testimonial-quote") {
                        +"\u201CPlayout Edge transformed how we manage our 50+ screens across locations. The setup was incredibly simple.\u201D"
                    }
                    div("signup-testimonial-author") {
                        strong { +"Sarah Chen" }
                        span { +"Head of Operations, RetailCo" }
                    }
                }
            }

            // Right form panel
            div("signup-form-panel") {
                div("signup-form-container") {
                    // Step indicator
                    div("signup-steps") {
                        id = "stepIndicator"
                        div("signup-step-dots") {
                            span("signup-dot active") { id = "dot1" }
                            span("signup-dot") { id = "dot2" }
                        }
                        span("signup-step-text") {
                            id = "stepText"
                            +"Step 1 of 2"
                        }
                    }

                    h2("signup-form-title") {
                        id = "stepTitle"
                        +"Your Details"
                    }
                    p("signup-form-subtitle") {
                        id = "stepSubtitle"
                        +"Let\u2019s get you started with Playout Edge"
                    }

                    if (error != null) {
                        div("alert alert-error") {
                            unsafe {
                                +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
                            }
                            div("alert-content") { +error }
                        }
                    }

                    form(action = "/signup", method = FormMethod.post, classes = "signup-form") {
                        id = "signupForm"

                        // Step 1: Your Details
                        div("signup-step") {
                            id = "step1"
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
                            button(type = ButtonType.button, classes = "btn btn-primary btn-block btn-lg") {
                                id = "continueBtn"
                                +"Continue"
                                unsafe {
                                    +""" <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-left:4px"><path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>"""
                                }
                            }
                        }

                        // Step 2: Secure Your Account
                        div("signup-step signup-step-hidden") {
                            id = "step2"
                            div("form-group") {
                                label {
                                    htmlFor = "password"
                                    +"Password"
                                }
                                div("password-wrapper") {
                                    input(type = InputType.password, name = "password", classes = "form-control") {
                                        id = "password"
                                        placeholder = "At least 8 characters"
                                        required = true
                                        minLength = "8"
                                        attributes["autocomplete"] = "new-password"
                                    }
                                    button(type = ButtonType.button, classes = "password-toggle") {
                                        id = "togglePassword"
                                        attributes["aria-label"] = "Show password"
                                        attributes["tabindex"] = "-1"
                                        unsafe {
                                            +"""<svg class="eye-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>"""
                                            +"""<svg class="eye-off-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display:none"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>"""
                                        }
                                    }
                                }
                                div("password-strength") {
                                    id = "passwordStrength"
                                    span("password-strength-segment") {}
                                    span("password-strength-segment") {}
                                    span("password-strength-segment") {}
                                    span("password-strength-segment") {}
                                }
                                small("password-strength-label") {
                                    id = "strengthLabel"
                                }
                            }
                            div("form-group") {
                                label {
                                    htmlFor = "confirmPassword"
                                    +"Confirm Password"
                                }
                                div("password-wrapper") {
                                    input(type = InputType.password, name = "confirmPassword", classes = "form-control") {
                                        id = "confirmPassword"
                                        placeholder = "Repeat your password"
                                        required = true
                                        attributes["autocomplete"] = "new-password"
                                    }
                                    span("password-match") {
                                        id = "passwordMatch"
                                    }
                                }
                            }
                            div("signup-step2-actions") {
                                button(type = ButtonType.button, classes = "btn btn-ghost") {
                                    id = "backBtn"
                                    unsafe {
                                        +"""<svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right:4px"><path d="M10 3l-5 5 5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg> """
                                    }
                                    +"Back"
                                }
                                button(type = ButtonType.submit, classes = "btn btn-primary btn-lg signup-submit-btn") {
                                    +"Create Account"
                                }
                            }
                        }
                    }

                    div("signup-form-footer") {
                        p {
                            +"Already have an account? "
                            a(href = "/admin/login") { +"Sign in" }
                        }
                        p("signup-terms") {
                            +"By signing up, you agree to our "
                            a(href = "#") { +"Terms of Service" }
                            +" and "
                            a(href = "#") { +"Privacy Policy" }
                        }
                    }
                }
            }
        }

        // Inline script for step navigation, password strength, show/hide, match check
        unsafe {
            +"""
<script>
(function() {
    var step1 = document.getElementById('step1');
    var step2 = document.getElementById('step2');
    var continueBtn = document.getElementById('continueBtn');
    var backBtn = document.getElementById('backBtn');
    var dot1 = document.getElementById('dot1');
    var dot2 = document.getElementById('dot2');
    var stepText = document.getElementById('stepText');
    var stepTitle = document.getElementById('stepTitle');
    var stepSubtitle = document.getElementById('stepSubtitle');
    var passwordInput = document.getElementById('password');
    var confirmInput = document.getElementById('confirmPassword');
    var toggleBtn = document.getElementById('togglePassword');
    var strengthBar = document.getElementById('passwordStrength');
    var strengthLabel = document.getElementById('strengthLabel');
    var matchIndicator = document.getElementById('passwordMatch');

    function goToStep2() {
        var orgName = document.getElementById('orgName');
        var email = document.getElementById('email');
        var displayName = document.getElementById('displayName');
        if (!orgName.checkValidity() || !email.checkValidity() || !displayName.checkValidity()) {
            orgName.reportValidity() || email.reportValidity() || displayName.reportValidity();
            return;
        }
        step1.classList.add('signup-step-hidden');
        step1.classList.add('signup-step-left');
        step2.classList.remove('signup-step-hidden');
        dot1.classList.remove('active');
        dot1.classList.add('completed');
        dot2.classList.add('active');
        stepText.textContent = 'Step 2 of 2';
        stepTitle.textContent = 'Secure Your Account';
        stepSubtitle.textContent = 'Choose a strong password to protect your account';
        passwordInput.focus();
    }

    function goToStep1() {
        step2.classList.add('signup-step-hidden');
        step1.classList.remove('signup-step-hidden');
        step1.classList.remove('signup-step-left');
        dot2.classList.remove('active');
        dot1.classList.remove('completed');
        dot1.classList.add('active');
        stepText.textContent = 'Step 1 of 2';
        stepTitle.textContent = 'Your Details';
        stepSubtitle.textContent = "Let\u2019s get you started with Playout Edge";
    }

    continueBtn.addEventListener('click', goToStep2);
    backBtn.addEventListener('click', goToStep1);

    // Password show/hide
    toggleBtn.addEventListener('click', function() {
        var isPassword = passwordInput.type === 'password';
        passwordInput.type = isPassword ? 'text' : 'password';
        toggleBtn.querySelector('.eye-icon').style.display = isPassword ? 'none' : 'block';
        toggleBtn.querySelector('.eye-off-icon').style.display = isPassword ? 'block' : 'none';
        toggleBtn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
    });

    // Password strength
    function calcStrength(pw) {
        var score = 0;
        if (pw.length >= 12) score++;
        if (/[A-Z]/.test(pw)) score++;
        if (/[0-9]/.test(pw)) score++;
        if (/[^A-Za-z0-9]/.test(pw)) score++;
        return score;
    }

    var strengthLabels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
    var strengthColors = ['', '#ef4444', '#f97316', '#eab308', '#22c55e'];

    passwordInput.addEventListener('input', function() {
        var pw = passwordInput.value;
        var score = calcStrength(pw);
        var segments = strengthBar.querySelectorAll('.password-strength-segment');
        for (var i = 0; i < 4; i++) {
            if (pw.length === 0) {
                segments[i].style.background = '#e5e7eb';
            } else if (i < score) {
                segments[i].style.background = strengthColors[score];
            } else {
                segments[i].style.background = '#e5e7eb';
            }
        }
        strengthLabel.textContent = pw.length > 0 ? strengthLabels[score] : '';
        strengthLabel.style.color = strengthColors[score];
        updateMatch();
    });

    // Password match
    function updateMatch() {
        var pw = passwordInput.value;
        var cpw = confirmInput.value;
        if (cpw.length === 0) {
            matchIndicator.textContent = '';
            matchIndicator.className = 'password-match';
        } else if (pw === cpw) {
            matchIndicator.innerHTML = '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M4 8l3 3 5-5" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
            matchIndicator.className = 'password-match match-ok';
        } else {
            matchIndicator.innerHTML = '<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M4 4l8 8M12 4l-8 8" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/></svg>';
            matchIndicator.className = 'password-match match-no';
        }
    }
    confirmInput.addEventListener('input', updateMatch);
})();
</script>
"""
        }
    }
}
