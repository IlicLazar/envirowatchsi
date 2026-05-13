package com.envirowatchsi.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:sqlite:envirowatchsi.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(
                AirQualityStationsTable,
                MeteoStationsTable,
                HydroStationsTable
            )
        }
    }
}