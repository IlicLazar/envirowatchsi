package com.envirowatchsi.network

import java.net.URL

fun fetchRawXml(): String {
    val url = URL("https://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml")
    return url.openStream().bufferedReader(Charsets.UTF_8).readText()
}