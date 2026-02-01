# Playout Edge - Use Cases

## Overview

This document describes all use cases for the Playout Edge digital signage platform, from initial landing page visit through complete user journeys.

---

## 1. Landing & Onboarding

### UC-1.1: Visit Landing Page
**Actor**: Visitor
**Precondition**: None
**Flow**:
1. Visitor opens the landing page URL
2. System displays hero section with value proposition
3. System shows features overview (content management, device fleet, overlays, analytics)
4. System displays pricing tiers
5. Visitor can click "Get Started" or "Request Demo"

**Postcondition**: Visitor understands product offering

---

### UC-1.2: Sign Up for New Account
**Actor**: New User
**Precondition**: User is on landing page
**Flow**:
1. User clicks "Get Started" button
2. System displays sign-up form (email, password, company name)
3. User fills in required fields
4. System validates input (email format, password strength)
5. System creates tenant and admin user
6. System sends verification email
7. User clicks verification link
8. System activates account and redirects to dashboard

**Postcondition**: User has active account and is logged in

---

### UC-1.3: Login to Admin Panel
**Actor**: Registered User
**Precondition**: User has active account
**Flow**:
1. User navigates to `/admin/login`
2. System displays login form
3. User enters email and password
4. System validates credentials
5. System creates session and redirects to dashboard

**Alternative Flow - Invalid Credentials**:
4a. System displays error "Invalid email or password"
4b. User can retry or reset password

**Postcondition**: User is authenticated and on dashboard

---

### UC-1.4: Reset Password
**Actor**: Registered User
**Precondition**: User forgot password
**Flow**:
1. User clicks "Forgot Password" on login page
2. System displays email input form
3. User enters registered email
4. System sends password reset link
5. User clicks link in email
6. System displays new password form
7. User enters and confirms new password
8. System updates password and redirects to login

**Postcondition**: User can login with new password

---

### UC-1.5: Complete Onboarding Wizard
**Actor**: New Admin
**Precondition**: First login after account creation
**Flow**:
1. System detects first login and shows onboarding wizard
2. Step 1: Upload first video asset
3. Step 2: Create first channel
4. Step 3: Generate enrollment code
5. Step 4: Show QR code for device enrollment
6. Step 5: Confirmation and link to dashboard

**Postcondition**: User has basic setup completed

---

## 2. Content Management

### UC-2.1: Upload Video Asset
**Actor**: Content Operator
**Precondition**: User is logged in
**Flow**:
1. User navigates to Assets page
2. User clicks "Upload Asset" button
3. System displays upload dialog
4. User selects video file from local filesystem
5. System validates file (format: MP4/WebM, size < quota)
6. System uploads file with progress indicator
7. System processes asset (extract metadata, generate thumbnail)
8. System displays asset in library with READY status

**Alternative Flow - Validation Failed**:
5a. System shows error (unsupported format, exceeds quota)
5b. User can select different file

**Postcondition**: Asset is available in library

---

### UC-2.2: Upload Image Asset
**Actor**: Content Operator
**Precondition**: User is logged in
**Flow**:
1. User navigates to Assets page
2. User clicks "Upload Asset"
3. User selects image file (PNG, JPEG, WebP)
4. System validates and uploads
5. System extracts dimensions
6. Asset is available in library

**Postcondition**: Image asset ready for use

---

### UC-2.3: View Asset Details
**Actor**: Content Operator
**Precondition**: Assets exist in library
**Flow**:
1. User navigates to Assets page
2. User clicks on asset thumbnail
3. System displays asset detail page
4. User sees: preview, name, type, status, dimensions, duration, file size, upload date
5. User sees which channels/schedules use this asset

**Postcondition**: User has full asset information

---

### UC-2.4: Archive Asset
**Actor**: Content Operator
**Precondition**: Asset exists and is not in active schedule
**Flow**:
1. User opens asset detail page
2. User clicks "Archive" button
3. System confirms action
4. System sets asset status to ARCHIVED
5. Asset is hidden from main library view

**Postcondition**: Asset is archived but recoverable

---

### UC-2.5: Search and Filter Assets
**Actor**: Content Operator
**Precondition**: Multiple assets exist
**Flow**:
1. User navigates to Assets page
2. User enters search term in search box
3. User optionally filters by type (VIDEO/IMAGE) or status
4. System displays matching assets
5. User can clear filters to see all

**Postcondition**: User finds desired asset

---

## 3. Channel Management

### UC-3.1: Create Channel
**Actor**: Admin
**Precondition**: User is logged in
**Flow**:
1. User navigates to Channels page
2. User clicks "Create Channel" button
3. System displays channel form
4. User enters channel name
5. User optionally selects default overlay profile
6. User clicks "Create"
7. System creates channel and redirects to detail page

**Postcondition**: Channel exists and can receive devices

---

### UC-3.2: View Channel Details
**Actor**: Operator
**Precondition**: Channel exists
**Flow**:
1. User navigates to Channels page
2. User clicks on channel row
3. System displays channel detail page showing:
   - Channel info (name, status, created date)
   - Current schedule with items
   - Assigned devices
   - Overlay binding info

**Postcondition**: User has full channel context

---

### UC-3.3: Edit Channel
**Actor**: Admin
**Precondition**: Channel exists
**Flow**:
1. User opens channel detail page
2. User clicks "Edit" button
3. User modifies name or overlay profile
4. User clicks "Save"
5. System updates channel

**Postcondition**: Channel is updated

---

### UC-3.4: Pause/Resume Channel
**Actor**: Admin
**Precondition**: Channel exists
**Flow**:
1. User opens channel detail page
2. User clicks "Pause" button
3. System sets channel status to PAUSED
4. Devices continue playing last known playlist but won't receive updates

**Resume Flow**:
1. User clicks "Resume" button
2. System sets status to ACTIVE
3. Devices receive latest playlist

**Postcondition**: Channel state is changed

---

### UC-3.5: Delete Channel
**Actor**: Admin
**Precondition**: Channel has no assigned devices
**Flow**:
1. User opens channel detail page
2. User clicks "Delete" button
3. System asks for confirmation
4. User confirms deletion
5. System archives channel

**Alternative Flow - Has Devices**:
4a. System shows error "Cannot delete: devices assigned"

**Postcondition**: Channel is archived

---

## 4. Schedule Management

### UC-4.1: Create Schedule Draft
**Actor**: Content Operator
**Precondition**: Channel exists
**Flow**:
1. User opens channel detail page
2. User clicks "Create Schedule" button
3. System creates new draft version
4. System displays schedule editor

**Postcondition**: Draft schedule exists

---

### UC-4.2: Add Item to Schedule
**Actor**: Content Operator
**Precondition**: Draft schedule exists
**Flow**:
1. User is in schedule editor
2. User clicks "Add Item" button
3. System displays asset picker
4. User selects asset from library
5. User sets playback rules (optional):
   - Valid from/to dates
   - Days of week
   - Time range (start/end)
   - Weight for random selection
6. User clicks "Add"
7. Item appears in schedule

**Postcondition**: Schedule has new item

---

### UC-4.3: Reorder Schedule Items
**Actor**: Content Operator
**Precondition**: Schedule has multiple items
**Flow**:
1. User is in schedule editor
2. User drags item to new position
3. System updates item order indexes

**Postcondition**: Items are reordered

---

### UC-4.4: Remove Item from Schedule
**Actor**: Content Operator
**Precondition**: Schedule has items
**Flow**:
1. User is in schedule editor
2. User clicks "Remove" on item
3. System removes item from schedule

**Postcondition**: Item is removed

---

### UC-4.5: Publish Schedule
**Actor**: Content Operator
**Precondition**: Draft schedule has items with READY assets
**Flow**:
1. User clicks "Publish" button
2. System validates:
   - All assets are READY
   - At least one item exists
3. System changes version status to PUBLISHED
4. System notifies devices of new playlist

**Alternative Flow - Validation Failed**:
2a. System shows validation errors
2b. User must fix issues before publishing

**Postcondition**: Schedule is live, devices receive update

---

### UC-4.6: Rollback Schedule
**Actor**: Admin
**Precondition**: Multiple published versions exist
**Flow**:
1. User opens schedule history
2. User selects previous version
3. User clicks "Rollback to this version"
4. System creates new version based on selected
5. System publishes new version

**Postcondition**: Previous schedule version is restored

---

### UC-4.7: View Schedule History
**Actor**: Operator
**Precondition**: Channel has schedule versions
**Flow**:
1. User opens channel detail page
2. User clicks "Schedule History" tab
3. System displays list of versions with:
   - Version number
   - Status (DRAFT, PUBLISHED, ROLLED_BACK)
   - Published date
   - Published by user

**Postcondition**: User sees version history

---

## 5. Device Management

### UC-5.1: Generate Enrollment Code
**Actor**: Admin
**Precondition**: User is logged in
**Flow**:
1. User navigates to Devices page
2. User clicks "Generate Enrollment Code"
3. System creates 6-digit code with 15-minute TTL
4. System displays code and QR code
5. User shows QR to device operator

**Postcondition**: Enrollment code ready for device

---

### UC-5.2: Enroll Device
**Actor**: Device (Android TV)
**Precondition**: Valid enrollment code exists
**Flow**:
1. Device starts and shows enrollment screen
2. Operator enters 6-digit code or scans QR
3. Device calls `/api/device-auth/enroll` with code
4. System validates code (exists, not expired, not used)
5. System creates device record with PENDING status
6. System returns device token
7. Device stores token securely
8. Admin sees new device in list with PENDING status

**Alternative Flow - Invalid Code**:
4a. System returns error
4b. Device shows "Invalid or expired code"

**Postcondition**: Device is enrolled and has auth token

---

### UC-5.3: Approve Device
**Actor**: Admin
**Precondition**: Device is in PENDING status
**Flow**:
1. Admin opens Devices page
2. Admin sees pending device
3. Admin clicks "Approve" button
4. System sets device status to ENROLLED
5. Device can now receive playlists

**Alternative Flow - Reject**:
3a. Admin clicks "Reject"
3b. System sets status to REJECTED
3c. Device cannot authenticate

**Postcondition**: Device is approved or rejected

---

### UC-5.4: Assign Device to Channel
**Actor**: Admin
**Precondition**: Device is enrolled, channel exists
**Flow**:
1. Admin opens device detail page
2. Admin clicks "Assign Channel"
3. Admin selects channel from dropdown
4. Admin clicks "Assign"
5. System updates device's assigned channel
6. Device fetches playlist for new channel

**Postcondition**: Device plays channel content

---

### UC-5.5: View Device Status
**Actor**: Operator
**Precondition**: Device exists
**Flow**:
1. User opens Devices page
2. User sees device list with:
   - Name
   - Assigned channel
   - Online/Offline status
   - Last seen timestamp
3. User clicks device for details:
   - App version
   - Android version
   - Device model
   - Recent heartbeats
   - Recent playback events

**Postcondition**: User has device status

---

### UC-5.6: Send Remote Action
**Actor**: Admin
**Precondition**: Device is enrolled
**Flow**:
1. Admin opens device detail page
2. Admin clicks "Actions" dropdown
3. Admin selects action:
   - Force Config Refresh
   - Force Playlist Refresh
   - Rotate Device Token
4. System creates pending action
5. Device polls and receives action
6. Device executes and acknowledges
7. Admin sees action status update

**Postcondition**: Action executed on device

---

### UC-5.7: Monitor Fleet Health
**Actor**: Operations
**Precondition**: Devices exist
**Flow**:
1. User opens Dashboard
2. System displays fleet health metrics:
   - Total devices
   - Online devices
   - Offline devices
   - Online percentage
3. User can drill down to offline devices
4. User can see alerts for devices offline > threshold

**Postcondition**: User knows fleet status

---

## 6. Overlay Management

### UC-6.1: Create Overlay Profile
**Actor**: Admin
**Precondition**: User is logged in
**Flow**:
1. User navigates to Overlays page
2. User clicks "Create Profile"
3. User enters profile name
4. User selects template or creates custom JSON:
   - Ticker (bottom text scroll)
   - KPI Tiles (top-right metrics)
   - Queue Table (center table)
   - QR Card (bottom-right QR code)
5. User customizes widget positions and properties
6. User clicks "Create"

**Postcondition**: Overlay profile ready for binding

---

### UC-6.2: Bind Overlay to Channel
**Actor**: Admin
**Precondition**: Profile and channel exist
**Flow**:
1. User navigates to Overlay Bindings
2. User clicks "Create Binding"
3. User selects channel
4. User selects overlay profile
5. User selects data source:
   - MANUAL (admin updates via UI)
   - REST_PULL (periodic fetch from URL)
   - WEBHOOK (push from external system)
6. User configures source settings
7. User clicks "Create"

**Postcondition**: Channel has overlay binding

---

### UC-6.3: Update Overlay State (Manual)
**Actor**: Operator
**Precondition**: Binding with MANUAL source exists
**Flow**:
1. User opens binding detail page
2. User edits widget values in form
3. User clicks "Update"
4. System saves state and pushes via SSE
5. Device receives and renders immediately

**Postcondition**: Overlay updated on screens

---

### UC-6.4: Configure REST Data Source
**Actor**: Admin
**Precondition**: Binding with REST_PULL source exists
**Flow**:
1. User opens binding detail page
2. User enters REST endpoint URL
3. User sets polling interval (e.g., 60 seconds)
4. User configures auth (API key, bearer token)
5. User sets field mapping
6. User clicks "Save"
7. System starts polling and updating overlay

**Postcondition**: Overlay auto-updates from external API

---

### UC-6.5: View Overlay on Device
**Actor**: Viewer
**Precondition**: Device has channel with overlay binding
**Flow**:
1. Device plays video content
2. Device connects to SSE stream
3. Device receives overlay state
4. Device renders overlay widgets on top of video
5. When state changes, overlay updates without interrupting video

**Postcondition**: Overlay visible on screen

---

## 7. Reporting & Analytics

### UC-7.1: View As-Run Report
**Actor**: Operator
**Precondition**: Devices have been playing content
**Flow**:
1. User navigates to Reports > As-Run
2. User sets date range filter
3. User optionally filters by device or channel
4. System displays playback events:
   - Timestamp
   - Device
   - Asset
   - Event type (PLAY_START, PLAY_END, SKIP, ERROR)
5. User can see summary stats

**Postcondition**: User has playback audit trail

---

### UC-7.2: Export As-Run to CSV
**Actor**: Operator
**Precondition**: As-run data exists
**Flow**:
1. User sets filters on As-Run report
2. User clicks "Export CSV"
3. System generates CSV file
4. Browser downloads file

**Postcondition**: User has exportable report

---

### UC-7.3: View Audit Log
**Actor**: Admin
**Precondition**: Actions have been performed
**Flow**:
1. User navigates to Reports > Audit
2. User sees log entries:
   - Timestamp
   - Actor (user or system)
   - Action (create, update, delete, publish)
   - Entity type and ID
3. User can filter by entity type or action
4. User can view diff for update actions

**Postcondition**: User has change history

---

### UC-7.4: View Device Uptime Report
**Actor**: Operations
**Precondition**: Devices have heartbeat history
**Flow**:
1. User navigates to Reports > Device Uptime
2. User selects date range
3. System calculates uptime percentage per device
4. System displays:
   - Device name
   - Total uptime %
   - Downtime periods
5. User can drill into specific device

**Postcondition**: User knows device reliability

---

## 8. Alerts & Monitoring

### UC-8.1: View Active Alerts
**Actor**: Operator
**Precondition**: Alerts have been triggered
**Flow**:
1. User sees alert badge on Dashboard
2. User clicks to view alerts list
3. System displays alerts:
   - Type (DEVICE_OFFLINE, PLAYBACK_ERROR, STORAGE_QUOTA)
   - Severity
   - First occurred
   - Last occurred
   - Occurrence count

**Postcondition**: User aware of issues

---

### UC-8.2: Acknowledge Alert
**Actor**: Operator
**Precondition**: Open alert exists
**Flow**:
1. User opens alert detail
2. User clicks "Acknowledge"
3. System updates alert status
4. Alert remains visible but marked as acknowledged

**Postcondition**: Alert acknowledged

---

### UC-8.3: Resolve Alert
**Actor**: Operator
**Precondition**: Issue is fixed
**Flow**:
1. User opens acknowledged alert
2. User clicks "Resolve"
3. User optionally adds resolution notes
4. System marks alert resolved with timestamp

**Postcondition**: Alert closed

---

## 9. Settings & Administration

### UC-9.1: View Tenant Settings
**Actor**: Admin
**Precondition**: User is tenant admin
**Flow**:
1. User navigates to Settings
2. System displays:
   - Tenant name
   - Storage quota (used/limit)
   - Device quota (enrolled/limit)
   - Timezone
   - Release ring

**Postcondition**: User sees tenant config

---

### UC-9.2: Manage Users
**Actor**: Admin
**Precondition**: User is tenant admin
**Flow**:
1. User navigates to Settings > Users
2. User sees user list with roles
3. User can:
   - Invite new user
   - Change user role
   - Deactivate user
   - Reset user password

**Postcondition**: Users managed

---

### UC-9.3: Invite User
**Actor**: Admin
**Precondition**: User has admin role
**Flow**:
1. User clicks "Invite User"
2. User enters email, display name, role
3. User sets temporary password
4. System creates user
5. User can share credentials with invitee

**Postcondition**: New user created

---

### UC-9.4: Manage API Keys
**Actor**: Admin
**Precondition**: User is tenant admin
**Flow**:
1. User navigates to Settings > API Keys
2. User clicks "Create API Key"
3. User enters name and selects scopes
4. System generates key and shows once
5. User copies key for integration

**Postcondition**: API key available for integrations

---

### UC-9.5: Revoke API Key
**Actor**: Admin
**Precondition**: API key exists
**Flow**:
1. User opens API Keys list
2. User clicks "Revoke" on key
3. System confirms
4. Key is immediately invalidated

**Postcondition**: API key no longer works

---

## 10. Logout & Session Management

### UC-10.1: Logout
**Actor**: Authenticated User
**Precondition**: User is logged in
**Flow**:
1. User clicks profile dropdown
2. User clicks "Logout"
3. System invalidates session
4. System redirects to login page

**Postcondition**: User is logged out

---

### UC-10.2: Session Timeout
**Actor**: System
**Precondition**: User session is idle
**Flow**:
1. User session reaches timeout threshold
2. System invalidates session
3. Next request redirects to login
4. User must re-authenticate

**Postcondition**: Session ended

---

## Summary

| Category | Use Cases |
|----------|-----------|
| Landing & Onboarding | 5 |
| Content Management | 5 |
| Channel Management | 5 |
| Schedule Management | 7 |
| Device Management | 7 |
| Overlay Management | 5 |
| Reporting & Analytics | 4 |
| Alerts & Monitoring | 3 |
| Settings & Administration | 5 |
| Session Management | 2 |
| **Total** | **48** |
