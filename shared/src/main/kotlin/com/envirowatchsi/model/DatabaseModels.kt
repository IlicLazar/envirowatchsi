package com.envirowatchsi.model

data class DatabaseInfo(
    val name: String = "EnviroWatchSI",
    val description: String = "Database model for environmental digital twin data",
    val supportedTables: List<String> = listOf(
        "air_quality_stations",
        "meteo_stations",
        "hydro_stations"
    )
)