package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.MeenaAiRepository
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class MeenaChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

class MeenaAiViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<MeenaChatMessage>>(emptyList())
    val messages: StateFlow<List<MeenaChatMessage>> = _messages.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _dangerAlertActive = MutableStateFlow(false)
    val dangerAlertActive: StateFlow<Boolean> = _dangerAlertActive.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    private var lastPrompt: String? = null
    private var lastLanguage: AppLanguage = AppLanguage.EN
    private var lastCity: String = "Puducherry"
    private var lastRiskContext: String = ""
    private var streamJob: Job? = null

    fun ensureWelcome(language: AppLanguage) {
        if (_messages.value.isEmpty()) {
            _messages.value = listOf(
                MeenaChatMessage(
                    id = "welcome",
                    isUser = false,
                    text = AppStrings.meenaWelcome(language)
                )
            )
        }
    }

    fun sendMessage(
        prompt: String,
        language: AppLanguage,
        cityContext: String,
        riskContext: String
    ) {
        val clean = prompt.trim()
        if (clean.isEmpty() || _isStreaming.value) return

        lastPrompt = clean
        lastLanguage = language
        lastCity = cityContext
        lastRiskContext = riskContext
        _errorBanner.value = null

        if (MeenaAiRepository.detectImmediateDangerIntent(clean)) {
            _dangerAlertActive.value = true
        }

        val userMsg = MeenaChatMessage(isUser = true, text = clean)
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantPlaceholder = MeenaChatMessage(id = assistantMsgId, isUser = false, text = "")
        val priorPairs = _messages.value
            .filter { !it.isError && it.text.isNotBlank() }
            .map { (if (it.isUser) "User" else "Meena") to it.text }

        _messages.value = _messages.value + userMsg + assistantPlaceholder
        _isStreaming.value = true

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            try {
                val flow = MeenaAiRepository.streamReply(
                    historyPairs = priorPairs,
                    userMessage = clean,
                    language = language,
                    cityContext = cityContext,
                    riskContext = riskContext
                )
                val sb = StringBuilder()
                flow.collect { chunk ->
                    sb.append(chunk)
                    val current = sb.toString()
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMsgId) msg.copy(text = current) else msg
                    }
                }
                if (sb.isBlank()) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(
                                text = if (language == AppLanguage.TA) {
                                    "மன்னிக்கவும், பதில் பெற முடியவில்லை. அவசரத்திற்கு 112 அல்லது 181-ஐ அழைக்கவும்."
                                } else {
                                    "I couldn't generate a response right now. If this is urgent, please tap Call 112 or Helpline 181."
                                },
                                isError = true
                            )
                        } else msg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errText = e.localizedMessage ?: "Unable to connect to Meena AI service."
                _errorBanner.value = errText
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == assistantMsgId) {
                        msg.copy(
                            text = "Connection error: $errText",
                            isError = true
                        )
                    } else msg
                }
            } finally {
                _isStreaming.value = false
            }
        }
    }

    fun retryLastPrompt() {
        val p = lastPrompt ?: return
        // Remove trailing error message + duplicate user message before resending cleanly
        val list = _messages.value
        val trimmed = if (list.size >= 2 && list.last().isError && list[list.lastIndex - 1].isUser) {
            list.dropLast(2)
        } else {
            list
        }
        _messages.value = trimmed
        sendMessage(p, lastLanguage, lastCity, lastRiskContext)
    }

    fun dismissDangerBanner() {
        _dangerAlertActive.value = false
    }
}
