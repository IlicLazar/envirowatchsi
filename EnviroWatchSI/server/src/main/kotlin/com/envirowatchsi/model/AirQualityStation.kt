package com.envirowatchsi.model

class AirQualityStation(
    val stationId: String,
    val stationName: String,
    val latitude: Double,
    val longitude: Double,
    val measuredAt: String,
    val pm10: Double?,
    val pm2_5: Double?,
    val o3: Double?,
    val co: Double?,
    val so2: Double?,
    val airQualityIndex: Double?
)