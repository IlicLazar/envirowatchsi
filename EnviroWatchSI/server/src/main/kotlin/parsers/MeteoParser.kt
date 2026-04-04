package com.envirowatchsi.parser

import com.envirowatchsi.model.MeteoStation
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

fun parseMeteoData(xmlText: String): List<MeteoStation> {
    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = db.parse(xmlText.byteInputStream(Charsets.UTF_8))
    doc.documentElement.normalize()

    val meritve = mutableListOf<MeteoStation>()
    val nodeList = doc.getElementsByTagName("metData")

    for (i in 0 until nodeList.length) {
        val el = nodeList.item(i) as Element

        fun tag(name: String) =
            el.getElementsByTagName(name).item(0)?.textContent?.trim() ?: ""

        val stationId   = tag("domain_meteosiId")
        val stationName = tag("domain_shortTitle")
        val latitude    = tag("domain_lat").toDoubleOrNull()
        val longitude   = tag("domain_lon").toDoubleOrNull()
        val measuredAt  = tag("tsValid_issued")

        val temperature   = tag("t").toDoubleOrNull()  ?: continue
        val humidity      = tag("rh").toDoubleOrNull() ?: continue
        val windSpeed     = tag("ff_val").toDoubleOrNull()
        val windDirection = tag("dd_shortText").takeIf { it.isNotEmpty() }
        val precipitation = tag("tp_acc").toDoubleOrNull()

        meritve.add(
            MeteoStation(
                stationId     = stationId,
                stationName   = stationName,
                latitude      = latitude,
                longitude     = longitude,
                measuredAt    = measuredAt,
                temperature   = temperature,
                humidity      = humidity,
                windSpeed     = windSpeed,
                windDirection = windDirection,
                precipitation = precipitation
            )
        )
    }

    return meritve
}