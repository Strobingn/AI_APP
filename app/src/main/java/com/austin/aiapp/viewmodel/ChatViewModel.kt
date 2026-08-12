package com.austin.aiapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austin.aiapp.data.ChatMessage
import com.austin.aiapp.network.LlmApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    // Default to local Ollama / LM Studio over Tailscale. Change in Settings later.
    private var baseUrl = "http://100.x.x.x:11434/v1" // replace with your Tailscale IP
    private val api = LlmApi.create(baseUrl)

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isStreaming.value) return

        val userMsg = ChatMessage(role = "user", content = text)
        _messages.value = _messages.value + userMsg
        _inputText.value = ""
        _isStreaming.value = true

        viewModelScope.launch {
            try {
                val response = api.chatCompletions(
                    mapOf(
                        "model" to "gemma-4-abliterated", // or whatever is loaded
                        "messages" to _messages.value.map {
                            mapOf("role" to it.role, "content" to it.content)
                        },
                        "stream" to false // switch to true + SSE later for full streaming
                    )
                )
                val content = response.choices?.firstOrNull()?.message?.content ?: "No response"
                _messages.value = _messages.value + ChatMessage(role = "assistant", content = content)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    role = "assistant",
                    content = "Error: ${e.message}\n\nCheck Tailscale IP and that Ollama/LM Studio is running."
                )
            } finally {
                _isStreaming.value = false
            }
        }
    }
}
