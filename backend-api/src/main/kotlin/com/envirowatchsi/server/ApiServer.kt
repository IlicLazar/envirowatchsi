package com.envirowatchsi.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.envirowatchsi.database.DatabaseRepository
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

object ApiServer {

    fun start() {
        com.envirowatchsi.database.DatabaseFactory.init()
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}

private const val JWT_SECRET = "enviro-watch-secret"
private const val JWT_ISSUER = "envirowatchsi"
private const val JWT_AUDIENCE = "envirowatchsi-users"

fun generateJwtToken(username: String): String {
    return JWT.create()
        .withAudience(JWT_AUDIENCE)
        .withIssuer(JWT_ISSUER)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
        .sign(Algorithm.HMAC256(JWT_SECRET))
}

fun Application.module() {
    val gson = Gson()
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to API"

            verifier(
                JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/") {
            call.respondText("EnviroWatch SI API is running")
        }
        get("/health") {
            call.respondText("OK")
        }
        get("/api/status") {
            val databaseStatus = try {
                transaction {
                    exec("SELECT 1")
                }
                "connected"
            } catch (e: Exception) {
                "error: ${e.message}"
            }
            call.respondText("API running, database: $databaseStatus")
        }

        post("/api/register") {
            val body = call.receiveText()
            val data = gson.fromJson(body, Map::class.java)

            val username = data["username"] as? String
            val password = data["password"] as? String

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                call.respondText("Username and password are required", status = HttpStatusCode.BadRequest)
                return@post
            }
            if (DatabaseRepository.userExists(username)) {
                call.respondText("User already exists", status = HttpStatusCode.Conflict)
                return@post
            }
            DatabaseRepository.registerUser(username, password)
            call.respondText("User registered", status = HttpStatusCode.Created)
        }

        post("/api/login") {
            val body = call.receiveText()
            val data = gson.fromJson(body, Map::class.java)

            val username = data["username"] as? String
            val password = data["password"] as? String

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                call.respondText("Username and password are required", status = HttpStatusCode.BadRequest)
                return@post
            }

            val isValidUser = DatabaseRepository.validateUser(username, password)

            if (!isValidUser) {
                call.respondText("Invalid username or password", status = HttpStatusCode.Unauthorized)
                return@post
            }
            val token = generateJwtToken(username)
            call.respondText(
                gson.toJson(mapOf("token" to token)),
                ContentType.Application.Json
            )
        }
        get("/api/air-quality") {
            call.respondText(
                gson.toJson(DatabaseRepository.getAirQualityRecords()),
                ContentType.Application.Json
            )
        }

        get("/api/meteo") {
            call.respondText(
                gson.toJson(DatabaseRepository.getMeteoRecords()),
                ContentType.Application.Json
            )
        }

        get("/api/hydro") {
            call.respondText(
                gson.toJson(DatabaseRepository.getHydroRecords()),
                ContentType.Application.Json
            )
        }
        get("/api/data") {
            val type = call.request.queryParameters["type"]
            when (type) {
                "air-quality" -> {
                    call.respondText(
                        gson.toJson(DatabaseRepository.getAirQualityRecords()),
                        ContentType.Application.Json
                    )
                }
                "meteo" -> {
                    call.respondText(
                        gson.toJson(DatabaseRepository.getMeteoRecords()),
                        ContentType.Application.Json
                    )
                }
                "hydro" -> {
                    call.respondText(
                        gson.toJson(DatabaseRepository.getHydroRecords()),
                        ContentType.Application.Json
                    )
                }
                else -> {
                    call.respondText(
                        "Invalid or missing type. Use: air-quality, meteo, hydro",
                        status = HttpStatusCode.BadRequest
                    )
                }
            }
        }
        get("/api/data/by-date") {
            val type = call.request.queryParameters["type"]
            val from = call.request.queryParameters["from"]
            val to = call.request.queryParameters["to"]
            
            if (type.isNullOrBlank() || from.isNullOrBlank() || to.isNullOrBlank()) {
                call.respondText(
                    "Missing parameters. Use: type, from, to",
                    status = HttpStatusCode.BadRequest
                )
                return@get
            }
            val records = when (type) {
                "air-quality" -> DatabaseRepository.getAirQualityRecords()
                "meteo" -> DatabaseRepository.getMeteoRecords()
                "hydro" -> DatabaseRepository.getHydroRecords()
                else -> {
                    call.respondText(
                        "Invalid type. Use: air-quality, meteo, hydro",
                        status = HttpStatusCode.BadRequest
                    )
                    return@get
                }
            }

            val result = records.filter { record ->
                val measuredAt = record["measuredAt"] as? String
                measuredAt != null && measuredAt >= from && measuredAt <= to
            }
            call.respondText(
                gson.toJson(result),
                ContentType.Application.Json
            )
        }

        authenticate("auth-jwt") {
        post("/api/air-quality") {

            val body = call.receiveText()

            val data = Gson().fromJson(
                body,
                Map::class.java
            )

            val stationName = data["stationName"] as? String
            val latitude = data["latitude"] as? Double
            val longitude = data["longitude"] as? Double
            val aqi = data["aqi"] as? Double

            if (stationName.isNullOrBlank() || aqi == null) {
                call.respondText(
                    "Invalid input",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            DatabaseRepository.insertAirQualityRecord(
                stationName,
                latitude,
                longitude,
                aqi.toInt()
            )

            call.respondText(
                "Air quality record saved",
                status = HttpStatusCode.Created
            )
        }


        post("/api/meteo") {
            val body = call.receiveText()

            val data = Gson().fromJson(
                body,
                Map::class.java
            )

            val stationName = data["stationName"] as? String
            val latitude = data["latitude"] as? Double
            val longitude = data["longitude"] as? Double
            val temperature = data["temperature"] as? Double
            val humidity = data["humidity"] as? Double
            val windSpeed = data["windSpeed"] as? Double
            val precipitation = data["precipitation"] as? Double

            if (stationName.isNullOrBlank() || temperature == null || humidity == null) {
                call.respondText("Invalid meteo input", status = HttpStatusCode.BadRequest)
                return@post
            }

            DatabaseRepository.insertMeteoRecord(
                stationName,
                latitude,
                longitude,
                temperature,
                humidity,
                windSpeed,
                precipitation
            )

            call.respondText("Meteo record saved", status = HttpStatusCode.Created)
        }

        post("/api/hydro") {
            val body = call.receiveText()

            val data = Gson().fromJson(
                body,
                Map::class.java
            )

            val stationName = data["stationName"] as? String
            val riverName = data["riverName"] as? String
            val latitude = data["latitude"] as? Double
            val longitude = data["longitude"] as? Double
            val waterLevel = data["waterLevel"] as? Double
            val waterFlow = data["waterFlow"] as? Double

            if (stationName.isNullOrBlank() || riverName.isNullOrBlank()) {
                call.respondText("Invalid hydro input", status = HttpStatusCode.BadRequest)
                return@post
            }

            DatabaseRepository.insertHydroRecord(
                stationName,
                riverName,
                latitude,
                longitude,
                waterLevel,
                waterFlow
            )

            call.respondText("Hydro record saved", status = HttpStatusCode.Created)
        }

        put("/api/air-quality/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val latitude = p["latitude"]?.toDoubleOrNull() ?: 0.0
            val longitude = p["longitude"]?.toDoubleOrNull() ?: 0.0
            val pm10 = p["pm10"]?.toDoubleOrNull()
            val pm25 = p["pm25"]?.toDoubleOrNull()
            val o3 = p["o3"]?.toDoubleOrNull()
            val co = p["co"]?.toDoubleOrNull()
            val so2 = p["so2"]?.toDoubleOrNull()
            val aqi = p["aqi"]?.toDoubleOrNull()

            if (id == null || stationName.isNullOrBlank() || aqi == null) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateAirQualityRecord(
                id,
                stationName,
                latitude,
                longitude,
                pm10,
                pm25,
                o3,
                co,
                so2,
                aqi
            )

            call.respondText("Air quality record updated")
        }

        put("/api/meteo/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val latitude = p["latitude"]?.toDoubleOrNull()
            val longitude = p["longitude"]?.toDoubleOrNull()
            val temperature = p["temperature"]?.toDoubleOrNull()
            val humidity = p["humidity"]?.toDoubleOrNull()
            val windSpeed = p["windSpeed"]?.toDoubleOrNull()
            val windDirection = p["windDirection"]?.takeIf { it.isNotBlank() }
            val precipitation = p["precipitation"]?.toDoubleOrNull()

            if (id == null || stationName.isNullOrBlank() || temperature == null || humidity == null) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateMeteoRecord(
                id,
                stationName,
                latitude,
                longitude,
                temperature,
                humidity,
                windSpeed,
                windDirection,
                precipitation
            )

            call.respondText("Meteo record updated")
        }

        put("/api/hydro/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val riverName = p["riverName"]
            val latitude = p["latitude"]?.toDoubleOrNull()
            val longitude = p["longitude"]?.toDoubleOrNull()
            val waterLevel = p["waterLevel"]?.toDoubleOrNull()
            val waterFlow = p["waterFlow"]?.toDoubleOrNull()

            if (id == null || stationName.isNullOrBlank() || riverName.isNullOrBlank()) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateHydroRecord(
                id,
                stationName,
                riverName,
                latitude,
                longitude,
                waterLevel,
                waterFlow
            )

            call.respondText("Hydro record updated")
        }

        delete("/api/air-quality/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText(
                    "Invalid ID",
                    status = HttpStatusCode.BadRequest
                )
                return@delete
            }

            DatabaseRepository.deleteAirQualityRecord(id)

            call.respondText("Air quality record deleted")
        }

        delete("/api/meteo/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText(
                    "Invalid ID",
                    status = HttpStatusCode.BadRequest
                )
                return@delete
            }

            DatabaseRepository.deleteMeteoRecord(id)

            call.respondText("Meteo record deleted")
        }

        delete("/api/hydro/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respondText(
                    "Invalid ID",
                    status = HttpStatusCode.BadRequest
                )
                return@delete
            }

            DatabaseRepository.deleteHydroRecord(id)

            call.respondText("Hydro record deleted")
        }
    }
    }
}

fun main() {
    ApiServer.start()
}