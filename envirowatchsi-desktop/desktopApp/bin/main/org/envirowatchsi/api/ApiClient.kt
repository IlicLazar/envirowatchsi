package org.envirowatchsi.api

import org.envirowatchsi.models.AirQualityStation
import org.envirowatchsi.models.HydroStation
import org.envirowatchsi.models.MeteoStation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "http://localhost:3000"
    private const val TIMEOUT_MS = 5000
    private val gson = Gson()

    fun getAirQualityStations(): List<AirQualityStation> {
        val json = getRaw("/api/air-quality")
        val type = object : TypeToken<List<AirQualityStation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getMeteoStations(): List<MeteoStation> {
        val json = getRaw("/api/meteo")
        val type = object : TypeToken<List<MeteoStation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getHydroStations(): List<HydroStation> {
        val json = getRaw("/api/hydro")
        val type = object : TypeToken<List<HydroStation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getRaw(path: String): String {
        return request(
            path = path,
            method = "GET"
        )
    }

    fun postJson(path: String, jsonBody: String): String {
        return request(
            path = path,
            method = "POST",
            body = jsonBody,
            contentType = "application/json"
        )
    }

    fun putJson(path: String, jsonBody: String): String {
        return request(
            path = path,
            method = "PUT",
            body = jsonBody,
            contentType = "application/json"
        )
    }

    fun putForm(path: String, formBody: String): String {
        return request(
            path = path,
            method = "PUT",
            body = formBody,
            contentType = "application/x-www-form-urlencoded"
        )
    }

    fun delete(path: String): String {
        return request(
            path = path,
            method = "DELETE"
        )
    }

    private fun request(
        path: String,
        method: String,
        body: String? = null,
        contentType: String? = null
    ): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection

        connection.requestMethod = method
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS

        if (body != null) {
            connection.doOutput = true
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType)
            }

            connection.outputStream.use {
                it.write(body.toByteArray(Charsets.UTF_8))
            }
        }

        return try {
            val responseCode = connection.responseCode
            val responseText =
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "No error body"
                }

            if (responseCode !in 200..299) {
                throw RuntimeException("HTTP $responseCode: $responseText")
            }

            responseText
        } finally {
            connection.disconnect()
        }
    }
}