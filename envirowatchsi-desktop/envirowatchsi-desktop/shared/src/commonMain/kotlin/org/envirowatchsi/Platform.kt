package org.envirowatchsi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform