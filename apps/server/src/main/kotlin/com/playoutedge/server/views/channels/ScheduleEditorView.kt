package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

/**
 * Schedule item for the editor.
 */
data class ScheduleEditorItem(
    val assetId: UUID,
    val assetName: String,
    val assetType: AssetType,
    val durationMs: Int?,
    val orderIndex: Int,
    val timeStart: String?,
    val timeEnd: String?,
    val daysOfWeek: Int?
)

/**
 * Library asset available for adding to schedule.
 */
data class LibraryAsset(
    val id: UUID,
    val name: String,
    val type: AssetType,
    val durationMs: Int?,
    val fileSizeFormatted: String
)

/**
 * Schedule editor view with drag & drop.
 */
data class CopySourceChannel(
    val id: UUID,
    val name: String
)

fun HTML.scheduleEditorView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    items: List<ScheduleEditorItem>,
    libraryAssets: List<LibraryAsset>,
    draftVersionId: UUID?,
    activeVersionNumber: Int?,
    error: String? = null,
    success: String? = null,
    otherChannels: List<CopySourceChannel> = emptyList()
) {
    adminLayout(title = "Edit Schedule - $channelName", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Schedule Editor",
            subtitle = channelName,
            backHref = "/admin/channels/$channelId",
            backLabel = "Back to Channel"
        ) {
            if (activeVersionNumber != null) {
                span("badge badge-success mr-2") { +"Published v$activeVersionNumber" }
            }
            if (draftVersionId != null) {
                span("badge badge-warning mr-2") { +"Draft" }
            }
        }

        if (error != null) {
            alertBox(error, "error")
        }
        if (success != null) {
            alertBox(success, "success")
        }

        // Overlap warning bar
        div("alert alert-warning") {
            id = "overlap-warning"
            style = "display:none;"
            strong { +"Warning:" }
            +" Some items have overlapping time windows."
        }

        // Validation results container
        div {
            id = "validation-results"
        }

        // Toolbar
        div("schedule-toolbar") {
            div("schedule-toolbar-left") {
                span("schedule-item-count") {
                    id = "item-count"
                    +"${items.size} items"
                }
                span("schedule-total-duration") {
                    id = "total-duration"
                    +formatTotalDuration(items)
                }
                span("text-sm text-muted") {
                    id = "autosave-indicator"
                    style = "margin-left:0.5rem;"
                }
            }
            div("schedule-toolbar-center") {
                a(href = "/admin/channels/$channelId/schedule/grid", classes = "btn btn-sm btn-secondary") {
                    +"Grid View"
                }
                button(classes = "btn btn-sm btn-secondary") {
                    id = "validate-btn"
                    +"Validate"
                }
                if (otherChannels.isNotEmpty()) {
                    // Copy from channel
                    form(action = "/admin/channels/$channelId/schedule/copy-from", method = FormMethod.post, classes = "inline") {
                        style = "display:flex;align-items:center;gap:0.25rem;"
                        select("form-control form-control-sm") {
                            name = "sourceChannelId"
                            option { value = ""; +"Copy from..." }
                            otherChannels.forEach { ch ->
                                option {
                                    value = ch.id.toString()
                                    +ch.name
                                }
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-sm btn-secondary") {
                            +"Copy"
                        }
                    }
                }
            }
            div("schedule-toolbar-right") {
                form(action = "/admin/channels/$channelId/schedule/save", method = FormMethod.post, classes = "inline") {
                    id = "schedule-form"
                    input(type = InputType.hidden) {
                        id = "items-json"
                        name = "itemsJson"
                        value = ""
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        id = "save-draft-btn"
                        +"Save Draft"
                    }
                }
                form(action = "/admin/channels/$channelId/schedule/publish", method = FormMethod.post, classes = "inline") {
                    input(type = InputType.hidden) {
                        id = "publish-items-json"
                        name = "itemsJson"
                        value = ""
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        id = "publish-btn"
                        +"Publish"
                    }
                }
            }
        }

        // Split layout
        div("schedule-editor") {
            // Left: Playlist
            div("schedule-playlist") {
                div("card") {
                    div("card-header") {
                        h3 { +"Playlist" }
                    }
                    div("card-body") {
                        div("playlist-drop-zone") {
                            id = "playlist"

                            if (items.isEmpty()) {
                                div("playlist-empty-hint") {
                                    id = "playlist-empty"
                                    +"Drag assets from the library to build your playlist"
                                }
                            }

                            items.forEachIndexed { index, item ->
                                playlistItemElement(item, index)
                            }
                        }
                    }
                }
            }

            // Right: Library
            div("schedule-library") {
                div("card") {
                    div("card-header") {
                        h3 { +"Asset Library" }
                        input(type = InputType.text, classes = "form-control form-control-sm") {
                            id = "library-search"
                            placeholder = "Search assets..."
                        }
                    }
                    div("card-body") {
                        if (libraryAssets.isEmpty()) {
                            emptyState(
                                icon = "🎬",
                                title = "No assets",
                                description = "Upload assets first to add them to your schedule.",
                                actionHref = "/admin/assets/upload",
                                actionLabel = "Upload Assets"
                            )
                        } else {
                            div("library-assets") {
                                id = "library-assets"
                                libraryAssets.forEach { asset ->
                                    div("library-asset") {
                                        attributes["draggable"] = "true"
                                        attributes["data-asset-id"] = asset.id.toString()
                                        attributes["data-asset-name"] = asset.name
                                        attributes["data-asset-type"] = asset.type.name
                                        attributes["data-asset-duration"] = (asset.durationMs ?: 0).toString()
                                        div("library-asset-icon") {
                                            +assetTypeIcon(asset.type)
                                        }
                                        div("library-asset-info") {
                                            div("library-asset-name") { +asset.name }
                                            div("library-asset-meta") {
                                                span { +asset.type.name.lowercase() }
                                                asset.durationMs?.let { ms ->
                                                    span { +" · ${formatDurationMs(ms)}" }
                                                }
                                                span { +" · ${asset.fileSizeFormatted}" }
                                            }
                                        }
                                        div("library-asset-add") {
                                            button(classes = "btn btn-sm btn-secondary library-add-btn") {
                                                +"+ Add"
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

        // Inline JavaScript for drag & drop
        script {
            unsafe {
                +scheduleEditorScript()
            }
        }
    }
}

/**
 * Single playlist item element.
 */
private fun FlowContent.playlistItemElement(item: ScheduleEditorItem, index: Int) {
    div("playlist-item") {
        attributes["draggable"] = "true"
        attributes["data-asset-id"] = item.assetId.toString()
        attributes["data-asset-name"] = item.assetName
        attributes["data-asset-type"] = item.assetType.name
        attributes["data-asset-duration"] = (item.durationMs ?: 0).toString()
        attributes["data-time-start"] = item.timeStart ?: ""
        attributes["data-time-end"] = item.timeEnd ?: ""
        attributes["data-days-of-week"] = (item.daysOfWeek ?: 127).toString()

        div("playlist-item-handle") { +"⠿" }
        div("playlist-item-icon") { +assetTypeIcon(item.assetType) }
        div("playlist-item-info") {
            div("playlist-item-name") { +item.assetName }
            div("playlist-item-meta") {
                span("playlist-item-order") { +"#${index + 1}" }
                item.durationMs?.let { ms ->
                    span { +" · ${formatDurationMs(ms)}" }
                }
                if (item.timeStart != null || item.timeEnd != null) {
                    span("playlist-item-time") {
                        +" · ${item.timeStart ?: "00:00"}-${item.timeEnd ?: "23:59"}"
                    }
                }
            }
        }
        div("playlist-item-actions") {
            button(classes = "btn-icon playlist-timing-btn") {
                title = "Set timing"
                +"⏱"
            }
            button(classes = "btn-icon playlist-remove-btn") {
                title = "Remove"
                +"✕"
            }
        }
        // Timing panel (hidden by default)
        div("item-timing") {
            style = "display:none"
            div("item-timing-row") {
                div("form-group") {
                    label { +"Start Time" }
                    input(type = InputType.time, classes = "form-control form-control-sm timing-start") {
                        value = item.timeStart ?: ""
                    }
                }
                div("form-group") {
                    label { +"End Time" }
                    input(type = InputType.time, classes = "form-control form-control-sm timing-end") {
                        value = item.timeEnd ?: ""
                    }
                }
            }
            div("item-timing-row") {
                div("form-group") {
                    label { +"Days" }
                    div("days-checkboxes") {
                        val days = item.daysOfWeek ?: 127
                        listOf("Mon" to 1, "Tue" to 2, "Wed" to 4, "Thu" to 8, "Fri" to 16, "Sat" to 32, "Sun" to 64).forEach { (name, bit) ->
                            label("day-checkbox") {
                                input(type = InputType.checkBox, classes = "day-check") {
                                    value = bit.toString()
                                    if (days and bit != 0) checked = true
                                }
                                +name
                            }
                        }
                    }
                }
            }
            // TTS Voiceover controls
            div("item-timing-row") {
                div("form-group") {
                    label { +"Voiceover" }
                    div("tts-controls") {
                        label("day-checkbox") {
                            input(type = InputType.checkBox, classes = "tts-enabled") {}
                            +"Generate voiceover"
                        }
                        select("form-control form-control-sm tts-voice") {
                            option { value = "RU_DARIYA"; +"Dariya (RU, female)" }
                            option { value = "RU_IVAN"; +"Ivan (RU, male)" }
                            option { value = "KK_AIGUL"; +"Aigul (KK, female)" }
                            option { value = "KK_DAULET"; +"Daulet (KK, male)" }
                            option { value = "EN_JENNY"; +"Jenny (EN, female)" }
                            option { value = "EN_GUY"; +"Guy (EN, male)" }
                        }
                    }
                }
            }
        }
    }
}

private fun assetTypeIcon(type: AssetType): String = when (type) {
    AssetType.VIDEO -> "🎬"
    AssetType.IMAGE -> "🖼"
    else -> "📄"
}

private fun formatDurationMs(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${String.format("%02d", seconds)}"
}

private fun formatTotalDuration(items: List<ScheduleEditorItem>): String {
    val totalMs = items.mapNotNull { it.durationMs }.sum()
    if (totalMs == 0) return ""
    val totalSeconds = totalMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "Total: ${hours}h ${minutes}m" else "Total: ${minutes}m ${seconds}s"
}

private fun scheduleEditorScript(): String = """
(function() {
    const playlist = document.getElementById('playlist');
    const emptyHint = document.getElementById('playlist-empty');
    const itemsJsonField = document.getElementById('items-json');
    const publishItemsJsonField = document.getElementById('publish-items-json');
    const searchInput = document.getElementById('library-search');
    const itemCountEl = document.getElementById('item-count');
    const totalDurationEl = document.getElementById('total-duration');

    let draggedEl = null;
    let dragSource = null; // 'playlist' or 'library'
    let libraryPlaceholder = null; // single placeholder for library drag

    function updateState() {
        const items = playlist.querySelectorAll('.playlist-item:not(.playlist-item-placeholder)');
        // Update order numbers
        items.forEach((el, i) => {
            const orderEl = el.querySelector('.playlist-item-order');
            if (orderEl) orderEl.textContent = '#' + (i + 1);
        });
        // Update counts
        if (itemCountEl) itemCountEl.textContent = items.length + ' items';
        // Update total duration
        let totalMs = 0;
        items.forEach(el => { totalMs += parseInt(el.dataset.assetDuration || '0'); });
        if (totalDurationEl) {
            if (totalMs === 0) { totalDurationEl.textContent = ''; }
            else {
                const s = Math.floor(totalMs / 1000);
                const h = Math.floor(s / 3600);
                const m = Math.floor((s % 3600) / 60);
                const sec = s % 60;
                totalDurationEl.textContent = h > 0 ? 'Total: ' + h + 'h ' + m + 'm' : 'Total: ' + m + 'm ' + sec + 's';
            }
        }
        // Update hidden JSON
        const json = [];
        items.forEach((el, i) => {
            const timingStart = el.querySelector('.timing-start');
            const timingEnd = el.querySelector('.timing-end');
            const dayChecks = el.querySelectorAll('.day-check');
            let daysOfWeek = 0;
            dayChecks.forEach(cb => { if (cb.checked) daysOfWeek |= parseInt(cb.value); });
            json.push({
                assetId: el.dataset.assetId,
                orderIndex: i,
                timeStart: timingStart ? timingStart.value || null : null,
                timeEnd: timingEnd ? timingEnd.value || null : null,
                daysOfWeek: daysOfWeek || null
            });
        });
        const jsonStr = JSON.stringify(json);
        if (itemsJsonField) itemsJsonField.value = jsonStr;
        if (publishItemsJsonField) publishItemsJsonField.value = jsonStr;
        // Show/hide empty hint
        if (emptyHint) emptyHint.style.display = items.length === 0 ? '' : 'none';
    }

    function createPlaylistItem(assetId, assetName, assetType, assetDuration) {
        const div = document.createElement('div');
        div.className = 'playlist-item';
        div.draggable = true;
        div.dataset.assetId = assetId;
        div.dataset.assetName = assetName;
        div.dataset.assetType = assetType;
        div.dataset.assetDuration = assetDuration;
        div.dataset.timeStart = '';
        div.dataset.timeEnd = '';
        div.dataset.daysOfWeek = '127';

        const icon = assetType === 'VIDEO' ? '\uD83C\uDFAC' : assetType === 'IMAGE' ? '\uD83D\uDDBC' : '\uD83D\uDCC4';
        const durMs = parseInt(assetDuration);
        const durStr = durMs > 0 ? (' \u00B7 ' + Math.floor(durMs/60000) + ':' + String(Math.floor((durMs/1000)%60)).padStart(2,'0')) : '';

        div.innerHTML = '<div class="playlist-item-handle">\u2807</div>' +
            '<div class="playlist-item-icon">' + icon + '</div>' +
            '<div class="playlist-item-info"><div class="playlist-item-name">' + assetName + '</div>' +
            '<div class="playlist-item-meta"><span class="playlist-item-order">#1</span>' +
            (durStr ? '<span>' + durStr + '</span>' : '') +
            '</div></div>' +
            '<div class="playlist-item-actions">' +
            '<button class="btn-icon playlist-timing-btn" title="Set timing">\u23F1</button>' +
            '<button class="btn-icon playlist-remove-btn" title="Remove">\u2715</button></div>' +
            '<div class="item-timing" style="display:none">' +
            '<div class="item-timing-row"><div class="form-group"><label>Start Time</label>' +
            '<input type="time" class="form-control form-control-sm timing-start" value=""></div>' +
            '<div class="form-group"><label>End Time</label>' +
            '<input type="time" class="form-control form-control-sm timing-end" value=""></div></div>' +
            '<div class="item-timing-row"><div class="form-group"><label>Days</label>' +
            '<div class="days-checkboxes">' +
            ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'].map((d,i) =>
                '<label class="day-checkbox"><input type="checkbox" class="day-check" value="' + (1<<i) + '" checked>' + d + '</label>'
            ).join('') + '</div></div></div></div>';

        attachItemEvents(div);
        return div;
    }

    function attachItemEvents(el) {
        el.addEventListener('dragstart', function(e) {
            draggedEl = el;
            dragSource = 'playlist';
            el.classList.add('dragging');
            e.dataTransfer.effectAllowed = 'move';
        });
        el.addEventListener('dragend', function() {
            el.classList.remove('dragging');
            draggedEl = null;
            dragSource = null;
            document.querySelectorAll('.drag-over').forEach(x => x.classList.remove('drag-over'));
        });
        // Remove button
        el.querySelector('.playlist-remove-btn').addEventListener('click', function() {
            el.remove();
            updateState();
        });
        // Timing toggle
        el.querySelector('.playlist-timing-btn').addEventListener('click', function() {
            const panel = el.querySelector('.item-timing');
            panel.style.display = panel.style.display === 'none' ? '' : 'none';
        });
        // Timing change events
        el.querySelectorAll('.timing-start, .timing-end, .day-check').forEach(input => {
            input.addEventListener('change', updateState);
        });
    }

    // Attach events to existing items
    playlist.querySelectorAll('.playlist-item').forEach(attachItemEvents);

    // Playlist drag over / drop
    playlist.addEventListener('dragover', function(e) {
        e.preventDefault();
        const target = e.target.closest('.playlist-item');
        document.querySelectorAll('.playlist-item.drag-over').forEach(x => x.classList.remove('drag-over'));

        if (dragSource === 'library' && draggedEl) {
            e.dataTransfer.dropEffect = 'copy';
            // Create placeholder once
            if (!libraryPlaceholder) {
                libraryPlaceholder = createPlaylistItem(
                    draggedEl.dataset.assetId,
                    draggedEl.dataset.assetName,
                    draggedEl.dataset.assetType,
                    draggedEl.dataset.assetDuration
                );
                libraryPlaceholder.classList.add('playlist-item-placeholder');
                playlist.appendChild(libraryPlaceholder);
            }
            // Move existing placeholder to correct position
            if (target && target !== libraryPlaceholder) {
                const rect = target.getBoundingClientRect();
                const mid = rect.top + rect.height / 2;
                if (e.clientY < mid) {
                    playlist.insertBefore(libraryPlaceholder, target);
                } else {
                    playlist.insertBefore(libraryPlaceholder, target.nextSibling);
                }
            }
        } else if (dragSource === 'playlist' && draggedEl) {
            e.dataTransfer.dropEffect = 'move';
            if (target && target !== draggedEl) {
                const rect = target.getBoundingClientRect();
                const mid = rect.top + rect.height / 2;
                if (e.clientY < mid) {
                    target.classList.add('drag-over');
                    playlist.insertBefore(draggedEl, target);
                } else {
                    target.classList.add('drag-over');
                    playlist.insertBefore(draggedEl, target.nextSibling);
                }
            }
        }
    });

    playlist.addEventListener('dragleave', function(e) {
        // Remove placeholder if leaving playlist entirely
        if (libraryPlaceholder && !playlist.contains(e.relatedTarget)) {
            libraryPlaceholder.remove();
            libraryPlaceholder = null;
        }
    });

    playlist.addEventListener('drop', function(e) {
        e.preventDefault();
        document.querySelectorAll('.drag-over').forEach(x => x.classList.remove('drag-over'));
        if (dragSource === 'library' && libraryPlaceholder) {
            // Finalize placeholder into real item
            libraryPlaceholder.classList.remove('playlist-item-placeholder');
            libraryPlaceholder = null;
        }
        updateState();
    });

    // Library drag
    document.querySelectorAll('.library-asset').forEach(function(el) {
        el.addEventListener('dragstart', function(e) {
            draggedEl = el;
            dragSource = 'library';
            e.dataTransfer.effectAllowed = 'copy';
        });
        el.addEventListener('dragend', function() {
            // Clean up placeholder if drop didn't happen
            if (libraryPlaceholder) {
                libraryPlaceholder.remove();
                libraryPlaceholder = null;
            }
            draggedEl = null;
            dragSource = null;
            updateState();
        });
        // Add button click
        const addBtn = el.querySelector('.library-add-btn');
        if (addBtn) {
            addBtn.addEventListener('click', function() {
                const newItem = createPlaylistItem(
                    el.dataset.assetId,
                    el.dataset.assetName,
                    el.dataset.assetType,
                    el.dataset.assetDuration
                );
                playlist.appendChild(newItem);
                updateState();
            });
        }
    });

    // Library search
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const q = this.value.toLowerCase();
            document.querySelectorAll('.library-asset').forEach(function(el) {
                el.style.display = el.dataset.assetName.toLowerCase().includes(q) ? '' : 'none';
            });
        });
    }

    // Overlap detection
    function checkOverlaps() {
        var items = playlist.querySelectorAll('.playlist-item:not(.playlist-item-placeholder)');
        var hasOverlap = false;
        var arr = [];
        items.forEach(function(el) {
            var s = el.querySelector('.timing-start');
            var e = el.querySelector('.timing-end');
            if (s && e && s.value && e.value) {
                arr.push({start: s.value, end: e.value});
            }
        });
        for (var i = 0; i < arr.length; i++) {
            for (var j = i + 1; j < arr.length; j++) {
                if (arr[i].start < arr[j].end && arr[j].start < arr[i].end) {
                    hasOverlap = true;
                }
            }
        }
        var w = document.getElementById('overlap-warning');
        if (w) w.style.display = hasOverlap ? '' : 'none';
    }

    // Wrap updateState to also check overlaps
    var _origUpdate = updateState;
    updateState = function() {
        _origUpdate();
        checkOverlaps();
    };

    // Autosave indicator
    var autosaveEl = document.getElementById('autosave-indicator');
    var autosaveTimer = null;
    function scheduleAutosave() {
        if (autosaveTimer) clearTimeout(autosaveTimer);
        if (autosaveEl) autosaveEl.textContent = 'Unsaved changes';
        autosaveTimer = setTimeout(function() {
            if (autosaveEl) autosaveEl.textContent = 'Saving...';
            // Submit save form via fetch
            var form = document.getElementById('schedule-form');
            if (form) {
                var formData = new FormData(form);
                fetch(form.action, { method: 'POST', body: formData }).then(function() {
                    if (autosaveEl) autosaveEl.textContent = 'Saved';
                    setTimeout(function() { if (autosaveEl) autosaveEl.textContent = ''; }, 3000);
                }).catch(function() {
                    if (autosaveEl) autosaveEl.textContent = 'Save failed';
                });
            }
        }, 5000);
    }

    // Hook into timing changes for autosave
    playlist.addEventListener('change', function(e) {
        if (e.target.classList.contains('timing-start') || e.target.classList.contains('timing-end') || e.target.classList.contains('day-check')) {
            updateState();
            scheduleAutosave();
        }
    });

    // Validate button
    var validateBtn = document.getElementById('validate-btn');
    if (validateBtn) {
        validateBtn.addEventListener('click', function() {
            // First save current state, then validate
            updateState();
            fetch(window.location.pathname + '/validate', { method: 'POST' })
                .then(function(r) { return r.text(); })
                .then(function(html) {
                    var container = document.getElementById('validation-results');
                    if (container) container.innerHTML = html;
                });
        });
    }

    // Initial state
    updateState();
})();
"""
