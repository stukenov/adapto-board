package com.playoutedge.server.views.settings

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.breadcrumb
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.icon
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.roleLabel
import com.playoutedge.server.views.statCard
import kotlinx.datetime.Clock
import kotlinx.html.*

/**
 * Settings main page with tabs and improved UX.
 */
fun HTML.settingsMainView(
    session: AdminClaims,
    settings: TenantSettingsView,
    storageQuota: StorageQuotaView,
    deviceQuota: DeviceQuotaView,
    success: String? = null,
    error: String? = null
) {
    adminLayout(title = "Settings", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = "Settings",
            subtitle = "Manage your organization settings and quotas"
        )

        // Tabs
        settingsTabs("/admin/settings")

        if (success != null) {
            alertBox(success, "success")
        }
        if (error != null) {
            alertBox(error, "error")
        }

        // Quotas
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-icon") { icon("database") }
                div("stat-content") {
                    div("stat-value") { +"${storageQuota.usedFormatted} / ${storageQuota.limitFormatted}" }
                    div("stat-label") { +"Storage Usage" }
                    div("quota-bar mt-2") {
                        val barClass = when {
                            storageQuota.usedPercent >= 90 -> "danger"
                            storageQuota.usedPercent >= 70 -> "warning"
                            else -> ""
                        }
                        div("quota-fill $barClass") {
                            style = "width: ${storageQuota.usedPercent}%"
                        }
                    }
                }
            }
            div("stat-card") {
                div("stat-icon") { icon("monitor") }
                div("stat-content") {
                    div("stat-value") { +"${deviceQuota.enrolled} / ${deviceQuota.limit}" }
                    div("stat-label") { +"Device Slots" }
                    div("quota-bar mt-2") {
                        val barClass = when {
                            deviceQuota.usedPercent >= 90 -> "danger"
                            deviceQuota.usedPercent >= 70 -> "warning"
                            else -> ""
                        }
                        div("quota-fill $barClass") {
                            style = "width: ${deviceQuota.usedPercent}%"
                        }
                    }
                }
            }
        }

        // Tenant Settings Form
        div("card") {
            div("card-header") {
                h3 { +"Organization Settings" }
            }
            div("card-body") {
                form(action = "/admin/settings", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "name"
                            +"Organization Name"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "name"
                            name = "name"
                            value = settings.name
                            required = true
                        }
                    }

                    div("form-row") {
                        div("form-group col-6") {
                            label {
                                htmlFor = "timezone"
                                +"Timezone"
                            }
                            select("form-control") {
                                id = "timezone"
                                name = "timezone"
                                TIMEZONES.forEach { (tzId, tzLabel) ->
                                    option {
                                        value = tzId
                                        if (settings.timezone == tzId) selected = true
                                        +tzLabel
                                    }
                                }
                            }
                            small("form-helper") { +"Used for scheduling and reporting." }
                        }

                        div("form-group col-6") {
                            label {
                                htmlFor = "releaseRing"
                                +"Release Ring"
                            }
                            select("form-control") {
                                id = "releaseRing"
                                name = "releaseRing"
                                RELEASE_RINGS.forEach { ring ->
                                    option {
                                        value = ring
                                        if (settings.releaseRing == ring) selected = true
                                        +ring
                                    }
                                }
                            }
                            small("form-helper") { +"CANARY receives updates earlier, STABLE is more conservative." }
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "offlineThresholdMinutes"
                            +"Offline Threshold"
                        }
                        div("input-group") {
                            input(type = InputType.number, classes = "form-control") {
                                id = "offlineThresholdMinutes"
                                name = "offlineThresholdMinutes"
                                min = "2"
                                max = "10"
                                value = settings.offlineThresholdMinutes.toString()
                            }
                            span("input-group-text") { +"minutes" }
                        }
                        small("form-helper") { +"Device is considered offline after this many minutes without heartbeat." }
                    }

                    div("card-section mt-4") {
                        h4 { +"Maintenance Mode" }
                        div("form-group") {
                            div("checkbox") {
                                label {
                                    input(type = InputType.checkBox) {
                                        id = "maintenanceMode"
                                        name = "maintenanceMode"
                                        if (settings.maintenanceMode) checked = true
                                    }
                                    +" Enable Maintenance Mode"
                                }
                            }
                            small("form-helper") { +"When enabled, devices will show a maintenance message instead of content." }
                        }

                        div("form-group") {
                            label {
                                htmlFor = "maintenanceReason"
                                +"Maintenance Reason"
                            }
                            input(type = InputType.text, classes = "form-control") {
                                id = "maintenanceReason"
                                name = "maintenanceReason"
                                value = settings.maintenanceReason ?: ""
                                placeholder = "e.g., Scheduled maintenance until 5 PM"
                            }
                            small("form-helper") { +"Optional message to display on devices during maintenance." }
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Settings"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Users list view with improved UX.
 */
fun HTML.usersListView(
    session: AdminClaims,
    users: List<UserListItem>
) {
    adminLayout(title = "Users", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = "Settings",
            subtitle = "Manage team members and their access"
        ) {
            a(href = "/admin/settings/users/invite", classes = "btn btn-primary") {
                +"+ Invite User"
            }
        }

        // Tabs
        settingsTabs("/admin/settings/users")

        div("card") {
            if (users.isEmpty()) {
                emptyState(
                    icon = "users",
                    title = "No users",
                    description = "Invite team members to help manage your content and devices.",
                    actionHref = "/admin/settings/users/invite",
                    actionLabel = "Invite User"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Email" }
                            th { +"Role" }
                            th { +"Status" }
                            th { +"Invite" }
                            th { }
                        }
                    }
                    tbody {
                        users.forEach { user ->
                            val now = Clock.System.now()
                            val isExpired = user.inviteExpiresAt?.let { it < now } ?: false
                            tr {
                                td {
                                    span("font-medium") { +user.displayName }
                                }
                                td {
                                    span("text-muted") { +user.email }
                                }
                                td {
                                    span("badge badge-${roleBadge(user.role)} badge-plain") {
                                        +user.role.name.replace("_", " ")
                                    }
                                }
                                td {
                                    span("badge badge-${statusBadge(user.status)}") {
                                        +user.status.name.lowercase()
                                    }
                                }
                                td {
                                    if (user.inviteExpiresAt != null) {
                                        if (isExpired) {
                                            span("badge badge-danger") { +"Expired" }
                                            form(action = "/admin/settings/users/${user.id}/resend-invite", method = FormMethod.post, classes = "inline mt-1") {
                                                button(type = ButtonType.submit, classes = "btn btn-sm btn-secondary") {
                                                    +"Resend"
                                                }
                                            }
                                        } else {
                                            span("text-muted") {
                                                +"Expires ${user.inviteExpiresAt.toString().substringBefore("T")}"
                                            }
                                        }
                                    } else {
                                        span("text-muted") { +"-" }
                                    }
                                }
                                td {
                                    div("table-actions") {
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
}

/**
 * Invite user form with improved UX.
 */
fun HTML.inviteUserView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "Invite User", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = "Invite User",
            subtitle = "Add a new team member to your organization",
            backHref = "/admin/settings/users",
            backLabel = "Back to Users"
        )

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }

                form(action = "/admin/settings/users/invite", method = FormMethod.post) {
                    div("form-row") {
                        div("form-group col-6") {
                            label {
                                htmlFor = "displayName"
                                +"Display Name"
                            }
                            input(type = InputType.text, classes = "form-control") {
                                id = "displayName"
                                name = "displayName"
                                required = true
                                placeholder = "John Doe"
                            }
                        }

                        div("form-group col-6") {
                            label {
                                htmlFor = "email"
                                +"Email Address"
                            }
                            input(type = InputType.email, classes = "form-control") {
                                id = "email"
                                name = "email"
                                required = true
                                placeholder = "user@example.com"
                            }
                        }
                    }

                    div("form-group") {
                        label {
                            htmlFor = "password"
                            +"Temporary Password"
                        }
                        div("input-group") {
                            input(type = InputType.password, classes = "form-control") {
                                id = "password"
                                name = "password"
                                required = true
                                minLength = "8"
                                placeholder = "Minimum 8 characters"
                            }
                            button(type = ButtonType.button, classes = "btn btn-secondary") {
                                attributes["onclick"] = "generatePassword('password')"
                                +"Generate"
                            }
                        }
                        small("form-helper") { +"User should change this password on their first login." }
                    }

                    div("form-group") {
                        label { +"Role" }
                        div("role-options") {
                            USER_ROLES.forEach { (role, description) ->
                                div("radio-card") {
                                    label {
                                        input(type = InputType.radio) {
                                            name = "role"
                                            value = role.name
                                            if (role == UserRole.OPERATOR) checked = true
                                        }
                                        div("radio-card-content") {
                                            span("radio-card-title") { +role.name.replace("_", " ") }
                                            span("radio-card-description") { +description }
                                        }
                                    }
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
 * Edit user view with improved UX.
 */
fun HTML.editUserView(
    session: AdminClaims,
    user: UserDetail,
    error: String? = null,
    success: String? = null
) {
    adminLayout(title = "Edit User", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = "Edit User",
            subtitle = user.email,
            backHref = "/admin/settings/users",
            backLabel = "Back to Users"
        ) {
            span("badge badge-${statusBadge(user.status)} badge-lg") {
                +user.status.name.lowercase()
            }
        }

        if (success != null) {
            alertBox(success, "success")
        }
        if (error != null) {
            alertBox(error, "error")
        }

        div("card mb-4") {
            div("card-header") {
                h3 { +"User Details" }
            }
            div("card-body") {
                form(action = "/admin/settings/users/${user.id}", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "email"
                            +"Email"
                        }
                        input(type = InputType.email, classes = "form-control") {
                            id = "email"
                            value = user.email
                            readonly = true
                            disabled = true
                        }
                        small("form-helper") { +"Email cannot be changed." }
                    }

                    div("form-row") {
                        div("form-group col-6") {
                            label {
                                htmlFor = "displayName"
                                +"Display Name"
                            }
                            input(type = InputType.text, classes = "form-control") {
                                id = "displayName"
                                name = "displayName"
                                value = user.displayName
                            }
                        }

                        div("form-group col-3") {
                            label {
                                htmlFor = "role"
                                +"Role"
                            }
                            select("form-control") {
                                id = "role"
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

                        div("form-group col-3") {
                            label {
                                htmlFor = "status"
                                +"Status"
                            }
                            select("form-control") {
                                id = "status"
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
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Changes"
                        }
                    }
                }
            }
        }

        div("card") {
            div("card-header") {
                h3 { +"Security" }
            }
            div("card-body") {
                form(action = "/admin/settings/users/${user.id}/reset-password", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "resetPassword"
                            +"New Password"
                        }
                        div("input-group") {
                            input(type = InputType.password, classes = "form-control") {
                                id = "resetPassword"
                                name = "password"
                                minLength = "8"
                                placeholder = "Minimum 8 characters"
                            }
                            button(type = ButtonType.button, classes = "btn btn-secondary") {
                                attributes["onclick"] = "generatePassword('resetPassword')"
                                +"Generate"
                            }
                        }
                        small("form-helper") { +"User will need to use this password on their next login." }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-warning") {
                            +"Reset Password"
                        }
                    }
                }
            }
        }
    }
}

/**
 * API keys view with improved UX.
 */
fun HTML.apiKeysView(
    session: AdminClaims,
    apiKeys: List<ApiKeyItem>
) {
    adminLayout(title = "API Keys", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = "Settings",
            subtitle = "Manage API keys for programmatic access"
        ) {
            a(href = "/admin/settings/api-keys/new", classes = "btn btn-primary") {
                +"+ Create API Key"
            }
        }

        // Tabs
        settingsTabs("/admin/settings/api-keys")

        div("card") {
            if (apiKeys.isEmpty()) {
                emptyState(
                    icon = "key",
                    title = "No API keys",
                    description = "Create API keys for programmatic access to your tenant's data and operations.",
                    actionHref = "/admin/settings/api-keys/new",
                    actionLabel = "Create API Key"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Key" }
                            th { +"Scopes" }
                            th { +"Last Used" }
                            th { +"Expires" }
                            th { }
                        }
                    }
                    tbody {
                        apiKeys.forEach { key ->
                            tr {
                                td {
                                    span("font-medium") { +key.name }
                                }
                                td {
                                    code("text-muted") { +"${key.prefix}..." }
                                }
                                td {
                                    key.scopes.forEach { scope ->
                                        span("badge badge-gray badge-plain mr-1") { +scope }
                                    }
                                }
                                td {
                                    key.lastUsed?.toString()?.substringBefore("T")?.let {
                                        span("text-muted") { +it }
                                    } ?: span("text-muted") { +"Never" }
                                }
                                td {
                                    key.expiresAt?.toString()?.substringBefore("T")?.let {
                                        span("text-muted") { +it }
                                    } ?: span("text-muted") { +"Never" }
                                }
                                td {
                                    div("table-actions") {
                                        form(action = "/admin/settings/api-keys/${key.id}/revoke", method = FormMethod.post, classes = "inline") {
                                            button(type = ButtonType.submit, classes = "btn btn-sm btn-danger") {
                                                attributes["onclick"] = "return confirm('Revoke this API key? This action cannot be undone.')"
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
}

/**
 * New API key view with improved UX.
 */
fun HTML.newApiKeyView(
    session: AdminClaims,
    createdKey: String? = null,
    error: String? = null
) {
    adminLayout(title = "Create API Key", userName = session.displayName, currentPath = "/admin/settings") {
        pageHeader(
            title = if (createdKey != null) "API Key Created" else "Create API Key",
            subtitle = if (createdKey != null) "Save your key now - it won't be shown again" else "Generate a new API key for programmatic access",
            backHref = "/admin/settings/api-keys",
            backLabel = "Back to API Keys"
        )

        if (createdKey != null) {
            div("card") {
                div("card-body") {
                    div("success-banner mb-4") {
                        div("success-icon") { +"✓" }
                        h3 { +"API Key Created Successfully" }
                        p("text-muted") { +"Copy your API key now. For security reasons, you won't be able to see it again." }
                    }

                    div("api-key-display mb-4") {
                        div("input-group input-group-lg") {
                            input(type = InputType.text, classes = "form-control code-font") {
                                id = "apiKey"
                                readonly = true
                                value = createdKey
                            }
                            button(type = ButtonType.button, classes = "btn btn-primary") {
                                attributes["onclick"] = "navigator.clipboard.writeText('$createdKey'); this.textContent = 'Copied!'; setTimeout(() => this.textContent = 'Copy', 2000)"
                                +"Copy"
                            }
                        }
                    }

                    alertBox("Store this key securely. It provides access to your tenant's data.", "warning")

                    div("form-actions mt-4") {
                        a(href = "/admin/settings/api-keys", classes = "btn btn-primary") {
                            +"Done"
                        }
                    }
                }
            }
        } else {
            div("card") {
                div("card-body") {
                    if (error != null) {
                        alertBox(error, "error")
                    }

                    form(action = "/admin/settings/api-keys", method = FormMethod.post) {
                        div("form-group") {
                            label {
                                htmlFor = "name"
                                +"Key Name"
                            }
                            input(type = InputType.text, classes = "form-control") {
                                id = "name"
                                name = "name"
                                required = true
                                placeholder = "e.g., Integration API, CMS Webhook"
                            }
                            small("form-helper") { +"A descriptive name to identify this key." }
                        }

                        div("form-group") {
                            label { +"Permissions" }
                            div("scope-options") {
                                div("checkbox-card") {
                                    label {
                                        input(type = InputType.checkBox) {
                                            name = "scopes"
                                            value = "read"
                                            checked = true
                                        }
                                        div("checkbox-card-content") {
                                            span("checkbox-card-title") { +"Read" }
                                            span("checkbox-card-description") { +"View assets, channels, schedules, and device status" }
                                        }
                                    }
                                }
                                div("checkbox-card") {
                                    label {
                                        input(type = InputType.checkBox) {
                                            name = "scopes"
                                            value = "write"
                                        }
                                        div("checkbox-card-content") {
                                            span("checkbox-card-title") { +"Write" }
                                            span("checkbox-card-description") { +"Create and modify assets, channels, and schedules" }
                                        }
                                    }
                                }
                                div("checkbox-card") {
                                    label {
                                        input(type = InputType.checkBox) {
                                            name = "scopes"
                                            value = "overlay"
                                        }
                                        div("checkbox-card-content") {
                                            span("checkbox-card-title") { +"Overlay" }
                                            span("checkbox-card-description") { +"Push real-time overlay data to devices" }
                                        }
                                    }
                                }
                            }
                        }

                        div("form-group") {
                            label {
                                htmlFor = "expiresIn"
                                +"Expiration"
                            }
                            select("form-control") {
                                id = "expiresIn"
                                name = "expiresIn"
                                option { value = "30"; +"30 days" }
                                option { value = "90"; +"90 days" }
                                option { value = "365"; +"1 year" }
                                option { value = "never"; +"Never (not recommended)" }
                            }
                            small("form-helper") { +"Shorter expiration periods are recommended for security." }
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

/**
 * Notification preferences view.
 */
fun HTML.notificationPreferencesView(
    session: AdminClaims,
    prefs: NotificationPrefsView,
    success: Boolean = false
) {
    adminLayout(
        title = "Notification Settings",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/settings"
    ) {
        pageHeader(
            title = "Settings",
            subtitle = "Configure your notification preferences"
        )

        // Tabs
        settingsTabs("/admin/settings/notifications")

        if (success) {
            alertBox("Notification preferences saved.", "success")
        }

        div("card") {
            div("card-header") {
                h3 { +"Email Notifications" }
            }
            div("card-body") {
                form(action = "/admin/settings/notifications", method = FormMethod.post) {
                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "emailOnDeviceOffline"
                                    if (prefs.emailOnDeviceOffline) checked = true
                                }
                                +" Device goes offline"
                            }
                        }
                        small("form-helper") { +"Receive an email when a device stops sending heartbeats." }
                    }

                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "emailOnAlert"
                                    if (prefs.emailOnAlert) checked = true
                                }
                                +" System alerts"
                            }
                        }
                        small("form-helper") { +"Receive an email for important system alerts and warnings." }
                    }

                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "emailOnSchedulePublish"
                                    if (prefs.emailOnSchedulePublish) checked = true
                                }
                                +" Schedule published"
                            }
                        }
                        small("form-helper") { +"Receive an email when a schedule is published to a channel." }
                    }

                    div("form-group") {
                        div("checkbox") {
                            label {
                                input(type = InputType.checkBox) {
                                    name = "emailDailyDigest"
                                    if (prefs.emailDailyDigest) checked = true
                                }
                                +" Daily digest"
                            }
                        }
                        small("form-helper") { +"Receive a daily summary of device status and activity." }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Preferences"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Security / active sessions view.
 */
fun HTML.securityView(
    session: AdminClaims,
    sessions: List<SessionItem>,
    success: String? = null
) {
    adminLayout(
        title = "Security",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/settings"
    ) {
        pageHeader(
            title = "Settings",
            subtitle = "Manage active sessions and security"
        )

        // Tabs
        settingsTabs("/admin/settings/security")

        if (success != null) {
            alertBox(success, "success")
        }

        div("card") {
            div("card-header") {
                h3 { +"Active Sessions" }
            }
            div("card-body") {
                if (sessions.isEmpty()) {
                    emptyState(
                        icon = "lock",
                        title = "No active sessions",
                        description = "No active sessions found for your account."
                    )
                } else {
                    table("table") {
                        thead {
                            tr {
                                th { +"IP Address" }
                                th { +"Browser / Client" }
                                th { +"Started" }
                                th { +"Last Active" }
                                th { }
                            }
                        }
                        tbody {
                            sessions.forEach { sess ->
                                tr {
                                    td {
                                        code { +(sess.ipAddress ?: "Unknown") }
                                    }
                                    td {
                                        span("text-muted") {
                                            +(sess.userAgent?.take(60)?.let { "$it..." } ?: "Unknown")
                                        }
                                    }
                                    td {
                                        span("text-muted") { +sess.createdAt.toString().substringBefore("T") }
                                    }
                                    td {
                                        span("text-muted") { +sess.lastActiveAt.toString().substringBefore("T") }
                                    }
                                    td {
                                        div("table-actions") {
                                            if (sess.isCurrent) {
                                                span("badge badge-success") { +"Current" }
                                            } else {
                                                form(action = "/admin/settings/security/terminate/${sess.id}", method = FormMethod.post, classes = "inline") {
                                                    button(type = ButtonType.submit, classes = "btn btn-sm btn-danger") {
                                                        attributes["onclick"] = "return confirm('Terminate this session?')"
                                                        +"Terminate"
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

private val SETTINGS_TABS = listOf(
    TabDef("General", "/admin/settings"),
    TabDef("Users", "/admin/settings/users"),
    TabDef("API Keys", "/admin/settings/api-keys"),
    TabDef("Notifications", "/admin/settings/notifications"),
    TabDef("Security", "/admin/settings/security")
)

fun FlowContent.settingsTabs(currentPath: String) {
    pageTabs(SETTINGS_TABS, currentPath)
}
