package com.envirowatchsi

import com.envirowatchsi.network.fetchRawAirQualityXml
import com.envirowatchsi.network.fetchRawXml

fun main() {
    val rawData = fetchRawXml()

    println("Raw XML")
    println(rawData)

    println("Air quality (RAW)")
    val rawDataAirQuality = fetchRawAirQualityXml()
    println(rawDataAirQuality)
}