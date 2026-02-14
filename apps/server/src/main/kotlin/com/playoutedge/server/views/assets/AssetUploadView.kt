package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Asset upload form view with drag & drop zone.
 */
fun HTML.assetUploadView(
    session: AdminClaims,
    quota: QuotaInfo,
    error: String? = null,
    success: String? = null
) {
    adminLayout(title = "Upload Asset", userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = "Upload Asset",
            subtitle = "Add new media to your library",
            backHref = "/admin/assets",
            backLabel = "Back to Assets"
        )

        // Storage quota info
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

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }
                if (success != null) {
                    alertBox(success, "success")
                }

                form(
                    action = "/admin/assets/upload",
                    method = FormMethod.post,
                    encType = FormEncType.multipartFormData
                ) {
                    id = "upload-form"

                    // Drag & drop zone
                    div("dropzone") {
                        id = "dropzone"
                        div("dropzone-content") {
                            div("dropzone-icon") { +"📁" }
                            div("dropzone-text") {
                                p("dropzone-primary") { +"Drag files here or click to browse" }
                                p("dropzone-secondary") {
                                    +"PNG, JPEG images (max 20MB) · MP4 videos (max 2GB) · MP3, WAV, OGG audio (max 100MB)"
                                }
                            }
                        }
                        input(type = InputType.file, classes = "dropzone-input") {
                            id = "file"
                            name = "file"
                            accept = "image/png,image/jpeg,video/mp4,video/x-m4v,audio/mpeg,audio/mp3,audio/wav,audio/ogg"
                            required = true
                        }
                    }

                    // File preview area
                    div("file-preview") {
                        id = "file-preview"
                        style = "display:none"
                        div("file-preview-image") {
                            id = "preview-image"
                        }
                        div("file-preview-info") {
                            id = "preview-info"
                        }
                    }

                    // Upload progress (shown on submit)
                    div("upload-progress") {
                        id = "upload-progress"
                        style = "display:none"
                        div("upload-progress-bar") {
                            div("upload-progress-fill") {
                                id = "progress-fill"
                            }
                        }
                        p("upload-progress-label progress-label") { +"Uploading..." }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            id = "upload-btn"
                            +"Upload"
                        }
                        a(href = "/admin/assets", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }

        script {
            unsafe {
                +"""
(function() {
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('file');
    const preview = document.getElementById('file-preview');
    const previewImage = document.getElementById('preview-image');
    const previewInfo = document.getElementById('preview-info');
    const form = document.getElementById('upload-form');
    const progressEl = document.getElementById('upload-progress');

    // Click to browse
    dropzone.addEventListener('click', function(e) {
        if (e.target !== fileInput) fileInput.click();
    });

    // Drag events
    ['dragenter', 'dragover'].forEach(function(evt) {
        dropzone.addEventListener(evt, function(e) {
            e.preventDefault();
            dropzone.classList.add('dragover');
        });
    });
    ['dragleave', 'drop'].forEach(function(evt) {
        dropzone.addEventListener(evt, function(e) {
            e.preventDefault();
            dropzone.classList.remove('dragover');
        });
    });

    dropzone.addEventListener('drop', function(e) {
        if (e.dataTransfer.files.length > 0) {
            fileInput.files = e.dataTransfer.files;
            showPreview(e.dataTransfer.files[0]);
        }
    });

    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) showPreview(this.files[0]);
    });

    function showPreview(file) {
        preview.style.display = '';
        var info = '<strong>' + file.name + '</strong><br>';
        info += (file.size / 1048576).toFixed(1) + ' MB · ' + file.type;
        previewInfo.innerHTML = info;
        previewImage.innerHTML = '';
        if (file.type.startsWith('image/')) {
            var reader = new FileReader();
            reader.onload = function(e) {
                previewImage.innerHTML = '<img src="' + e.target.result + '" alt="Preview">';
            };
            reader.readAsDataURL(file);
        } else {
            previewImage.innerHTML = '<div class="file-preview-icon">' + (file.type.startsWith('audio/') ? '🎵' : '🎬') + '</div>';
        }
    }

    form.addEventListener('submit', function() {
        if (progressEl) progressEl.style.display = '';
    });
})();
"""
            }
        }
    }
}
