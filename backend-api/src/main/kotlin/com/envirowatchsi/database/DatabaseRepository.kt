package com.envirowatchsi.database

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

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

    fun insertAirQualityRecord(
        station: String,
        latitudeValue: Double?,
        longitudeValue: Double?,
        aqi: Int
    ) {
        transaction {
            AirQualityStationsTable.insert {
                it[stationId] = station.lowercase().replace(" ", "_")
                it[stationName] = station
                it[latitude] = latitudeValue ?: 0.0
                it[longitude] = longitudeValue ?: 0.0
                it[measuredAt] = java.time.LocalDateTime.now().toString()

                it[pm10] = null
                it[pm2_5] = null
                it[o3] = null
                it[co] = null
                it[so2] = null
                it[airQualityIndex] = aqi.toDouble()
            }
        }
    }

    fun insertMeteoRecord(
        stationName: String,
        latitude: Double?,
        longitude: Double?,
        temperature: Double,
        humidity: Double,
        windSpeed: Double?,
        precipitation: Double?
    ) {
        transaction {
            MeteoStationsTable.insert {
                it[stationId] = stationName.lowercase().replace(" ", "_")
                it[MeteoStationsTable.stationName] = stationName
                it[MeteoStationsTable.latitude] = latitude
                it[MeteoStationsTable.longitude] = longitude
                it[measuredAt] = java.time.LocalDateTime.now().toString()
                it[MeteoStationsTable.temperature] = temperature
                it[MeteoStationsTable.humidity] = humidity
                it[MeteoStationsTable.windSpeed] = windSpeed
                it[windDirection] = null
                it[MeteoStationsTable.precipitation] = precipitation
            }
        }
    }

    fun insertHydroRecord(
        stationName: String,
        riverName: String,
        latitude: Double?,
        longitude: Double?,
        waterLevel: Double?,
        waterFlow: Double?
    ) {
        transaction {
            HydroStationsTable.insert {
                it[stationId] = stationName.lowercase().replace(" ", "_")
                it[HydroStationsTable.stationName] = stationName
                it[HydroStationsTable.latitude] = latitude
                it[HydroStationsTable.longitude] = longitude
                it[HydroStationsTable.riverName] = riverName
                it[measuredAt] = java.time.LocalDateTime.now().toString()
                it[HydroStationsTable.waterLevel] = waterLevel
                it[HydroStationsTable.waterFlow] = waterFlow
            }
        }
    }

    fun updateAirQualityRecord(
        id: Int,
        station: String,
        latitudeValue: Double,
        longitudeValue: Double,
        pm10Value: Double?,
        pm25Value: Double?,
        o3Value: Double?,
        coValue: Double?,
        so2Value: Double?,
        aqi: Double?
    ) {
        transaction {
            AirQualityStationsTable.update({ AirQualityStationsTable.id eq id }) {
                it[stationName] = station
                it[latitude] = latitudeValue
                it[longitude] = longitudeValue
                it[measuredAt] = java.time.LocalDateTime.now().toString()
                it[pm10] = pm10Value
                it[pm2_5] = pm25Value
                it[o3] = o3Value
                it[co] = coValue
                it[so2] = so2Value
                it[airQualityIndex] = aqi
            }
        }
    }

    fun updateMeteoRecord(
        id: Int,
        station: String,
        latitudeValue: Double?,
        longitudeValue: Double?,
        temperatureValue: Double,
        humidityValue: Double,
        windSpeedValue: Double?,
        windDirectionValue: String?,
        precipitationValue: Double?
    ) {
        transaction {
            MeteoStationsTable.update({ MeteoStationsTable.id eq id }) {
                it[stationName] = station
                it[latitude] = latitudeValue
                it[longitude] = longitudeValue
                it[measuredAt] = java.time.LocalDateTime.now().toString()
                it[temperature] = temperatureValue
                it[humidity] = humidityValue
                it[windSpeed] = windSpeedValue
                it[windDirection] = windDirectionValue
                it[precipitation] = precipitationValue
            }
        }
    }

    fun updateHydroRecord(
        id: Int,
        station: String,
        river: String,
        latitudeValue: Double?,
        longitudeValue: Double?,
        level: Double?,
        flow: Double?
    ) {
        transaction {
            HydroStationsTable.update({ HydroStationsTable.id eq id }) {
                it[stationName] = station
                it[riverName] = river
                it[latitude] = latitudeValue
                it[longitude] = longitudeValue
                it[measuredAt] = java.time.LocalDateTime.now().toString()
                it[waterLevel] = level
                it[waterFlow] = flow
            }
        }
    }

    fun deleteAirQualityRecord(id: Int) {
        transaction {
            AirQualityStationsTable.deleteWhere {
                AirQualityStationsTable.id eq id
            }
        }
    }

    fun deleteMeteoRecord(id: Int) {
        transaction {
            MeteoStationsTable.deleteWhere {
                MeteoStationsTable.id eq id
            }
        }
    }

    fun deleteHydroRecord(id: Int) {
        transaction {
            HydroStationsTable.deleteWhere {
                HydroStationsTable.id eq id
            }
        }
    }

    fun registerUser(
        username: String,
        password: String
    ) {
        transaction {
            UsersTable.insert {
                it[UsersTable.username] = username
                it[UsersTable.password] = password
            }
        }
    }

    fun userExists(username: String): Boolean {
        return transaction {
            UsersTable.selectAll()
                .where { UsersTable.username eq username }
                .count() > 0
        }
    }

    fun validateUser(
        username: String,
        password: String
    ): Boolean {
        return transaction {
            UsersTable.selectAll()
                .where {
                    (UsersTable.username eq username) and
                            (UsersTable.password eq password)
                }
                .count() > 0
        }
    }
}