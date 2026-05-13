package com.envirowatchsi.api

import com.envirowatchsi.model.AirQualityStation
import com.envirowatchsi.model.HydroStation
import com.envirowatchsi.model.MeteoStation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "http://localhost:8080"
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
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun postJson(path: String, jsonBody: String): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        connection.outputStream.use {
            it.write(jsonBody.toByteArray(Charsets.UTF_8))
        }

        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun putForm(path: String, formBody: String): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        connection.outputStream.use {
            it.write(formBody.toByteArray(Charsets.UTF_8))
        }

        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun delete(path: String): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}