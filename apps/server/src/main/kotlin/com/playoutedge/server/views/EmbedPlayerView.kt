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

/* === Overlay Widget Styles === */
.embed-overlay { font-family: 'Segoe UI', Arial, sans-serif; }

/* Ticker */
.overlay-ticker {
    position: absolute; bottom: 0; left: 0; width: 100%;
    height: 48px; background: rgba(0,0,0,0.85); color: #fff;
    display: flex; align-items: center; overflow: hidden;
    pointer-events: auto;
}
.overlay-ticker .ticker-label {
    background: #e50914; color: #fff; font-weight: 700; font-size: 14px;
    padding: 0 16px; height: 100%; display: flex; align-items: center;
    white-space: nowrap; flex-shrink: 0; text-transform: uppercase;
}
.overlay-ticker .ticker-track {
    flex: 1; overflow: hidden; position: relative; height: 100%;
    display: flex; align-items: center;
}
.overlay-ticker .ticker-text {
    white-space: nowrap; font-size: 16px; font-weight: 500;
    animation: ticker-scroll linear infinite;
    padding-left: 100%;
}
@keyframes ticker-scroll {
    0% { transform: translateX(0); }
    100% { transform: translateX(-100%); }
}

/* KPI Tiles */
.overlay-kpi-grid {
    position: absolute; display: grid; gap: 8px; padding: 12px;
}
.overlay-kpi-tile {
    background: rgba(0,0,0,0.8); border-radius: 8px; padding: 12px 16px;
    color: #fff; text-align: center; backdrop-filter: blur(8px);
    border: 1px solid rgba(255,255,255,0.1);
}
.overlay-kpi-tile .kpi-value {
    font-size: 28px; font-weight: 700; line-height: 1.2;
}
.overlay-kpi-tile .kpi-label {
    font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px;
    opacity: 0.7; margin-top: 4px;
}

/* Clock */
.overlay-clock {
    position: absolute; background: rgba(0,0,0,0.75); color: #fff;
    border-radius: 8px; padding: 8px 16px; backdrop-filter: blur(8px);
    font-size: 24px; font-weight: 600; font-variant-numeric: tabular-nums;
}

/* QR Card */
.overlay-qr-card {
    position: absolute; background: #fff; border-radius: 12px;
    padding: 16px; text-align: center; box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
.overlay-qr-card canvas { display: block; margin: 0 auto; }
.overlay-qr-card .qr-text {
    margin-top: 8px; font-size: 12px; color: #333; font-weight: 600;
}

/* Table */
.overlay-table {
    position: absolute; background: rgba(0,0,0,0.85); color: #fff;
    border-radius: 8px; padding: 12px; backdrop-filter: blur(8px);
    max-width: 500px;
}
.overlay-table table { width: 100%; border-collapse: collapse; font-size: 13px; }
.overlay-table th {
    text-align: left; padding: 6px 8px; border-bottom: 1px solid rgba(255,255,255,0.2);
    font-weight: 600; text-transform: uppercase; font-size: 11px; opacity: 0.7;
}
.overlay-table td { padding: 6px 8px; border-bottom: 1px solid rgba(255,255,255,0.08); }

/* Poll */
.overlay-poll {
    position: absolute; background: rgba(0,0,0,0.85); color: #fff;
    border-radius: 12px; padding: 16px; backdrop-filter: blur(8px);
    min-width: 280px;
}
.overlay-poll .poll-question { font-size: 16px; font-weight: 700; margin-bottom: 12px; }
.overlay-poll .poll-option { margin-bottom: 8px; }
.overlay-poll .poll-bar-bg {
    background: rgba(255,255,255,0.15); border-radius: 4px; height: 28px;
    position: relative; overflow: hidden;
}
.overlay-poll .poll-bar-fill {
    height: 100%; border-radius: 4px; transition: width 0.5s ease;
    background: linear-gradient(90deg, #667eea, #764ba2);
}
.overlay-poll .poll-bar-label {
    position: absolute; top: 0; left: 8px; right: 8px; height: 28px;
    display: flex; align-items: center; justify-content: space-between;
    font-size: 13px; font-weight: 500;
}

/* Reactions */
.overlay-reactions {
    position: absolute; pointer-events: auto;
}
.overlay-reaction-emoji {
    position: absolute; font-size: 32px; animation: reaction-float 3s ease-out forwards;
    opacity: 0;
}
@keyframes reaction-float {
    0% { opacity: 1; transform: translateY(0) scale(1); }
    100% { opacity: 0; transform: translateY(-120px) scale(0.5); }
}

/* Weather */
.overlay-weather {
    position: absolute; background: rgba(0,0,0,0.75); color: #fff;
    border-radius: 8px; padding: 12px 16px; backdrop-filter: blur(8px);
    display: flex; align-items: center; gap: 12px;
}
.overlay-weather .weather-icon { font-size: 32px; }
.overlay-weather .weather-temp { font-size: 24px; font-weight: 700; }
.overlay-weather .weather-desc { font-size: 12px; opacity: 0.7; }

/* News Ticker - same as ticker but with alternating items */
.overlay-news-ticker { composes: overlay-ticker; }

/* Position helpers */
.pos-top-left { top: 16px; left: 16px; }
.pos-top-right { top: 16px; right: 16px; }
.pos-top-center { top: 16px; left: 50%; transform: translateX(-50%); }
.pos-bottom-left { bottom: 60px; left: 16px; }
.pos-bottom-right { bottom: 60px; right: 16px; }
.pos-center { top: 50%; left: 50%; transform: translate(-50%, -50%); }
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

    // === OVERLAY RENDERER ===
    const OVERLAY_URL = '/embed/$channelId/overlay.json';
    const OVERLAY_POLL_MS = 5000;
    const overlayContainer = document.getElementById('overlay-container');
    let lastOverlayJson = '';

    async function loadOverlay() {
        try {
            const res = await fetch(OVERLAY_URL);
            if (!res.ok) return;
            const text = await res.text();
            if (text === lastOverlayJson || text === '{}') return;
            lastOverlayJson = text;
            const state = JSON.parse(text);
            renderOverlay(state);
        } catch(e) { console.error('Overlay error:', e); }
    }

    function renderOverlay(state) {
        overlayContainer.innerHTML = '';
        if (!state.widgets) return;

        // widgets can be array or object
        const widgets = Array.isArray(state.widgets)
            ? state.widgets
            : Object.values(state.widgets);

        widgets.forEach(function(w) {
            const el = createWidget(w, state);
            if (el) overlayContainer.appendChild(el);
        });
    }

    function posClass(pos) {
        if (!pos) return 'pos-bottom-right';
        return 'pos-' + pos.replace(/([A-Z])/g, '-$1').toLowerCase()
            .replace(/\s+/g, '-').replace('_', '-');
    }

    function createWidget(w, state) {
        var type = (w.type || '').toLowerCase();

        if (type === 'ticker' || type === 'news-ticker') return createTicker(w, state);
        if (type === 'kpi-grid' || type === 'kpi' || type === 'kpi_tiles') return createKpiGrid(w, state);
        if (type === 'clock') return createClock(w);
        if (type === 'table' || type === 'queue-table' || type === 'queue_table') return createTable(w, state);
        if (type === 'qr-card' || type === 'qr_card') return createQrCard(w);
        if (type === 'poll') return createPoll(w, state);
        if (type === 'reactions') return createReactions(w, state);
        if (type === 'weather') return createWeather(w, state);
        return null;
    }

    function createTicker(w, state) {
        var el = document.createElement('div');
        el.className = 'overlay-ticker';
        var text = w.text || state.ticker_text || state.tickerText || '';
        var items = w.items || state.ticker_items || [];
        if (items.length > 0) text = items.join('  ●  ');
        if (!text) return null;

        var label = w.label || state.ticker_label || '';
        var speed = w.scrollSpeed || 60;
        var dur = Math.max(text.length * 0.15, 10);

        if (label) el.innerHTML = '<div class="ticker-label">' + esc(label) + '</div>';
        el.innerHTML += '<div class="ticker-track"><span class="ticker-text" style="animation-duration:' + dur + 's">' + esc(text) + '</span></div>';
        return el;
    }

    function createKpiGrid(w, state) {
        var kpis = w.kpis || w.tiles || state.kpis || [];
        if (!kpis.length) return null;
        var cols = w.columns || 2;
        var el = document.createElement('div');
        el.className = 'overlay-kpi-grid ' + posClass(w.position);
        el.style.gridTemplateColumns = 'repeat(' + cols + ', 140px)';
        kpis.forEach(function(kpi) {
            var tile = document.createElement('div');
            tile.className = 'overlay-kpi-tile';
            var color = kpi.color ? 'color:' + kpi.color : '';
            tile.innerHTML = '<div class="kpi-value" style="' + color + '">' + esc(String(kpi.value || '0')) + '</div>'
                + '<div class="kpi-label">' + esc(kpi.label || '') + '</div>';
            el.appendChild(tile);
        });
        return el;
    }

    function createClock(w) {
        var el = document.createElement('div');
        el.className = 'overlay-clock ' + posClass(w.position || 'top-right');
        var tz = w.timezone || 'Asia/Almaty';
        var fmt = w.format || '24h';
        function tick() {
            try {
                var opts = { timeZone: tz, hour: '2-digit', minute: '2-digit', second: '2-digit' };
                if (fmt === '12h') opts.hour12 = true;
                else opts.hour12 = false;
                el.textContent = new Date().toLocaleTimeString('en-GB', opts);
            } catch(e) { el.textContent = new Date().toLocaleTimeString(); }
        }
        tick();
        setInterval(tick, 1000);
        return el;
    }

    function createTable(w, state) {
        var rows = w.rows || state.rows || state.queue || [];
        var columns = w.columns || [];
        if (!rows.length) return null;
        var el = document.createElement('div');
        el.className = 'overlay-table ' + posClass(w.position || 'center');
        var html = '<table>';
        if (columns.length) {
            html += '<thead><tr>';
            columns.forEach(function(c) { html += '<th>' + esc(String(c)) + '</th>'; });
            html += '</tr></thead>';
        }
        html += '<tbody>';
        rows.forEach(function(row) {
            html += '<tr>';
            if (Array.isArray(row)) {
                row.forEach(function(cell) { html += '<td>' + esc(String(cell)) + '</td>'; });
            } else if (columns.length) {
                columns.forEach(function(c) { html += '<td>' + esc(String(row[c] || '')) + '</td>'; });
            } else {
                Object.values(row).forEach(function(v) { html += '<td>' + esc(String(v)) + '</td>'; });
            }
            html += '</tr>';
        });
        html += '</tbody></table>';
        el.innerHTML = html;
        return el;
    }

    function createQrCard(w) {
        var url = w.url || w.data || '';
        var text = w.text || w.cta || '';
        var size = w.size || 120;
        if (!url) return null;
        var el = document.createElement('div');
        el.className = 'overlay-qr-card ' + posClass(w.position || 'bottom-right');
        // Simple QR using a public API (no external lib needed)
        el.innerHTML = '<img src="https://api.qrserver.com/v1/create-qr-code/?size=' + size + 'x' + size + '&data=' + encodeURIComponent(url) + '" width="' + size + '" height="' + size + '">'
            + (text ? '<div class="qr-text">' + esc(text) + '</div>' : '');
        return el;
    }

    function createPoll(w, state) {
        var question = w.question || state.poll?.question || '';
        var options = w.options || state.poll?.options || [];
        var votes = w.votes || state.poll?.votes || [];
        if (!options.length) return null;
        var total = votes.reduce(function(a,b) { return a + (b||0); }, 0) || 1;
        var el = document.createElement('div');
        el.className = 'overlay-poll ' + posClass(w.position || 'center');
        var html = '<div class="poll-question">' + esc(question) + '</div>';
        options.forEach(function(opt, i) {
            var pct = Math.round(((votes[i]||0) / total) * 100);
            html += '<div class="poll-option"><div class="poll-bar-bg">'
                + '<div class="poll-bar-fill" style="width:' + pct + '%"></div>'
                + '<div class="poll-bar-label"><span>' + esc(String(opt)) + '</span><span>' + pct + '%</span></div>'
                + '</div></div>';
        });
        el.innerHTML = html;
        return el;
    }

    function createReactions(w, state) {
        var emojis = w.emojis || ['👍','❤️','😂','🎉','👏'];
        var recent = state.reactions || [];
        if (!recent.length) return null;
        var el = document.createElement('div');
        el.className = 'overlay-reactions ' + posClass(w.position || 'bottom-right');
        el.style.width = '200px'; el.style.height = '150px';
        recent.slice(-10).forEach(function(r, i) {
            var span = document.createElement('span');
            span.className = 'overlay-reaction-emoji';
            span.textContent = r.emoji || emojis[0];
            span.style.left = (Math.random() * 160) + 'px';
            span.style.bottom = '0';
            span.style.animationDelay = (i * 0.2) + 's';
            el.appendChild(span);
        });
        return el;
    }

    function createWeather(w, state) {
        var temp = w.temp || state.weather?.temp || '';
        var desc = w.description || state.weather?.description || '';
        var icon = w.icon || state.weather?.icon || '🌤';
        if (!temp) return null;
        var el = document.createElement('div');
        el.className = 'overlay-weather ' + posClass(w.position || 'top-right');
        el.innerHTML = '<span class="weather-icon">' + esc(icon) + '</span>'
            + '<div><div class="weather-temp">' + esc(String(temp)) + '°</div>'
            + '<div class="weather-desc">' + esc(desc) + '</div></div>';
        return el;
    }

    function esc(s) {
        var d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }

    // Start overlay polling
    loadOverlay();
    setInterval(loadOverlay, OVERLAY_POLL_MS);
})();
"""
