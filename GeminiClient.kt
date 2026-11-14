
package com.myra.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    suspend fun ask(apiKey: String, prompt: String): String =
        withContext(Dispatchers.IO) {

            val url = URL(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"
            )

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty(
                "Content-Type", "application/json"
            )
            connection.doOutput = true

            val body = JSONObject().apply {
                put("contents", org.json.JSONArray().put(
                    JSONObject().put("parts",
                        org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        )
                    )
                ))
            }

            connection.outputStream.use {
                it.write(body.toString().toByteArray())
            }

            val responseCode = connection.responseCode

            val response = (
                if (responseCode in 200..299)
                    connection.inputStream
                else connection.errorStream
            ).bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("Gemini Error $responseCode: $response")
            }

            JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
}
