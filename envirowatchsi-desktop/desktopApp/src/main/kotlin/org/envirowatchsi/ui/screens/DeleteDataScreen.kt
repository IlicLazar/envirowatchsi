package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.ui.components.EnviroPanel
import org.envirowatchsi.ui.components.InlineRecordDetails
import org.envirowatchsi.ui.components.StatusText

@Composable
fun DeleteDataScreen() {
    val scope = rememberCoroutineScope()
    var selectedTable by remember { mutableStateOf("air-quality") }
    var records by remember { mutableStateOf(emptyList<DatabaseRecordItem>()) }
    var selectedRecord by remember { mutableStateOf<DatabaseRecordItem?>(null) }
    var message by remember { mutableStateOf("Najprej naloži zapise za izbrano tabelo.") }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(DatabasePeriod.LAST_7_DAYS) }

    fun resetSelection() {
        selectedRecord = null
        showConfirmation = false
    }

    fun loadRecords() {
        scope.launch {
            isLoading = true
            message = "Nalagam zapise..."

            try {
                val loaded = withContext(Dispatchers.IO) {
                    parseDatabaseRecords(fetchDatabaseRecords(selectedTable, selectedPeriod), selectedTable)
                }

                records = loaded
                resetSelection()
                message = if (loaded.isEmpty()) {
                    "V izbrani tabeli ni zapisov."
                } else {
                    ""
                }
            } catch (e: Exception) {
                records = emptyList()
                resetSelection()
                message = "Napaka pri nalaganju podatkov: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Brisanje podatkov", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        EnviroPanel(modifier = Modifier.fillMaxWidth(), title = "Izbor zapisov") {
            Text("Tip podatkov", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            TableSelector(
                selectedTable = selectedTable,
                onSelected = {
                    selectedTable = it
                    records = emptyList()
                    resetSelection()
                    message = "Najprej naloži zapise za izbrano tabelo."
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Obdobje", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                DatabasePeriod.entries.forEachIndexed { index, period ->
                    PeriodButton(
                        period = period,
                        selectedPeriod = selectedPeriod,
                        onSelected = {
                            selectedPeriod = it
                            records = emptyList()
                            resetSelection()
                            message = "Najprej naloži zapise za izbrano obdobje."
                        }
                    )

                    if (index < DatabasePeriod.entries.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = !isLoading,
                onClick = { loadRecords() }
            ) {
                Text(if (isLoading) "Nalagam..." else "Naloži zapise")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        StatusText(message)
        Spacer(modifier = Modifier.height(16.dp))

        if (records.isNotEmpty()) {
            EnviroPanel(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Postaja", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                    Text("Podatki", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.width(120.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                records.forEach { record ->
                    val isSelected = selectedRecord?.id == record.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.stationName,
                            modifier = Modifier
                                .weight(2f)
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = record.detail,
                            modifier = Modifier
                                .weight(2f)
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        OutlinedButton(
                            onClick = {
                                selectedRecord = if (isSelected) null else record
                                showConfirmation = false
                            },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(if (isSelected) "Skrij" else "Izberi")
                        }
                    }

                    if (isSelected) {
                        InlineRecordDetails(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 10.dp),
                            title = record.stationName,
                            values = record.deleteDetailValues(selectedTable),
                            saveButtonText = "Izbriši izbrani zapis",
                            onSave = { showConfirmation = true }
                        )
                    }

                    if (isSelected && showConfirmation) {
                        EnviroPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "Ali si prepričan, da želiš izbrisati izbrani zapis?",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        scope.launch {
                                            message = "Brišem zapis..."

                                            message = try {
                                                withContext(Dispatchers.IO) {
                                                    ApiClient.delete("/api/$selectedTable/${record.id}")
                                                }

                                                val loaded = withContext(Dispatchers.IO) {
                                                    parseDatabaseRecords(fetchDatabaseRecords(selectedTable, selectedPeriod), selectedTable)
                                                }

                                                records = loaded
                                                resetSelection()

                                                "Zapis je uspešno izbrisan."
                                            } catch (e: Exception) {
                                                showConfirmation = false
                                                "Napaka pri brisanju: ${e.message}"
                                            }
                                        }
                                    }
                                ) {
                                    Text("Da, izbriši")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        showConfirmation = false
                                    }
                                ) {
                                    Text("Prekliči")
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}

private fun DatabaseRecordItem.deleteDetailValues(table: String): List<Pair<String, String>> {
    return when (table) {
        "air-quality" -> listOf(
            "Zemljepisna širina" to raw.displayValue("latitude"),
            "Zemljepisna dolžina" to raw.displayValue("longitude"),
            "PM10" to raw.displayValue("pm10"),
            "PM2.5" to raw.displayValue("pm2_5"),
            "O3" to raw.displayValue("o3"),
            "CO" to raw.displayValue("co"),
            "SO2" to raw.displayValue("so2"),
            "AQI" to raw.displayValue("airQualityIndex")
        )
        "meteo" -> listOf(
            "Zemljepisna širina" to raw.displayValue("latitude"),
            "Zemljepisna dolžina" to raw.displayValue("longitude"),
            "Temperatura" to raw.displayValue("temperature"),
            "Vlažnost" to raw.displayValue("humidity"),
            "Hitrost vetra" to raw.displayValue("windSpeed"),
            "Padavine" to raw.displayValue("precipitation")
        )
        "hydro" -> listOf(
            "Zemljepisna širina" to raw.displayValue("latitude"),
            "Zemljepisna dolžina" to raw.displayValue("longitude"),
            "Reka" to raw.displayValue("riverName"),
            "Vodostaj" to raw.displayValue("waterLevel"),
            "Pretok" to raw.displayValue("waterFlow")
        )
        else -> emptyList()
    }
}

private fun com.google.gson.JsonObject.displayValue(name: String): String {
    return stringValue(name).ifBlank { "-" }
}
