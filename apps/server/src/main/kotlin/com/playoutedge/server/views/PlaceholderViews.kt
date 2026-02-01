package com.playoutedge.server.views

import com.playoutedge.auth.AdminClaims
import kotlinx.html.*

/**
 * Placeholder view for features under construction with improved UX.
 */
fun HTML.placeholderView(
    session: AdminClaims,
    title: String,
    description: String,
    backLink: String = "/admin",
    currentPath: String = "/admin"
) {
    adminLayout(title = title, userName = session.displayName, currentPath = currentPath) {
        pageHeader(
            title = title,
            backHref = backLink,
            backLabel = "Back"
        )

        div("card") {
            div("card-body") {
                emptyState(
                    icon = "🚧",
                    title = "Coming Soon",
                    description = description
                )
            }
        }
    }
}

/**
 * Error page view for displaying error messages.
 */
fun HTML.errorView(
    session: AdminClaims?,
    title: String = "Error",
    message: String,
    statusCode: Int = 500,
    backLink: String = "/admin"
) {
    if (session != null) {
        adminLayout(title = title, userName = session.displayName, currentPath = "") {
            div("error-container") {
                div("error-code") { +"$statusCode" }
                h1 { +title }
                p("lead text-muted") { +message }
                div("error-actions mt-4") {
                    a(href = backLink, classes = "btn btn-primary") {
                        +"Go Back"
                    }
                    a(href = "/admin", classes = "btn btn-secondary") {
                        +"Go to Dashboard"
                    }
                }
            }
        }
    } else {
        // Unauthenticated error page
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            title { +title }
            link(rel = "stylesheet", href = "/static/styles.css")
        }
        body("auth-page") {
            div("auth-container") {
                div("auth-card") {
                    div("error-container") {
                        div("error-code") { +"$statusCode" }
                        h1 { +title }
                        p("lead text-muted") { +message }
                        div("error-actions mt-4") {
                            a(href = "/admin/login", classes = "btn btn-primary") {
                                +"Go to Login"
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Not found (404) page.
 */
fun HTML.notFoundView(
    session: AdminClaims?,
    path: String
) {
    errorView(
        session = session,
        title = "Page Not Found",
        message = "The page you're looking for doesn't exist or has been moved.",
        statusCode = 404,
        backLink = "/admin"
    )
}

/**
 * Forbidden (403) page.
 */
fun HTML.forbiddenView(
    session: AdminClaims?
) {
    errorView(
        session = session,
        title = "Access Denied",
        message = "You don't have permission to access this resource.",
        statusCode = 403,
        backLink = "/admin"
    )
}
