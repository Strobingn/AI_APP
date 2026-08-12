package com.austin.aiapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.austin.aiapp.data.AppDatabase
import com.austin.aiapp.data.ChatMessage
import com.austin.aiapp.data.MessageEntity
import com.austin.aiapp.data.PreferencesRepository
import com.austin.aiapp.network.StreamingClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesRepository(application)
    private val db = AppDatabase.get(application)
    private val dao = db.messageDao()

    private val conversationId = UUID.randomUUID().toString()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _serverUrl = MutableStateFlow("http://100.x.x.x:11434/v1")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _modelName = MutableStateFlow("gemma-4-abliterated")
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.serverUrl.collect { _serverUrl.value = it }
        }
        viewModelScope.launch {
            prefs.modelName.collect { _modelName.value = it }
        }
        // Load history if needed later
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            prefs.setServerUrl(url)
            _serverUrl.value = url
        }
    }

    fun updateModelName(name: String) {
        viewModelScope.launch {
            prefs.setModelName(name)
            _modelName.value = name
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isStreaming.value) return

        val userMsg = ChatMessage(role = "user", content = text)
        _messages.value = _messages.value + userMsg
        _inputText.value = ""
        _isStreaming.value = true

        viewModelScope.launch {
            dao.insert(MessageEntity(
                conversationId = conversationId,
                role = "user",
                content = text
            ))

            val assistantBuffer = StringBuilder()
            _messages.value = _messages.value + ChatMessage(role = "assistant", content = "")

            try {
                val client = StreamingClient(_serverUrl.value)
                val history = _messages.value.dropLast(1).map { it.role to it.content }

                client.streamChat(_modelName.value, history).collect { token ->
                    assistantBuffer.append(token)
                    val current = _messages.value.toMutableList()
                    current[current.lastIndex] = ChatMessage(role = "assistant", content = assistantBuffer.toString())
                    _messages.value = current
                }

                dao.insert(MessageEntity(
                    conversationId = conversationId,
                    role = "assistant",
                    content = assistantBuffer.toString()
                ))
            } catch (e: Exception) {
                val current = _messages.value.toMutableList()
                current[current.lastIndex] = ChatMessage(
                    role = "assistant",
                    content = "Error: ${e.message}\n\nCheck Tailscale IP and that the server is running."
                )
                _messages.value = current
            } finally {
                _isStreaming.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        viewModelScope.launch {
            dao.clearConversation(conversationId)
        }
    }
}
