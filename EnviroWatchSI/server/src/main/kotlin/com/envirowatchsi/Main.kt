package com.envirowatchsi

import com.envirowatchsi.network.fetchRawXml

fun main() {
    val rawData = fetchRawXml()

    println("Raw XML")
    println(rawData)
}