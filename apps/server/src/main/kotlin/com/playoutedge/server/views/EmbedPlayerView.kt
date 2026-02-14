package com.playoutedge.server.views

import kotlinx.html.*

fun HTML.embedPlayerView(channelId: String, channelName: String) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$channelName - Adapto Board" }
        style {
            unsafe {
                +embedPlayerStyles()
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
        }

        // Voiceover audio (hidden)
        audio {
            id = "voiceover-audio"
            attributes["preload"] = "auto"
        }

        script {
            unsafe {
                +embedPlayerScript(channelId)
            }
        }
    }
}

private fun embedPlayerStyles(): String = """
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body { width: 100%; height: 100%; overflow: hidden; background: #000; }

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

    async function loadManifest() {
        try {
            const res = await fetch(MANIFEST_URL);
            const data = await res.json();
            if (data.items && data.items.length > 0) {
                playlist = data.items;
                if (currentIndex < 0) {
                    currentIndex = 0;
                    playItem(currentIndex);
                }
            }
        } catch (e) {
            console.error('Failed to load manifest:', e);
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
        videoEl.muted = false;
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

        // Apply random Ken Burns effect
        const effect = kenburnsEffects[Math.floor(Math.random() * kenburnsEffects.length)];
        void imageEl.offsetWidth; // force reflow
        imageEl.classList.add(effect);

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
