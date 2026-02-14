package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

fun HTML.slideshowEditorView(
    session: AdminClaims,
    slideshowId: String? = null
) {
    val isEdit = slideshowId != null
    val title = if (isEdit) "Edit Slideshow" else "Create Slideshow"

    adminLayout(title = title, userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = title,
            subtitle = if (isEdit) "Modify slideshow composition" else "Compose a slideshow from images and audio",
            backHref = "/admin/assets",
            backLabel = "Back to Assets"
        )

        div("card") {
            div("card-body") {
                // Name input
                div("form-group mb-3") {
                    label { htmlFor = "slideshow-name"; +"Slideshow Name" }
                    input(type = InputType.text, classes = "form-control") {
                        id = "slideshow-name"
                        placeholder = "My Slideshow"
                        required = true
                    }
                }

                // Slide picker area
                div("form-group mb-3") {
                    label { +"Slides" }
                    div {
                        id = "slides-container"
                        style = "min-height:80px;border:2px dashed var(--border-color,#ccc);border-radius:8px;padding:12px;display:flex;flex-wrap:wrap;gap:8px;align-items:flex-start"
                    }
                    div("mt-2") {
                        button(type = ButtonType.button, classes = "btn btn-secondary btn-sm") {
                            id = "add-slide-btn"
                            +"+ Add Slide"
                        }
                    }
                }

                // Audio picker
                div("form-group mb-3") {
                    label { htmlFor = "audio-select"; +"Audio Track (optional)" }
                    select(classes = "form-control") {
                        id = "audio-select"
                        option {
                            value = ""
                            +"No audio"
                        }
                    }
                }

                // Preview
                div("form-group mb-3") {
                    label { +"Preview" }
                    div {
                        id = "preview-area"
                        style = "width:100%;max-width:640px;aspect-ratio:16/9;background:#000;border-radius:8px;overflow:hidden;position:relative;display:flex;align-items:center;justify-content:center;color:#666;font-size:14px"
                        +"Add slides to preview"
                    }
                    div("mt-2") {
                        button(type = ButtonType.button, classes = "btn btn-secondary btn-sm") {
                            id = "preview-btn"
                            +"Preview Slideshow"
                        }
                        button(type = ButtonType.button, classes = "btn btn-secondary btn-sm ms-2") {
                            id = "stop-btn"
                            style = "display:none"
                            +"Stop"
                        }
                    }
                }

                // Actions
                div("form-actions mt-4") {
                    button(type = ButtonType.button, classes = "btn btn-primary") {
                        id = "save-btn"
                        +"Save Slideshow"
                    }
                    a(href = "/admin/assets", classes = "btn btn-secondary") {
                        +"Cancel"
                    }
                }

                // Status messages
                div {
                    id = "status-msg"
                    style = "margin-top:12px;display:none"
                }
            }
        }

        // Image picker modal
        div {
            id = "image-picker-modal"
            style = "display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center"
            div {
                style = "background:var(--card-bg,#fff);border-radius:12px;padding:24px;max-width:720px;width:90%;max-height:80vh;overflow-y:auto"
                h3 { +"Select Image" }
                div {
                    id = "image-grid"
                    style = "display:grid;grid-template-columns:repeat(auto-fill,minmax(120px,1fr));gap:8px;margin:16px 0"
                }
                div("form-actions") {
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        id = "close-picker-btn"
                        +"Close"
                    }
                }
            }
        }

        script {
            unsafe {
                +"""
(function() {
    var slideshowId = ${if (slideshowId != null) "'$slideshowId'" else "null"};
    var slides = [];
    var imageAssets = [];
    var audioAssets = [];
    var previewTimer = null;

    // Fetch assets
    function loadAssets() {
        fetch('/api/admin/assets', {
            headers: { 'Authorization': 'Bearer ' + getToken() }
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            imageAssets = data.assets.filter(function(a) { return a.type === 'IMAGE' && a.status === 'READY'; });
            audioAssets = data.assets.filter(function(a) { return a.type === 'AUDIO' && a.status === 'READY'; });
            populateAudioSelect();
            populateImageGrid();
            if (slideshowId) loadExisting();
        })
        .catch(function(e) { showStatus('Failed to load assets: ' + e.message, 'error'); });
    }

    function getToken() {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var c = cookies[i].trim();
            if (c.indexOf('admin_session=') === 0) return c.substring(14);
        }
        return '';
    }

    function populateAudioSelect() {
        var sel = document.getElementById('audio-select');
        audioAssets.forEach(function(a) {
            var opt = document.createElement('option');
            opt.value = a.id;
            opt.textContent = a.name;
            sel.appendChild(opt);
        });
    }

    function populateImageGrid() {
        var grid = document.getElementById('image-grid');
        grid.innerHTML = '';
        if (imageAssets.length === 0) {
            grid.innerHTML = '<p style="color:#999">No images uploaded yet. Upload images first.</p>';
            return;
        }
        imageAssets.forEach(function(a) {
            var div = document.createElement('div');
            div.style.cssText = 'cursor:pointer;border:2px solid transparent;border-radius:8px;padding:4px;text-align:center;transition:border-color 0.2s';
            div.dataset.assetId = a.id;
            div.innerHTML = '<img src="/storage/' + a.id + '" alt="' + a.name + '" style="width:100%;height:80px;object-fit:cover;border-radius:4px"><div style="font-size:11px;margin-top:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' + a.name + '</div>';
            div.onclick = function() { addSlide(a); closeModal(); };
            div.onmouseenter = function() { div.style.borderColor = 'var(--primary,#4f46e5)'; };
            div.onmouseleave = function() { div.style.borderColor = 'transparent'; };
            grid.appendChild(div);
        });
    }

    function addSlide(asset) {
        slides.push({ assetId: asset.id, name: asset.name, durationMs: 5000, transition: 'fade' });
        renderSlides();
    }

    function removeSlide(index) {
        slides.splice(index, 1);
        renderSlides();
    }

    function renderSlides() {
        var container = document.getElementById('slides-container');
        container.innerHTML = '';
        if (slides.length === 0) {
            container.innerHTML = '<p style="color:#999;margin:auto">Drag images here or click "+ Add Slide"</p>';
            return;
        }
        slides.forEach(function(slide, i) {
            var card = document.createElement('div');
            card.draggable = true;
            card.dataset.index = i;
            card.style.cssText = 'width:140px;background:var(--card-bg,#f9f9f9);border:1px solid var(--border-color,#ddd);border-radius:8px;padding:8px;position:relative;cursor:grab';
            card.innerHTML = '<img src="/storage/' + slide.assetId + '" style="width:100%;height:70px;object-fit:cover;border-radius:4px">'
                + '<div style="font-size:11px;margin-top:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">' + slide.name + '</div>'
                + '<div style="display:flex;gap:4px;margin-top:4px;align-items:center">'
                + '<input type="number" min="500" step="500" value="' + slide.durationMs + '" style="width:60px;font-size:11px;padding:2px 4px;border:1px solid #ccc;border-radius:4px" data-idx="' + i + '" class="slide-duration">'
                + '<span style="font-size:10px">ms</span>'
                + '<select style="font-size:11px;padding:2px;border:1px solid #ccc;border-radius:4px;flex:1" data-idx="' + i + '" class="slide-transition">'
                + '<option value="fade"' + (slide.transition === 'fade' ? ' selected' : '') + '>Fade</option>'
                + '<option value="slide"' + (slide.transition === 'slide' ? ' selected' : '') + '>Slide</option>'
                + '<option value="none"' + (slide.transition === 'none' ? ' selected' : '') + '>None</option>'
                + '</select></div>'
                + '<button style="position:absolute;top:2px;right:2px;background:none;border:none;cursor:pointer;font-size:14px;color:#e53e3e" data-remove="' + i + '">x</button>';

            // Drag-and-drop
            card.ondragstart = function(e) { e.dataTransfer.setData('text/plain', i.toString()); card.style.opacity = '0.5'; };
            card.ondragend = function() { card.style.opacity = '1'; };
            card.ondragover = function(e) { e.preventDefault(); card.style.borderColor = 'var(--primary,#4f46e5)'; };
            card.ondragleave = function() { card.style.borderColor = 'var(--border-color,#ddd)'; };
            card.ondrop = function(e) {
                e.preventDefault();
                card.style.borderColor = 'var(--border-color,#ddd)';
                var from = parseInt(e.dataTransfer.getData('text/plain'));
                var to = i;
                if (from !== to) {
                    var moved = slides.splice(from, 1)[0];
                    slides.splice(to, 0, moved);
                    renderSlides();
                }
            };

            container.appendChild(card);
        });

        // Event delegation for duration/transition changes and remove
        container.onclick = function(e) {
            var removeIdx = e.target.dataset.remove;
            if (removeIdx !== undefined) removeSlide(parseInt(removeIdx));
        };
        container.onchange = function(e) {
            var idx = e.target.dataset.idx;
            if (idx === undefined) return;
            idx = parseInt(idx);
            if (e.target.classList.contains('slide-duration')) {
                slides[idx].durationMs = parseInt(e.target.value) || 5000;
            } else if (e.target.classList.contains('slide-transition')) {
                slides[idx].transition = e.target.value;
            }
        };
    }

    // Modal
    function openModal() {
        var m = document.getElementById('image-picker-modal');
        m.style.display = 'flex';
    }
    function closeModal() {
        document.getElementById('image-picker-modal').style.display = 'none';
    }
    document.getElementById('add-slide-btn').onclick = openModal;
    document.getElementById('close-picker-btn').onclick = closeModal;
    document.getElementById('image-picker-modal').onclick = function(e) {
        if (e.target === this) closeModal();
    };

    // Preview
    document.getElementById('preview-btn').onclick = function() {
        if (slides.length === 0) return;
        var area = document.getElementById('preview-area');
        var stopBtn = document.getElementById('stop-btn');
        stopBtn.style.display = '';
        var idx = 0;
        function showSlide() {
            if (idx >= slides.length) idx = 0;
            var s = slides[idx];
            var img = '<img src="/storage/' + s.assetId + '" style="width:100%;height:100%;object-fit:contain;position:absolute;top:0;left:0;animation:' + getAnimation(s.transition) + '">';
            area.innerHTML = img;
            idx++;
            previewTimer = setTimeout(showSlide, s.durationMs);
        }
        showSlide();
    };
    document.getElementById('stop-btn').onclick = function() {
        clearTimeout(previewTimer);
        previewTimer = null;
        this.style.display = 'none';
        document.getElementById('preview-area').innerHTML = 'Add slides to preview';
    };

    function getAnimation(transition) {
        if (transition === 'fade') return 'slideshowFadeIn 0.5s ease-in';
        if (transition === 'slide') return 'slideshowSlideIn 0.5s ease-out';
        return 'none';
    }

    // Save
    document.getElementById('save-btn').onclick = function() {
        var name = document.getElementById('slideshow-name').value.trim();
        if (!name) { showStatus('Please enter a name', 'error'); return; }
        if (slides.length === 0) { showStatus('Please add at least one slide', 'error'); return; }

        var audioId = document.getElementById('audio-select').value || null;
        var totalMs = slides.reduce(function(sum, s) { return sum + s.durationMs; }, 0);

        var body = {
            name: name,
            slides: slides.map(function(s) { return { assetId: s.assetId, durationMs: s.durationMs, transition: s.transition }; }),
            audioAssetId: audioId,
            totalDurationMs: totalMs
        };

        var url = slideshowId ? '/api/admin/assets/' + slideshowId + '/slideshow' : '/api/admin/assets/slideshow';
        var method = slideshowId ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
            body: JSON.stringify(body)
        })
        .then(function(r) {
            if (!r.ok) return r.json().then(function(e) { throw new Error(e.message || 'Save failed'); });
            return r.json();
        })
        .then(function(data) {
            showStatus('Slideshow saved!', 'success');
            setTimeout(function() { window.location.href = '/admin/assets/' + data.id; }, 1000);
        })
        .catch(function(e) { showStatus('Error: ' + e.message, 'error'); });
    };

    // Load existing slideshow
    function loadExisting() {
        fetch('/api/admin/assets/' + slideshowId + '/slideshow', {
            headers: { 'Authorization': 'Bearer ' + getToken() }
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            document.getElementById('slideshow-name').value = data.name;
            if (data.definition.audioAssetId) {
                document.getElementById('audio-select').value = data.definition.audioAssetId;
            }
            data.definition.slides.forEach(function(s) {
                var img = imageAssets.find(function(a) { return a.id === s.assetId; });
                slides.push({ assetId: s.assetId, name: img ? img.name : s.assetId, durationMs: s.durationMs, transition: s.transition });
            });
            renderSlides();
        })
        .catch(function(e) { showStatus('Failed to load slideshow: ' + e.message, 'error'); });
    }

    function showStatus(msg, type) {
        var el = document.getElementById('status-msg');
        el.style.display = '';
        el.className = type === 'error' ? 'alert alert-error' : 'alert alert-success';
        el.textContent = msg;
        if (type === 'success') setTimeout(function() { el.style.display = 'none'; }, 3000);
    }

    // Init
    renderSlides();
    loadAssets();

    // Add CSS animation keyframes
    var style = document.createElement('style');
    style.textContent = '@keyframes slideshowFadeIn{from{opacity:0}to{opacity:1}}@keyframes slideshowSlideIn{from{transform:translateX(100%)}to{transform:translateX(0)}}';
    document.head.appendChild(style);
})();
"""
            }
        }
    }
}
