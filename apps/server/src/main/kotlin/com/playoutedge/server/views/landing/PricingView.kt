package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.pricingView() {
    publicLayout("Pricing", currentPath = "/pricing") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Simple, Transparent Pricing" }
                    p { +"Choose the plan that fits your signage network." }
                }
                div("pricing-grid") {
                    pricingCard(
                        name = "Basic",
                        price = "$29",
                        period = "/month",
                        description = "For small businesses getting started with digital signage.",
                        features = listOf(
                            "Up to 5 screens",
                            "1 GB storage",
                            "Basic scheduling",
                            "Email support"
                        ),
                        cta = "Get Started",
                        highlighted = false
                    )
                    pricingCard(
                        name = "Premium",
                        price = "$79",
                        period = "/month",
                        description = "For growing businesses that need more power and flexibility.",
                        features = listOf(
                            "Up to 25 screens",
                            "10 GB storage",
                            "Advanced scheduling",
                            "Overlay system",
                            "Priority support",
                            "Analytics & reports"
                        ),
                        cta = "Get Started",
                        highlighted = true
                    )
                    pricingCard(
                        name = "Enterprise",
                        price = "Custom",
                        period = "",
                        description = "For large organizations with custom requirements.",
                        features = listOf(
                            "Unlimited screens",
                            "Unlimited storage",
                            "Custom integrations",
                            "Dedicated support",
                            "SLA guarantee",
                            "On-premise option"
                        ),
                        cta = "Contact Us",
                        highlighted = false
                    )
                }
            }
        }
    }
}

private fun FlowContent.pricingCard(
    name: String,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    cta: String,
    highlighted: Boolean
) {
    div("pricing-card${if (highlighted) " pricing-card-highlighted" else ""}") {
        if (highlighted) {
            div("pricing-badge") { +"Most Popular" }
        }
        h3("pricing-name") { +name }
        div("pricing-price") {
            span("pricing-amount") { +price }
            if (period.isNotEmpty()) {
                span("pricing-period") { +period }
            }
        }
        p("pricing-description") { +description }
        ul("pricing-features") {
            features.forEach { feature ->
                li {
                    span("pricing-check") { +"✓" }
                    +feature
                }
            }
        }
        val href = if (name == "Enterprise") "/contact" else "/signup"
        a(href = href, classes = "btn ${if (highlighted) "btn-primary" else "btn-ghost"} btn-block") { +cta }
    }
}
