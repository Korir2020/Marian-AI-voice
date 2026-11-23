package com.marian.voiceai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object RealtimeSessionClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun createSession(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(BuildConfig.BACKEND_BASE_URL.trimEnd('/') + "/api/realtime/session")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            http.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = runCatching { JSONObject(body).optString("error") }
                        .getOrDefault("Backend returned HTTP ${response.code}")
                    throw IOException(message.ifBlank { "Backend returned HTTP ${response.code}" })
                }
                // This is short-lived. Do not log or persist it.
                val json = JSONObject(body)
                val secret = json.optString("value").takeIf { it.isNotBlank() }
                    ?: json.optString("client_secret").takeIf { it.isNotBlank() }
                    ?: json.optString("secret")
                if (secret.isNullOrBlank()) {
                    throw IOException("Session response did not contain a client secret.")
                }
                secret
            }
        }
    }
}
