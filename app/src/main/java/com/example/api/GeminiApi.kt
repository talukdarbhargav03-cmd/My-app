package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiHelper {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .retryOnConnectionFailure(true)
        .build()

    suspend fun callGemini(
        model: String,
        prompt: String,
        systemInstruction: String? = null,
        imageData: Pair<String, String>? = null, // mimeType, base64Data
        isHighThinking: Boolean = false,
        isMapsGrounding: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing or default. Please configure GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        try {
            // Build the JSON request body manually using standard org.json objects
            val requestJson = JSONObject()
            
            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Main text part
            val textPartObj = JSONObject().put("text", prompt)
            partsArray.put(textPartObj)

            // Image part if present
            if (imageData != null) {
                val imagePartObj = JSONObject()
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", imageData.first)
                inlineDataObj.put("data", imageData.second)
                imagePartObj.put("inlineData", inlineDataObj)
                partsArray.put(imagePartObj)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            if (isHighThinking && model.contains("pro")) {
                val thinkingConfig = JSONObject().put("thinkingLevel", "high")
                generationConfig.put("thinkingConfig", thinkingConfig)
            } else {
                generationConfig.put("temperature", 0.7)
            }
            requestJson.put("generationConfig", generationConfig)

            // System instructions
            if (systemInstruction != null) {
                val sysInstructionObj = JSONObject()
                val sysPartsArray = JSONArray().put(JSONObject().put("text", systemInstruction))
                sysInstructionObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstructionObj)
            }

            // Tools (Google Search / Maps Grounding)
            if (isMapsGrounding) {
                val toolsArray = JSONArray()
                val googleSearchObj = JSONObject().put("googleSearch", JSONObject())
                toolsArray.put(googleSearchObj)
                requestJson.put("tools", toolsArray)
            }

            val requestBodyString = requestJson.toString()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyString.toRequestBody(mediaType)

            val url = "$BASE_URL$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            var lastErrorMessage = ""
            var delayMs = 1000L

            for (attempt in 1..3) {
                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body"
                            val jsonResponse = JSONObject(responseBody)
                            val candidates = jsonResponse.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val content = firstCandidate.optJSONObject("content")
                                if (content != null) {
                                    val parts = content.optJSONArray("parts")
                                    if (parts != null && parts.length() > 0) {
                                        return@withContext parts.getJSONObject(0).optString("text", "No text in candidate parts.")
                                    }
                                }
                            }
                            return@withContext "No suggestion available. Please retry."
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            Log.e("GeminiApi", "Attempt $attempt failed with status code ${response.code}: $errorBody")
                            
                            try {
                                val json = JSONObject(errorBody)
                                if (json.has("error")) {
                                    val errorObj = json.getJSONObject("error")
                                    lastErrorMessage = errorObj.optString("message", "Unknown API error")
                                } else {
                                    lastErrorMessage = "Request failed with status code ${response.code}"
                                }
                            } catch (e: Exception) {
                                lastErrorMessage = "Request failed with status code ${response.code}"
                            }

                            // If it's a retriable error (like 503, 429, 504, 408, or 5xx server error) and we have more attempts, wait and retry
                            if ((response.code == 503 || response.code == 429 || response.code == 504 || response.code == 408 || response.code >= 500) && attempt < 3) {
                                kotlinx.coroutines.delay(delayMs)
                                delayMs *= 2
                                continue
                            } else {
                                return@withContext "API Error (Code ${response.code}): $lastErrorMessage"
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeminiApi", "Attempt $attempt exception", e)
                    lastErrorMessage = e.localizedMessage ?: e.message ?: "Unknown socket error"
                    if (attempt < 3) {
                        kotlinx.coroutines.delay(delayMs)
                        delayMs *= 2
                    } else {
                        return@withContext "Connection failed: $lastErrorMessage"
                    }
                }
            }
            "No suggestion available. Please retry."
        } catch (e: Exception) {
            Log.e("GeminiApi", "Error making Gemini request", e)
            "Connection failed: ${e.localizedMessage ?: e.message}"
        }
    }
}
