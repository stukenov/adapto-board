package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*

/**
 * Overlay profiles list view with improved UX.
 */
fun HTML.overlayProfilesListView(
    session: AdminClaims,
    profiles: List<OverlayProfileItem>,
    pagination: PaginationInfo? = null
) {
    adminLayout(title = "Overlay Profiles", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Overlay Profiles",
            subtitle = "Manage dynamic content templates for your displays"
        ) {
            a(href = "/admin/overlay/profiles/new", classes = "btn btn-primary") {
                +"+ New Profile"
            }
        }

        // Navigation tabs
        pageTabs(listOf(
            TabDef("Profiles", "/admin/overlay/profiles", profiles.size),
            TabDef("Bindings", "/admin/overlay/bindings")
        ), "/admin/overlay/profiles")

        if (profiles.isEmpty()) {
            div("card") {
                emptyState(
                    icon = "palette",
                    title = "No overlay profiles",
                    description = "Create your first overlay profile to add dynamic content like tickers, tables, and KPIs to your channels.",
                    actionHref = "/admin/overlay/profiles/new",
                    actionLabel = "Create Profile"
                )
            }
        } else {
            div("template-gallery") {
                profiles.forEach { profile ->
                    val templateClass = when {
                        profile.widgetTypes.any { it.contains("ticker", ignoreCase = true) } -> "template-ticker"
                        profile.widgetTypes.any { it.contains("kpi", ignoreCase = true) } -> "template-kpi"
                        profile.widgetTypes.any { it.contains("table", ignoreCase = true) || it.contains("queue", ignoreCase = true) } -> "template-table"
                        profile.widgetTypes.any { it.contains("qr", ignoreCase = true) } -> "template-qr"
                        profile.widgetTypes.any { it.contains("poll", ignoreCase = true) } -> "template-poll"
                        profile.widgetTypes.any { it.contains("reaction", ignoreCase = true) } -> "template-reaction"
                        profile.widgetTypes.any { it.contains("weather", ignoreCase = true) } -> "template-weather"
                        profile.widgetTypes.any { it.contains("clock", ignoreCase = true) } -> "template-clock"
                        else -> "template-default"
                    }
                    a(href = "/admin/overlay/profiles/${profile.id}", classes = "template-card $templateClass") {
                        div("template-card-preview") {
                            // Visual layout preview based on template type
                            when {
                                templateClass == "template-ticker" -> {
                                    div("preview-bar preview-bar-bottom") {}
                                }
                                templateClass == "template-kpi" -> {
                                    div("preview-grid-2x2") {
                                        repeat(4) { div("preview-tile") {} }
                                    }
                                }
                                templateClass == "template-table" -> {
                                    div("preview-table-lines") {
                                        repeat(3) { div("preview-line") {} }
                                    }
                                }
                                templateClass == "template-qr" -> {
                                    div("preview-qr-box") {}
                                }
                                templateClass == "template-poll" -> {
                                    div("preview-bars-horizontal") {
                                        repeat(3) { div("preview-hbar") {} }
                                    }
                                }
                                else -> {
                                    div("preview-generic") {}
                                }
                            }
                        }
                        div("template-card-name") { +profile.name }
                        div("template-card-desc") {
                            if (profile.widgetTypes.isNotEmpty()) {
                                +profile.widgetTypes.joinToString(", ")
                            } else {
                                +"Custom layout"
                            }
                        }
                        div("template-card-meta") {
                            if (profile.bindingsCount > 0) {
                                span("badge badge-info badge-plain") { +"${profile.bindingsCount} binding${if (profile.bindingsCount != 1) "s" else ""}" }
                            }
                            span("text-muted text-sm") { +profile.createdAt.toString().substringBefore("T") }
                        }
                    }
                }
            }
        }

        // Pagination
        if (pagination != null) {
            paginationNav(pagination)
        }
    }
}

/**
 * New overlay profile form with improved UX.
 */
fun HTML.newOverlayProfileView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "New Overlay Profile", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "New Overlay Profile",
            subtitle = "Create a template for dynamic content",
            backHref = "/admin/overlay/profiles",
            backLabel = "Back to Profiles"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/overlay/profiles", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Profile Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            required = true
                            placeholder = "e.g., Main Ticker, Queue Display"
                        }
                        small("form-helper") {
                            +"Choose a descriptive name for this overlay profile."
                        }
                    }

                    div("form-group") {
                        label { +"Template" }
                        input(type = InputType.hidden) {
                            id = "template"
                            name = "template"
                            value = OverlayTemplate.entries.first().name
                        }
                        div("template-gallery") {
                            OverlayTemplate.entries.forEach { template ->
                                val templateIcon = when (template) {
                                    OverlayTemplate.TICKER -> "📰"
                                    OverlayTemplate.KPI_TILES -> "📊"
                                    OverlayTemplate.QUEUE_TABLE -> "📋"
                                    OverlayTemplate.QR_CARD -> "📱"
                                    OverlayTemplate.POLL -> "📊"
                                    OverlayTemplate.REACTION -> "👍"
                                    OverlayTemplate.WEATHER -> "🌤"
                                    OverlayTemplate.CLOCK -> "🕐"
                                    OverlayTemplate.NEWS_TICKER -> "📰"
                                }
                                div("template-card") {
                                    attributes["data-template"] = template.name
                                    if (template == OverlayTemplate.entries.first()) {
                                        classes = classes + " selected"
                                    }
                                    div("template-card-icon") { +templateIcon }
                                    div("template-card-name") { +template.displayName }
                                    div("template-card-desc") { +template.description }
                                }
                            }
                        }
                        small("form-helper") {
                            +"Select a template to start with."
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "definitionJson"
                            +"Definition JSON"
                        }
                        textArea(classes = "form-control code-editor") {
                            id = "definitionJson"
                            name = "definitionJson"
                            rows = "10"
                            placeholder = """{"widgets": []}"""
                        }
                        small("form-helper") {
                            +"Leave empty to use template defaults. Advanced users can customize the JSON directly."
                        }
                    }

                    // Template gallery selection script
                    script {
                        unsafe {
                            +"""
(function() {
    var cards = document.querySelectorAll('.template-card');
    var input = document.getElementById('template');
    cards.forEach(function(card) {
        card.addEventListener('click', function() {
            cards.forEach(function(c) { c.classList.remove('selected'); });
            card.classList.add('selected');
            input.value = card.dataset.template;
        });
    });
})();
"""
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create Profile"
                        }
                        a(href = "/admin/overlay/profiles", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay profile detail view with improved layout.
 */
fun HTML.overlayProfileDetailView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    bindings: List<OverlayBindingItem>
) {
    adminLayout(title = profile.name, userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = profile.name,
            backHref = "/admin/overlay/profiles",
            backLabel = "Back to Profiles"
        ) {
            a(href = "/admin/overlay/profiles/${profile.id}/edit", classes = "btn btn-secondary") {
                +"Edit"
            }
            form(action = "/admin/overlay/profiles/${profile.id}/delete", method = FormMethod.post, classes = "inline") {
                button(type = ButtonType.submit, classes = "btn btn-danger") {
                    attributes["onclick"] = "return confirm('Delete this profile? This will also remove all bindings.')"
                    +"Delete"
                }
            }
        }

        // Preview of this profile's definition
        div("card mb-4") {
            div("card-header") {
                h3 { +"Preview" }
            }
            div("card-body") {
                div {
                    style = "position:relative;width:100%;padding-top:56.25%;background:#000;border-radius:8px;overflow:hidden;"
                    div {
                        id = "overlay-preview"
                        style = "position:absolute;top:0;left:0;width:100%;height:100%;font-family:'Segoe UI',Arial,sans-serif;color:#fff;"
                    }
                }
                script {
                    unsafe {
                        +"""
(function() {
    try {
        var def = ${profile.definitionJson};
        var container = document.getElementById('overlay-preview');
        if (!def || !def.widgets) return;
        var widgets = Array.isArray(def.widgets) ? def.widgets : Object.values(def.widgets);
        widgets.forEach(function(w) {
            var type = (w.type || '').toLowerCase();
            if (type === 'ticker' || type === 'news-ticker') {
                var el = document.createElement('div');
                el.style.cssText = 'position:absolute;bottom:0;left:0;width:100%;height:48px;background:rgba(0,0,0,0.85);display:flex;align-items:center;overflow:hidden;';
                var label = w.label || 'TICKER';
                el.innerHTML = '<div style="background:#e50914;color:#fff;font-weight:700;font-size:14px;padding:0 16px;height:100%;display:flex;align-items:center;white-space:nowrap">' + label + '</div><div style="flex:1;overflow:hidden;font-size:16px;padding-left:12px">' + (w.text || 'Sample ticker text...') + '</div>';
                container.appendChild(el);
            }
            if (type === 'kpi-grid' || type === 'kpi') {
                var el = document.createElement('div');
                var pos = w.position || 'top-right';
                el.style.cssText = 'position:absolute;display:grid;gap:8px;padding:12px;grid-template-columns:repeat(' + (w.columns || 2) + ',140px);' + (pos.includes('right') ? 'right:16px;' : 'left:16px;') + (pos.includes('bottom') ? 'bottom:60px;' : 'top:16px;');
                var kpis = w.kpis || [{label:'KPI 1',value:'100'},{label:'KPI 2',value:'42'},{label:'KPI 3',value:'87%'},{label:'KPI 4',value:'3.2k'}];
                kpis.forEach(function(k) {
                    var tile = document.createElement('div');
                    tile.style.cssText = 'background:rgba(0,0,0,0.8);border-radius:8px;padding:12px 16px;text-align:center;border:1px solid rgba(255,255,255,0.1)';
                    tile.innerHTML = '<div style="font-size:28px;font-weight:700;color:' + (k.color || '#fff') + '">' + (k.value || '0') + '</div><div style="font-size:11px;text-transform:uppercase;opacity:0.7;margin-top:4px">' + (k.label || '') + '</div>';
                    el.appendChild(tile);
                });
                container.appendChild(el);
            }
            if (type === 'clock') {
                var el = document.createElement('div');
                var pos = w.position || 'top-right';
                el.style.cssText = 'position:absolute;background:rgba(0,0,0,0.75);border-radius:8px;padding:8px 16px;font-size:24px;font-weight:600;' + (pos.includes('right') ? 'right:16px;' : 'left:16px;') + (pos.includes('bottom') ? 'bottom:60px;' : 'top:16px;');
                function tick() { try { el.textContent = new Date().toLocaleTimeString('en-GB', {timeZone: w.timezone || 'Asia/Almaty', hour:'2-digit', minute:'2-digit', second:'2-digit', hour12: false}); } catch(e) { el.textContent = new Date().toLocaleTimeString(); } }
                tick(); setInterval(tick, 1000);
                container.appendChild(el);
            }
            if (type === 'table' || type === 'queue-table') {
                var el = document.createElement('div');
                el.style.cssText = 'position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.85);border-radius:8px;padding:12px;max-width:500px;font-size:13px;';
                var cols = w.columns || ['Name','Status','Wait'];
                el.innerHTML = '<table style="width:100%;border-collapse:collapse"><thead><tr>' + cols.map(function(c){return '<th style="text-align:left;padding:6px 8px;border-bottom:1px solid rgba(255,255,255,0.2);font-size:11px;opacity:0.7;text-transform:uppercase">' + c + '</th>';}).join('') + '</tr></thead><tbody><tr>' + cols.map(function(){return '<td style="padding:6px 8px;border-bottom:1px solid rgba(255,255,255,0.08)">—</td>';}).join('') + '</tr></tbody></table>';
                container.appendChild(el);
            }
        });
    } catch(e) { console.error('Preview error:', e); }
})();
"""
                    }
                }
            }
        }

        // Warning banner if profile has active bindings
        if (bindings.isNotEmpty()) {
            alertBox("Changes to this profile affect all ${bindings.size} live channel${if (bindings.size != 1) "s" else ""} using it.", "warning")
        }

        // Profile definition
        div("card mb-4") {
            div("card-header") {
                h3 { +"Definition" }
            }
            div("card-body") {
                pre("code-block code-editor") {
                    code {
                        +profile.definitionJson
                    }
                }
            }
        }

        // Bindings using this profile
        div("card") {
            div("card-header") {
                h3 { +"Channel Bindings" }
                if (bindings.isNotEmpty()) {
                    span("badge badge-gray badge-plain") { +"${bindings.size}" }
                }
            }
            if (bindings.isEmpty()) {
                div("card-body") {
                    emptyState(
                        icon = "link",
                        title = "No bindings yet",
                        description = "Connect this profile to a channel to start displaying dynamic content.",
                        actionHref = "/admin/overlay/bindings/new?profile=${profile.id}",
                        actionLabel = "Create Binding"
                    )
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Channel" }
                            th { +"Source" }
                            th { +"Status" }
                        }
                    }
                    tbody {
                        bindings.forEach { binding ->
                            tr {
                                td {
                                    a(href = "/admin/channels/${binding.channelId}", classes = "font-medium") {
                                        +binding.channelName
                                    }
                                }
                                td {
                                    span("badge badge-gray badge-plain") {
                                        +binding.sourceType.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${bindingStatusBadge(binding.status)}") {
                                        +binding.status.name.lowercase()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Edit overlay profile view with improved UX.
 */
fun HTML.editOverlayProfileView(
    session: AdminClaims,
    profile: OverlayProfileDetail,
    error: String? = null
) {
    adminLayout(title = "Edit ${profile.name}", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Edit Profile",
            subtitle = profile.name,
            backHref = "/admin/overlay/profiles/${profile.id}",
            backLabel = "Back to Profile"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/overlay/profiles/${profile.id}", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Profile Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            required = true
                            value = profile.name
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "definitionJson"
                            +"Definition JSON"
                        }
                        textArea(classes = "form-control") {
                            id = "definitionJson"
                            name = "definitionJson"
                            rows = "15"
                            +profile.definitionJson
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Changes"
                        }
                        a(href = "/admin/overlay/profiles/${profile.id}", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

private fun bindingStatusBadge(status: com.playoutedge.domain.enums.BindingStatus): String = when (status) {
    com.playoutedge.domain.enums.BindingStatus.ACTIVE -> "success"
    com.playoutedge.domain.enums.BindingStatus.PAUSED -> "warning"
}
