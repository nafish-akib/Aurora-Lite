package com.aurora.browser.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class VoiceSearchManager(context: Context) {
    private var recognizer: SpeechRecognizer? = null
    private val appContext = context.applicationContext

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    suspend fun listen(): VoiceResult = suspendCancellableCoroutine { cont ->
        cleanup() // destroy any active recognizer first
        val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
        recognizer = sr
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        var retryCount = 0
        val maxRetries = 2
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("AuroraVoice", "Ready for speech")
            }
            override fun onBeginningOfSpeech() {
                Log.d("AuroraVoice", "Speech started")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("AuroraVoice", "Speech ended")
            }
            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
                    else -> "Recognition error $error"
                }
                Log.e("AuroraVoice", "Error: $msg")
                if (cont.isActive) {
                    if ((error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) && retryCount < maxRetries) {
                        retryCount++
                        Log.d("AuroraVoice", "Retrying ($retryCount/$maxRetries)...")
                        try { sr.cancel() } catch (_: Exception) {}
                        sr.startListening(intent)
                    } else {
                        cont.resume(VoiceResult(error = msg))
                        cleanup()
                    }
                } else {
                    cleanup()
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                Log.d("AuroraVoice", "Result: $text")
                if (cont.isActive) cont.resume(VoiceResult(text = text))
                cleanup()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    Log.d("AuroraVoice", "Partial: $text")
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        sr.startListening(intent)
        cont.invokeOnCancellation { cleanup() }
    }

    fun cancel() {
        recognizer?.cancel()
        cleanup()
    }

    private fun cleanup() {
        try { recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
    }
}

data class VoiceResult(
    val text: String = "",
    val error: String? = null
)
