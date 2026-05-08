package com.envirowatchsi.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

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
    }
}