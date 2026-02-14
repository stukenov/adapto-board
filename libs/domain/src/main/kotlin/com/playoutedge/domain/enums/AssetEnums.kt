package com.playoutedge.domain.enums

enum class AssetType {
    VIDEO,
    IMAGE,
    AUDIO,
    SLIDESHOW
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
