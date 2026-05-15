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
    var recordsText by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Brisanje podatkov",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { selectedTable = "air-quality" }) {
                Text("Air Quality")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedTable = "meteo" }) {
                Text("Meteo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedTable = "hydro" }) {
                Text("Hydro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    recordsText = try {
                        withContext(Dispatchers.IO) {
                            ApiClient.getRaw("/api/$selectedTable")
                        }
                    } catch (e: Exception) {
                        "Napaka: ${e.message}"
                    }
                }
            }
        ) {
            Text("Naloži zapise")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(recordsText)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = selectedId,
            onValueChange = { selectedId = it },
            label = { Text("ID zapisa za brisanje") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val id = selectedId.toIntOrNull()

                if (id == null) {
                    message = "Vnesi veljaven ID."
                } else {
                    showConfirmation = true
                }
            }
        ) {
            Text("Izbriši zapis")
        }

        if (showConfirmation) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ali si prepričan, da želiš izbrisati zapis?",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(
                    onClick = {
                        val id = selectedId.toIntOrNull()

                        if (id == null) {
                            message = "Vnesi veljaven ID."
                            showConfirmation = false
                            return@Button
                        }

                        scope.launch {
                            message = "Brišem zapis..."

                            message = try {
                                withContext(Dispatchers.IO) {
                                    ApiClient.delete("/api/$selectedTable/$id")
                                }

                                recordsText = withContext(Dispatchers.IO) {
                                    ApiClient.getRaw("/api/$selectedTable")
                                }.let { response ->
                                    if (response == "[]") {
                                        "Ni zapisov v izbrani tabeli."
                                    } else {
                                        response
                                    }
                                }

                                selectedId = ""
                                showConfirmation = false

                                "Zapis je uspešno izbrisan."
                            } catch (e: Exception) {
                                showConfirmation = false
                                "Napaka pri brisanju: ${e.message}"
                            }
                        }
                    }
                ) {
                    Text("Da")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        message = "Brisanje preklicano."
                        showConfirmation = false
                    }
                ) {
                    Text("Ne")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(message)
    }
}