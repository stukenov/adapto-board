package com.playoutedge.server.services

import com.playoutedge.storage.StorageService
import com.playoutedge.tts.EdgeTtsClient
import com.playoutedge.tts.EdgeTtsConfig
import com.playoutedge.tts.TtsVoice
import java.io.ByteArrayInputStream
import java.util.UUID

class TtsService(
    private val storageService: StorageService
) {
    private val ttsClient = EdgeTtsClient()

    suspend fun generateVoiceover(text: String, voice: TtsVoice = TtsVoice.RU_DARIYA): String {
        val config = EdgeTtsConfig(voice = voice)
        val audioBytes = ttsClient.synthesize(text, config)
        val storageKey = "voiceover/${UUID.randomUUID()}.mp3"
        storageService.put(storageKey, ByteArrayInputStream(audioBytes), audioBytes.size.toLong())
        return storageKey
    }

    suspend fun previewVoiceover(text: String, voice: TtsVoice = TtsVoice.RU_DARIYA): ByteArray {
        val config = EdgeTtsConfig(voice = voice)
        return ttsClient.synthesize(text, config)
    }
}
