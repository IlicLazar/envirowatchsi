package com.envirowatchsi

import com.envirowatchsi.network.fetchRawAirQualityXml
import com.envirowatchsi.network.fetchRawMeteoXml
import com.envirowatchsi.network.fetchRawXml
import com.envirowatchsi.parsers.parseAirQualityData

fun main() {
    val rawData = fetchRawXml()
    println("Raw Hydro XML")
    println(rawData)

    val rawData1 = fetchRawMeteoXml()
    println("Raw Meteo XML")
    println(rawData1)

    val rawAirQualityXml = fetchRawAirQualityXml()
    val stations = parseAirQualityData(rawAirQualityXml)

    println("=== PARSIRANI PODATKI - KAKOVOST ZRAKA ===")
    println("Stevilo postaj: ${stations.size}")
    println("-".repeat(80))

    for (station in stations) {
        println("ID: ${station.stationId}")
        println("Ime: ${station.stationName}")
        println("Lokacija: (${station.latitude}, ${station.longitude})")
        println("Cas meritve: ${station.measuredAt}")
        println("PM10: ${station.pm10 ?: "N/A"} mikrogramov po kubnem metru")
        println("PM2.5: ${station.pm2_5 ?: "N/A"} mikrogramov po kubnem metru")
        println("O3: ${station.o3 ?: "N/A"} mikrogramov po kubnem metru")
        println("CO: ${station.co ?: "N/A"} mikrogramov po kubnem metru")
        println("SO2: ${station.so2 ?: "N/A"} mikrogramov po kubnem metru")
        println("Indeks kakovosti: ${station.airQualityIndex ?: "N/A"}")
        println()
    }
}