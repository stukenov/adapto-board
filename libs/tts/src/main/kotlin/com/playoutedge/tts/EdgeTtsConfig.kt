package com.playoutedge.tts

enum class TtsVoice(val voiceName: String, val displayName: String) {
    RU_DARIYA("ru-RU-DariyaNeural", "Дарья (женский)"),
    RU_IVAN("ru-RU-IvanNeural", "Иван (мужской)"),
    EN_JENNY("en-US-JennyNeural", "Jenny (female)"),
    EN_GUY("en-US-GuyNeural", "Guy (male)"),
    KK_AIGUL("kk-KZ-AigulNeural", "Айгуль (женский)"),
    KK_DAULET("kk-KZ-DauletNeural", "Дәулет (мужской)")
}

data class EdgeTtsConfig(
    val voice: TtsVoice = TtsVoice.RU_DARIYA,
    val rate: String = "+0%",
    val volume: String = "+0%",
    val pitch: String = "+0Hz",
    val outputFormat: String = "audio-24khz-48kbitrate-mono-mp3"
)
