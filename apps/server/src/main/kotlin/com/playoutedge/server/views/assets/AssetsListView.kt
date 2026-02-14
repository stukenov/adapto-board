package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.*
import kotlinx.html.*

/**
 * Assets list view with grid/list toggle and sorting.
 */
fun HTML.assetsListView(
    session: AdminClaims,
    assets: List<AssetViewItem>,
    quota: QuotaInfo,
    filters: AssetFilters,
    pagination: PaginationInfo? = null
) {
    adminLayout(title = "Assets", userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = "Assets",
            subtitle = "Manage your media library"
        ) {
            a(href = "/admin/assets/upload", classes = "btn btn-primary") {
                +"+ Upload"
            }
            a(href = "/admin/assets/slideshow/new", classes = "btn btn-secondary") {
                +"+ Slideshow"
            }
        }

        // Tabs
        pageTabs(listOf(
            TabDef("All", "/admin/assets"),
            TabDef("Video", "/admin/assets?type=VIDEO"),
            TabDef("Image", "/admin/assets?type=IMAGE"),
            TabDef("Audio", "/admin/assets?type=AUDIO"),
            TabDef("Slideshow", "/admin/assets?type=SLIDESHOW")
        ), "/admin/assets${if (filters.type != null) "?type=${filters.type.name}" else ""}")

        // Storage quota warning
        if (quota.usedPercent >= 80) {
            div("alert alert-warning mb-4") {
                if (quota.usedPercent >= 90) {
                    +"Storage usage is critically high (${quota.usedPercent}%). Consider archiving or deleting unused assets."
                } else {
                    +"Storage usage is above 80% (${quota.usedPercent}%). Consider cleaning up unused assets."
                }
            }
        }

        // Bulk action bar (hidden by default, shown via JS when checkboxes selected)
        div("bulk-action-bar") {
            id = "bulk-action-bar"
            span("bulk-count") {
                id = "bulk-count"
                +"0"
            }
            span { +" asset(s) selected" }
            form(action = "/admin/assets/bulk-archive", method = FormMethod.post, classes = "no-loading") {
                id = "bulk-archive-form"
                input(type = InputType.hidden, name = "ids") { id = "bulk-archive-ids" }
                button(type = ButtonType.submit, classes = "btn btn-secondary btn-sm") { +"Archive Selected" }
            }
            form(action = "/admin/assets/bulk-delete", method = FormMethod.post, classes = "no-loading") {
                id = "bulk-delete-form"
                input(type = InputType.hidden, name = "ids") { id = "bulk-delete-ids" }
                button(type = ButtonType.submit, classes = "btn btn-danger btn-sm") {
                    attributes["onclick"] = "return confirm('Are you sure you want to delete the selected assets?')"
                    +"Delete Selected"
                }
            }
        }

        // Storage quota
        div("card mb-4") {
            div("card-body") {
                div("quota-info") {
                    div("quota-label") {
                        span { +"Storage Usage" }
                        span { +"${quota.usedFormatted} / ${quota.limitFormatted}" }
                    }
                    div("quota-bar") {
                        val barClass = when {
                            quota.usedPercent >= 90 -> "danger"
                            quota.usedPercent >= 70 -> "warning"
                            else -> ""
                        }
                        div("quota-fill $barClass") {
                            style = "width: ${quota.usedPercent}%"
                        }
                    }
                }
            }
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/assets", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label {
                            htmlFor = "filter-type"
                            +"Type"
                        }
                        select("form-control") {
                            id = "filter-type"
                            name = "type"
                            option {
                                value = ""
                                if (filters.type == null) selected = true
                                +"All Types"
                            }
                            AssetType.entries.forEach { type ->
                                option {
                                    value = type.name
                                    if (filters.type == type) selected = true
                                    +type.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-status"
                            +"Status"
                        }
                        select("form-control") {
                            id = "filter-status"
                            name = "status"
                            option {
                                value = ""
                                if (filters.status == null) selected = true
                                +"All Statuses"
                            }
                            AssetStatus.entries.forEach { status ->
                                option {
                                    value = status.name
                                    if (filters.status == status) selected = true
                                    +status.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-search"
                            +"Search"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "filter-search"
                            name = "search"
                            placeholder = "Search assets..."
                            value = filters.search ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/assets", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // View toggle + sort controls
        if (assets.isNotEmpty()) {
            div("assets-controls mb-4") {
                div("assets-view-toggle") {
                    button(classes = "btn btn-sm btn-secondary active") {
                        id = "view-list-btn"
                        +"☰ List"
                    }
                    button(classes = "btn btn-sm btn-ghost") {
                        id = "view-grid-btn"
                        +"⊞ Grid"
                    }
                }
                div("assets-sort") {
                    label { +"Sort:" }
                    select("form-control form-control-sm") {
                        id = "sort-select"
                        option { value = "date-desc"; +"Newest first" }
                        option { value = "date-asc"; +"Oldest first" }
                        option { value = "name-asc"; +"Name A-Z" }
                        option { value = "name-desc"; +"Name Z-A" }
                        option { value = "size-desc"; +"Largest first" }
                        option { value = "size-asc"; +"Smallest first" }
                    }
                }
            }
        }

        // Assets content
        div("card") {
            if (assets.isEmpty()) {
                emptyState(
                    icon = "🎬",
                    title = "No assets found",
                    description = if (filters.hasActiveFilters())
                        "Try adjusting your filters or upload new content."
                    else
                        "Upload your first video or image to get started.",
                    actionHref = "/admin/assets/upload",
                    actionLabel = "Upload Assets"
                )
            } else {
                // List view (default)
                div("assets-list-view") {
                    id = "assets-list-view"
                    table("table") {
                        thead {
                            tr {
                                th {
                                    input(type = InputType.checkBox) {
                                        id = "select-all-assets"
                                        title = "Select all"
                                    }
                                }
                                th { }  // Thumbnail
                                th { +"Name" }
                                th { +"Type" }
                                th { +"Status" }
                                th { +"Size" }
                                th { +"Duration" }
                                th { +"Created" }
                                th { }
                            }
                        }
                        tbody {
                            id = "assets-tbody"
                            assets.forEach { asset ->
                                tr("asset-row") {
                                    attributes["data-name"] = asset.name
                                    attributes["data-date"] = asset.createdAt.toString()
                                    attributes["data-size"] = asset.fileSize.toString()
                                    attributes["data-id"] = asset.id.toString()
                                    td {
                                        input(type = InputType.checkBox, classes = "asset-checkbox") {
                                            value = asset.id.toString()
                                        }
                                    }
                                    td {
                                        // Thumbnail or type icon
                                        if (asset.type == AssetType.IMAGE && asset.storageKey != null) {
                                            img(classes = "asset-thumbnail") {
                                                src = "/storage/${asset.id}"
                                                alt = asset.name
                                                attributes["loading"] = "lazy"
                                            }
                                        } else if (asset.thumbnailStorageKey != null) {
                                            img(classes = "asset-thumbnail") {
                                                src = "/storage/${asset.thumbnailStorageKey}"
                                                alt = asset.name
                                            }
                                        } else {
                                            div("asset-thumbnail-placeholder") {
                                                +when (asset.type) {
                                                    AssetType.VIDEO -> "🎬"
                                                    AssetType.IMAGE -> "🖼"
                                                    AssetType.AUDIO -> "🎵"
                                                    AssetType.SLIDESHOW -> "📽"
                                                }
                                            }
                                        }
                                    }
                                    td {
                                        a(href = "/admin/assets/${asset.id}", classes = "font-medium") {
                                            +asset.name
                                        }
                                        // Approval status badge (if not APPROVED)
                                        if (asset.approvalStatus != "APPROVED") {
                                            val badgeClass = when (asset.approvalStatus) {
                                                "PENDING_REVIEW" -> "badge-pending-review"
                                                "REJECTED" -> "badge-rejected"
                                                else -> "badge-gray"
                                            }
                                            span("badge $badgeClass") {
                                                style = "margin-left: 6px; font-size: 0.75rem;"
                                                +asset.approvalStatus.lowercase().replace('_', ' ')
                                            }
                                        }
                                        // Aspect ratio for video/image
                                        if (asset.width != null && asset.height != null &&
                                            (asset.type == AssetType.VIDEO || asset.type == AssetType.IMAGE)) {
                                            span("text-muted") {
                                                style = "display: block; font-size: 0.75rem;"
                                                +"${asset.width} x ${asset.height}"
                                            }
                                        }
                                    }
                                    td {
                                        val typeIcon = when (asset.type) {
                                            AssetType.VIDEO -> "🎬"
                                            AssetType.IMAGE -> "🖼"
                                            else -> "📄"
                                        }
                                        span("badge badge-gray badge-plain") {
                                            +"$typeIcon ${asset.type.name.lowercase()}"
                                        }
                                    }
                                    td {
                                        span("badge badge-${assetStatusBadge(asset.status)}") {
                                            +asset.status.name.lowercase()
                                        }
                                    }
                                    td {
                                        span("text-muted") { +asset.fileSizeFormatted }
                                    }
                                    td {
                                        asset.durationFormatted?.let { +it } ?: span("text-muted") { +"—" }
                                    }
                                    td {
                                        span("text-muted") {
                                            +asset.createdAt.toString().substringBefore("T")
                                        }
                                    }
                                    td {
                                        div("table-actions") {
                                            a(href = "/admin/assets/${asset.id}", classes = "btn btn-secondary btn-sm") {
                                                +"View"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Grid view (hidden by default)
                div("asset-grid-view") {
                    id = "assets-grid-view"
                    style = "display:none"
                    assets.forEach { asset ->
                        a(href = "/admin/assets/${asset.id}", classes = "asset-grid-card") {
                            attributes["data-name"] = asset.name
                            attributes["data-date"] = asset.createdAt.toString()
                            attributes["data-size"] = asset.fileSize.toString()
                            if (asset.type == AssetType.IMAGE && asset.storageKey != null) {
                                div("asset-grid-thumb") {
                                    img {
                                        src = "/storage/${asset.id}"
                                        alt = asset.name
                                        attributes["loading"] = "lazy"
                                        style = "width:100%;height:100%;object-fit:cover"
                                    }
                                }
                            } else {
                                div("asset-grid-icon") {
                                    +when (asset.type) {
                                        AssetType.VIDEO -> "🎬"
                                        AssetType.IMAGE -> "🖼"
                                        AssetType.AUDIO -> "🎵"
                                        AssetType.SLIDESHOW -> "📽"
                                    }
                                }
                            }
                            div("asset-grid-info") {
                                div("asset-grid-name") { +asset.name }
                                div("asset-grid-meta") {
                                    span("badge badge-${assetStatusBadge(asset.status)} badge-sm") {
                                        +asset.status.name.lowercase()
                                    }
                                    span { +asset.fileSizeFormatted }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pagination
        if (pagination != null) {
            paginationNav(pagination)
        }

        // JS for view toggle and sorting
        if (assets.isNotEmpty()) {
            script {
                unsafe {
                    +"""
(function() {
    var listView = document.getElementById('assets-list-view');
    var gridView = document.getElementById('assets-grid-view');
    var listBtn = document.getElementById('view-list-btn');
    var gridBtn = document.getElementById('view-grid-btn');
    var sortSelect = document.getElementById('sort-select');

    if (listBtn) listBtn.addEventListener('click', function() {
        listView.style.display = '';
        gridView.style.display = 'none';
        listBtn.className = 'btn btn-sm btn-secondary active';
        gridBtn.className = 'btn btn-sm btn-ghost';
    });
    if (gridBtn) gridBtn.addEventListener('click', function() {
        listView.style.display = 'none';
        gridView.style.display = '';
        gridBtn.className = 'btn btn-sm btn-secondary active';
        listBtn.className = 'btn btn-sm btn-ghost';
    });

    if (sortSelect) sortSelect.addEventListener('change', function() {
        var sv = this.value;
        var tbody = document.getElementById('assets-tbody');
        var rows = Array.from(tbody.querySelectorAll('.asset-row'));
        rows.sort(function(a, b) {
            if (sv === 'name-asc') return a.dataset.name.localeCompare(b.dataset.name);
            if (sv === 'name-desc') return b.dataset.name.localeCompare(a.dataset.name);
            if (sv === 'date-asc') return a.dataset.date.localeCompare(b.dataset.date);
            if (sv === 'date-desc') return b.dataset.date.localeCompare(a.dataset.date);
            if (sv === 'size-asc') return parseInt(a.dataset.size) - parseInt(b.dataset.size);
            if (sv === 'size-desc') return parseInt(b.dataset.size) - parseInt(a.dataset.size);
            return 0;
        });
        rows.forEach(function(r) { tbody.appendChild(r); });
        // Also sort grid
        var grid = document.getElementById('assets-grid-view');
        var cards = Array.from(grid.querySelectorAll('.asset-grid-card'));
        cards.sort(function(a, b) {
            if (sv === 'name-asc') return a.dataset.name.localeCompare(b.dataset.name);
            if (sv === 'name-desc') return b.dataset.name.localeCompare(a.dataset.name);
            if (sv === 'date-asc') return a.dataset.date.localeCompare(b.dataset.date);
            if (sv === 'date-desc') return b.dataset.date.localeCompare(a.dataset.date);
            if (sv === 'size-asc') return parseInt(a.dataset.size) - parseInt(b.dataset.size);
            if (sv === 'size-desc') return parseInt(b.dataset.size) - parseInt(a.dataset.size);
            return 0;
        });
        cards.forEach(function(c) { grid.appendChild(c); });
    });
})();
"""
                }
            }
        }
    }
}

private fun assetStatusBadge(status: AssetStatus): String = when (status) {
    AssetStatus.UPLOADING -> "warning"
    AssetStatus.PROCESSING -> "warning"
    AssetStatus.READY -> "success"
    AssetStatus.REJECTED -> "danger"
    AssetStatus.ARCHIVED -> "gray"
}

fun AssetFilters.hasActiveFilters(): Boolean =
    type != null || status != null || !search.isNullOrBlank()
