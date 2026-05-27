package org.envirowatchsi.api

import org.envirowatchsi.models.AirQualityStation
import org.envirowatchsi.models.HydroStation
import org.envirowatchsi.models.MeteoStation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "http://68.210.201.189:3000"
    private const val TIMEOUT_MS = 5000
    private const val TOKEN_ENV = "ENVIROWATCHSI_ADMIN_TOKEN"
    private const val TOKEN_PROPERTY = "envirowatchsi.adminToken"
    private val gson = Gson()
    private var sessionAdminToken: String? = null
    private val tokenFiles = listOf(
        File("admin-token.txt"),
        File("envirowatchsi-desktop/admin-token.txt"),
        File("../admin-token.txt")
    )

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

    fun login(email: String, password: String): LoginResult {
        val response = request(
            path = "/api/auth/login",
            method = "POST",
            body = gson.toJson(mapOf("email" to email, "password" to password)),
            contentType = "application/json"
        )
        val authResponse = gson.fromJson(response, AuthResponse::class.java)
        val token = authResponse.token?.trim()

        if (token.isNullOrBlank()) {
            throw RuntimeException("Prijava je uspela, vendar API ni vrnil tokena.")
        }

        sessionAdminToken = token

        return LoginResult(
            token = token,
            username = authResponse.user?.username,
            email = authResponse.user?.email,
            role = authResponse.user?.role
        )
    }

    fun logout() {
        sessionAdminToken = null
    }

    fun postJson(path: String, jsonBody: String): String {
        return request(
            path = path,
            method = "POST",
            body = jsonBody,
            contentType = "application/json",
            requiresAdminToken = true
        )
    }

    fun putJson(path: String, jsonBody: String): String {
        return request(
            path = path,
            method = "PUT",
            body = jsonBody,
            contentType = "application/json",
            requiresAdminToken = true
        )
    }

    fun putForm(path: String, formBody: String): String {
        return request(
            path = path,
            method = "PUT",
            body = formBody,
            contentType = "application/json",
            requiresAdminToken = true
        )
    }

    fun delete(path: String): String {
        return request(
            path = path,
            method = "DELETE",
            requiresAdminToken = true
        )
    }

    private fun request(
        path: String,
        method: String,
        body: String? = null,
        contentType: String? = null,
        requiresAdminToken: Boolean = false
    ): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection

        connection.requestMethod = method
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS

        if (requiresAdminToken) {
            connection.setRequestProperty("Authorization", "Bearer ${adminToken()}")
        }

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

    private fun adminToken(): String {
        if (!sessionAdminToken.isNullOrBlank()) {
            return sessionAdminToken!!.trim()
        }

        val configuredToken = listOf(
            System.getProperty(TOKEN_PROPERTY),
            System.getenv(TOKEN_ENV)
        ).firstOrNull { !it.isNullOrBlank() }

        if (!configuredToken.isNullOrBlank()) {
            return configuredToken.trim()
        }

        val token = tokenFiles
            .firstOrNull { it.isFile }
            ?.readText()
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotBlank() && !it.startsWith("#") }

        if (!token.isNullOrBlank()) {
            return token
        }

        val expectedFiles = tokenFiles.joinToString(", ") { it.path }
        throw IllegalStateException(
            "Administratorski token manjka. Vnesi JWT token v envirowatchsi-desktop/admin-token.txt " +
                "ali nastavi $TOKEN_ENV. Preverjene poti: $expectedFiles"
        )
    }
}

data class LoginResult(
    val token: String,
    val username: String?,
    val email: String?,
    val role: String?
)

private data class AuthResponse(
    val token: String?,
    val user: AuthUser?
)

private data class AuthUser(
    val username: String?,
    val email: String?,
    val role: String?
)
