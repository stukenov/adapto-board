package com.playoutedge.server.views

import com.playoutedge.auth.AdminClaims
import kotlinx.html.*

/**
 * Extension property to get display name for admin session.
 * Returns "Admin" as default since AdminClaims doesn't store display name.
 */
val AdminClaims.displayName: String
    get() = "Admin"

/**
 * Navigation item definition.
 */
data class NavItem(
    val href: String,
    val label: String,
    val icon: String  // SVG path or emoji
)

/**
 * Main navigation items.
 */
val mainNavItems = listOf(
    NavItem("/admin", "Dashboard", "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"),
    NavItem("/admin/channels", "Channels", "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"),
    NavItem("/admin/devices", "Devices", "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"),
    NavItem("/admin/assets", "Assets", "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"),
    NavItem("/admin/overlay", "Overlay", "M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01")
)

/**
 * Base layout for admin pages.
 */
fun HTML.adminLayout(
    title: String,
    userName: String? = null,
    currentPath: String = "",
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%232563eb' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body {
        if (userName != null) {
            nav("admin-nav") {
                div("nav-brand") {
                    a(href = "/admin") {
                        div("nav-brand-icon") { +"P" }
                        +"Playout Edge"
                    }
                }
                div("nav-links") {
                    mainNavItems.forEach { item ->
                        val isActive = when {
                            item.href == "/admin" -> currentPath == "/admin" || currentPath.isEmpty()
                            else -> currentPath.startsWith(item.href)
                        }
                        a(href = item.href, classes = "nav-link${if (isActive) " active" else ""}") {
                            navIcon(item.icon)
                            +item.label
                        }
                    }
                }
                div("nav-user") {
                    userDropdown(userName)
                }
            }
        }
        main("admin-main") {
            content()
        }
    }
}

/**
 * SVG icon for navigation.
 */
fun FlowContent.navIcon(path: String) {
    span("nav-icon-wrapper") {
        unsafe {
            +"""<svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="$path"></path></svg>"""
        }
    }
}

/**
 * Minimal layout for login page.
 */
fun HTML.authLayout(
    title: String,
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%232563eb' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body("auth-body") {
        main("auth-main") {
            content()
        }
    }
}

/**
 * User dropdown menu with avatar.
 */
fun FlowContent.userDropdown(userName: String) {
    div("dropdown") {
        button(classes = "dropdown-toggle") {
            span("user-avatar") { +userName.take(1).uppercase() }
            +userName
            span("dropdown-caret") { +"▼" }
        }
        div("dropdown-menu") {
            a(href = "/admin/settings", classes = "dropdown-item") {
                +"Settings"
            }
            a(href = "/admin/reports", classes = "dropdown-item") {
                +"Reports"
            }
            div("dropdown-divider")
            form(action = "/admin/logout", method = FormMethod.post, classes = "dropdown-item") {
                button(type = ButtonType.submit, classes = "logout-btn") {
                    +"Logout"
                }
            }
        }
    }
}

/**
 * Breadcrumb navigation component.
 */
fun FlowContent.breadcrumb(vararg items: Pair<String, String?>) {
    nav("breadcrumb") {
        items.forEachIndexed { index, (label, href) ->
            if (index > 0) {
                span("breadcrumb-separator") { +"/" }
            }
            if (href != null && index < items.size - 1) {
                a(href = href) { +label }
            } else {
                span("breadcrumb-current") { +label }
            }
        }
    }
}

/**
 * Back link component.
 */
fun FlowContent.backLink(href: String, label: String) {
    a(href = href, classes = "back-link") {
        +"← $label"
    }
}

/**
 * Page header with title and optional actions.
 */
fun FlowContent.pageHeader(
    title: String,
    subtitle: String? = null,
    backHref: String? = null,
    backLabel: String? = null,
    actions: (DIV.() -> Unit)? = null
) {
    div("page-header") {
        div("page-header-content") {
            if (backHref != null && backLabel != null) {
                backLink(backHref, backLabel)
            }
            h1("page-title") { +title }
            if (subtitle != null) {
                p("page-subtitle") { +subtitle }
            }
        }
        if (actions != null) {
            div("header-actions") {
                actions()
            }
        }
    }
}

/**
 * Alert component with icon.
 */
fun FlowContent.alertBox(message: String, type: String = "error") {
    val icon = when (type) {
        "success" -> "M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        "warning" -> "M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        "info" -> "M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        else -> "M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
    }
    div("alert alert-$type") {
        unsafe {
            +"""<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="$icon"></path></svg>"""
        }
        div("alert-content") { +message }
    }
}

/**
 * Empty state component with icon.
 */
fun FlowContent.emptyState(
    icon: String,
    title: String,
    description: String,
    actionHref: String? = null,
    actionLabel: String? = null
) {
    div("empty-state") {
        div("empty-state-icon") { +icon }
        p("empty-state-title") { +title }
        p("empty-state-text") { +description }
        if (actionHref != null && actionLabel != null) {
            a(href = actionHref, classes = "btn btn-primary") {
                +actionLabel
            }
        }
    }
}

/**
 * Stat card component with icon.
 */
fun FlowContent.statCard(
    label: String,
    value: String,
    variant: String = "gray",
    icon: String? = null
) {
    div("stat-card") {
        if (icon != null) {
            div("stat-icon icon-$variant") { +icon }
        }
        span("stat-label") { +label }
        span("stat-value text-$variant") { +value }
    }
}

/**
 * Info list (definition list) component.
 */
fun FlowContent.infoList(items: List<Pair<String, String>>) {
    dl("info-list") {
        items.forEach { (label, value) ->
            dt { +label }
            dd { +value }
        }
    }
}

/**
 * Danger zone wrapper.
 */
fun FlowContent.dangerZone(content: DIV.() -> Unit) {
    div("danger-zone") {
        content()
    }
}

/**
 * Danger zone item.
 */
fun FlowContent.dangerItem(
    title: String,
    description: String,
    content: DIV.() -> Unit
) {
    div("danger-item") {
        div("danger-item-content") {
            strong { +title }
            p { +description }
        }
        div {
            content()
        }
    }
}
