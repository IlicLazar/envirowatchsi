package com.envirowatchsi.model

data class HydroStation(
    val stationId: String,
    val stationName: String,
    val riverName: String,
    val measuredAt: String,
    val waterLevel: Double?,
    val waterFlow: Double?
)