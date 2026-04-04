package com.envirowatchsi.network

import java.net.URL

fun fetchRawAirQualityXml(): String {
    val url = URL("https://www.arso.gov.si/xml/zrak/ones_zrak_urni_podatki_zadnji.xml")
    return url.openStream().bufferedReader(Charsets.UTF_8).readText()
}