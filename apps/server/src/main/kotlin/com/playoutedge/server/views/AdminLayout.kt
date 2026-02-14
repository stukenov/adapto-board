package com.playoutedge.server.views

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.UserRole
import kotlinx.html.*

/**
 * Extension property to get display name for admin session.
 * Returns "Admin" as default since AdminClaims doesn't store display name.
 */
val AdminClaims.displayName: String
    get() = "Admin"

/**
 * Extension to get role display string.
 */
val AdminClaims.roleLabel: String
    get() = role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

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

data class NavGroup(val label: String, val items: List<NavItem>)

val navGroups = listOf(
    NavGroup("Core", listOf(
        NavItem("/admin", "Dashboard", "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"),
        NavItem("/admin/channels", "Channels", "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"),
        NavItem("/admin/devices", "Devices", "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"),
        NavItem("/admin/assets", "Assets", "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z")
    )),
    NavGroup("Broadcast", listOf(
        NavItem("/admin/channels#schedules", "Schedules", "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"),
        NavItem("/admin/overlay", "Overlays", "M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01")
    )),
    NavGroup("Monitor", listOf(
        NavItem("/admin/reports", "Reports", "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z")
    ))
)

/**
 * Base layout for admin pages.
 */
/**
 * Sortable table header helper.
 * Renders a <th> with data-sort attribute and active sort indicator.
 */
fun TR.sortableHeader(label: String, sortField: String, currentSort: String? = null, currentDir: String? = null) {
    th {
        attributes["data-sort"] = sortField
        if (currentSort == sortField) {
            classes = classes + setOf(if (currentDir == "asc") "sort-asc" else "sort-desc")
        }
        +label
    }
}

fun HTML.adminLayout(
    title: String,
    userName: String? = null,
    userEmail: String? = null,
    userRole: String? = null,
    currentPath: String = "",
    breadcrumbs: List<Pair<String, String?>>? = null,
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%23d97706' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body {
        // Skip to main content link (a11y)
        a(href = "#main-content", classes = "skip-link") { +"Skip to main content" }

        if (userName != null) {
            // Sidebar overlay for mobile
            div("sidebar-overlay") {
                attributes["onclick"] = "closeSidebar()"
            }

            // Sidebar
            aside("sidebar") {
                attributes["role"] = "navigation"
                attributes["aria-label"] = "Main navigation"

                a(href = "/admin", classes = "sidebar-brand") {
                    div("sidebar-brand-icon") { +"P" }
                    +"Playout Edge"
                    span("sidebar-brand-dot") {}
                }

                navGroups.forEach { group ->
                    div("nav-group") {
                        div("nav-group-label") { +group.label }
                        group.items.forEach { item ->
                            val isActive = when {
                                item.href == "/admin" -> currentPath == "/admin" || currentPath.isEmpty()
                                else -> currentPath.startsWith(item.href)
                            }
                            a(href = item.href, classes = "nav-link${if (isActive) " active" else ""}") {
                                if (isActive) attributes["aria-current"] = "page"
                                navIcon(item.icon)
                                +item.label
                            }
                        }
                    }
                }

                // Settings at bottom
                div("sidebar-footer") {
                    a(href = "/admin/settings", classes = "nav-link${if (currentPath.startsWith("/admin/settings")) " active" else ""}") {
                        navIcon("M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z")
                        +"Settings"
                    }

                    div("sidebar-user") {
                        span("sidebar-user-avatar") { +userName.take(1).uppercase() }
                        div("sidebar-user-info") {
                            div("sidebar-user-name") { +userName }
                            if (userRole != null) {
                                div("sidebar-user-role") { +userRole }
                            }
                        }
                    }
                }
            }

            // Main wrapper
            div("main-wrapper") {
                // Topbar
                header("topbar") {
                    div("topbar-left") {
                        button(classes = "sidebar-toggle") {
                            attributes["onclick"] = "toggleSidebar()"
                            +"☰"
                        }
                        // Breadcrumbs
                        if (breadcrumbs != null && breadcrumbs.isNotEmpty()) {
                            nav("breadcrumbs") {
                                attributes["aria-label"] = "Breadcrumb"
                                val allCrumbs = listOf(Pair("Home", "/admin")) + breadcrumbs
                                allCrumbs.forEachIndexed { index, (label, url) ->
                                    if (index > 0) {
                                        span("separator") { +"/" }
                                    }
                                    if (url != null && index < allCrumbs.size - 1) {
                                        a(href = url) { +label }
                                    } else {
                                        span { attributes["aria-current"] = "page"; +label }
                                    }
                                }
                            }
                        }
                    }
                    div("topbar-right") {
                        // Search
                        div("global-search") {
                            span("search-icon") {
                                unsafe { +"""<svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>""" }
                            }
                            input(type = InputType.search, classes = "form-control") {
                                placeholder = "Search..."
                                attributes["aria-label"] = "Global search"
                                attributes["onkeydown"] = "if(event.key==='Enter'){window.location='/admin/assets?q='+encodeURIComponent(this.value)}"
                            }
                        }
                        // Notification bell
                        div("notification-bell") {
                            unsafe { +"""<svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>""" }
                            span("badge-dot") {}
                        }
                        // Dark mode
                        button(classes = "dark-mode-toggle") {
                            attributes["onclick"] = "toggleDarkMode()"
                            attributes["aria-label"] = "Toggle dark mode"
                            +"◑"
                        }
                    }
                }

                main("admin-main") {
                    id = "main-content"
                    content()
                }
            }
        } else {
            main("admin-main") {
                id = "main-content"
                content()
            }
        }
        // Include admin utilities JS
        script(src = "/admin/static/admin.js") {}

        // Session expiry warning
        script {
            unsafe {
                +"""
                (function() {
                    var sessionExpires = null;
                    var warningShown = false;

                    // Check X-Session-Expires on HTMX responses
                    document.body.addEventListener('htmx:afterRequest', function(evt) {
                        var header = evt.detail.xhr && evt.detail.xhr.getResponseHeader('X-Session-Expires');
                        if (header) sessionExpires = parseInt(header, 10);
                    });

                    // Also check on fetch responses via interceptor
                    var origFetch = window.fetch;
                    window.fetch = function() {
                        return origFetch.apply(this, arguments).then(function(resp) {
                            var header = resp.headers.get('X-Session-Expires');
                            if (header) sessionExpires = parseInt(header, 10);
                            return resp;
                        });
                    };

                    setInterval(function() {
                        if (!sessionExpires) return;
                        var now = Math.floor(Date.now() / 1000);
                        var remaining = sessionExpires - now;

                        if (remaining <= 0) {
                            window.location.href = '/admin/login?error=expired';
                            return;
                        }

                        if (remaining <= 300 && !warningShown) {
                            warningShown = true;
                            var banner = document.createElement('div');
                            banner.id = 'session-expiry-banner';
                            banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:9999;background:#f59e0b;color:#000;text-align:center;padding:8px 16px;font-size:14px;';
                            banner.textContent = 'Your session will expire in less than 5 minutes. Please save your work.';
                            document.body.appendChild(banner);
                        }

                        if (remaining > 300) {
                            warningShown = false;
                            var existing = document.getElementById('session-expiry-banner');
                            if (existing) existing.remove();
                        }
                    }, 10000);
                })();
                """.trimIndent()
            }
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
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%23d97706' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body("auth-body") {
        main("auth-main") {
            content()
        }
        script(src = "/admin/static/admin.js") {}
    }
}

/**
 * User dropdown menu with avatar.
 */
fun FlowContent.userDropdown(userName: String, userEmail: String? = null, userRole: String? = null) {
    div("dropdown") {
        button(classes = "dropdown-toggle") {
            attributes["aria-haspopup"] = "true"
            span("user-avatar") { +userName.take(1).uppercase() }
            +userName
            span("dropdown-caret") { +"▼" }
        }
        div("dropdown-menu") {
            attributes["role"] = "menu"
            if (userEmail != null || userRole != null) {
                div("dropdown-user-info") {
                    if (userEmail != null) {
                        div("dropdown-user-email") { +userEmail }
                    }
                    if (userRole != null) {
                        div("dropdown-user-role") {
                            span("badge badge-primary badge-plain") { +userRole }
                        }
                    }
                }
                div("dropdown-divider")
            }
            a(href = "/admin/settings/profile", classes = "dropdown-item") {
                attributes["role"] = "menuitem"
                +"Profile"
            }
            a(href = "/admin/settings", classes = "dropdown-item") {
                attributes["role"] = "menuitem"
                +"Settings"
            }
            a(href = "/admin/reports", classes = "dropdown-item") {
                attributes["role"] = "menuitem"
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

/**
 * Tab definition for page tabs.
 */
data class TabDef(
    val label: String,
    val href: String,
    val count: Int? = null
)

/**
 * Page tabs navigation.
 */
fun FlowContent.pageTabs(tabs: List<TabDef>, currentPath: String) {
    div("page-tabs") {
        tabs.forEach { tab ->
            val isActive = currentPath == tab.href || currentPath.startsWith(tab.href + "/")
            a(href = tab.href, classes = "tab${if (isActive) " active" else ""}") {
                +tab.label
                if (tab.count != null) {
                    span("tab-count") { +"${tab.count}" }
                }
            }
        }
    }
}

/**
 * Two-column detail layout (main + sidebar).
 */
fun FlowContent.detailLayout(
    main: DIV.() -> Unit,
    sidebar: DIV.() -> Unit
) {
    div("layout-detail") {
        div("stack-md") { main() }
        div("stack-md") { sidebar() }
    }
}

/**
 * Section card with title and optional actions.
 */
fun FlowContent.sectionCard(
    title: String,
    actions: (DIV.() -> Unit)? = null,
    content: DIV.() -> Unit
) {
    div("card section-card") {
        div("card-header") {
            h3 { +title }
            if (actions != null) {
                div { actions() }
            }
        }
        div("card-body") {
            content()
        }
    }
}

/**
 * Two-column form grid.
 */
fun FlowContent.formGrid(content: DIV.() -> Unit) {
    div("form-grid-2") {
        content()
    }
}

/**
 * Pagination info model.
 */
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long,
    val basePath: String,
    val queryParams: String = ""
) {
    val hasPrevious: Boolean get() = currentPage > 1
    val hasNext: Boolean get() = currentPage < totalPages
}

/**
 * Pagination navigation component.
 */
fun FlowContent.paginationNav(pagination: PaginationInfo) {
    if (pagination.totalPages <= 1) return
    val sep = if (pagination.queryParams.isEmpty()) "?" else "${pagination.queryParams}&"
    nav("pagination-nav") {
        div("pagination-info") {
            +"Page ${pagination.currentPage} of ${pagination.totalPages} (${pagination.totalItems} items)"
        }
        div("pagination-buttons") {
            if (pagination.hasPrevious) {
                a(href = "${pagination.basePath}${sep}page=${pagination.currentPage - 1}", classes = "btn btn-secondary btn-sm") {
                    +"Previous"
                }
            }
            if (pagination.hasNext) {
                a(href = "${pagination.basePath}${sep}page=${pagination.currentPage + 1}", classes = "btn btn-secondary btn-sm") {
                    +"Next"
                }
            }
        }
    }
}
