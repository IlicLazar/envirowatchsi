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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Meteorološki podatki",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            Text("Pridobi meteo podatke")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(message)

        Spacer(modifier = Modifier.height(16.dp))

        records.forEach { record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Button(
                    onClick = {
                        selectedRecord = record
                        message = "Izbran zapis: ${record.stationName}"
                    }
                ) {
                    Text("Izberi")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${record.stationName} | ${record.temperature} °C | ${record.humidity} %",
                    modifier = Modifier.padding(8.dp)
                )
            }

            Divider()
        }

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pregled izbranega zapisa",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Podatki so pripravljeni za shranjevanje v bazo.",
                style = MaterialTheme.typography.titleSmall
            )

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
                            val jsonBody = buildMeteoJson(
                                record.stationName,
                                record.latitude?.toString() ?: "",
                                record.longitude?.toString() ?: "",
                                record.temperature.toString(),
                                record.humidity.toString(),
                                record.windSpeed?.toString() ?: "",
                                record.precipitation?.toString() ?: ""
                            )

                            withContext(Dispatchers.IO) {
                                ApiClient.postJson("/api/meteo", jsonBody)
                            }

                            "Izbran meteo zapis je shranjen v bazo."
                        } catch (e: Exception) {
                            "Napaka pri shranjevanju: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Shrani v bazo")
            }
        }
    }
}