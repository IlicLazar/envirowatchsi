package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.models.AirQualityStation
import org.envirowatchsi.network.fetchRawAirQualityXml
import org.envirowatchsi.parsers.parseAirQualityData

@Composable
fun AirQualityScreen() {
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf(emptyList<AirQualityStation>()) }
    var selectedRecord by remember { mutableStateOf<AirQualityStation?>(null) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev podatkov o kakovosti zraka.") }
    var isSavingAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Kakovost zraka", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    scope.launch {
                        message = "Pridobivanje in razčlenjevanje podatkov o kakovosti zraka..."

                        try {
                            val parsed = withContext(Dispatchers.IO) {
                                val xml = fetchRawAirQualityXml()
                                parseAirQualityData(xml)
                            }

                            records = parsed
                            selectedRecord = null
                            message = "Pridobljenih zapisov: ${parsed.size}"
                        } catch (e: Exception) {
                            records = emptyList()
                            message = "Napaka: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Pridobi podatke")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                enabled = records.isNotEmpty() && !isSavingAll,
                onClick = {
                    scope.launch {
                        isSavingAll = true
                        message = "Shranjevanje vseh zapisov kakovosti zraka..."

                        message = try {
                            val savedCount = withContext(Dispatchers.IO) {
                                records.forEach { record ->
                                    ApiClient.postJson("/api/air-quality", record.toAirQualityJson())
                                }
                                records.size
                            }

                            "Vsi zapisi kakovosti zraka so shranjeni v bazo. Skupaj: $savedCount"
                        } catch (e: Exception) {
                            "Napaka pri shranjevanju vseh zapisov: ${e.message}"
                        } finally {
                            isSavingAll = false
                        }
                    }
                }
            ) {
                Text(if (isSavingAll) "Shranjujem..." else "Shrani vse v bazo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(message)
        Spacer(modifier = Modifier.height(16.dp))

        if (records.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Razčlenjeni zapisi (${records.size})", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    records.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedRecord = record
                                    message = "Izbran zapis: ${record.stationName}"
                                }
                            ) {
                                Text("Izberi")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${record.stationName} | AQI: ${record.airQualityIndex}",
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        HorizontalDivider()
                    }
                }
            }
        }

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pregled izbranega zapisa", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Postaja: ${record.stationName}")
                    Text("Latitude: ${record.latitude}")
                    Text("Longitude: ${record.longitude}")
                    Text("PM10: ${record.pm10}")
                    Text("PM2.5: ${record.pm2_5}")
                    Text("O3: ${record.o3}")
                    Text("CO: ${record.co}")
                    Text("SO2: ${record.so2}")
                    Text("AQI: ${record.airQualityIndex}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                message = "Shranjevanje izbranega zapisa..."

                                message = try {
                                    withContext(Dispatchers.IO) {
                                        ApiClient.postJson("/api/air-quality", record.toAirQualityJson())
                                    }

                                    "Izbran zapis kakovosti zraka je shranjen v bazo."
                                } catch (e: Exception) {
                                    "Napaka pri shranjevanju: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text("Shrani izbrani zapis")
                    }
                }
            }
        }
    }
}

private fun AirQualityStation.toAirQualityJson(): String {
    return buildAirQualityJson(
        stationName,
        latitude.toString(),
        longitude.toString(),
        airQualityIndex?.toInt()?.toString() ?: "0",
        pm10 = pm10?.toString(),
        pm2_5 = pm2_5?.toString(),
        o3 = o3?.toString(),
        co = co?.toString(),
        so2 = so2?.toString()
    )
}
