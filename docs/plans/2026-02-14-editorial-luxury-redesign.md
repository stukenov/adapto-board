# Editorial Luxury Redesign — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Redesign Playout Edge frontend to Editorial Luxury aesthetic (Stripe-inspired) with sidebar navigation, DM Sans/DM Mono typography, warm stone/amber palette.

**Architecture:** CSS-first approach. Rewrite `styles.css` design tokens and all component styles. Modify `AdminLayout.kt` to switch from horizontal nav to sidebar layout. Modify `PublicLayout.kt` for updated branding. Update `admin.js` for sidebar toggle. No other Kotlin view files change.

**Tech Stack:** CSS custom properties, Google Fonts (DM Sans, DM Mono), kotlinx.html, vanilla JS

---

### Task 1: Add Google Fonts and Update Design Tokens

**Files:**
- Modify: `apps/server/src/main/resources/static/styles.css:1-110`

**Step 1: Replace design tokens section**

Replace lines 1-110 of `styles.css` with new tokens. Keep the reset. Replace `:root` block with:

```css
/* Google Fonts */
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,400;0,9..40,500;0,9..40,700;1,9..40,400&family=DM+Mono:wght@400;500&display=swap');

/* Reset */
*, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

:root {
    /* Colors - Primary */
    --primary: #1e293b;
    --primary-hover: #0f172a;
    --primary-light: #f1f5f9;

    /* Colors - Accent */
    --accent: #d97706;
    --accent-hover: #b45309;
    --accent-light: #fffbeb;
    --accent-border: #fde68a;

    /* Colors - Semantic */
    --danger: #dc2626;
    --danger-light: #fef2f2;
    --danger-border: #fecaca;
    --success: #166534;
    --success-light: #f0fdf4;
    --success-border: #bbf7d0;
    --warning: #d97706;
    --warning-light: #fffbeb;
    --warning-border: #fde68a;
    --info: #0891b2;
    --info-light: #ecfeff;
    --info-border: #a5f3fc;

    /* Colors - Neutrals (warm stone palette) */
    --gray-50: #fafaf9;
    --gray-100: #f5f5f4;
    --gray-200: #e7e5e4;
    --gray-300: #d6d3d1;
    --gray-400: #a8a29e;
    --gray-500: #78716c;
    --gray-600: #57534e;
    --gray-700: #44403c;
    --gray-800: #292524;
    --gray-900: #1c1917;

    /* Spacing */
    --spacing-xs: 4px;
    --spacing-sm: 8px;
    --spacing-md: 16px;
    --spacing-lg: 24px;
    --spacing-xl: 32px;
    --spacing-2xl: 48px;

    /* Typography */
    --font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-mono: 'DM Mono', 'SF Mono', 'Fira Code', monospace;

    --font-xs: 11px;
    --font-sm: 12px;
    --font-base: 14px;
    --font-lg: 16px;
    --font-xl: 18px;
    --font-2xl: 24px;
    --font-3xl: 30px;
    --font-4xl: 36px;
    --font-5xl: 48px;
    --font-hero: 56px;

    --font-weight-normal: 400;
    --font-weight-medium: 500;
    --font-weight-semibold: 600;
    --font-weight-bold: 700;

    --line-height-tight: 1.1;
    --line-height-snug: 1.25;
    --line-height-normal: 1.5;
    --line-height-relaxed: 1.75;

    --letter-spacing-tight: -0.02em;
    --letter-spacing-wide: 0.05em;
    --letter-spacing-wider: 0.06em;

    /* Borders */
    --radius-sm: 6px;
    --radius: 10px;
    --radius-lg: 14px;
    --radius-full: 9999px;

    /* Shadows (warm) */
    --shadow-sm: 0 1px 2px rgba(28,25,23,0.04);
    --shadow: 0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04);
    --shadow-md: 0 4px 12px rgba(28,25,23,0.08);
    --shadow-lg: 0 12px 24px rgba(28,25,23,0.1);
    --shadow-xl: 0 20px 40px rgba(28,25,23,0.12);

    /* Transitions */
    --transition-fast: 0.15s ease;
    --transition-normal: 0.2s ease;
    --transition-slow: 0.35s ease;

    /* Z-index layers */
    --z-dropdown: 100;
    --z-sticky: 200;
    --z-sidebar: 250;
    --z-modal: 300;
    --z-toast: 400;

    /* Layout */
    --sidebar-width: 260px;
    --topbar-height: 48px;
}
```

**Step 2: Update body base styles**

Replace the body rule:
```css
body {
    font-family: var(--font-family);
    font-size: var(--font-base);
    line-height: var(--line-height-normal);
    color: var(--gray-900);
    background: var(--gray-50);
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
}
```

**Step 3: Verify build compiles**

Run: `cd /Users/sakentukenov/adapto-board/.claude/worktrees/distracted-diffie && ./gradlew :apps:server:build -x test`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add apps/server/src/main/resources/static/styles.css
git commit -m "feat: update design tokens to Editorial Luxury palette with DM Sans/Mono"
```

---

### Task 2: Rewrite Sidebar Navigation CSS

**Files:**
- Modify: `apps/server/src/main/resources/static/styles.css` (NAVIGATION section, ~lines 121-200)

**Step 1: Replace navigation CSS**

Remove old `.admin-nav`, `.nav-brand`, `.nav-links`, `.nav-link`, `.nav-user` styles. Replace with sidebar + topbar:

```css
/* ========================================
   SIDEBAR NAVIGATION
   ======================================== */
.sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: var(--sidebar-width);
    background: var(--gray-900);
    color: var(--gray-400);
    display: flex;
    flex-direction: column;
    z-index: var(--z-sidebar);
    overflow-y: auto;
    transition: transform var(--transition-slow);
}

.sidebar-brand {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-lg);
    padding-bottom: var(--spacing-xl);
    text-decoration: none;
    color: #fafaf9;
    font-size: var(--font-xl);
    font-weight: var(--font-weight-bold);
    letter-spacing: var(--letter-spacing-tight);
}

.sidebar-brand-icon {
    width: 32px;
    height: 32px;
    background: var(--accent);
    border-radius: var(--radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: var(--font-weight-bold);
    font-size: var(--font-base);
}

.sidebar-brand-dot {
    width: 6px;
    height: 6px;
    background: var(--accent);
    border-radius: var(--radius-full);
    display: inline-block;
    margin-left: 2px;
}

.nav-group {
    padding: 0 var(--spacing-md);
    margin-bottom: var(--spacing-lg);
}

.nav-group-label {
    font-size: var(--font-xs);
    font-weight: var(--font-weight-medium);
    text-transform: uppercase;
    letter-spacing: var(--letter-spacing-wider);
    color: var(--gray-600);
    padding: var(--spacing-sm) var(--spacing-sm);
    margin-bottom: var(--spacing-xs);
}

.sidebar .nav-link {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: 8px 12px;
    border-radius: var(--radius-sm);
    color: var(--gray-400);
    text-decoration: none;
    font-size: var(--font-sm);
    font-weight: var(--font-weight-medium);
    transition: all var(--transition-fast);
    border-left: 3px solid transparent;
    margin-bottom: 2px;
}

.sidebar .nav-link:hover {
    color: #fafaf9;
    background: rgba(255,255,255,0.05);
}

.sidebar .nav-link.active {
    color: #fbbf24;
    background: rgba(217,119,6,0.12);
    border-left-color: var(--accent);
}

.sidebar .nav-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    opacity: 0.7;
}

.sidebar .nav-link.active .nav-icon {
    opacity: 1;
}

.sidebar-footer {
    margin-top: auto;
    padding: var(--spacing-md);
    border-top: 1px solid rgba(255,255,255,0.08);
}

.sidebar-user {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-sm);
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: background var(--transition-fast);
}

.sidebar-user:hover {
    background: rgba(255,255,255,0.05);
}

.sidebar-user-avatar {
    width: 32px;
    height: 32px;
    border-radius: var(--radius-full);
    background: var(--accent);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: var(--font-xs);
    font-weight: var(--font-weight-bold);
    flex-shrink: 0;
}

.sidebar-user-info {
    flex: 1;
    min-width: 0;
}

.sidebar-user-name {
    font-size: var(--font-sm);
    font-weight: var(--font-weight-medium);
    color: #fafaf9;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.sidebar-user-role {
    font-size: var(--font-xs);
    color: var(--gray-500);
}

/* ========================================
   TOPBAR
   ======================================== */
.topbar {
    height: var(--topbar-height);
    background: white;
    border-bottom: 1px solid var(--gray-200);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 var(--spacing-lg);
    position: sticky;
    top: 0;
    z-index: var(--z-sticky);
}

.topbar-left {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
}

.topbar-right {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
}

/* ========================================
   MAIN WRAPPER (sidebar offset)
   ======================================== */
.main-wrapper {
    margin-left: var(--sidebar-width);
    min-height: 100vh;
}

.admin-main {
    padding: var(--spacing-xl);
    max-width: 1200px;
}

/* Mobile sidebar */
.sidebar-overlay {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.5);
    z-index: calc(var(--z-sidebar) - 1);
}

.sidebar-toggle {
    display: none;
    background: none;
    border: none;
    color: var(--gray-600);
    cursor: pointer;
    padding: var(--spacing-xs);
}

@media (max-width: 768px) {
    .sidebar {
        transform: translateX(-100%);
    }
    .sidebar.open {
        transform: translateX(0);
    }
    .sidebar-overlay.open {
        display: block;
    }
    .sidebar-toggle {
        display: flex;
    }
    .main-wrapper {
        margin-left: 0;
    }
}
```

**Step 2: Commit**

```bash
git add apps/server/src/main/resources/static/styles.css
git commit -m "feat: add sidebar navigation and topbar CSS"
```

---

### Task 3: Update AdminLayout.kt for Sidebar Structure

**Files:**
- Modify: `apps/server/src/main/kotlin/com/playoutedge/server/views/AdminLayout.kt`

**Step 1: Add nav group definitions**

After `mainNavItems`, add:

```kotlin
data class NavGroup(val label: String, val items: List<NavItem>)

val navGroups = listOf(
    NavGroup("Core", listOf(
        NavItem("/admin", "Dashboard", "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"),
        NavItem("/admin/channels", "Channels", "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"),
        NavItem("/admin/devices", "Devices", "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"),
        NavItem("/admin/assets", "Assets", "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z")
    )),
    NavGroup("Broadcast", listOf(
        NavItem("/admin/channels", "Schedules", "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"),
        NavItem("/admin/overlay", "Overlays", "M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01")
    )),
    NavGroup("Monitor", listOf(
        NavItem("/admin/reports", "Reports", "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z")
    ))
)
```

**Step 2: Replace nav rendering in `adminLayout` function**

Replace the `nav("admin-nav") { ... }` block (and everything up to `main("admin-main")`) with sidebar + topbar + main-wrapper structure:

```kotlin
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
```

**Step 3: Remove old breadcrumb rendering from inside `main("admin-main")`**

The breadcrumbs are now in the topbar, so remove the duplicate block.

**Step 4: Remove old `userDropdown` call from nav** (it's now in sidebar-footer)

**Step 5: Build and verify**

Run: `./gradlew :apps:server:build -x test`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add apps/server/src/main/kotlin/com/playoutedge/server/views/AdminLayout.kt
git commit -m "feat: convert admin nav to sidebar layout with grouped navigation"
```

---

### Task 4: Update admin.js for Sidebar Toggle

**Files:**
- Modify: `apps/server/src/main/resources/static/admin.js`

**Step 1: Replace hamburger code with sidebar toggle**

Find the hamburger menu section and replace with:

```javascript
// ========================================
// SIDEBAR TOGGLE (mobile)
// ========================================
function toggleSidebar() {
    var sidebar = document.querySelector('.sidebar');
    var overlay = document.querySelector('.sidebar-overlay');
    if (sidebar) {
        sidebar.classList.toggle('open');
        if (overlay) overlay.classList.toggle('open');
    }
}
function closeSidebar() {
    var sidebar = document.querySelector('.sidebar');
    var overlay = document.querySelector('.sidebar-overlay');
    if (sidebar) sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('open');
}
window.toggleSidebar = toggleSidebar;
window.closeSidebar = closeSidebar;
```

**Step 2: Commit**

```bash
git add apps/server/src/main/resources/static/admin.js
git commit -m "feat: add sidebar toggle functions for mobile"
```

---

### Task 5: Rewrite Component Styles — Cards, Buttons, Forms

**Files:**
- Modify: `apps/server/src/main/resources/static/styles.css` (component sections)

**Step 1: Update card styles**

```css
.card {
    background: white;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius);
    padding: var(--spacing-lg);
    box-shadow: var(--shadow-sm);
    transition: box-shadow var(--transition-normal);
}
.card:hover {
    box-shadow: var(--shadow-md);
}
```

**Step 2: Update stat cards**

```css
.stat-card {
    background: white;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius);
    padding: var(--spacing-lg);
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xs);
}
.stat-icon {
    width: 40px;
    height: 40px;
    border-radius: var(--radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    margin-bottom: var(--spacing-sm);
    background: var(--accent-light);
    color: var(--accent);
}
.stat-label {
    font-size: var(--font-xs);
    font-weight: var(--font-weight-medium);
    text-transform: uppercase;
    letter-spacing: var(--letter-spacing-wide);
    color: var(--gray-500);
}
.stat-value {
    font-size: var(--font-2xl);
    font-weight: var(--font-weight-bold);
    color: var(--gray-900);
    letter-spacing: var(--letter-spacing-tight);
}
```

**Step 3: Update buttons**

```css
.btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--spacing-sm);
    height: 38px;
    padding: 0 var(--spacing-md);
    font-family: var(--font-family);
    font-size: var(--font-sm);
    font-weight: var(--font-weight-medium);
    border-radius: var(--radius);
    border: 1px solid transparent;
    cursor: pointer;
    transition: all var(--transition-fast);
    text-decoration: none;
    white-space: nowrap;
}
.btn-primary {
    background: var(--primary);
    color: white;
    border-color: var(--primary);
}
.btn-primary:hover {
    background: var(--primary-hover);
    border-color: var(--primary-hover);
}
.btn-accent {
    background: var(--accent);
    color: white;
    border-color: var(--accent);
}
.btn-accent:hover {
    background: var(--accent-hover);
    border-color: var(--accent-hover);
}
.btn-secondary {
    background: transparent;
    color: var(--gray-700);
    border-color: var(--gray-300);
}
.btn-secondary:hover {
    background: var(--gray-100);
}
.btn-danger {
    background: var(--danger);
    color: white;
    border-color: var(--danger);
}
.btn-ghost {
    background: transparent;
    color: var(--gray-600);
    border-color: transparent;
}
.btn-ghost:hover {
    background: var(--gray-100);
}
```

**Step 4: Update form controls**

```css
.form-control {
    width: 100%;
    height: 38px;
    padding: 0 var(--spacing-md);
    font-family: var(--font-family);
    font-size: var(--font-base);
    color: var(--gray-900);
    background: white;
    border: 1px solid var(--gray-300);
    border-radius: var(--radius);
    transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.form-control:focus {
    outline: none;
    border-color: var(--accent);
    box-shadow: 0 0 0 3px rgba(217,119,6,0.1);
}
.form-label {
    display: block;
    font-size: var(--font-xs);
    font-weight: var(--font-weight-medium);
    text-transform: uppercase;
    letter-spacing: var(--letter-spacing-wide);
    color: var(--gray-500);
    margin-bottom: 6px;
}
```

**Step 5: Update table styles**

```css
.table {
    width: 100%;
    border-collapse: collapse;
}
.table th {
    font-size: var(--font-xs);
    font-weight: var(--font-weight-medium);
    text-transform: uppercase;
    letter-spacing: var(--letter-spacing-wider);
    color: var(--gray-400);
    text-align: left;
    padding: var(--spacing-sm) var(--spacing-md);
    border-bottom: 1px solid var(--gray-200);
}
.table td {
    padding: var(--spacing-sm) var(--spacing-md);
    border-bottom: 1px solid var(--gray-100);
    font-size: var(--font-base);
}
.table tbody tr:nth-child(even) {
    background: var(--gray-50);
}
.table tbody tr:hover {
    background: var(--accent-light);
}
.table .mono {
    font-family: var(--font-mono);
    font-size: var(--font-sm);
}
```

**Step 6: Update badge styles**

```css
.badge {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    font-size: var(--font-xs);
    font-weight: var(--font-weight-medium);
    border-radius: var(--radius-full);
    white-space: nowrap;
}
.badge-success { background: var(--success-light); color: var(--success); }
.badge-danger { background: var(--danger-light); color: var(--danger); }
.badge-warning { background: var(--warning-light); color: var(--warning); }
.badge-info { background: var(--info-light); color: var(--info); }
.badge-primary { background: var(--primary-light); color: var(--primary); }
```

**Step 7: Commit**

```bash
git add apps/server/src/main/resources/static/styles.css
git commit -m "feat: update cards, buttons, forms, tables, badges to Editorial Luxury style"
```

---

### Task 6: Rewrite Remaining CSS Sections

**Files:**
- Modify: `apps/server/src/main/resources/static/styles.css`

**Step 1: Update all remaining sections**

Update these sections keeping the same class names but applying the new aesthetic:
- Page header: larger title (font-2xl, font-weight-bold), subtitle in gray-500
- Alerts: left amber border for info/warning, softer backgrounds
- Empty state: larger icon, refined spacing
- Modals: larger radius, refined shadows, amber accent for confirm button
- Toasts: amber left border for info, warm shadows
- Pagination: refined buttons
- Auth pages: centered card on warm gradient background
- Dark mode: update `.dark` variables to match new warm palette
- Loading skeletons: update shimmer animation colors
- Dropdowns: match new radius and shadow tokens

**Step 2: Build and verify**

Run: `./gradlew :apps:server:build -x test`

**Step 3: Commit**

```bash
git add apps/server/src/main/resources/static/styles.css
git commit -m "feat: complete Editorial Luxury CSS — modals, toasts, pagination, auth, dark mode"
```

---

### Task 7: Update Landing Page Styles

**Files:**
- Modify: `apps/server/src/main/resources/static/styles.css` (public/landing sections)

**Step 1: Update public nav**

```css
.public-nav {
    padding: var(--spacing-md) var(--spacing-2xl);
    background: white;
    border-bottom: 1px solid var(--gray-200);
}
.public-nav-inner {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
```

**Step 2: Add hero section styles**

```css
.hero {
    padding: 120px var(--spacing-2xl) 80px;
    text-align: center;
    background: linear-gradient(180deg, #fafaf9 0%, #f5f5f4 100%);
    position: relative;
}
.hero::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.03'/%3E%3C/svg%3E");
    pointer-events: none;
}
.hero h1 {
    font-size: var(--font-hero);
    font-weight: var(--font-weight-bold);
    line-height: var(--line-height-tight);
    letter-spacing: var(--letter-spacing-tight);
    color: var(--gray-900);
    max-width: 700px;
    margin: 0 auto var(--spacing-lg);
}
.hero p {
    font-size: 20px;
    color: var(--gray-500);
    max-width: 560px;
    margin: 0 auto var(--spacing-xl);
    line-height: var(--line-height-relaxed);
}
.hero .btn {
    height: 48px;
    padding: 0 var(--spacing-xl);
    font-size: var(--font-lg);
}
```

**Step 3: Features grid**

```css
.features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-xl);
    max-width: 1200px;
    margin: 0 auto;
    padding: 80px var(--spacing-2xl);
}
.feature-card {
    text-align: center;
    padding: var(--spacing-xl);
}
.feature-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius);
    background: var(--accent-light);
    color: var(--accent);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto var(--spacing-md);
    font-size: 24px;
}
.feature-card h3 {
    font-size: var(--font-xl);
    font-weight: var(--font-weight-bold);
    margin-bottom: var(--spacing-sm);
}
.feature-card p {
    color: var(--gray-500);
    line-height: var(--line-height-relaxed);
}
```

**Step 4: Pricing cards**

```css
.pricing-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-xl);
    max-width: 1000px;
    margin: 0 auto;
    padding: 80px var(--spacing-2xl);
}
.pricing-card {
    background: white;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius-lg);
    padding: var(--spacing-xl);
    text-align: center;
}
.pricing-card.featured {
    border-top: 3px solid var(--accent);
    transform: scale(1.02);
    box-shadow: var(--shadow-lg);
}
.pricing-price {
    font-family: var(--font-mono);
    font-size: var(--font-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--gray-900);
}
```

**Step 5: Footer**

```css
.public-footer {
    background: var(--gray-900);
    color: var(--gray-400);
    padding: 60px var(--spacing-2xl) var(--spacing-xl);
}
.public-footer a {
    color: var(--gray-400);
    transition: color var(--transition-fast);
}
.public-footer a:hover {
    color: var(--accent);
}
```

**Step 6: Scroll animations**

```css
@keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
.fade-in {
    opacity: 0;
    animation: fadeInUp 0.6s ease forwards;
}
.fade-in-1 { animation-delay: 0.1s; }
.fade-in-2 { animation-delay: 0.2s; }
.fade-in-3 { animation-delay: 0.3s; }
```

**Step 7: Commit**

```bash
git add apps/server/src/main/resources/static/styles.css
git commit -m "feat: add Editorial Luxury landing page styles — hero, features, pricing, footer"
```

---

### Task 8: Update Favicon and Brand Colors

**Files:**
- Modify: `apps/server/src/main/kotlin/com/playoutedge/server/views/AdminLayout.kt`
- Modify: `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt`

**Step 1: Update favicon SVG data URI in both files**

Replace `fill='%232563eb'` with `fill='%23d97706'` (amber accent) in both `AdminLayout.kt` and `PublicLayout.kt` favicon link tags.

**Step 2: Commit**

```bash
git add apps/server/src/main/kotlin/com/playoutedge/server/views/AdminLayout.kt apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt
git commit -m "feat: update favicon to amber accent color"
```

---

### Task 9: Final Build Verification

**Step 1: Full build**

Run: `./gradlew :apps:server:build -x test`
Expected: BUILD SUCCESSFUL

**Step 2: Review CSS file size**

Run: `wc -l apps/server/src/main/resources/static/styles.css`
Expected: Similar to original (~4700 lines) or slightly more

**Step 3: Commit any remaining changes**

```bash
git status
```

If clean, no commit needed. If anything remaining, commit.
