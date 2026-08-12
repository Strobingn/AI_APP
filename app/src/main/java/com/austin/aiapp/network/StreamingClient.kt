package com.austin.aiapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StreamingClient(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // infinite for streaming
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun streamChat(
        model: String,
        messages: List<Pair<String, String>>
    ): Flow<String> = flow {
        val jsonMessages = JSONArray()
        messages.forEach { (role, content) ->
            jsonMessages.put(JSONObject().apply {
                put("role", role)
                put("content", content)
            })
        }

        val body = JSONObject().apply {
            put("model", model)
            put("messages", jsonMessages)
            put("stream", true)
        }.toString()

        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/chat/completions")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                emit("Error: ${response.code} ${response.message}")
                return@flow
            }

            val source = response.body?.source() ?: return@flow
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    try {
                        val obj = JSONObject(data)
                        val delta = obj
                            .optJSONArray("choices")
                            ?.optJSONObject(0)
                            ?.optJSONObject("delta")
                            ?.optString("content")
                        if (!delta.isNullOrEmpty()) {
                            emit(delta)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
