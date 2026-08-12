package com.austin.aiapp.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface LlmApi {
    @POST("chat/completions")
    suspend fun chatCompletions(@Body body: Map<String, Any>): ChatResponse

    companion object {
        fun create(baseUrl: String): LlmApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(LlmApi::class.java)
        }
    }
}

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<Choice>?
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: Message?
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String?,
    val content: String?
)
