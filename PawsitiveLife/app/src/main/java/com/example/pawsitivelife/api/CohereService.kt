package com.example.pawsitivelife.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object CohereService {

    // API key for accessing the Cohere API
    private const val API_KEY = "B2yoHjSTcy2jETz3OoUCZrdMblAvPb17IO6B4wmK"
    // Cohere's chat endpoint used for classification
    private const val ENDPOINT = "https://api.cohere.ai/v1/chat"
    // Handler to post results back to the main (UI) thread
    private val mainHandler = Handler(Looper.getMainLooper()) // Used to post to UI thread

    // This function sends the article content to the Cohere API to classify it by age category
    fun classifyArticleAge(articleContent: String, onResult: (String?) -> Unit) {

        // Prompt sent to the AI model for classification
        val prompt = """
              Classify the following article for dogs into the relevant age groups.
              Possible groups:
              - Puppies (up to 6 months)
              - Adults (6 months to 6 years)
              - Seniors (above 6 years)

              You may choose more than one group if appropriate.
              Return only the list of group names, comma-separated, no explanation.

              Article:
              $articleContent
        """.trimIndent()

        // Create JSON body for the request
        val json = JSONObject().apply {
            put("message", prompt)
            put("temperature", 0.3)  //// Controls randomness; low value = more consistent responses
        }

        // Convert JSON to RequestBody
        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        // Build the HTTP request
        val request = Request.Builder()
            .url(ENDPOINT)
            .post(body)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .build()

        // Asynchronously send the HTTP request
        OkHttpClient().newCall(request).enqueue(object : Callback {

            // Called if request fails (e.g., network error)
            override fun onFailure(call: Call, e: IOException) {
                Log.e("COHERE", "❌ Network error: ${e.message}", e)
                mainHandler.post { onResult(null) } // Return null result to UI thread
            }

            // Called when a response is received
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("COHERE", "❌ API error ${response.code}: ${response.message}")
                        mainHandler.post { onResult(null) }
                        return
                    }

                    try {
                        // Extract and parse the response
                        val responseBody = response.body?.string()
                        val responseJson = JSONObject(responseBody ?: "")
                        val content = responseJson.getString("text") // Extract classification result

                        Log.d("COHERE", "✔️ Age category: $content")
                        mainHandler.post { onResult(content) } // Return result to UI thread
                    } catch (e: Exception) {
                        Log.e("COHERE", "❌ Failed to parse response", e)
                        mainHandler.post { onResult(null) }
                    }
                }
            }
        })
    }
}