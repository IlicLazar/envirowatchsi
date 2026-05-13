package com.envirowatchsi.database

import org.jetbrains.exposed.sql.Table

object AirQualityStationsTable : Table("air_quality_stations") {
    val id = integer("id").autoIncrement()
    val stationId = varchar("station_id", 50)
    val stationName = varchar("station_name", 255)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val measuredAt = varchar("measured_at", 100)

    val pm10 = double("pm10").nullable()
    val pm2_5 = double("pm2_5").nullable()
    val o3 = double("o3").nullable()
    val co = double("co").nullable()
    val so2 = double("so2").nullable()
    val airQualityIndex = double("air_quality_index").nullable()
    init {
        index(false, stationId)
        index(false, measuredAt)
        index(false, latitude, longitude)
    }
    override val primaryKey = PrimaryKey(id)
}

object MeteoStationsTable : Table("meteo_stations") {
    val id = integer("id").autoIncrement()
    val stationId = varchar("station_id", 50)
    val stationName = varchar("station_name", 255)
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val measuredAt = varchar("measured_at", 100)

    val temperature = double("temperature")
    val humidity = double("humidity")
    val windSpeed = double("wind_speed").nullable()
    val windDirection = varchar("wind_direction", 50).nullable()
    val precipitation = double("precipitation").nullable()
    init {
        index(false, stationId)
        index(false, measuredAt)
        index(false, latitude, longitude)
    }
    override val primaryKey = PrimaryKey(id)
}

object HydroStationsTable : Table("hydro_stations") {
    val id = integer("id").autoIncrement()
    val stationId = varchar("station_id", 50)
    val stationName = varchar("station_name", 255)
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val riverName = varchar("river_name", 255)
    val measuredAt = varchar("measured_at", 100)

    val waterLevel = double("water_level").nullable()
    val waterFlow = double("water_flow").nullable()
    init {
        index(false, stationId)
        index(false, riverName)
        index(false, measuredAt)
        index(false, latitude, longitude)
    }
    override val primaryKey = PrimaryKey(id)
}