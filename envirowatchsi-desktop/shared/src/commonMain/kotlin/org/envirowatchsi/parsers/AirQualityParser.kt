package org.envirowatchsi.parsers

import org.envirowatchsi.models.AirQualityStation
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

fun parseAirQualityData(xmlText: String): List<AirQualityStation> {
    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = db.parse(ByteArrayInputStream(xmlText.toByteArray(Charsets.UTF_8)))
    doc.documentElement.normalize()

    val airQualityStations = mutableListOf<AirQualityStation>()
    val nodeList = doc.getElementsByTagName("postaja")

    for (i in 0 until nodeList.length) {
        val el = nodeList.item(i) as Element

        fun textOrNull(tagName: String): Double? {
            val value = el.getElementsByTagName(tagName).item(0)?.textContent?.trim()
            if (value.isNullOrEmpty()) return null
            val numericValue = value.toDoubleOrNull() ?: return null
            return if (numericValue < 1.0) null else numericValue
        }

        val stationId = el.getAttribute("sifra")
        val stationName = el.getElementsByTagName("merilno_mesto").item(0)?.textContent ?: ""
        val lat = el.getAttribute("wgs84_sirina").toDoubleOrNull() ?: 0.0
        val lon = el.getAttribute("wgs84_dolzina").toDoubleOrNull() ?: 0.0
        val measuredAt = el.getElementsByTagName("datum_od").item(0)?.textContent ?: ""

        val pm10 = textOrNull("pm10")
        val pm2_5 = textOrNull("pm2.5")
        val o3 = textOrNull("o3")
        val co = textOrNull("co")
        val so2 = textOrNull("so2")

        val aqi = listOfNotNull(pm10, pm2_5, o3, co, so2).maxOrNull()

        airQualityStations.add(AirQualityStation(
            stationId = stationId,
            stationName = stationName,
            latitude = lat,
            longitude = lon,
            measuredAt = measuredAt,
            pm10 = pm10,
            pm2_5 = pm2_5,
            o3 = o3,
            co = co,
            so2 = so2,
            airQualityIndex = aqi
        ))
    }
    return airQualityStations
}