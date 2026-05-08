package com.envirowatchsi.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object ApiServer {

    fun start() {
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
    }
}