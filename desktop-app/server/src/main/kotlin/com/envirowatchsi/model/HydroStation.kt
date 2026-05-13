package com.envirowatchsi.model

data class HydroStation(
    val stationId: String,
    val stationName: String,
    val latitude: Double?,
    val longitude: Double?,
    val riverName: String,
    val measuredAt: String,
    val waterLevel: Double?,
    val waterFlow: Double?
)