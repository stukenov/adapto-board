package com.playoutedge.server.views

import kotlinx.html.*

fun HTML.embedPlayerView(
    channelId: String,
    channelName: String,
    bgColor: String? = null,
    muted: Boolean = true,
    controls: Boolean = false,
    kenburns: Boolean = true,
    shuffle: Boolean = false
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$channelName - Adapto Board" }
        style {
            unsafe {
                +embedPlayerStyles(bgColor)
            }
        }
    }
    body {
        div("embed-container") {
            id = "embed-container"

            // Image layer
            img(classes = "embed-media embed-image") {
                id = "embed-image"
                style = "display:none"
            }

            // Video layer
            video(classes = "embed-media embed-video") {
                id = "embed-video"
                autoPlay = true
                attributes["muted"] = "true"
                attributes["playsinline"] = "true"
                style = "display:none"
            }

            // Crossfade layer (for transitions)
            div("embed-crossfade") {
                id = "crossfade-layer"
                style = "display:none"
            }

            // Overlay container
            div("embed-overlay") {
                id = "overlay-container"
            }

            // Player controls overlay
            div("embed-controls") {
                button {
                    id = "btn-mute"
                    title = "Toggle mute"
                    attributes["onclick"] = "toggleMute()"
                    unsafe { +"""<span id="mute-icon">🔇</span>""" }
                }
                button {
                    id = "btn-fullscreen"
                    title = "Toggle fullscreen"
                    attributes["onclick"] = "toggleFullscreen()"
                    unsafe { +"⛶" }
                }
            }
        }

        // Voiceover audio (hidden)
        audio {
            id = "voiceover-audio"
            attributes["preload"] = "auto"
        }

        script {
            unsafe {
                +"""
                var EMBED_MUTED = $muted;
                var EMBED_CONTROLS = $controls;
                var EMBED_KENBURNS = $kenburns;
                var EMBED_SHUFFLE = $shuffle;

                function toggleFullscreen() {
                    if (!document.fullscreenElement) {
                        document.documentElement.requestFullscreen().catch(function(){});
                    } else {
                        document.exitFullscreen().catch(function(){});
                    }
                }

                function toggleMute() {
                    var video = document.getElementById('embed-video');
                    var voiceover = document.getElementById('voiceover-audio');
                    EMBED_MUTED = !EMBED_MUTED;
                    video.muted = EMBED_MUTED;
                    voiceover.muted = EMBED_MUTED;
                    document.getElementById('mute-icon').textContent = EMBED_MUTED ? '🔇' : '🔊';
                }
                """
                +embedPlayerScript(channelId)
            }
        }
    }
}

private fun embedPlayerStyles(bgColor: String? = null): String {
    val bg = bgColor?.let { if (it.matches(Regex("^[a-fA-F0-9]{3,8}$"))) "#$it" else it } ?: "#000"
    return """
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body { width: 100%; height: 100%; overflow: hidden; background: $bg; }

.embed-container {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
}

.embed-media {
    position: absolute;
    top: 0; left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.embed-image {
    z-index: 1;
    transition: opacity 1s ease-in-out;
}

.embed-video {
    z-index: 2;
}

.embed-crossfade {
    position: absolute;
    top: 0; left: 0;
    width: 100%;
    height: 100%;
    z-index: 3;
    background-size: cover;
    background-position: center;
    transition: opacity 1s ease-in-out;
    pointer-events: none;
}

.embed-overlay {
    position: absolute;
    top: 0; left: 0;
    width: 100%;
    height: 100%;
    z-index: 10;
    pointer-events: none;
}

/* Ken Burns Animations */
@keyframes kenburns-zoom-in {
    0% { transform: scale(1) translate(0, 0); }
    100% { transform: scale(1.15) translate(-2%, -1%); }
}
@keyframes kenburns-zoom-out {
    0% { transform: scale(1.15) translate(-2%, -1%); }
    100% { transform: scale(1) translate(0, 0); }
}
@keyframes kenburns-pan-left {
    0% { transform: scale(1.1) translate(2%, 0); }
    100% { transform: scale(1.1) translate(-2%, 0); }
}
@keyframes kenburns-pan-right {
    0% { transform: scale(1.1) translate(-2%, 0); }
    100% { transform: scale(1.1) translate(2%, 0); }
}
@keyframes kenburns-pan-up {
    0% { transform: scale(1.1) translate(0, 2%); }
    100% { transform: scale(1.1) translate(0, -2%); }
}

.kenburns-zoom-in { animation: kenburns-zoom-in 10s ease-in-out forwards; }
.kenburns-zoom-out { animation: kenburns-zoom-out 10s ease-in-out forwards; }
.kenburns-pan-left { animation: kenburns-pan-left 10s ease-in-out forwards; }
.kenburns-pan-right { animation: kenburns-pan-right 10s ease-in-out forwards; }
.kenburns-pan-up { animation: kenburns-pan-up 10s ease-in-out forwards; }

.embed-image.fading-out { opacity: 0; }
"""
}

private fun embedPlayerScript(channelId: String): String = """
(function() {
    const MANIFEST_URL = '/embed/$channelId/manifest.json';
    const REFRESH_INTERVAL = 60000;

    const imageEl = document.getElementById('embed-image');
    const videoEl = document.getElementById('embed-video');
    const voiceoverEl = document.getElementById('voiceover-audio');

    const kenburnsEffects = ['kenburns-zoom-in', 'kenburns-zoom-out', 'kenburns-pan-left', 'kenburns-pan-right', 'kenburns-pan-up'];

    let playlist = [];
    let currentIndex = -1;
    let timer = null;

    let retryCount = 0;
    const MAX_RETRIES = 10;

    function showError(msg) {
        const container = document.getElementById('embed-container');
        let errEl = document.getElementById('embed-error');
        if (!errEl) {
            errEl = document.createElement('div');
            errEl.id = 'embed-error';
            errEl.style.cssText = 'position:absolute;inset:0;z-index:20;display:flex;flex-direction:column;align-items:center;justify-content:center;color:#999;font-family:sans-serif;text-align:center;padding:20px;';
            container.appendChild(errEl);
        }
        errEl.innerHTML = '<div style="font-size:48px;margin-bottom:16px">📺</div><div style="font-size:16px">' + msg + '</div><div style="font-size:12px;margin-top:8px;color:#666">Retrying automatically...</div>';
    }

    function hideError() {
        const errEl = document.getElementById('embed-error');
        if (errEl) errEl.remove();
    }

    async function loadManifest() {
        try {
            const res = await fetch(MANIFEST_URL);
            if (!res.ok) throw new Error('HTTP ' + res.status);
            const data = await res.json();
            if (data.items && data.items.length > 0) {
                playlist = data.items;
                if (EMBED_SHUFFLE) {
                    for (var i = playlist.length - 1; i > 0; i--) {
                        var j = Math.floor(Math.random() * (i + 1));
                        var tmp = playlist[i]; playlist[i] = playlist[j]; playlist[j] = tmp;
                    }
                }
                retryCount = 0;
                hideError();
                if (currentIndex < 0) {
                    currentIndex = 0;
                    playItem(currentIndex);
                }
            } else {
                showError('No content available');
            }
        } catch (e) {
            console.error('Failed to load manifest:', e);
            retryCount++;
            if (playlist.length === 0) {
                showError('Unable to load content');
            }
            if (retryCount < MAX_RETRIES) {
                setTimeout(loadManifest, Math.min(retryCount * 5000, 30000));
            }
        }
    }

    function playItem(index) {
        if (playlist.length === 0) return;
        const item = playlist[index];

        if (item.type === 'VIDEO') {
            showVideo(item);
        } else {
            showImage(item);
        }

        // Play voiceover if available
        if (item.voiceoverUrl) {
            voiceoverEl.src = item.voiceoverUrl;
            voiceoverEl.play().catch(() => {});
        }
    }

    function showVideo(item) {
        imageEl.style.display = 'none';
        videoEl.style.display = 'block';
        videoEl.src = item.url;
        videoEl.muted = EMBED_MUTED;
        videoEl.controls = EMBED_CONTROLS;
        videoEl.play().catch(() => { videoEl.muted = true; videoEl.play(); });

        videoEl.onended = function() {
            advance();
        };

        // Fallback timeout
        if (timer) clearTimeout(timer);
        const duration = item.durationMs || 30000;
        timer = setTimeout(advance, duration + 2000);
    }

    function showImage(item) {
        videoEl.style.display = 'none';
        videoEl.pause();

        // Remove previous Ken Burns class
        kenburnsEffects.forEach(c => imageEl.classList.remove(c));

        imageEl.src = item.url;
        imageEl.style.display = 'block';
        imageEl.style.opacity = '1';

        // Apply random Ken Burns effect if enabled
        if (EMBED_KENBURNS) {
            const effect = kenburnsEffects[Math.floor(Math.random() * kenburnsEffects.length)];
            void imageEl.offsetWidth; // force reflow
            imageEl.classList.add(effect);
        }

        const duration = item.durationMs || 10000;
        if (timer) clearTimeout(timer);
        timer = setTimeout(advance, duration);
    }

    function advance() {
        if (timer) clearTimeout(timer);
        currentIndex = (currentIndex + 1) % playlist.length;
        playItem(currentIndex);
    }

    // Initial load
    loadManifest();

    // Periodic refresh
    setInterval(loadManifest, REFRESH_INTERVAL);
})();
"""
