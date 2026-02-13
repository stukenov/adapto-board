package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.faqView() {
    publicLayout("FAQ", currentPath = "/faq") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Frequently Asked Questions" }
                    p { +"Find answers to common questions about Playout Edge." }
                }
                div("faq-list") {
                    faqItem(
                        "What is Playout Edge?",
                        "Playout Edge is a cloud-based digital signage platform that lets you manage screens, schedule content, and engage your audience from anywhere."
                    )
                    faqItem(
                        "How do I get started?",
                        "Simply create an account, set up your first channel, upload your content, and connect your devices. Our onboarding wizard will guide you through each step."
                    )
                    faqItem(
                        "What devices are supported?",
                        "Playout Edge works with Android-based media players, smart TVs, and any device with a modern web browser. We provide a dedicated Android player app for the best experience."
                    )
                    faqItem(
                        "Can I try it for free?",
                        "Yes! You can sign up and start using Playout Edge immediately. The Basic plan includes a free trial period so you can evaluate the platform."
                    )
                    faqItem(
                        "How does billing work?",
                        "Plans are billed monthly. You can upgrade or downgrade at any time. Enterprise customers can arrange annual billing with custom terms."
                    )
                    faqItem(
                        "Is my content secure?",
                        "Absolutely. All data is encrypted in transit and at rest. Each tenant's data is fully isolated, and we follow industry best practices for security."
                    )
                    faqItem(
                        "Can I cancel anytime?",
                        "Yes, you can cancel your subscription at any time. Your data will be retained for 30 days after cancellation in case you change your mind."
                    )
                }
            }
        }
    }
}

private fun FlowContent.faqItem(question: String, answer: String) {
    details("faq-item") {
        summary { +question }
        p { +answer }
    }
}
