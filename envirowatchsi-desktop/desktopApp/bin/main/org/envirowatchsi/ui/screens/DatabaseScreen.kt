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
import org.envirowatchsi.ui.components.DataTable
import org.envirowatchsi.ui.components.SortDropdown

@Composable
fun DatabaseScreen() {
    val scope = rememberCoroutineScope()

    var selectedTable by remember { mutableStateOf("air-quality") }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev podatkov.") }

    var headers by remember { mutableStateOf(listOf("ID", "Postaja", "Vrednost")) }
    var rows by remember { mutableStateOf(emptyList<List<String>>()) }

    var stationFilter by remember { mutableStateOf("")}

    var minValueFilter by remember { mutableStateOf("")}
    var maxValueFilter by remember { mutableStateOf("")}

    var sortMode by remember { mutableStateOf("station") }
    var sortAscending by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Podatkovna baza",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                selectedTable = "air-quality"
                stationFilter = ""
                rows = emptyList()
            }) {
                Text("Kakovost zraka")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                selectedTable = "meteo"
                stationFilter = ""
                rows = emptyList()
            }) {
                Text("Meteo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                selectedTable = "hydro"
                stationFilter = ""
                rows = emptyList()
            }) {
                Text("Hidro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = "Pridobivanje podatkov..."

                    try {
                        val response = fetchDatabaseRecords(selectedTable)

                        val parsedRows = parseRowsForTable(selectedTable, response)

                        headers = headersForTable(selectedTable)
                        rows = parsedRows

                        message = if (parsedRows.isEmpty()) {
                            "Ni zapisov v izbrani tabeli."
                        } else {
                            "Prikazanih zapisov: ${parsedRows.size}"
                        }
                    } catch (e: Exception) {
                        rows = emptyList()
                        message = "Napaka pri pridobivanju podatkov: ${e.message}"
                    }
                }
            }
        ) {
            Text("Prikaži zapise")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Izbrana tabela: $selectedTable",
            style = MaterialTheme.typography.titleSmall

        )
        Text("Aktiven filter tipa podatka: $selectedTable")
        Spacer(modifier = Modifier.height(8.dp))
        Text(message)

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = stationFilter,
            onValueChange = { stationFilter = it },
            label = { Text("Filtriraj po merilni postaji") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sortiranje",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            SortDropdown(
                label = "Sortiraj po",
                selectedText = when (sortMode) {
                    "station" -> "Postaja"
                    "date" -> "Datum meritve"
                    "value" -> "Vrednost meritve"
                    else -> "Postaja"
                },
                options = listOf("Postaja", "Datum meritve", "Vrednost meritve"),
                onOptionSelected = { selected ->
                    sortMode = when (selected) {
                        "Postaja" -> "station"
                        "Datum meritve" -> "date"
                        "Vrednost meritve" -> "value"
                        else -> "station"
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            SortDropdown(
                label = "Smer",
                selectedText = when (sortMode) {
                    "station" -> if (sortAscending) "A-Z" else "Z-A"
                    "date" -> if (sortAscending) "Najstariji" else "Najnoviji"
                    "value" -> if (sortAscending) "Najmanjša" else "Največja"
                    else -> "A-Z"
                },
                options = when (sortMode) {
                    "station" -> listOf("A-Z", "Z-A")
                    "date" -> listOf("Najstariji", "Najnoviji")
                    "value" -> listOf("Najmanjša", "Največja")
                    else -> listOf("A-Z", "Z-A")
                },
                onOptionSelected = { selected ->
                    sortAscending = when (selected) {
                        "A-Z", "Najstariji", "Najmanjša" -> true
                        else -> false
                    }
                }
            )
        }
        Row {
            OutlinedTextField(
                value = minValueFilter,
                onValueChange = { minValueFilter = it },
                label = { Text("Minimalna vrednost") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = maxValueFilter,
                onValueChange = { maxValueFilter = it },
                label = { Text("Maksimalna vrednost") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        val minValue = minValueFilter.toDoubleOrNull()
        val maxValue = maxValueFilter.toDoubleOrNull()

        val stationNameColumnIndex = 1
        val measuredAtColumnIndex = 2
        val numericValueColumnIndex = headers.lastIndex

        val filteredRows = rows
            .filter { row ->
                val stationMatches =
                    stationFilter.isBlank() || row.getOrNull(stationNameColumnIndex)
                        ?.contains(
                            stationFilter,
                            ignoreCase = true
                        ) == true

                val numericValue = row.lastOrNull()?.toDoubleOrNull()

                val minMatches = minValue == null || (numericValue != null && numericValue >= minValue)
                val maxMatches = maxValue == null || (numericValue != null && numericValue <= maxValue)
                stationMatches && minMatches && maxMatches
            }
            .let { filteredList ->
                val sortedRows =
                    when (sortMode) {
                        "date" -> filteredList.sortedBy {
                            it.getOrNull(measuredAtColumnIndex)
                        }
                        "value" -> filteredList.sortedBy {
                            it.getOrNull(numericValueColumnIndex)
                                ?.toDoubleOrNull()
                        }
                        "station" -> filteredList.sortedBy {
                            it.getOrNull(stationNameColumnIndex)
                                ?.lowercase()
                        }
                        else -> filteredList
                    }
                if (sortAscending) {
                    sortedRows
                } else {
                    sortedRows.reversed()
                }
            }
        DataTable(
            headers = headers,
            rows = filteredRows
        )
    }
}

suspend fun fetchDatabaseRecords(table: String): String {
    return withContext(Dispatchers.IO) {
        ApiClient.getRaw("/api/$table")
    }
}

fun headersForTable(table: String): List<String> {
    return when (table) {
        "air-quality" -> listOf("ID", "Postaja", "Datum meritve", "Lat", "Lon", "AQI")
        "meteo" -> listOf("ID", "Postaja", "Datum meritve", "Lat", "Lon", "Temp.", "Vlažnost")
        "hydro" -> listOf("ID", "Postaja", "Datum meritve", "Lat", "Lon", "Reka", "Vodostaj", "Pretok")
        else -> listOf("ID", "Postaja")
    }
}

fun parseRowsForTable(table: String, response: String): List<List<String>> {
    if (response.isBlank() || response == "[]") return emptyList()

    return try {
        val gson = com.google.gson.Gson()
        val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
        val records: List<Map<String, Any?>> = gson.fromJson(response, listType)

        records.map { record ->
            when (table) {
                "air-quality" -> listOf(
                    record["id"].toString(),
                    record["stationName"].toString(),
                    record["measuredAt"].toString(),
                    record["latitude"].toString(),
                    record["longitude"].toString(),
                    record["airQualityIndex"].toString()
                )

                "meteo" -> listOf(
                    record["id"].toString(),
                    record["stationName"].toString(),
                    record["measuredAt"].toString(),
                    record["latitude"].toString(),
                    record["longitude"].toString(),
                    record["temperature"].toString(),
                    record["humidity"].toString()
                )

                "hydro" -> listOf(
                    record["id"].toString(),
                    record["stationName"].toString(),
                    record["measuredAt"].toString(),
                    record["latitude"].toString(),
                    record["longitude"].toString(),
                    record["riverName"].toString(),
                    record["waterLevel"].toString(),
                    record["waterFlow"].toString()
                )
                else -> emptyList()
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}