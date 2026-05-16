package com.envirowatchsi.model

data class MeteoStation(
    val stationId: String,
    val stationName: String,
    val latitude: Double?,
    val longitude: Double?,
    val measuredAt: String,
    val temperature: Double,
    val humidity: Double,
    val windSpeed: Double?,
    val windDirection: String?,
    val precipitation: Double?
)