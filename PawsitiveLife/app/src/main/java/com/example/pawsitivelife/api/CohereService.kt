package com.example.pawsitivelife.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object CohereService {

    private const val API_KEY = "jML1cAhTmdPnpJkpZal1FtAeIjLKAEqm9WRnHJBa"
    private const val ENDPOINT = "https://api.cohere.ai/v1/chat"
    private val mainHandler = Handler(Looper.getMainLooper()) // Used to post to UI thread

    fun classifyArticleAge(articleContent: String, onResult: (String?) -> Unit) {
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

        val json = JSONObject().apply {
            put("message", prompt)
            put("temperature", 0.3)
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(ENDPOINT)
            .post(body)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("COHERE", "❌ Network error: ${e.message}", e)
                // Ensure result is returned on main thread
                mainHandler.post { onResult(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("COHERE", "❌ API error ${response.code}: ${response.message}")
                        mainHandler.post { onResult(null) }
                        return
                    }

                    try {
                        val responseBody = response.body?.string()
                        val responseJson = JSONObject(responseBody ?: "")
                        val content = responseJson.getString("text")

                        Log.d("COHERE", "✔️ Age category: $content")
                        // Ensure result is returned on main thread
                        mainHandler.post { onResult(content) }
                    } catch (e: Exception) {
                        Log.e("COHERE", "❌ Failed to parse response", e)
                        mainHandler.post { onResult(null) }
                    }
                }
            }
        })
    }
}