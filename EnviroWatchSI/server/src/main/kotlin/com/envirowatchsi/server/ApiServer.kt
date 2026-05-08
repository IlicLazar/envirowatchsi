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
    }
}