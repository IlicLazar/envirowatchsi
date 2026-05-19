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
fun UpdateDataScreen() {
    val scope = rememberCoroutineScope()
    var selectedTable by remember { mutableStateOf("air-quality") }
    var records by remember { mutableStateOf(emptyList<DatabaseRecordItem>()) }
    var selectedRecord by remember { mutableStateOf<DatabaseRecordItem?>(null) }
    var message by remember { mutableStateOf("Najprej naloži zapise za izbrano tabelo.") }
    var isLoading by remember { mutableStateOf(false) }

    var stationName by remember { mutableStateOf("") }
    var aqi by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var riverName by remember { mutableStateOf("") }
    var waterLevel by remember { mutableStateOf("") }
    var waterFlow by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var pm10 by remember { mutableStateOf("") }
    var pm25 by remember { mutableStateOf("") }
    var o3 by remember { mutableStateOf("") }
    var co by remember { mutableStateOf("") }
    var so2 by remember { mutableStateOf("") }
    var windSpeed by remember { mutableStateOf("") }
    var precipitation by remember { mutableStateOf("") }

    fun clearForm() {
        selectedRecord = null
        stationName = ""
        aqi = ""
        temperature = ""
        humidity = ""
        riverName = ""
        waterLevel = ""
        waterFlow = ""
        latitude = ""
        longitude = ""
        pm10 = ""
        pm25 = ""
        o3 = ""
        co = ""
        so2 = ""
        windSpeed = ""
        precipitation = ""
    }

    fun fillForm(record: DatabaseRecordItem) {
        selectedRecord = record
        stationName = record.raw.stringValue("stationName")
        latitude = record.raw.stringValue("latitude")
        longitude = record.raw.stringValue("longitude")
        pm10 = record.raw.stringValue("pm10")
        pm25 = record.raw.stringValue("pm2_5")
        o3 = record.raw.stringValue("o3")
        co = record.raw.stringValue("co")
        so2 = record.raw.stringValue("so2")
        aqi = record.raw.stringValue("airQualityIndex")
        temperature = record.raw.stringValue("temperature")
        humidity = record.raw.stringValue("humidity")
        windSpeed = record.raw.stringValue("windSpeed")
        precipitation = record.raw.stringValue("precipitation")
        riverName = record.raw.stringValue("riverName")
        waterLevel = record.raw.stringValue("waterLevel")
        waterFlow = record.raw.stringValue("waterFlow")
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
                clearForm()
                message = if (loaded.isEmpty()) {
                    "V izbrani tabeli ni zapisov."
                } else {
                    "Naloženih zapisov: ${loaded.size}"
                }
            } catch (e: Exception) {
                records = emptyList()
                clearForm()
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
        Text("Posodabljanje podatkov", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TableSelector(
            selectedTable = selectedTable,
            onSelected = {
                selectedTable = it
                records = emptyList()
                clearForm()
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
            actionText = "Uredi",
            onRecordSelected = {
                fillForm(it)
                message = "Izbran zapis: ${it.stationName}"
            }
        )

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Urejanje izbranega zapisa", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID: ${record.id}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = stationName,
                        onValueChange = { stationName = it },
                        label = { Text("Ime postaje") },
                        modifier = Modifier.width(360.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.width(176.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.width(176.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedTable) {
                        "air-quality" -> {
                            Row {
                                OutlinedTextField(pm10, { pm10 = it }, label = { Text("PM10") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(pm25, { pm25 = it }, label = { Text("PM2.5") }, modifier = Modifier.width(176.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                OutlinedTextField(o3, { o3 = it }, label = { Text("O3") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(co, { co = it }, label = { Text("CO") }, modifier = Modifier.width(176.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                OutlinedTextField(so2, { so2 = it }, label = { Text("SO2") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(aqi, { aqi = it }, label = { Text("AQI") }, modifier = Modifier.width(176.dp))
                            }
                        }

                        "meteo" -> {
                            Row {
                                OutlinedTextField(temperature, { temperature = it }, label = { Text("Temperatura") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(humidity, { humidity = it }, label = { Text("Vlažnost") }, modifier = Modifier.width(176.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                OutlinedTextField(windSpeed, { windSpeed = it }, label = { Text("Hitrost vetra") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(precipitation, { precipitation = it }, label = { Text("Padavine") }, modifier = Modifier.width(176.dp))
                            }
                        }

                        "hydro" -> {
                            OutlinedTextField(
                                value = riverName,
                                onValueChange = { riverName = it },
                                label = { Text("Ime reke") },
                                modifier = Modifier.width(360.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                OutlinedTextField(waterLevel, { waterLevel = it }, label = { Text("Vodostaj") }, modifier = Modifier.width(176.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(waterFlow, { waterFlow = it }, label = { Text("Pretok") }, modifier = Modifier.width(176.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val id = selectedRecord?.id ?: return@Button

                            if (stationName.isBlank()) {
                                message = "Ime postaje je obvezno."
                                return@Button
                            }

                            scope.launch {
                                message = "Shranjujem spremembe..."

                                message = try {
                                    val endpoint: String
                                    val jsonBody: String

                                    when (selectedTable) {
                                        "air-quality" -> {
                                            if (aqi.toDoubleOrNull() == null) {
                                                message = "AQI mora biti številka."
                                                return@launch
                                            }

                                            endpoint = "air-quality"
                                            jsonBody = buildAirQualityJson(
                                                stationName = stationName,
                                                latitude = latitude,
                                                longitude = longitude,
                                                aqi = aqi,
                                                pm10 = pm10,
                                                pm2_5 = pm25,
                                                o3 = o3,
                                                co = co,
                                                so2 = so2
                                            )
                                        }

                                        "meteo" -> {
                                            if (temperature.toDoubleOrNull() == null || humidity.toDoubleOrNull() == null) {
                                                message = "Temperatura in vlažnost morata biti številki."
                                                return@launch
                                            }

                                            endpoint = "meteo"
                                            jsonBody = buildMeteoJson(
                                                stationName = stationName,
                                                latitude = latitude,
                                                longitude = longitude,
                                                temperature = temperature,
                                                humidity = humidity,
                                                windSpeed = windSpeed,
                                                precipitation = precipitation
                                            )
                                        }

                                        else -> {
                                            if (riverName.isBlank()) {
                                                message = "Ime reke je obvezno."
                                                return@launch
                                            }

                                            endpoint = "hydro"
                                            jsonBody = buildHydroJson(
                                                stationName = stationName,
                                                riverName = riverName,
                                                latitude = latitude,
                                                longitude = longitude,
                                                waterLevel = waterLevel,
                                                waterFlow = waterFlow
                                            )
                                        }
                                    }

                                    withContext(Dispatchers.IO) {
                                        ApiClient.putForm("/api/$endpoint/$id", jsonBody)
                                    }

                                    val loaded = withContext(Dispatchers.IO) {
                                        parseDatabaseRecords(ApiClient.getRaw("/api/$selectedTable"), selectedTable)
                                    }

                                    records = loaded
                                    selectedRecord = loaded.firstOrNull { it.id == id }

                                    "Spremembe so uspešno shranjene."
                                } catch (e: Exception) {
                                    "Napaka pri shranjevanju sprememb: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text("Shrani spremembe")
                    }
                }
            }
        }
    }
}
