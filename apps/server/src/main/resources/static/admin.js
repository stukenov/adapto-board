/* ========================================
   PLAYOUT EDGE - ADMIN UI UTILITIES
   ======================================== */

// ========================================
// TOAST NOTIFICATIONS
// ========================================
window.Toast = {
    _container: null,
    _init: function() {
        if (this._container) return;
        this._container = document.createElement('div');
        this._container.className = 'toast-container';
        this._container.setAttribute('aria-live', 'polite');
        document.body.appendChild(this._container);
    },
    show: function(message, type, duration) {
        this._init();
        type = type || 'info';
        duration = duration || 4000;
        var toast = document.createElement('div');
        toast.className = 'toast toast-' + type;
        toast.setAttribute('role', 'alert');
        var icons = {success:'✓',error:'✕',warning:'⚠',info:'ℹ'};
        toast.innerHTML = '<span class="toast-icon">' + (icons[type]||icons.info) + '</span>' +
            '<span class="toast-message">' + message + '</span>' +
            '<button class="toast-close" aria-label="Close">&times;</button>';
        toast.querySelector('.toast-close').addEventListener('click', function() {
            toast.classList.add('toast-exit');
            setTimeout(function() { toast.remove(); }, 300);
        });
        this._container.appendChild(toast);
        requestAnimationFrame(function() { toast.classList.add('toast-enter'); });
        setTimeout(function() {
            if (toast.parentNode) {
                toast.classList.add('toast-exit');
                setTimeout(function() { toast.remove(); }, 300);
            }
        }, duration);
    }
};

// Show flash message from URL params
(function() {
    var params = new URLSearchParams(window.location.search);
    var msg = params.get('success');
    if (msg) Toast.show(decodeURIComponent(msg), 'success');
    msg = params.get('error');
    if (msg && msg !== 'invalid' && msg !== 'expired') Toast.show(decodeURIComponent(msg), 'error');
})();

// ========================================
// CONFIRM MODAL (replaces native confirm)
// ========================================
window.ConfirmModal = {
    _modal: null,
    _init: function() {
        if (this._modal) return;
        var m = document.createElement('div');
        m.className = 'modal-overlay';
        m.id = 'confirm-modal';
        m.setAttribute('role', 'dialog');
        m.setAttribute('aria-modal', 'true');
        m.innerHTML = '<div class="modal-card">' +
            '<div class="modal-header"><h3 class="modal-title">Confirm</h3></div>' +
            '<div class="modal-body"><p class="modal-message"></p></div>' +
            '<div class="modal-footer">' +
            '<button class="btn btn-secondary modal-cancel">Cancel</button>' +
            '<button class="btn btn-danger modal-confirm">Confirm</button>' +
            '</div></div>';
        document.body.appendChild(m);
        this._modal = m;
        var self = this;
        m.querySelector('.modal-cancel').addEventListener('click', function() { self._resolve(false); self.hide(); });
        m.addEventListener('click', function(e) { if (e.target === m) { self._resolve(false); self.hide(); } });
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && m.classList.contains('modal-visible')) { self._resolve(false); self.hide(); }
        });
    },
    _resolve: null,
    show: function(message, title, confirmLabel) {
        this._init();
        this._modal.querySelector('.modal-title').textContent = title || 'Confirm';
        this._modal.querySelector('.modal-message').textContent = message;
        if (confirmLabel) this._modal.querySelector('.modal-confirm').textContent = confirmLabel;
        this._modal.classList.add('modal-visible');
        this._modal.querySelector('.modal-cancel').focus();
        var self = this;
        return new Promise(function(resolve) {
            self._resolve = resolve;
            self._modal.querySelector('.modal-confirm').onclick = function() { resolve(true); self.hide(); };
        });
    },
    hide: function() {
        if (this._modal) this._modal.classList.remove('modal-visible');
    }
};

// Intercept confirm() calls on forms
document.addEventListener('click', function(e) {
    var btn = e.target.closest('[onclick*="confirm("]');
    if (!btn) return;
    var match = btn.getAttribute('onclick').match(/confirm\(['"](.+?)['"]\)/);
    if (!match) return;
    e.preventDefault();
    e.stopImmediatePropagation();
    var message = match[1];
    var form = btn.closest('form');
    ConfirmModal.show(message, 'Are you sure?', btn.classList.contains('btn-danger') ? 'Delete' : 'Confirm').then(function(ok) {
        if (ok && form) {
            btn.removeAttribute('onclick');
            form.submit();
        }
    });
}, true);

// ========================================
// LOADING STATES ON FORM SUBMIT
// ========================================
document.addEventListener('submit', function(e) {
    var form = e.target;
    if (form.classList.contains('no-loading')) return;
    var btn = form.querySelector('button[type="submit"]:not(.no-loading)');
    if (!btn || btn.disabled) return;
    btn.disabled = true;
    btn.dataset.originalText = btn.textContent;
    btn.innerHTML = '<span class="btn-spinner"></span> ' + btn.textContent;
    // Re-enable after 10s as safety net
    setTimeout(function() {
        btn.disabled = false;
        btn.textContent = btn.dataset.originalText || btn.textContent;
    }, 10000);
});

// ========================================
// SIDEBAR TOGGLE (mobile)
// ========================================
function toggleSidebar() {
    var sidebar = document.querySelector('.sidebar');
    var overlay = document.querySelector('.sidebar-overlay');
    if (sidebar) {
        sidebar.classList.toggle('open');
        if (overlay) overlay.classList.toggle('open');
    }
}
function closeSidebar() {
    var sidebar = document.querySelector('.sidebar');
    var overlay = document.querySelector('.sidebar-overlay');
    if (sidebar) sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('open');
}
window.toggleSidebar = toggleSidebar;
window.closeSidebar = closeSidebar;

// ========================================
// KEYBOARD SHORTCUTS
// ========================================
(function() {
    var shortcuts = {
        'g d': '/admin',
        'g c': '/admin/channels',
        'g v': '/admin/devices',
        'g a': '/admin/assets',
        'g o': '/admin/overlay',
        'g s': '/admin/settings',
        'g r': '/admin/reports'
    };
    var buffer = '';
    var timer = null;

    document.addEventListener('keydown', function(e) {
        // Don't trigger in inputs
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') return;
        if (e.ctrlKey || e.metaKey || e.altKey) return;

        // ? to show help
        if (e.key === '?') {
            e.preventDefault();
            toggleShortcutsHelp();
            return;
        }

        buffer += e.key;
        clearTimeout(timer);
        timer = setTimeout(function() { buffer = ''; }, 500);

        var url = shortcuts[buffer];
        if (url) {
            buffer = '';
            window.location.href = url;
        }
    });

    function toggleShortcutsHelp() {
        var el = document.getElementById('shortcuts-help');
        if (el) { el.remove(); return; }
        el = document.createElement('div');
        el.id = 'shortcuts-help';
        el.className = 'modal-overlay modal-visible';
        el.setAttribute('role', 'dialog');
        el.innerHTML = '<div class="modal-card"><div class="modal-header"><h3 class="modal-title">Keyboard Shortcuts</h3></div>' +
            '<div class="modal-body"><div class="shortcuts-grid">' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>d</kbd><span>Dashboard</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>c</kbd><span>Channels</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>v</kbd><span>Devices</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>a</kbd><span>Assets</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>o</kbd><span>Overlay</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>s</kbd><span>Settings</span></div>' +
            '<div class="shortcut-item"><kbd>g</kbd> <kbd>r</kbd><span>Reports</span></div>' +
            '<div class="shortcut-item"><kbd>?</kbd><span>This help</span></div>' +
            '</div></div><div class="modal-footer"><button class="btn btn-secondary" onclick="this.closest(\'.modal-overlay\').remove()">Close</button></div></div>';
        document.body.appendChild(el);
        el.addEventListener('click', function(e) { if (e.target === el) el.remove(); });
    }
})();

// ========================================
// AUTO-GENERATE PASSWORD
// ========================================
window.generatePassword = function(targetId) {
    var chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%';
    var pw = '';
    for (var i = 0; i < 12; i++) pw += chars[Math.floor(Math.random() * chars.length)];
    var el = document.getElementById(targetId);
    if (el) { el.value = pw; el.type = 'text'; }
    return pw;
};

// ========================================
// DARK MODE
// ========================================
(function() {
    var saved = localStorage.getItem('pe-theme');
    if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
        document.documentElement.classList.add('dark');
    }
})();

window.toggleDarkMode = function() {
    var isDark = document.documentElement.classList.toggle('dark');
    localStorage.setItem('pe-theme', isDark ? 'dark' : 'light');
};

// Password strength meter
function updatePasswordStrength(password) {
    const bar = document.getElementById('strength-bar');
    const text = document.getElementById('strength-text');
    if (!bar || !text) return;

    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^a-zA-Z0-9]/.test(password)) score++;

    const levels = [
        { width: '0%', color: '#dc2626', label: '' },
        { width: '20%', color: '#dc2626', label: 'Very weak' },
        { width: '40%', color: '#f59e0b', label: 'Weak' },
        { width: '60%', color: '#f59e0b', label: 'Fair' },
        { width: '80%', color: '#22c55e', label: 'Strong' },
        { width: '100%', color: '#16a34a', label: 'Very strong' }
    ];

    const level = levels[score] || levels[0];
    bar.style.width = level.width;
    bar.style.backgroundColor = level.color;
    text.textContent = level.label;
}
window.updatePasswordStrength = updatePasswordStrength;

// Schedule autosave
(function() {
    let scheduleChanged = false;
    let autosaveInterval = null;

    window.markScheduleChanged = function() {
        scheduleChanged = true;
    };

    function startAutosave() {
        if (autosaveInterval) return;
        autosaveInterval = setInterval(function() {
            if (!scheduleChanged) return;
            scheduleChanged = false;
            const form = document.getElementById('schedule-form');
            if (!form) return;
            const formData = new FormData(form);
            fetch(form.action, { method: 'POST', body: formData })
                .then(function(r) {
                    if (r.ok) window.Toast && window.Toast.show('Schedule saved', 'success', 2000);
                })
                .catch(function() {});
        }, 30000);
    }

    if (document.getElementById('schedule-form')) {
        startAutosave();
    }
})();

// Guided tour for post-onboarding
(function() {
    const tourSteps = [
        { selector: '.nav-link[href="/admin/channels"]', title: 'Channels', text: 'Manage your broadcast channels here.' },
        { selector: '.nav-link[href="/admin/devices"]', title: 'Devices', text: 'Enroll and monitor your playback devices.' },
        { selector: '.nav-link[href="/admin/assets"]', title: 'Assets', text: 'Upload and manage your media content.' },
        { selector: '.nav-link[href="/admin/overlay"]', title: 'Overlay', text: 'Create dynamic overlays for your channels.' },
        { selector: '.dropdown-toggle', title: 'Settings', text: 'Access your profile, settings, and reports.' }
    ];

    function showTour() {
        if (localStorage.getItem('tour_completed')) return;
        if (!document.querySelector('.sidebar')) return;

        let step = 0;

        function showStep() {
            // Remove previous
            document.querySelectorAll('.tour-overlay, .tour-tooltip').forEach(function(el) { el.remove(); });
            if (step >= tourSteps.length) {
                localStorage.setItem('tour_completed', 'true');
                return;
            }

            const target = document.querySelector(tourSteps[step].selector);
            if (!target) { step++; showStep(); return; }

            const overlay = document.createElement('div');
            overlay.className = 'tour-overlay';
            document.body.appendChild(overlay);

            const rect = target.getBoundingClientRect();
            const tooltip = document.createElement('div');
            tooltip.className = 'tour-tooltip';
            tooltip.style.top = (rect.bottom + 12) + 'px';
            tooltip.style.left = Math.max(12, rect.left) + 'px';
            tooltip.innerHTML = '<h4>' + tourSteps[step].title + '</h4><p>' + tourSteps[step].text + '</p>' +
                '<div class="tour-actions"><span class="tour-step-indicator">' + (step + 1) + '/' + tourSteps.length + '</span>' +
                '<div><button class="btn btn-secondary btn-sm tour-skip">Skip</button> ' +
                '<button class="btn btn-primary btn-sm tour-next">' + (step < tourSteps.length - 1 ? 'Next' : 'Done') + '</button></div></div>';
            document.body.appendChild(tooltip);

            tooltip.querySelector('.tour-next').onclick = function() { step++; showStep(); };
            tooltip.querySelector('.tour-skip').onclick = function() {
                document.querySelectorAll('.tour-overlay, .tour-tooltip').forEach(function(el) { el.remove(); });
                localStorage.setItem('tour_completed', 'true');
            };
            overlay.onclick = function() {
                document.querySelectorAll('.tour-overlay, .tour-tooltip').forEach(function(el) { el.remove(); });
                localStorage.setItem('tour_completed', 'true');
            };
        }

        // Delay tour start slightly
        setTimeout(showStep, 1000);
    }

    // Start tour if URL indicates post-onboarding
    if (window.location.search.includes('tour=true') || (window.location.pathname === '/admin' && !localStorage.getItem('tour_completed') && document.referrer.includes('onboarding'))) {
        showTour();
    }
})();

// ========================================
// BULK UPLOAD PROGRESS TRACKING
// ========================================
document.querySelectorAll('input[type="file"][multiple]').forEach(function(input) {
    input.addEventListener('change', function() {
        var fileList = document.getElementById('file-list-preview');
        if (fileList) {
            fileList.innerHTML = '';
            Array.from(this.files).forEach(function(f) {
                var div = document.createElement('div');
                div.className = 'file-preview-item';
                div.textContent = f.name + ' (' + (f.size/1024/1024).toFixed(2) + ' MB)';
                fileList.appendChild(div);
            });
        }
    });
});

// ========================================
// DRAG AND DROP ZONE ENHANCEMENT
// ========================================
document.querySelectorAll('.upload-drop-zone').forEach(function(zone) {
    zone.addEventListener('dragover', function(e) { e.preventDefault(); zone.classList.add('drag-over'); });
    zone.addEventListener('dragleave', function() { zone.classList.remove('drag-over'); });
    zone.addEventListener('drop', function(e) {
        e.preventDefault();
        zone.classList.remove('drag-over');
        var input = zone.querySelector('input[type="file"]');
        if (input) { input.files = e.dataTransfer.files; input.dispatchEvent(new Event('change')); }
    });
});

// ========================================
// BULK SELECTION FOR ASSETS
// ========================================
(function() {
    var selectAll = document.getElementById('select-all-assets');
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            document.querySelectorAll('.asset-checkbox').forEach(function(cb) { cb.checked = selectAll.checked; });
            toggleBulkBar();
        });
    }
    document.querySelectorAll('.asset-checkbox').forEach(function(cb) {
        cb.addEventListener('change', toggleBulkBar);
    });

    function toggleBulkBar() {
        var bar = document.getElementById('bulk-action-bar');
        if (!bar) return;
        var checked = document.querySelectorAll('.asset-checkbox:checked').length;
        bar.style.display = checked > 0 ? 'flex' : 'none';
        var count = document.getElementById('bulk-count');
        if (count) count.textContent = checked;

        // Update hidden inputs with selected IDs
        var ids = Array.from(document.querySelectorAll('.asset-checkbox:checked')).map(function(cb) { return cb.value; }).join(',');
        var archiveIds = document.getElementById('bulk-archive-ids');
        var deleteIds = document.getElementById('bulk-delete-ids');
        if (archiveIds) archiveIds.value = ids;
        if (deleteIds) deleteIds.value = ids;
    }
})();

// ========================================
// CLIENT-SIDE FILE TYPE VALIDATION
// ========================================
document.querySelectorAll('form.upload-form').forEach(function(form) {
    form.addEventListener('submit', function(e) {
        var input = form.querySelector('input[type="file"]');
        if (!input || !input.files.length) return;
        var allowed = ['video/mp4','video/webm','image/jpeg','image/png','image/webp','image/gif','audio/mpeg','audio/wav'];
        for (var i = 0; i < input.files.length; i++) {
            var file = input.files[i];
            if (!allowed.some(function(t) { return file.type.startsWith(t.split('/')[0]); })) {
                e.preventDefault();
                Toast.show('Unsupported file type: ' + file.name, 'error');
                return;
            }
        }
    });
});

// ========================================
// JSON EDITOR VALIDATION
// ========================================
document.querySelectorAll('.json-editor').forEach(function(textarea) {
    textarea.addEventListener('input', function() {
        try {
            if (this.value.trim()) JSON.parse(this.value);
            this.classList.remove('input-error');
            this.classList.add('input-success');
        } catch(e) {
            this.classList.remove('input-success');
            this.classList.add('input-error');
        }
    });
});

// ========================================
// OVERLAY PREVIEW POPUP
// ========================================
document.querySelectorAll('.overlay-preview-btn').forEach(function(btn) {
    btn.addEventListener('click', function(e) {
        e.preventDefault();
        var url = this.href;
        window.open(url, 'overlay-preview', 'width=1280,height=720');
    });
});

// ========================================
// SESSION TIMEOUT WARNING
// ========================================
(function() {
    function checkSession() {
        const expires = document.cookie.split(';').find(c => c.trim().startsWith('admin_session='));
        if (!expires) return;
        // Check every minute
        setTimeout(checkSession, 60000);
    }
    checkSession();
})();

// ========================================
// UNDO TOAST FUNCTIONALITY
// ========================================
window.showUndoToast = function(message, undoCallback, timeout) {
    timeout = timeout || 5000;
    const toast = document.createElement('div');
    toast.className = 'toast toast-undo';
    toast.innerHTML = '<span>' + message + '</span><button class="btn btn-sm btn-ghost" onclick="this.parentElement.undoFn()">Undo</button>';
    toast.undoFn = function() { undoCallback(); toast.remove(); };
    document.body.appendChild(toast);
    setTimeout(() => { if (toast.parentElement) toast.remove(); }, timeout);
};

// ========================================
// LOADING SKELETONS
// ========================================
document.querySelectorAll('[data-skeleton]').forEach(el => {
    if (!el.children.length) {
        el.innerHTML = '<div class="skeleton skeleton-text"></div>'.repeat(3);
    }
});

// ========================================
// CONFIRMATION FOR BULK ACTIONS
// ========================================
document.querySelectorAll('.bulk-action-form').forEach(form => {
    form.addEventListener('submit', function(e) {
        const count = document.querySelectorAll('.asset-checkbox:checked, .device-checkbox:checked').length;
        if (count === 0) { e.preventDefault(); Toast.show('No items selected', 'warning'); return; }
        if (!confirm('Apply action to ' + count + ' items?')) e.preventDefault();
    });
});

// ========================================
// SORT TABLE COLUMNS
// ========================================
document.querySelectorAll('th[data-sort]').forEach(th => {
    th.style.cursor = 'pointer';
    th.addEventListener('click', function() {
        const sort = this.dataset.sort;
        const url = new URL(window.location);
        const currentDir = url.searchParams.get('dir') === 'asc' ? 'desc' : 'asc';
        url.searchParams.set('sort', sort);
        url.searchParams.set('dir', currentDir);
        window.location = url;
    });
});

// ========================================
// DIFF VIEWER TOGGLE
// ========================================
document.querySelectorAll('.diff-toggle').forEach(toggle => {
    toggle.addEventListener('click', function() {
        const content = this.nextElementSibling;
        if (content) content.classList.toggle('open');
    });
});

// ========================================
// NOTIFICATION DROPDOWN
// ========================================
const bellBtn = document.getElementById('notification-bell');
if (bellBtn) {
    bellBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        document.getElementById('notification-dropdown')?.classList.toggle('open');
    });
    document.addEventListener('click', () => {
        document.getElementById('notification-dropdown')?.classList.remove('open');
    });
}

// ========================================
// HTMX INTEGRATION
// ========================================

// Show toast from HX-Trigger header {"showToast":{message,type}}
document.body.addEventListener('showToast', function(evt) {
    var detail = evt.detail || {};
    Toast.show(detail.message || 'Done', detail.type || 'success');
});

// Show/hide HTMX loading indicators
document.body.addEventListener('htmx:beforeRequest', function(evt) {
    var indicator = evt.detail.elt.querySelector('.htmx-indicator');
    if (indicator) indicator.style.display = 'inline';
});
document.body.addEventListener('htmx:afterRequest', function(evt) {
    var indicator = evt.detail.elt.querySelector('.htmx-indicator');
    if (indicator) indicator.style.display = 'none';
});

// Handle HTMX errors
document.body.addEventListener('htmx:responseError', function(evt) {
    Toast.show('Request failed. Please try again.', 'error');
});

// Handle 401 — redirect to login
document.body.addEventListener('htmx:beforeSwap', function(evt) {
    if (evt.detail.xhr.status === 401) {
        window.location.href = '/admin/login?error=expired';
        evt.detail.shouldSwap = false;
    }
});
