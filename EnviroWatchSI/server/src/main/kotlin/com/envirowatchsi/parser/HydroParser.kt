package com.envirowatchsi.parser

import com.envirowatchsi.model.HydroStation
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

fun parseHydroData(xmlText: String): List<HydroStation> {
    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = db.parse(xmlText.byteInputStream(Charsets.UTF_8))
    doc.documentElement.normalize()

    val stations = mutableListOf<HydroStation>()
    val nodeList = doc.getElementsByTagName("postaja")

    for (i in 0 until nodeList.length) {
        val el = nodeList.item(i) as Element
        fun tag(name: String) =
            el.getElementsByTagName(name).item(0)?.textContent?.trim() ?: ""

        stations.add(
            HydroStation(
                stationId   = el.getAttribute("sifra"),
                stationName = tag("merilno_mesto"),
                latitude    = el.getAttribute("wgs84_sirina").toDoubleOrNull(),
                longitude   = el.getAttribute("wgs84_dolzina").toDoubleOrNull(),
                measuredAt  = "${tag("datum")} ${tag("ura")}",
                riverName   = tag("reka"),
                waterLevel  = tag("vodostaj").toDoubleOrNull(),
                waterFlow   = tag("pretok").toDoubleOrNull()
            )
        )
    }
    return stations
}