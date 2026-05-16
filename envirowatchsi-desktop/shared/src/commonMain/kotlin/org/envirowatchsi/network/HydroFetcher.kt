package org.envirowatchsi.network

import java.net.URL

fun fetchRawXml(): String {
    return URL(ArsoConfig.HYDRO_URL)
        .openStream()
        .bufferedReader(Charsets.UTF_8)
        .readText()
}