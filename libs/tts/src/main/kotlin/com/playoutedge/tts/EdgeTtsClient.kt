package com.playoutedge.tts

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.io.ByteArrayOutputStream
import java.util.UUID

class EdgeTtsClient {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    suspend fun synthesize(text: String, config: EdgeTtsConfig = EdgeTtsConfig()): ByteArray {
        val requestId = UUID.randomUUID().toString().replace("-", "")
        val gecToken = GecTokenGenerator.generate()
        val trustedToken = GecTokenGenerator.getTrustedClientToken()

        val wsUrl = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1" +
            "?TrustedClientToken=$trustedToken" +
            "&Sec-MS-GEC=$gecToken" +
            "&Sec-MS-GEC-Version=1-130.0.2849.68" +
            "&ConnectionId=$requestId"

        val audioOutput = ByteArrayOutputStream()

        client.webSocket(
            urlString = wsUrl,
            request = {
                headers.append("Origin", "chrome-extension://jdiccldimpdaibmpdmdieoccpehgjnai")
                headers.append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0")
            }
        ) {
            // Send speech config
            val speechConfig = buildString {
                append("Content-Type:application/json; charset=utf-8\r\n")
                append("Path:speech.config\r\n\r\n")
                append("""{"context":{"synthesis":{"audio":{"metadataOptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"${config.outputFormat}"}}}}""")
            }
            send(Frame.Text(speechConfig))

            // Send SSML
            val ssml = buildString {
                append("X-RequestId:$requestId\r\n")
                append("Content-Type:application/ssml+xml\r\n")
                append("Path:ssml\r\n\r\n")
                append("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>")
                append("<voice name='${config.voice.voiceName}'>")
                append("<prosody rate='${config.rate}' volume='${config.volume}' pitch='${config.pitch}'>")
                append(escapeXml(text))
                append("</prosody></voice></speak>")
            }
            send(Frame.Text(ssml))

            // Receive audio frames
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val data = frame.readBytes()
                            // Binary frames have a 2-byte header length prefix
                            if (data.size > 2) {
                                val headerLen = (data[0].toInt() and 0xFF shl 8) or (data[1].toInt() and 0xFF)
                                if (headerLen + 2 < data.size) {
                                    audioOutput.write(data, headerLen + 2, data.size - headerLen - 2)
                                }
                            }
                        }
                        is Frame.Text -> {
                            val text = frame.readText()
                            if (text.contains("Path:turn.end")) {
                                break
                            }
                        }
                        else -> {}
                    }
                }
            } catch (_: ClosedReceiveChannelException) {
                // Connection closed
            }
        }

        return audioOutput.toByteArray()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun close() {
        client.close()
    }
}
