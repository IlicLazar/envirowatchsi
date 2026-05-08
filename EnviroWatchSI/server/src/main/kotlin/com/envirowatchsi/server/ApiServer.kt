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
            val parameters = call.receiveParameters()

            val stationName = parameters["stationName"]
            val aqi = parameters["aqi"]?.toIntOrNull()

            if (stationName.isNullOrBlank() || aqi == null) {
                call.respondText(
                    "Invalid input",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            DatabaseRepository.insertAirQualityRecord(stationName, aqi)

            call.respondText(
                "Air quality record saved",
                status = HttpStatusCode.Created
            )
        }

        post("/api/meteo") {
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val latitude = p["latitude"]?.toDoubleOrNull()
            val longitude = p["longitude"]?.toDoubleOrNull()
            val temperature = p["temperature"]?.toDoubleOrNull()
            val humidity = p["humidity"]?.toDoubleOrNull()
            val windSpeed = p["windSpeed"]?.toDoubleOrNull()
            val precipitation = p["precipitation"]?.toDoubleOrNull()

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
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val riverName = p["riverName"]
            val latitude = p["latitude"]?.toDoubleOrNull()
            val longitude = p["longitude"]?.toDoubleOrNull()
            val waterLevel = p["waterLevel"]?.toDoubleOrNull()
            val waterFlow = p["waterFlow"]?.toDoubleOrNull()

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
            val aqi = p["aqi"]?.toIntOrNull()

            if (id == null || stationName.isNullOrBlank() || aqi == null) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateAirQualityRecord(id, stationName, aqi)
            call.respondText("Air quality record updated")
        }

        put("/api/meteo/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val temperature = p["temperature"]?.toDoubleOrNull()
            val humidity = p["humidity"]?.toDoubleOrNull()

            if (id == null || stationName.isNullOrBlank() || temperature == null || humidity == null) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateMeteoRecord(id, stationName, temperature, humidity)
            call.respondText("Meteo record updated")
        }

        put("/api/hydro/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val p = call.receiveParameters()

            val stationName = p["stationName"]
            val riverName = p["riverName"]
            val waterLevel = p["waterLevel"]?.toDoubleOrNull()
            val waterFlow = p["waterFlow"]?.toDoubleOrNull()

            if (id == null || stationName.isNullOrBlank() || riverName.isNullOrBlank()) {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
                return@put
            }

            DatabaseRepository.updateHydroRecord(id, stationName, riverName, waterLevel, waterFlow)
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