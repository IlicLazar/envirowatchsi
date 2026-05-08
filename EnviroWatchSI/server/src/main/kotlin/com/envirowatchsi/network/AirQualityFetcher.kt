package com.envirowatchsi.network

import java.net.URL

fun fetchRawAirQualityXml(): String {
    return URL(ArsoConfig.AIR_QUALITY_URL)
        .openStream()
        .bufferedReader(Charsets.UTF_8)
        .readText()
}