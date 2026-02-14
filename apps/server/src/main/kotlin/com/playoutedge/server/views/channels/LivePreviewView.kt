package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

/**
 * Live preview item for timeline.
 */
data class LivePreviewItem(
    val assetId: UUID,
    val assetName: String,
    val assetType: AssetType = AssetType.VIDEO,
    val assetStorageKey: String = "",
    val assetUrl: String = "",
    val durationMs: Int?,
    val orderIndex: Int,
    val timeStart: String?,
    val timeEnd: String?
)

/**
 * Live preview page with real media playback.
 */
fun HTML.livePreviewView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    scheduleVersion: Int?,
    items: List<LivePreviewItem>,
    currentIndex: Int,
    currentTimeFormatted: String
) {
    adminLayout(title = "Live Preview - $channelName", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Live Preview",
            subtitle = channelName,
            backHref = "/admin/channels/$channelId",
            backLabel = "Back to Channel"
        ) {
            if (scheduleVersion != null) {
                span("badge badge-success mr-2") { +"v$scheduleVersion" }
            }
            span("badge badge-info") {
                id = "current-time"
                +currentTimeFormatted
            }
        }

        if (items.isEmpty()) {
            div("card") {
                div("card-body") {
                    emptyState(
                        icon = "📺",
                        title = "No active schedule",
                        description = "Publish a schedule to see the live preview.",
                        actionHref = "/admin/channels/$channelId/schedule",
                        actionLabel = "Edit Schedule"
                    )
                }
            }
        } else {
            // Full embed preview with overlay support
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Embed Preview (with Overlays)" }
                }
                div("card-body") {
                    div {
                        style = "position:relative;width:100%;padding-top:56.25%;background:#000;border-radius:8px;overflow:hidden;"
                        iframe {
                            src = "/embed/$channelId"
                            style = "position:absolute;top:0;left:0;width:100%;height:100%;border:none;"
                            attributes["allow"] = "autoplay"
                        }
                    }
                }
            }

            // Preview player
            div("preview-player card mb-4") {
                div("preview-player-screen") {
                    id = "player-screen"
                    // Video element (hidden by default)
                    video("preview-media") {
                        id = "preview-video"
                        attributes["muted"] = "true"
                        attributes["playsinline"] = "true"
                        attributes["style"] = "display:none"
                    }
                    // Image element (hidden by default)
                    img(classes = "preview-media") {
                        id = "preview-image"
                        attributes["style"] = "display:none"
                    }
                }
                div("preview-player-actions") {
                    button(classes = "btn btn-sm btn-secondary") {
                        id = "btn-preview-unmute"
                        attributes["onclick"] = """
                            var v = document.getElementById('preview-video');
                            v.muted = !v.muted;
                            this.textContent = v.muted ? '🔇 Unmute' : '🔊 Mute';
                        """.trimIndent()
                        +"🔇 Unmute"
                    }
                    button(classes = "btn btn-sm btn-secondary") {
                        id = "btn-preview-fullscreen"
                        attributes["onclick"] = """
                            var screen = document.getElementById('player-screen');
                            if (!document.fullscreenElement) { screen.requestFullscreen().catch(function(){}); }
                            else { document.exitFullscreen().catch(function(){}); }
                        """.trimIndent()
                        +"⛶ Fullscreen"
                    }
                }
                div("preview-player-controls") {
                    div("preview-player-info") {
                        span("now-playing-badge") { +"NOW PLAYING" }
                        span("preview-asset-name") {
                            id = "playing-name"
                            +if (currentIndex in items.indices) items[currentIndex].assetName else ""
                        }
                        span("preview-counter") {
                            id = "playing-counter"
                            +"${currentIndex + 1} / ${items.size}"
                        }
                    }
                    div("preview-progress-bar") {
                        div("preview-progress-fill") {
                            id = "progress-fill"
                        }
                    }
                    div("preview-player-time") {
                        span {
                            id = "playing-elapsed"
                            +"0:00"
                        }
                        span {
                            id = "playing-duration"
                            +""
                        }
                    }
                }
            }

            // Timeline
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Timeline" }
                }
                div("card-body") {
                    div("timeline") {
                        id = "timeline"
                        val totalDuration = items.mapNotNull { it.durationMs }.sum().coerceAtLeast(1)
                        items.forEachIndexed { index, item ->
                            val width = ((item.durationMs ?: (totalDuration / items.size)) * 100.0 / totalDuration)
                            div("timeline-segment") {
                                attributes["data-index"] = index.toString()
                                style = "width: ${width.coerceAtLeast(2.0)}%"
                                title = "${item.assetName} (${item.durationMs?.let { "${it/1000}s" } ?: "?"})"
                                span("timeline-segment-label") {
                                    +item.assetName.take(15)
                                }
                            }
                        }
                    }
                }
            }

            // Full playlist
            div("card") {
                div("card-header") {
                    h3 { +"Schedule Items" }
                }
                table("table") {
                    thead {
                        tr {
                            th { +"#" }
                            th { +"Asset" }
                            th { +"Type" }
                            th { +"Duration" }
                            th { +"Time Window" }
                            th { +"Status" }
                        }
                    }
                    tbody {
                        id = "playlist-table"
                        items.forEachIndexed { index, item ->
                            tr {
                                attributes["data-index"] = index.toString()
                                td { +"${index + 1}" }
                                td { +item.assetName }
                                td { +item.assetType.name.lowercase() }
                                td {
                                    item.durationMs?.let { ms ->
                                        val s = ms / 1000
                                        +"${s / 60}:${String.format("%02d", s % 60)}"
                                    } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    if (item.timeStart != null || item.timeEnd != null) {
                                        +"${item.timeStart ?: "00:00"} - ${item.timeEnd ?: "23:59"}"
                                    } else {
                                        span("text-muted") { +"All day" }
                                    }
                                }
                                td("status-cell") { +"" }
                            }
                        }
                    }
                }
            }
        }

        // Player script with playlist data
        script {
            unsafe {
                +livePreviewScript(items, currentIndex)
            }
        }
    }
}

private fun livePreviewScript(items: List<LivePreviewItem>, serverCurrentIndex: Int): String {
    val itemsJson = items.joinToString(",") { item ->
        """{"url":"${item.assetUrl.replace("\"", "\\\"")}","name":"${item.assetName.replace("\"", "\\\"")}","type":"${item.assetType.name}","durationMs":${item.durationMs ?: 10000}}"""
    }

    return """
(function() {
    var playlist = [$itemsJson];
    if (playlist.length === 0) return;

    var video = document.getElementById('preview-video');
    var image = document.getElementById('preview-image');
    var nameEl = document.getElementById('playing-name');
    var counterEl = document.getElementById('playing-counter');
    var elapsedEl = document.getElementById('playing-elapsed');
    var durationEl = document.getElementById('playing-duration');
    var progressEl = document.getElementById('progress-fill');
    var timeEl = document.getElementById('current-time');

    var currentIdx = $serverCurrentIndex;
    var imageTimer = null;
    var imageStartTime = 0;

    function formatTime(ms) {
        var s = Math.floor(ms / 1000);
        var m = Math.floor(s / 60);
        s = s % 60;
        return m + ':' + (s < 10 ? '0' : '') + s;
    }

    function updateClock() {
        var now = new Date();
        timeEl.textContent = now.getHours() + ':' + (now.getMinutes() < 10 ? '0' : '') + now.getMinutes();
    }

    function updateUI() {
        var item = playlist[currentIdx];
        nameEl.textContent = item.name;
        counterEl.textContent = (currentIdx + 1) + ' / ' + playlist.length;
        durationEl.textContent = formatTime(item.durationMs);

        // Update timeline
        document.querySelectorAll('.timeline-segment').forEach(function(seg) {
            seg.classList.toggle('active', parseInt(seg.dataset.index) === currentIdx);
        });

        // Update table
        document.querySelectorAll('#playlist-table tr').forEach(function(row) {
            var idx = parseInt(row.dataset.index);
            var cell = row.querySelector('.status-cell');
            var nextIdx = (currentIdx + 1) % playlist.length;
            if (idx === currentIdx) {
                row.className = 'table-row-active';
                cell.innerHTML = '<span class="badge badge-success">Playing</span>';
            } else if (idx === nextIdx) {
                row.className = '';
                cell.innerHTML = '<span class="badge badge-info">Next</span>';
            } else {
                row.className = '';
                cell.innerHTML = '<span class="badge badge-gray">Queued</span>';
            }
        });
    }

    function playItem(index) {
        currentIdx = index % playlist.length;
        var item = playlist[currentIdx];

        if (imageTimer) { clearInterval(imageTimer); imageTimer = null; }

        updateUI();

        if (item.type === 'VIDEO') {
            image.style.display = 'none';
            video.style.display = '';
            video.src = item.url;
            video.load();
            video.play().catch(function() {});
        } else {
            video.pause();
            video.style.display = 'none';
            image.style.display = '';
            image.src = item.url;

            imageStartTime = Date.now();
            var dur = item.durationMs;
            imageTimer = setInterval(function() {
                var elapsed = Date.now() - imageStartTime;
                elapsedEl.textContent = formatTime(elapsed);
                var pct = Math.min(100, (elapsed / dur) * 100);
                progressEl.style.width = pct + '%';
                if (elapsed >= dur) {
                    clearInterval(imageTimer);
                    imageTimer = null;
                    playItem(currentIdx + 1);
                }
            }, 250);
        }
    }

    // Video progress tracking
    video.addEventListener('timeupdate', function() {
        var elapsed = video.currentTime * 1000;
        var dur = playlist[currentIdx].durationMs;
        elapsedEl.textContent = formatTime(elapsed);
        var pct = dur > 0 ? Math.min(100, (elapsed / dur) * 100) : 0;
        progressEl.style.width = pct + '%';
    });

    // Video ended -> next item
    video.addEventListener('ended', function() {
        playItem(currentIdx + 1);
    });

    // Clock update
    setInterval(updateClock, 10000);
    updateClock();

    // Start playback
    playItem(currentIdx);
})();
"""
}
