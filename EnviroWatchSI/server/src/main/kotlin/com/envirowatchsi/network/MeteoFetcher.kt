package com.envirowatchsi.network

import java.net.URL

fun fetchRawMeteoXml(): String {
    return URL(ArsoConfig.METEO_URL)
        .openStream()
        .bufferedReader(Charsets.UTF_8)
        .readText()
}