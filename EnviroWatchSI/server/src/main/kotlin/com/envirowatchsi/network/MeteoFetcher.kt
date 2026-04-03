package com.envirowatchsi.network

import java.net.URL

fun fetchRawMeteoXml(): String {
    val url = URL("https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observation_si_latest.xml")
    return url.openStream().bufferedReader(Charsets.UTF_8).readText()
}