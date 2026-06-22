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
import org.envirowatchsi.ui.components.EnviroPanel
import org.envirowatchsi.ui.components.InlineRecordDetails
import org.envirowatchsi.ui.components.StatusText
import org.envirowatchsi.ui.components.readableValue

@Composable
fun MeteoScreen(canManageData: Boolean = false) {
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
                            message = ""
                        } catch (e: Exception) {
                            records = emptyList()
                            message = "Napaka: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Pridobi podatke")
            }

            if (canManageData) {
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
        }

        Spacer(modifier = Modifier.height(12.dp))
        StatusText(message)
        Spacer(modifier = Modifier.height(16.dp))

        if (records.isNotEmpty()) {
            EnviroPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text("Postaja", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                        Text("Podatek", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                        Text("Akcija", modifier = Modifier.width(120.dp), style = MaterialTheme.typography.titleSmall)
                    }

                    HorizontalDivider()

                    records.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = record.stationName,
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(vertical = 10.dp)
                            )

                            Text(
                                text = "${readableValue(record.temperature)} C | ${readableValue(record.humidity)} %",
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(vertical = 10.dp)
                            )

                            OutlinedButton(
                                onClick = {
                                    selectedRecord = if (selectedRecord == record) null else record
                                },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text(if (selectedRecord == record) "Skrij" else "Izberi")
                            }
                        }

                        if (selectedRecord == record) {
                            InlineRecordDetails(
                                title = record.stationName,
                                values = listOf(
                                    "Zemljepisna širina" to readableValue(record.latitude),
                                    "Zemljepisna dolžina" to readableValue(record.longitude),
                                    "Temperatura" to readableValue(record.temperature),
                                    "Vlažnost" to readableValue(record.humidity),
                                    "Veter" to readableValue(record.windSpeed),
                                    "Padavine" to readableValue(record.precipitation)
                                ),
                                saveButtonText = "Shrani izbrani zapis",
                                onSave = {
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
                                },
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                                showActionButton = canManageData
                            )
                        }

                        HorizontalDivider()
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
