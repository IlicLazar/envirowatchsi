package com.envirowatchsi.database

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseRepository {

    fun getAirQualityRecords(): List<Map<String, Any?>> = transaction {
        AirQualityStationsTable.selectAll().map {
            mapOf(
                "id" to it[AirQualityStationsTable.id],
                "stationId" to it[AirQualityStationsTable.stationId],
                "stationName" to it[AirQualityStationsTable.stationName],
                "latitude" to it[AirQualityStationsTable.latitude],
                "longitude" to it[AirQualityStationsTable.longitude],
                "measuredAt" to it[AirQualityStationsTable.measuredAt],
                "pm10" to it[AirQualityStationsTable.pm10],
                "pm2_5" to it[AirQualityStationsTable.pm2_5],
                "o3" to it[AirQualityStationsTable.o3],
                "co" to it[AirQualityStationsTable.co],
                "so2" to it[AirQualityStationsTable.so2],
                "airQualityIndex" to it[AirQualityStationsTable.airQualityIndex]
            )
        }
    }

    fun getMeteoRecords(): List<Map<String, Any?>> = transaction {
        MeteoStationsTable.selectAll().map {
            mapOf(
                "id" to it[MeteoStationsTable.id],
                "stationId" to it[MeteoStationsTable.stationId],
                "stationName" to it[MeteoStationsTable.stationName],
                "latitude" to it[MeteoStationsTable.latitude],
                "longitude" to it[MeteoStationsTable.longitude],
                "measuredAt" to it[MeteoStationsTable.measuredAt],
                "temperature" to it[MeteoStationsTable.temperature],
                "humidity" to it[MeteoStationsTable.humidity],
                "windSpeed" to it[MeteoStationsTable.windSpeed],
                "windDirection" to it[MeteoStationsTable.windDirection],
                "precipitation" to it[MeteoStationsTable.precipitation]
            )
        }
    }

    fun getHydroRecords(): List<Map<String, Any?>> = transaction {
        HydroStationsTable.selectAll().map {
            mapOf(
                "id" to it[HydroStationsTable.id],
                "stationId" to it[HydroStationsTable.stationId],
                "stationName" to it[HydroStationsTable.stationName],
                "latitude" to it[HydroStationsTable.latitude],
                "longitude" to it[HydroStationsTable.longitude],
                "riverName" to it[HydroStationsTable.riverName],
                "measuredAt" to it[HydroStationsTable.measuredAt],
                "waterLevel" to it[HydroStationsTable.waterLevel],
                "waterFlow" to it[HydroStationsTable.waterFlow]
            )
        }
    }
}