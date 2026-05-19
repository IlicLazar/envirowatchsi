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

@Composable
fun DeleteDataScreen() {
    val scope = rememberCoroutineScope()
    var selectedTable by remember { mutableStateOf("air-quality") }
    var records by remember { mutableStateOf(emptyList<DatabaseRecordItem>()) }
    var selectedRecord by remember { mutableStateOf<DatabaseRecordItem?>(null) }
    var message by remember { mutableStateOf("Najprej naloži zapise za izbrano tabelo.") }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

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
                    parseDatabaseRecords(ApiClient.getRaw("/api/$selectedTable"), selectedTable)
                }

                records = loaded
                resetSelection()
                message = if (loaded.isEmpty()) {
                    "V izbrani tabeli ni zapisov."
                } else {
                    "Naloženih zapisov: ${loaded.size}"
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

        TableSelector(
            selectedTable = selectedTable,
            onSelected = {
                selectedTable = it
                records = emptyList()
                resetSelection()
                message = "Najprej naloži zapise za izbrano tabelo."
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = !isLoading,
            onClick = { loadRecords() }
        ) {
            Text(if (isLoading) "Nalagam..." else "Naloži zapise")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(message)
        Spacer(modifier = Modifier.height(16.dp))

        RecordsTable(
            records = records,
            selectedId = selectedRecord?.id,
            actionText = "Izberi",
            onRecordSelected = {
                selectedRecord = it
                showConfirmation = false
                message = "Izbran zapis: ${it.stationName}"
            }
        )

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Izbrani zapis", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Postaja: ${record.stationName}")
                    Text(record.detail)
                    Text("ID: ${record.id}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showConfirmation = true }
                    ) {
                        Text("Izbriši izbrani zapis")
                    }
                }
            }
        }

        if (showConfirmation && selectedRecord != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ali si prepričan, da želiš izbrisati izbrani zapis?",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Button(
                            onClick = {
                                val record = selectedRecord ?: return@Button

                                scope.launch {
                                    message = "Brišem zapis..."

                                    message = try {
                                        withContext(Dispatchers.IO) {
                                            ApiClient.delete("/api/$selectedTable/${record.id}")
                                        }

                                        val loaded = withContext(Dispatchers.IO) {
                                            parseDatabaseRecords(ApiClient.getRaw("/api/$selectedTable"), selectedTable)
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
                                message = "Brisanje preklicano."
                                showConfirmation = false
                            }
                        ) {
                            Text("Prekliči")
                        }
                    }
                }
            }
        }
    }
}
