package com.playoutedge.domain.media

/**
 * System-wide media validation defaults.
 * These can be overridden at tenant level in future.
 */
object MediaValidationDefaults {
    // Video constraints
    val MAX_VIDEO_WIDTH = 1920
    val MAX_VIDEO_HEIGHT = 1080
    val MAX_VIDEO_BITRATE_BPS = 15_000_000L   // 15 Mbps
    val MAX_VIDEO_DURATION_SEC = 3600         // 1 hour
    val MAX_VIDEO_SIZE_BYTES = 2_147_483_648L // 2 GB
    val ALLOWED_VIDEO_CODECS = setOf("h264", "avc", "h.264")
    val ALLOWED_VIDEO_CONTAINERS = setOf("mp4", "m4v")
    val ALLOWED_VIDEO_MIME_TYPES = setOf("video/mp4", "video/x-m4v")

    // Image constraints
    val MAX_IMAGE_WIDTH = 3840
    val MAX_IMAGE_HEIGHT = 2160
    val MAX_IMAGE_SIZE_BYTES = 20_971_520L    // 20 MB
    val ALLOWED_IMAGE_FORMATS = setOf("png", "jpeg", "jpg")
    val ALLOWED_IMAGE_MIME_TYPES = setOf("image/png", "image/jpeg")

    // Audio constraints
    val MAX_AUDIO_SIZE_BYTES = 104_857_600L    // 100 MB
    val MAX_AUDIO_DURATION_SEC = 3600          // 1 hour
    val ALLOWED_AUDIO_MIME_TYPES = setOf("audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg")
}

/**
 * Rejection reasons with human-readable messages.
 * Used when an asset fails validation.
 */
enum class RejectionReason(val message: String, val hint: String) {
    UNSUPPORTED_FORMAT(
        "File format is not supported",
        "Supported: MP4 video, PNG/JPEG images, MP3/WAV/OGG audio."
    ),
    UNSUPPORTED_CONTAINER(
        "Only MP4 video files are supported",
        "Please convert your video to MP4 format using H.264 codec."
    ),
    UNSUPPORTED_CODEC(
        "Video codec must be H.264",
        "Re-encode your video using H.264 (AVC) codec."
    ),
    RESOLUTION_TOO_HIGH(
        "Resolution exceeds maximum allowed (1920x1080)",
        "Resize your video to Full HD (1920x1080) or lower."
    ),
    BITRATE_TOO_HIGH(
        "Bitrate exceeds maximum allowed (15 Mbps)",
        "Re-encode your video with a lower bitrate."
    ),
    FILE_TOO_LARGE(
        "File size exceeds maximum allowed",
        "Compress or trim your file to reduce size."
    ),
    DURATION_TOO_LONG(
        "Duration exceeds maximum allowed (1 hour)",
        "Split your video into shorter segments."
    ),
    CORRUPT_FILE(
        "File appears to be corrupted or unreadable",
        "Try uploading the file again or use a different source."
    )
}

/**
 * Result of validating a media file.
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()

    data class Invalid(
        val reason: RejectionReason,
        val details: String? = null
    ) : ValidationResult()
}

/**
 * Media metadata extracted from a file.
 */
data class MediaMetadata(
    val mimeType: String,
    val fileSizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null,
    val codec: String? = null,
    val bitrateBps: Long? = null,
    val container: String? = null
)
