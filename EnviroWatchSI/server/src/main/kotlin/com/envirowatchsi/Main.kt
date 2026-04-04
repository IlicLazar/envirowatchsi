package com.envirowatchsi

import com.envirowatchsi.network.fetchRawAirQualityXml
import com.envirowatchsi.network.fetchRawMeteoXml
import com.envirowatchsi.network.fetchRawXml
import com.envirowatchsi.parser.parseMeteoData
import com.envirowatchsi.parser.parseAirQualityData
import com.envirowatchsi.parser.parseHydroData


fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

    val rawHydroData = fetchRawXml()
    val hydroStations = parseHydroData(rawHydroData)
    println("\nParsed Hydro Data")
    for (s in hydroStations) {
        println("Station:     ${s.stationName} (${s.stationId})")
        println("Location:    lat=${s.latitude}, lon=${s.longitude}")
        println("River:       ${s.riverName}")
        println("Time:        ${s.measuredAt}")
        println("Water level: ${s.waterLevel ?: "N/A"} cm")
        println("Water flow:  ${s.waterFlow ?: "N/A"} m3/s")
        println()
    }

    val rawMeteoData = fetchRawMeteoXml()
    val meteoStations = parseMeteoData(rawMeteoData)
    println("\nParsed Meteo Data")
    for (s in meteoStations) {
        println("Station:        ${s.stationName} (${s.stationId})")
        println("Location:       lat=${s.latitude}, lon=${s.longitude}")
        println("Time:           ${s.measuredAt}")
        println("Temperature:    ${s.temperature} C")
        println("Humidity:       ${s.humidity} %")
        println("Wind speed:     ${s.windSpeed ?: "N/A"} m/s")
        println("Wind direction: ${s.windDirection ?: "N/A"}")
        println("Precipitation:  ${s.precipitation ?: "N/A"} mm")
        println()
    }

    val rawAirQualityXml = fetchRawAirQualityXml()
    val airStations = parseAirQualityData(rawAirQualityXml)
    println("\nParsed Air Quality Data")
    for (s in airStations) {
        println("Station:       ${s.stationName} (${s.stationId})")
        println("Location:      lat=${s.latitude}, lon=${s.longitude}")
        println("Time:          ${s.measuredAt}")
        println("PM10:          ${s.pm10 ?: "N/A"} ug/m3")
        println("PM2.5:         ${s.pm2_5 ?: "N/A"} ug/m3")
        println("O3:            ${s.o3 ?: "N/A"} ug/m3")
        println("CO:            ${s.co ?: "N/A"} ug/m3")
        println("SO2:           ${s.so2 ?: "N/A"} ug/m3")
        println("Air quality index: ${s.airQualityIndex ?: "N/A"}")
        println()
    }
}