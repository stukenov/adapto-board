package com.playoutedge.server.views.components

import kotlinx.html.*

// ========================================
// HTMX ATTRIBUTE HELPERS
// ========================================

fun HTMLTag.hxGet(url: String) { attributes["hx-get"] = url }
fun HTMLTag.hxPost(url: String) { attributes["hx-post"] = url }
fun HTMLTag.hxPut(url: String) { attributes["hx-put"] = url }
fun HTMLTag.hxPatch(url: String) { attributes["hx-patch"] = url }
fun HTMLTag.hxDelete(url: String) { attributes["hx-delete"] = url }
fun HTMLTag.hxTarget(selector: String) { attributes["hx-target"] = selector }
fun HTMLTag.hxSwap(mode: String) { attributes["hx-swap"] = mode }
fun HTMLTag.hxTrigger(event: String) { attributes["hx-trigger"] = event }
fun HTMLTag.hxIndicator(selector: String) { attributes["hx-indicator"] = selector }
fun HTMLTag.hxConfirm(message: String) { attributes["hx-confirm"] = message }
fun HTMLTag.hxPushUrl(url: String = "true") { attributes["hx-push-url"] = url }
fun HTMLTag.hxVals(json: String) { attributes["hx-vals"] = json }
fun HTMLTag.hxHeaders(json: String) { attributes["hx-headers"] = json }
fun HTMLTag.hxInclude(selector: String) { attributes["hx-include"] = selector }
fun HTMLTag.hxSelect(selector: String) { attributes["hx-select"] = selector }
fun HTMLTag.hxSelectOob(selector: String) { attributes["hx-select-oob"] = selector }
fun HTMLTag.hxSwapOob(value: String) { attributes["hx-swap-oob"] = value }

// ========================================
// BUTTON VARIANTS
// ========================================

enum class BtnVariant(val css: String) {
    PRIMARY("btn btn-primary"),
    SECONDARY("btn btn-secondary"),
    DANGER("btn btn-danger"),
    GHOST("btn btn-ghost"),
    SM_PRIMARY("btn btn-primary btn-sm"),
    SM_SECONDARY("btn btn-secondary btn-sm"),
    SM_DANGER("btn btn-danger btn-sm"),
    SM_GHOST("btn btn-ghost btn-sm")
}

fun FlowContent.hxButton(
    label: String,
    variant: BtnVariant = BtnVariant.PRIMARY,
    hxGet: String? = null,
    hxPost: String? = null,
    hxDelete: String? = null,
    hxPatch: String? = null,
    target: String? = null,
    swap: String = "innerHTML",
    confirm: String? = null,
    indicator: String? = null,
    disabled: Boolean = false,
    extra: (BUTTON.() -> Unit)? = null
) {
    button(classes = variant.css) {
        type = ButtonType.button
        hxGet?.let { attributes["hx-get"] = it }
        hxPost?.let { attributes["hx-post"] = it }
        hxDelete?.let { attributes["hx-delete"] = it }
        hxPatch?.let { attributes["hx-patch"] = it }
        target?.let { attributes["hx-target"] = it }
        attributes["hx-swap"] = swap
        confirm?.let { attributes["hx-confirm"] = it }
        indicator?.let { attributes["hx-indicator"] = it }
        if (disabled) this.disabled = true
        extra?.invoke(this)
        +label
    }
}

// ========================================
// SEARCH INPUT WITH DEBOUNCE
// ========================================

fun FlowContent.hxSearchInput(
    url: String,
    target: String,
    name: String = "q",
    placeholder: String = "Search...",
    currentValue: String? = null,
    debounceMs: Int = 300
) {
    div("form-group") {
        input(type = InputType.search, classes = "form-control") {
            this.name = name
            this.placeholder = placeholder
            currentValue?.let { value = it }
            attributes["hx-get"] = url
            attributes["hx-target"] = target
            attributes["hx-swap"] = "outerHTML"
            attributes["hx-trigger"] = "input changed delay:${debounceMs}ms, search"
            attributes["hx-include"] = "[name='status']"
            attributes["aria-label"] = placeholder
        }
    }
}

// ========================================
// CONFIRMABLE DELETE
// ========================================

fun FlowContent.hxDeleteButton(
    label: String = "Delete",
    url: String,
    target: String = "closest tr",
    swap: String = "outerHTML swap:500ms",
    confirmMessage: String = "Are you sure you want to delete this item?"
) {
    hxButton(
        label = label,
        variant = BtnVariant.SM_DANGER,
        hxDelete = url,
        target = target,
        swap = swap,
        confirm = confirmMessage
    )
}

// ========================================
// LAZY-LOADED CONTENT
// ========================================

fun FlowContent.hxLazy(
    url: String,
    id: String,
    placeholder: (DIV.() -> Unit)? = null
) {
    div {
        this.id = id
        attributes["hx-get"] = url
        attributes["hx-trigger"] = "load"
        attributes["hx-swap"] = "innerHTML"
        if (placeholder != null) {
            placeholder()
        } else {
            div("skeleton-loading") {
                div("skeleton-line") {}
                div("skeleton-line") {}
                div("skeleton-line short") {}
            }
        }
    }
}

// ========================================
// INLINE EDIT
// ========================================

fun FlowContent.hxInlineEdit(
    displayValue: String,
    editUrl: String,
    id: String
) {
    span {
        this.id = id
        attributes["hx-get"] = editUrl
        attributes["hx-trigger"] = "click"
        attributes["hx-swap"] = "outerHTML"
        attributes["role"] = "button"
        attributes["tabindex"] = "0"
        +displayValue
        unsafe { +"""<svg class="icon" width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" style="display:inline;margin-left:4px;opacity:0.4"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>""" }
    }
}

// ========================================
// MODAL
// ========================================

fun FlowContent.hxModal(
    id: String,
    title: String,
    loadUrl: String? = null,
    content: (DIV.() -> Unit)? = null
) {
    div("modal-overlay") {
        this.id = id
        style = "display:none"
        attributes["onclick"] = "if(event.target===this)this.style.display='none'"
        div("modal-card") {
            div("modal-header") {
                h3 { +title }
                button(classes = "modal-close") {
                    attributes["onclick"] = "this.closest('.modal-overlay').style.display='none'"
                    +"\u00D7"
                }
            }
            div("modal-body") {
                if (loadUrl != null) {
                    attributes["hx-get"] = loadUrl
                    attributes["hx-trigger"] = "intersect once"
                    attributes["hx-swap"] = "innerHTML"
                }
                content?.invoke(this)
            }
        }
    }
}

fun FlowContent.hxModalTrigger(
    label: String,
    modalId: String,
    loadUrl: String,
    variant: BtnVariant = BtnVariant.PRIMARY
) {
    button(classes = variant.css) {
        type = ButtonType.button
        attributes["hx-get"] = loadUrl
        attributes["hx-target"] = "#$modalId .modal-body"
        attributes["hx-swap"] = "innerHTML"
        attributes["onclick"] = "document.getElementById('$modalId').style.display='flex'"
        +label
    }
}

// ========================================
// FORM WITH HTMX SUBMIT
// ========================================

fun FlowContent.hxForm(
    action: String,
    method: String = "post",
    target: String? = null,
    swap: String = "outerHTML",
    id: String? = null,
    cssClass: String = "",
    content: FORM.() -> Unit
) {
    form(classes = cssClass) {
        id?.let { this.id = it }
        when (method.lowercase()) {
            "post" -> attributes["hx-post"] = action
            "put" -> attributes["hx-put"] = action
            "patch" -> attributes["hx-patch"] = action
            "delete" -> attributes["hx-delete"] = action
            else -> attributes["hx-get"] = action
        }
        target?.let { attributes["hx-target"] = it }
        attributes["hx-swap"] = swap
        content()
    }
}

// ========================================
// TOAST TRIGGER HELPER
// ========================================

fun hxToastTrigger(message: String, type: String = "success"): String {
    val escaped = message
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    return """{"showToast":{"message":"$escaped","type":"$type"}}"""
}

// ========================================
// PAGINATION WITH HTMX
// ========================================

fun FlowContent.hxPagination(
    currentPage: Int,
    totalPages: Int,
    totalItems: Long,
    baseUrl: String,
    target: String,
    extraParams: String = ""
) {
    if (totalPages <= 1) return
    val sep = if (extraParams.isEmpty()) "?" else "$extraParams&"
    nav("pagination-nav") {
        div("pagination-info") {
            +"Page $currentPage of $totalPages ($totalItems items)"
        }
        div("pagination-buttons") {
            if (currentPage > 1) {
                button(classes = "btn btn-secondary btn-sm") {
                    attributes["hx-get"] = "$baseUrl${sep}page=${currentPage - 1}"
                    attributes["hx-target"] = target
                    attributes["hx-swap"] = "outerHTML"
                    attributes["hx-push-url"] = "true"
                    +"Previous"
                }
            }
            if (currentPage < totalPages) {
                button(classes = "btn btn-secondary btn-sm") {
                    attributes["hx-get"] = "$baseUrl${sep}page=${currentPage + 1}"
                    attributes["hx-target"] = target
                    attributes["hx-swap"] = "outerHTML"
                    attributes["hx-push-url"] = "true"
                    +"Next"
                }
            }
        }
    }
}

// ========================================
// SORTABLE TABLE HEADER
// ========================================

fun TR.hxSortableHeader(
    label: String,
    field: String,
    currentSort: String?,
    currentDir: String?,
    url: String,
    target: String
) {
    th {
        val newDir = if (currentSort == field && currentDir == "asc") "desc" else "asc"
        attributes["hx-get"] = "$url?sort=$field&dir=$newDir"
        attributes["hx-target"] = target
        attributes["hx-swap"] = "outerHTML"
        attributes["hx-push-url"] = "true"
        style = "cursor:pointer;user-select:none"
        +label
        if (currentSort == field) {
            span { +(if (currentDir == "asc") " \u2191" else " \u2193") }
        }
    }
}

// ========================================
// POLLING / LIVE UPDATE
// ========================================

fun FlowContent.hxPolling(
    url: String,
    id: String,
    intervalSeconds: Int = 30,
    content: DIV.() -> Unit
) {
    div {
        this.id = id
        attributes["hx-get"] = url
        attributes["hx-trigger"] = "every ${intervalSeconds}s"
        attributes["hx-swap"] = "innerHTML"
        content()
    }
}
