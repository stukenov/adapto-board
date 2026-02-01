package com.playoutedge.domain.enums

enum class ActionType {
    FORCE_CONFIG_REFRESH,
    FORCE_PLAYLIST_REFRESH,
    ROTATE_DEVICE_TOKEN
}

enum class ActionStatus {
    PENDING,
    ACKED,
    FAILED,
    EXPIRED
}

enum class AlertType {
    DEVICE_OFFLINE,
    PLAYBACK_ERROR,
    STORAGE_QUOTA
}

enum class AlertStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED
}

enum class JobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

enum class ContactType {
    TECHNICAL,
    BUSINESS,
    BILLING
}
