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
import org.envirowatchsi.models.MeteoStation
import org.envirowatchsi.network.fetchRawMeteoXml
import org.envirowatchsi.parsers.parseMeteoData

@Composable
fun MeteoScreen() {
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf(emptyList<MeteoStation>()) }
    var selectedRecord by remember { mutableStateOf<MeteoStation?>(null) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev meteo podatkov.") }
    var isSavingAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Meteorološki podatki", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    scope.launch {
                        message = "Pridobivanje in razčlenjevanje meteo podatkov..."

                        try {
                            val parsed = withContext(Dispatchers.IO) {
                                val xml = fetchRawMeteoXml()
                                parseMeteoData(xml)
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
                        message = "Shranjevanje vseh meteo zapisov..."

                        message = try {
                            val savedCount = withContext(Dispatchers.IO) {
                                records.forEach { record ->
                                    ApiClient.postJson("/api/meteo", record.toMeteoJson())
                                }
                                records.size
                            }

                            "Vsi meteo zapisi so shranjeni v bazo. Skupaj: $savedCount"
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
                                text = "${record.stationName} | ${record.temperature} C | ${record.humidity} %",
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
                    Text("Temperatura: ${record.temperature}")
                    Text("Vlažnost: ${record.humidity}")
                    Text("Veter: ${record.windSpeed}")
                    Text("Padavine: ${record.precipitation}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                message = "Shranjevanje izbranega zapisa..."

                                message = try {
                                    withContext(Dispatchers.IO) {
                                        ApiClient.postJson("/api/meteo", record.toMeteoJson())
                                    }

                                    "Izbran meteo zapis je shranjen v bazo."
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

private fun MeteoStation.toMeteoJson(): String {
    return buildMeteoJson(
        stationName,
        latitude?.toString() ?: "",
        longitude?.toString() ?: "",
        temperature.toString(),
        humidity.toString(),
        windSpeed?.toString() ?: "",
        precipitation?.toString() ?: ""
    )
}
