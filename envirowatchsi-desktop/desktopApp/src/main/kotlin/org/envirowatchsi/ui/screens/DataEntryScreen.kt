package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.ui.components.EnviroPanel
import org.envirowatchsi.ui.components.StatusText

@Composable
fun DataEntryScreen() {
    val scope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf(EntryType.AIR_QUALITY) }

    var stationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    var aqi by remember { mutableStateOf("") }
    var pm10 by remember { mutableStateOf("") }
    var pm25 by remember { mutableStateOf("") }
    var o3 by remember { mutableStateOf("") }
    var co by remember { mutableStateOf("") }
    var so2 by remember { mutableStateOf("") }

    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var windSpeed by remember { mutableStateOf("") }
    var precipitation by remember { mutableStateOf("") }

    var riverName by remember { mutableStateOf("") }
    var waterLevel by remember { mutableStateOf("") }
    var waterFlow by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Vnos podatkov",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        EnviroPanel(modifier = Modifier.fillMaxWidth(), title = "Tip podatkov") {
            Row {
                DataTypeButton("Kakovost zraka", EntryType.AIR_QUALITY, selectedType) { selectedType = it }
                Spacer(modifier = Modifier.width(8.dp))
                DataTypeButton("Meteo", EntryType.METEO, selectedType) { selectedType = it }
                Spacer(modifier = Modifier.width(8.dp))
                DataTypeButton("Hidro", EntryType.HYDRO, selectedType) { selectedType = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        EnviroPanel(modifier = Modifier.fillMaxWidth(), title = "Podatki meritve") {
            OutlinedTextField(
                value = stationName,
                onValueChange = { stationName = it },
                label = { Text("Ime postaje") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormFieldRow {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Zemljepisna širina") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Zemljepisna dolžina") },
                    modifier = Modifier.weight(1f)
                )
            }

            when (selectedType) {
            EntryType.AIR_QUALITY -> {
                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = pm10,
                        onValueChange = { pm10 = it },
                        label = { Text("PM10") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = pm25,
                        onValueChange = { pm25 = it },
                        label = { Text("PM2.5") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = o3,
                        onValueChange = { o3 = it },
                        label = { Text("O3") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = co,
                        onValueChange = { co = it },
                        label = { Text("CO") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = so2,
                        onValueChange = { so2 = it },
                        label = { Text("SO2") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = aqi,
                        onValueChange = { aqi = it },
                        label = { Text("AQI") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            EntryType.METEO -> {
                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = { Text("Temperatura") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = humidity,
                        onValueChange = { humidity = it },
                        label = { Text("Vlažnost") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = windSpeed,
                        onValueChange = { windSpeed = it },
                        label = { Text("Hitrost vetra") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = precipitation,
                        onValueChange = { precipitation = it },
                        label = { Text("Padavine") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            EntryType.HYDRO -> {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = riverName,
                    onValueChange = { riverName = it },
                    label = { Text("Ime reke") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                FormFieldRow {
                    OutlinedTextField(
                        value = waterLevel,
                        onValueChange = { waterLevel = it },
                        label = { Text("Vodostaj") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = waterFlow,
                        onValueChange = { waterFlow = it },
                        label = { Text("Pretok") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (stationName.isBlank()) {
                    message = "Ime postaje je obvezno."
                    return@Button
                }

                scope.launch {
                    message = "Shranjevanje podatkov..."

                    message = try {
                        val endpoint: String
                        val jsonBody: String

                        when (selectedType) {
                            EntryType.AIR_QUALITY -> {
                                val aqiValue = aqi.toDoubleOrNull()

                                if (aqiValue == null) {
                                    message = "AQI mora biti številka."
                                    return@launch
                                }

                                endpoint = "air-quality"

                                jsonBody = buildAirQualityJson(
                                    stationName,
                                    latitude,
                                    longitude,
                                    aqi,
                                    pm10 = pm10,
                                    pm2_5 = pm25,
                                    o3 = o3,
                                    co = co,
                                    so2 = so2
                                )
                            }

                            EntryType.METEO -> {
                                val temperatureValue = temperature.toDoubleOrNull()
                                val humidityValue = humidity.toDoubleOrNull()

                                if (temperatureValue == null || humidityValue == null) {
                                    message = "Temperature in humidity morata biti številki."
                                    return@launch
                                }

                                endpoint = "meteo"

                                jsonBody = buildMeteoJson(
                                    stationName,
                                    latitude,
                                    longitude,
                                    temperature,
                                    humidity,
                                    windSpeed,
                                    precipitation
                                )
                            }

                            EntryType.HYDRO -> {
                                if (riverName.isBlank()) {
                                    message = "Ime reke je obvezno."
                                    return@launch
                                }

                                endpoint = "hydro"

                                jsonBody = buildHydroJson(
                                    stationName,
                                    riverName,
                                    latitude,
                                    longitude,
                                    waterLevel,
                                    waterFlow
                                )
                            }
                        }

                        withContext(Dispatchers.IO) {
                            ApiClient.postJson("/api/$endpoint", jsonBody)
                        }

                        stationName = ""
                        latitude = ""
                        longitude = ""
                        aqi = ""
                        pm10 = ""
                        pm25 = ""
                        o3 = ""
                        co = ""
                        so2 = ""
                        temperature = ""
                        humidity = ""
                        windSpeed = ""
                        precipitation = ""
                        riverName = ""
                        waterLevel = ""
                        waterFlow = ""

                        "Podatki so uspešno shranjeni."
                    } catch (e: Exception) {
                        "Napaka pri shranjevanju: ${e.message}"
                    }
                }
            }
        ) {
            Text("Shrani")
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatusText(message)
    }
}

@Composable
private fun FormFieldRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun DataTypeButton(
    text: String,
    type: EntryType,
    selectedType: EntryType,
    onSelected: (EntryType) -> Unit
) {
    if (type == selectedType) {
        Button(onClick = { onSelected(type) }) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = { onSelected(type) }) {
            Text(text)
        }
    }
}

fun buildAirQualityJson(
    stationName: String,
    latitude: String,
    longitude: String,
    aqi: String,
    pm10: String? = null,
    pm2_5: String? = null,
    o3: String? = null,
    co: String? = null,
    so2: String? = null
): String {
    val body = mutableMapOf<String, Any?>(
        "stationName" to stationName,
        "latitude" to latitude.toDoubleOrNull(),
        "longitude" to longitude.toDoubleOrNull(),
        "aqi" to aqi.toIntOrNull()
    )
    pm10?.toDoubleOrNull()?.let { body["pm10"] = it }
    pm2_5?.toDoubleOrNull()?.let { body["pm2_5"] = it }
    o3?.toDoubleOrNull()?.let { body["o3"] = it }
    co?.toDoubleOrNull()?.let { body["co"] = it }
    so2?.toDoubleOrNull()?.let { body["so2"] = it }
    return com.google.gson.Gson().toJson(body)
}

fun buildMeteoJson(
    stationName: String,
    latitude: String,
    longitude: String,
    temperature: String,
    humidity: String,
    windSpeed: String,
    precipitation: String
): String {
    return com.google.gson.Gson().toJson(
        mapOf(
            "stationName" to stationName,
            "latitude" to latitude.toDoubleOrNull(),
            "longitude" to longitude.toDoubleOrNull(),
            "temperature" to temperature.toDoubleOrNull(),
            "humidity" to humidity.toDoubleOrNull(),
            "windSpeed" to windSpeed.toDoubleOrNull(),
            "precipitation" to precipitation.toDoubleOrNull()
        )
    )
}

fun buildHydroJson(
    stationName: String,
    riverName: String,
    latitude: String,
    longitude: String,
    waterLevel: String,
    waterFlow: String
): String {
    return com.google.gson.Gson().toJson(
        mapOf(
            "stationName" to stationName,
            "riverName" to riverName,
            "latitude" to latitude.toDoubleOrNull(),
            "longitude" to longitude.toDoubleOrNull(),
            "waterLevel" to waterLevel.toDoubleOrNull(),
            "waterFlow" to waterFlow.toDoubleOrNull()
        )
    )
}
