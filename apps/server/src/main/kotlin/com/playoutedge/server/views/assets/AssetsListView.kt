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
        // Compact header: title + quota bar + actions in one row
        pageHeader(
            title = "Assets",
            subtitle = "${quota.usedFormatted} / ${quota.limitFormatted}"
        ) {
            a(href = "/admin/assets/upload", classes = "btn btn-primary") {
                +"+ Upload"
            }
            a(href = "/admin/assets/slideshow/new", classes = "btn btn-secondary") {
                +"+ Slideshow"
            }
        }

        // Storage quota bar (compact, no card wrapper)
        if (quota.usedPercent > 0) {
            div("quota-bar-compact") {
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

        // Compact toolbar: tabs + filters + view toggle all in one row
        div("assets-toolbar") {
            // Type tabs (inline)
            div("assets-type-tabs") {
                val currentType = filters.type?.name
                a(href = "/admin/assets", classes = "tab-pill${if (currentType == null) " active" else ""}") { +"All" }
                AssetType.entries.forEach { type ->
                    val typeName = type.name
                    a(href = "/admin/assets?type=$typeName", classes = "tab-pill${if (currentType == typeName) " active" else ""}") {
                        +typeName.lowercase().replaceFirstChar { it.uppercase() }
                    }
                }
            }

            // Filters inline
            form(action = "/admin/assets", method = FormMethod.get, classes = "assets-inline-filters") {
                // Preserve type filter from tabs
                filters.type?.let {
                    input(type = InputType.hidden, name = "type") { value = it.name }
                }
                select("form-control form-control-sm") {
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
                input(type = InputType.text, classes = "form-control form-control-sm") {
                    id = "filter-search"
                    name = "search"
                    placeholder = "Search..."
                    value = filters.search ?: ""
                }
                button(type = ButtonType.submit, classes = "btn btn-secondary btn-sm") { +"Filter" }
                if (filters.hasActiveFilters()) {
                    a(href = "/admin/assets", classes = "btn btn-ghost btn-sm") { +"Clear" }
                }
            }

            // View toggle + sort (right side)
            if (assets.isNotEmpty()) {
                div("assets-view-controls") {
                    button(classes = "btn btn-sm btn-secondary active") {
                        id = "view-list-btn"
                        +"List"
                    }
                    button(classes = "btn btn-sm btn-ghost") {
                        id = "view-grid-btn"
                        +"Grid"
                    }
                    select("form-control form-control-sm") {
                        id = "sort-select"
                        option { value = "date-desc"; +"Newest" }
                        option { value = "date-asc"; +"Oldest" }
                        option { value = "name-asc"; +"A-Z" }
                        option { value = "name-desc"; +"Z-A" }
                        option { value = "size-desc"; +"Largest" }
                        option { value = "size-asc"; +"Smallest" }
                    }
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

        // Assets content
        div("card") {
            if (assets.isEmpty()) {
                emptyState(
                    icon = "film",
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
                                                icon(when (asset.type) {
                                                    AssetType.VIDEO -> "film"
                                                    AssetType.IMAGE -> "image"
                                                    AssetType.AUDIO -> "music"
                                                    AssetType.SLIDESHOW -> "play"
                                                }, 32)
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
                                        span("badge badge-gray badge-plain") {
                                            icon(when (asset.type) {
                                                AssetType.VIDEO -> "film"
                                                AssetType.IMAGE -> "image"
                                                else -> "document"
                                            }, 14)
                                            +" ${asset.type.name.lowercase()}"
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
                                    icon(when (asset.type) {
                                        AssetType.VIDEO -> "film"
                                        AssetType.IMAGE -> "image"
                                        AssetType.AUDIO -> "music"
                                        AssetType.SLIDESHOW -> "play"
                                    }, 32)
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
