package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.dangerItem
import com.playoutedge.server.views.dangerZone
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.icon
import kotlinx.html.*

/**
 * Asset detail view with improved layout.
 */
fun HTML.assetDetailView(
    session: AdminClaims,
    asset: AssetDetailModel
) {
    adminLayout(title = asset.name, userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = asset.name,
            backHref = "/admin/assets",
            backLabel = "Back to Assets"
        ) {
            span("badge badge-${assetStatusBadge(asset.status)} badge-lg") {
                +asset.status.name.lowercase()
            }
        }

        // Rejection reason alert
        if (asset.status == AssetStatus.REJECTED && asset.rejectionReason != null) {
            alertBox("Rejected: ${asset.rejectionReason}", "error")
        }

        // Preview section
        if (asset.status == AssetStatus.READY && asset.storageKey != null) {
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Preview" }
                    if (asset.type == AssetType.SLIDESHOW) {
                        a(href = "/admin/assets/slideshow/${asset.id}/edit", classes = "btn btn-secondary btn-sm") {
                            +"Edit Slideshow"
                        }
                    }
                }
                div("card-body") {
                    div {
                        id = "preview-container"
                        style = "max-width:800px"
                        when (asset.type) {
                            AssetType.IMAGE -> {
                                img {
                                    id = "preview-img"
                                    src = "/storage/${asset.id}"
                                    alt = asset.name
                                    style = "max-width:100%;max-height:500px;border-radius:8px;display:block"
                                }
                            }
                            AssetType.VIDEO -> {
                                video {
                                    id = "preview-video"
                                    controls = true
                                    style = "max-width:100%;max-height:500px;border-radius:8px;background:#000"
                                    source {
                                        src = "/storage/${asset.id}"
                                        type = asset.mimeType ?: "video/mp4"
                                    }
                                    +"Your browser does not support video playback."
                                }
                            }
                            AssetType.AUDIO -> {
                                div {
                                    style = "text-align:center;padding:40px 0"
                                    div {
                                        style = "font-size:64px;margin-bottom:16px"
                                        +"🎵"
                                    }
                                    audio {
                                        id = "preview-audio"
                                        controls = true
                                        style = "width:100%;max-width:500px"
                                        source {
                                            src = "/storage/${asset.id}"
                                            type = asset.mimeType ?: "audio/mpeg"
                                        }
                                        +"Your browser does not support audio playback."
                                    }
                                }
                            }
                            AssetType.SLIDESHOW -> {
                                div {
                                    id = "slideshow-player"
                                    style = "width:100%;aspect-ratio:16/9;background:#000;border-radius:8px;overflow:hidden;position:relative"
                                }
                                div("mt-2") {
                                    style = "display:flex;gap:8px;align-items:center"
                                    button(type = ButtonType.button, classes = "btn btn-primary btn-sm") {
                                        id = "play-btn"
                                        +"Play"
                                    }
                                    button(type = ButtonType.button, classes = "btn btn-secondary btn-sm") {
                                        id = "stop-btn"
                                        style = "display:none"
                                        +"Stop"
                                    }
                                    span {
                                        id = "slide-indicator"
                                        style = "font-size:13px;color:var(--text-muted,#999)"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Slideshow player script
        if (asset.type == AssetType.SLIDESHOW && asset.slideshowDefinition != null) {
            script {
                unsafe {
                    +"""
(function() {
    var def = ${asset.slideshowDefinition};
    var slides = def.slides || [];
    var audioId = def.audioAssetId;
    var player = document.getElementById('slideshow-player');
    var playBtn = document.getElementById('play-btn');
    var stopBtn = document.getElementById('stop-btn');
    var indicator = document.getElementById('slide-indicator');
    var timer = null;
    var audioEl = null;
    var idx = 0;

    if (slides.length === 0) {
        player.innerHTML = '<p style="color:#999;text-align:center;padding:40px">No slides</p>';
        return;
    }

    // Preload first slide
    player.innerHTML = '<img src="/storage/' + slides[0].assetId + '" style="width:100%;height:100%;object-fit:contain">';
    indicator.textContent = slides.length + ' slides, ' + (def.totalDurationMs / 1000) + 's total';

    function showSlide() {
        if (idx >= slides.length) { stopPlayback(); return; }
        var s = slides[idx];
        var anim = s.transition === 'fade' ? 'slideshowFadeIn 0.5s ease-in' :
                   s.transition === 'slide' ? 'slideshowSlideIn 0.5s ease-out' : 'none';
        player.innerHTML = '<img src="/storage/' + s.assetId + '" style="width:100%;height:100%;object-fit:contain;position:absolute;top:0;left:0;animation:' + anim + '">';
        indicator.textContent = 'Slide ' + (idx + 1) + ' / ' + slides.length;
        idx++;
        timer = setTimeout(showSlide, s.durationMs);
    }

    function stopPlayback() {
        clearTimeout(timer);
        timer = null;
        if (audioEl) { audioEl.pause(); audioEl.currentTime = 0; }
        playBtn.style.display = '';
        stopBtn.style.display = 'none';
        idx = 0;
        indicator.textContent = slides.length + ' slides, ' + (def.totalDurationMs / 1000) + 's total';
    }

    playBtn.onclick = function() {
        idx = 0;
        playBtn.style.display = 'none';
        stopBtn.style.display = '';
        if (audioId) {
            if (!audioEl) {
                audioEl = new Audio('/storage/' + audioId);
            }
            audioEl.currentTime = 0;
            audioEl.play().catch(function(){});
        }
        showSlide();
    };
    stopBtn.onclick = stopPlayback;

    // CSS animations
    if (!document.getElementById('slideshow-anims')) {
        var style = document.createElement('style');
        style.id = 'slideshow-anims';
        style.textContent = '@keyframes slideshowFadeIn{from{opacity:0}to{opacity:1}}@keyframes slideshowSlideIn{from{transform:translateX(100%)}to{transform:translateX(0)}}';
        document.head.appendChild(style);
    }
})();
"""
                }
            }
        }

        // Two-column layout: preview already above, metadata in sidebar
        detailLayout(
            main = {
                sectionCard("Information") {
                    dl("info-list") {
                        dt { +"Type" }
                        dd {
                            span("badge badge-gray badge-plain") {
                                when (asset.type) {
                                    AssetType.VIDEO -> { icon("film"); +" video" }
                                    AssetType.IMAGE -> { icon("image"); +" image" }
                                    AssetType.AUDIO -> { icon("music"); +" audio" }
                                    AssetType.SLIDESHOW -> { icon("play"); +" slideshow" }
                                }
                            }
                        }
                        dt { +"MIME Type" }
                        dd { asset.mimeType?.let { code { +it } } ?: span("text-muted") { +"—" } }
                        dt { +"File Size" }
                        dd { +asset.fileSizeFormatted }
                        dt { +"Resolution" }
                        dd { asset.resolution?.let { +it } ?: span("text-muted") { +"—" } }
                        dt { +"Duration" }
                        dd { asset.durationFormatted?.let { +it } ?: span("text-muted") { +"—" } }
                        dt { +"Created" }
                        dd { +asset.createdAt.toString().replace("T", " ").substringBeforeLast(":") }
                        dt { +"Asset ID" }
                        dd { code { +asset.id.toString() } }
                    }
                }
            },
            sidebar = {
                sectionCard("Usage") {
                    p("text-muted") { +"View channels to see where this asset is used." }
                }

                if (asset.status != AssetStatus.ARCHIVED) {
                    sectionCard("Danger Zone") {
                        dangerZone {
                            dangerItem(
                                title = "Archive Asset",
                                description = "Remove this asset from active use."
                            ) {
                                form(action = "/admin/assets/${asset.id}/archive", method = FormMethod.post) {
                                    button(type = ButtonType.submit, classes = "btn btn-danger") {
                                        attributes["onclick"] = "return confirm('Are you sure you want to archive this asset?')"
                                        +"Archive"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

private fun assetStatusBadge(status: AssetStatus): String = when (status) {
    AssetStatus.UPLOADING -> "warning"
    AssetStatus.PROCESSING -> "warning"
    AssetStatus.READY -> "success"
    AssetStatus.REJECTED -> "danger"
    AssetStatus.ARCHIVED -> "gray"
}
