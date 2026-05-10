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

object ApiServer {

    fun start() {
        com.envirowatchsi.database.DatabaseFactory.init()
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = false)
    }
}

fun Application.module() {
    val gson = Gson()
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