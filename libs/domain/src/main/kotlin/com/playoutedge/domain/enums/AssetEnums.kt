package com.playoutedge.domain.enums

enum class AssetType {
    VIDEO,
    IMAGE
}

enum class AssetStatus {
    UPLOADING,
    PROCESSING,
    READY,
    REJECTED,
    ARCHIVED
}

enum class AssetProfile {
    ORIGINAL,
    NORMALIZED
}
