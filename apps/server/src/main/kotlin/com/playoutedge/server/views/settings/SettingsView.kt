package com.playoutedge.server.views.settings

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Settings main page with tabs.
 */
fun HTML.settingsMainView(
    session: AdminClaims,
    settings: TenantSettingsView,
    storageQuota: StorageQuotaView,
    deviceQuota: DeviceQuotaView,
    success: String? = null,
    error: String? = null
) {
    adminLayout(title = "Settings", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Settings" }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/settings", classes = "tab active") { +"General" }
            a(href = "/admin/settings/users", classes = "tab") { +"Users" }
            a(href = "/admin/settings/api-keys", classes = "tab") { +"API Keys" }
        }

        if (success != null) {
            div("alert alert-success") { +success }
        }
        if (error != null) {
            div("alert alert-danger") { +error }
        }

        // Quotas
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-value") { +"${storageQuota.usedFormatted} / ${storageQuota.limitFormatted}" }
                div("stat-label") { +"Storage" }
                div("quota-bar mt-2") {
                    div("quota-fill") {
                        style = "width: ${storageQuota.usedPercent}%"
                    }
                }
            }
            div("stat-card") {
                div("stat-value") { +"${deviceQuota.enrolled} / ${deviceQuota.limit}" }
                div("stat-label") { +"Devices" }
                div("quota-bar mt-2") {
                    div("quota-fill") {
                        style = "width: ${deviceQuota.usedPercent}%"
                    }
                }
            }
        }

        // Tenant Settings Form
        div("card") {
            div("card-header") { +"Tenant Settings" }
            div("card-body") {
                form(action = "/admin/settings", method = FormMethod.post) {
                    div("form-group") {
                        label { +"Organization Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "name"
                            value = settings.name
                            required = true
                        }
                    }

                    div("form-group") {
                        label { +"Timezone" }
                        select("form-control") {
                            name = "timezone"
                            TIMEZONES.forEach { tz ->
                                option {
                                    value = tz
                                    if (settings.timezone == tz) selected = true
                                    +tz
                                }
                            }
                        }
                    }

                    div("form-group") {
                        label { +"Offline Threshold (minutes)" }
                        input(type = InputType.number, classes = "form-control") {
                            name = "offlineThresholdMinutes"
                            min = "2"
                            max = "10"
                            value = settings.offlineThresholdMinutes.toString()
                        }
                        small("text-muted") { +"Device is considered offline after this many minutes without heartbeat" }
                    }

                    div("form-group") {
                        label { +"Release Ring" }
                        select("form-control") {
                            name = "releaseRing"
                            RELEASE_RINGS.forEach { ring ->
                                option {
                                    value = ring
                                    if (settings.releaseRing == ring) selected = true
                                    +ring
                                }
                            }
                        }
                        small("text-muted") { +"CANARY receives updates earlier, STABLE is more conservative" }
                    }

                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "maintenanceMode"
                                    if (settings.maintenanceMode) checked = true
                                }
                                +" Enable Maintenance Mode"
                            }
                        }
                        small("text-muted") { +"When enabled, devices will show maintenance message" }
                    }

                    div("form-group") {
                        label { +"Maintenance Reason (optional)" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "maintenanceReason"
                            value = settings.maintenanceReason ?: ""
                            placeholder = "Scheduled maintenance"
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Save Settings"
                    }
                }
            }
        }
    }
}

/**
 * Users list view.
 */
fun HTML.usersListView(
    session: AdminClaims,
    users: List<UserListItem>
) {
    adminLayout(title = "Users", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Settings" }
            a(href = "/admin/settings/users/invite", classes = "btn btn-primary") {
                +"Invite User"
            }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/settings", classes = "tab") { +"General" }
            a(href = "/admin/settings/users", classes = "tab active") { +"Users" }
            a(href = "/admin/settings/api-keys", classes = "tab") { +"API Keys" }
        }

        div("card") {
            if (users.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"👥" }
                    p("empty-state-title") { +"No users" }
                    p("empty-state-text") { +"Invite team members to help manage your content." }
                    a(href = "/admin/settings/users/invite", classes = "btn btn-primary") {
                        +"Invite User"
                    }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Email" }
                            th { +"Role" }
                            th { +"Status" }
                            th { +"Actions" }
                        }
                    }
                    tbody {
                        users.forEach { user ->
                            tr {
                                td { +user.displayName }
                                td { +user.email }
                                td {
                                    span("badge badge-${roleBadge(user.role)}") {
                                        +user.role.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${statusBadge(user.status)}") {
                                        +user.status.name.lowercase()
                                    }
                                }
                                td {
                                    a(href = "/admin/settings/users/${user.id}", classes = "btn btn-sm btn-secondary") {
                                        +"Edit"
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

/**
 * Invite user form.
 */
fun HTML.inviteUserView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "Invite User", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/settings/users", classes = "link") { +"← Back to Users" }
                h1("page-title") { +"Invite User" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-danger") { +error }
                }

                form(action = "/admin/settings/users/invite", method = FormMethod.post) {
                    div("form-group") {
                        label { +"Email Address" }
                        input(type = InputType.email, classes = "form-control") {
                            name = "email"
                            required = true
                            placeholder = "user@example.com"
                        }
                    }

                    div("form-group") {
                        label { +"Display Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "displayName"
                            required = true
                            placeholder = "John Doe"
                        }
                    }

                    div("form-group") {
                        label { +"Temporary Password" }
                        input(type = InputType.password, classes = "form-control") {
                            name = "password"
                            required = true
                            minLength = "8"
                            placeholder = "Minimum 8 characters"
                        }
                        small("text-muted") { +"User should change this on first login" }
                    }

                    div("form-group") {
                        label { +"Role" }
                        USER_ROLES.forEach { (role, description) ->
                            div("radio") {
                                label {
                                    input(type = InputType.radio) {
                                        name = "role"
                                        value = role.name
                                        if (role == UserRole.OPERATOR) checked = true
                                    }
                                    +" ${role.name.replace("_", " ")}"
                                    small("text-muted") { +" - $description" }
                                }
                            }
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create User"
                        }
                        a(href = "/admin/settings/users", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Edit user view.
 */
fun HTML.editUserView(
    session: AdminClaims,
    user: UserDetail,
    error: String? = null,
    success: String? = null
) {
    adminLayout(title = "Edit User", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/settings/users", classes = "link") { +"← Back to Users" }
                h1("page-title") { +"Edit User" }
            }
        }

        if (success != null) {
            div("alert alert-success") { +success }
        }
        if (error != null) {
            div("alert alert-danger") { +error }
        }

        div("card mb-4") {
            div("card-header") { +"User Details" }
            div("card-body") {
                form(action = "/admin/settings/users/${user.id}", method = FormMethod.post) {
                    div("form-group") {
                        label { +"Email" }
                        input(type = InputType.email, classes = "form-control") {
                            value = user.email
                            readonly = true
                        }
                    }

                    div("form-group") {
                        label { +"Display Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "displayName"
                            value = user.displayName
                        }
                    }

                    div("form-group") {
                        label { +"Role" }
                        select("form-control") {
                            name = "role"
                            USER_ROLES.keys.forEach { role ->
                                option {
                                    value = role.name
                                    if (user.role == role) selected = true
                                    +role.name.replace("_", " ")
                                }
                            }
                        }
                    }

                    div("form-group") {
                        label { +"Status" }
                        select("form-control") {
                            name = "status"
                            UserStatus.entries.forEach { status ->
                                option {
                                    value = status.name
                                    if (user.status == status) selected = true
                                    +status.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        +"Save Changes"
                    }
                }
            }
        }

        div("card") {
            div("card-header") { +"Reset Password" }
            div("card-body") {
                form(action = "/admin/settings/users/${user.id}/reset-password", method = FormMethod.post) {
                    div("form-group") {
                        label { +"New Password" }
                        input(type = InputType.password, classes = "form-control") {
                            name = "password"
                            minLength = "8"
                            placeholder = "Minimum 8 characters"
                        }
                    }

                    button(type = ButtonType.submit, classes = "btn btn-warning") {
                        +"Reset Password"
                    }
                }
            }
        }
    }
}

/**
 * API keys view.
 */
fun HTML.apiKeysView(
    session: AdminClaims,
    apiKeys: List<ApiKeyItem>
) {
    adminLayout(title = "API Keys", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Settings" }
            a(href = "/admin/settings/api-keys/new", classes = "btn btn-primary") {
                +"Create API Key"
            }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/settings", classes = "tab") { +"General" }
            a(href = "/admin/settings/users", classes = "tab") { +"Users" }
            a(href = "/admin/settings/api-keys", classes = "tab active") { +"API Keys" }
        }

        div("card") {
            if (apiKeys.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"🔑" }
                    p("empty-state-title") { +"No API keys" }
                    p("empty-state-text") { +"Create API keys for programmatic access to your tenant." }
                    a(href = "/admin/settings/api-keys/new", classes = "btn btn-primary") {
                        +"Create API Key"
                    }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Key" }
                            th { +"Scopes" }
                            th { +"Last Used" }
                            th { +"Expires" }
                            th { +"Actions" }
                        }
                    }
                    tbody {
                        apiKeys.forEach { key ->
                            tr {
                                td { +key.name }
                                td {
                                    code { +"${key.prefix}..." }
                                }
                                td {
                                    key.scopes.forEach { scope ->
                                        span("badge badge-gray mr-1") { +scope }
                                    }
                                }
                                td {
                                    key.lastUsed?.toString()?.substringBefore("T")
                                        ?: span("text-muted") { +"Never" }
                                }
                                td {
                                    key.expiresAt?.toString()?.substringBefore("T")
                                        ?: span("text-muted") { +"Never" }
                                }
                                td {
                                    form(action = "/admin/settings/api-keys/${key.id}/revoke", method = FormMethod.post, classes = "inline") {
                                        button(type = ButtonType.submit, classes = "btn btn-sm btn-danger") {
                                            attributes["onclick"] = "return confirm('Revoke this API key?')"
                                            +"Revoke"
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
}

/**
 * New API key view.
 */
fun HTML.newApiKeyView(
    session: AdminClaims,
    createdKey: String? = null,
    error: String? = null
) {
    adminLayout(title = "Create API Key", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/settings/api-keys", classes = "link") { +"← Back to API Keys" }
                h1("page-title") { +"Create API Key" }
            }
        }

        if (createdKey != null) {
            div("alert alert-success") {
                p { strong { +"API Key Created!" } }
                p { +"Copy your API key now. You won't be able to see it again." }
                div("input-group") {
                    input(type = InputType.text, classes = "form-control") {
                        readonly = true
                        value = createdKey
                    }
                    button(type = ButtonType.button, classes = "btn btn-secondary") {
                        attributes["onclick"] = "navigator.clipboard.writeText('$createdKey')"
                        +"Copy"
                    }
                }
            }
            a(href = "/admin/settings/api-keys", classes = "btn btn-primary") {
                +"Done"
            }
        } else {
            div("card") {
                div("card-body") {
                    if (error != null) {
                        div("alert alert-danger") { +error }
                    }

                    form(action = "/admin/settings/api-keys", method = FormMethod.post) {
                        div("form-group") {
                            label { +"Key Name" }
                            input(type = InputType.text, classes = "form-control") {
                                name = "name"
                                required = true
                                placeholder = "e.g., Integration API"
                            }
                        }

                        div("form-group") {
                            label { +"Scopes" }
                            div("checkbox") {
                                label {
                                    input(type = InputType.checkBox) {
                                        name = "scopes"
                                        value = "read"
                                        checked = true
                                    }
                                    +" Read - View assets, channels, schedules"
                                }
                            }
                            div("checkbox") {
                                label {
                                    input(type = InputType.checkBox) {
                                        name = "scopes"
                                        value = "write"
                                    }
                                    +" Write - Create and modify content"
                                }
                            }
                            div("checkbox") {
                                label {
                                    input(type = InputType.checkBox) {
                                        name = "scopes"
                                        value = "overlay"
                                    }
                                    +" Overlay - Push overlay data"
                                }
                            }
                        }

                        div("form-group") {
                            label { +"Expiration" }
                            select("form-control") {
                                name = "expiresIn"
                                option { value = "30"; +"30 days" }
                                option { value = "90"; +"90 days" }
                                option { value = "365"; +"1 year" }
                                option { value = "never"; +"Never" }
                            }
                        }

                        div("form-actions") {
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Create API Key"
                            }
                            a(href = "/admin/settings/api-keys", classes = "btn btn-secondary") {
                                +"Cancel"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun roleBadge(role: UserRole): String = when (role) {
    UserRole.TENANT_ADMIN -> "primary"
    UserRole.OPERATOR -> "info"
    UserRole.SUPPORT_AGENT -> "warning"
    UserRole.SUPPORT_ADMIN -> "danger"
}

private fun statusBadge(status: UserStatus): String = when (status) {
    UserStatus.ACTIVE -> "success"
    UserStatus.INACTIVE -> "warning"
    UserStatus.LOCKED -> "danger"
}
