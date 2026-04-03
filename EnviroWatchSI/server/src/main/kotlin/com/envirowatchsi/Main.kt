package com.envirowatchsi

import com.envirowatchsi.network.fetchRawMeteoXml
import com.envirowatchsi.network.fetchRawXml

fun main() {
    val rawData = fetchRawXml()
    println("Raw Hydro XML")
    println(rawData)

    val rawData1 = fetchRawMeteoXml()
    println("Raw Meteo XML")
    println(rawData1)
}