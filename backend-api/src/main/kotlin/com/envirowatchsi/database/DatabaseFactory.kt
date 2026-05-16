package com.envirowatchsi.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5433/envirowatchsi",
            driver = "org.postgresql.Driver",
            user = "envirowatch",
            password = "envirowatch123"
        )

        transaction {
            SchemaUtils.create(
                AirQualityStationsTable,
                MeteoStationsTable,
                HydroStationsTable,
                UsersTable,
            )
        }
    }
}