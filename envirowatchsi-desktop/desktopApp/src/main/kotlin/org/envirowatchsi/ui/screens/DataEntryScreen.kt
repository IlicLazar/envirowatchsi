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

        Row {
            Button(onClick = { selectedType = EntryType.AIR_QUALITY }) {
                Text("Air Quality")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedType = EntryType.METEO }) {
                Text("Meteo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedType = EntryType.HYDRO }) {
                Text("Hydro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Ime postaje") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") }
        )

        when (selectedType) {
            EntryType.AIR_QUALITY -> {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pm10,
                    onValueChange = { pm10 = it },
                    label = { Text("PM10") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pm25,
                    onValueChange = { pm25 = it },
                    label = { Text("PM2.5") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = o3,
                    onValueChange = { o3 = it },
                    label = { Text("O3") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = co,
                    onValueChange = { co = it },
                    label = { Text("CO") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = so2,
                    onValueChange = { so2 = it },
                    label = { Text("SO2") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aqi,
                    onValueChange = { aqi = it },
                    label = { Text("AQI") }
                )
            }

            EntryType.METEO -> {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = humidity,
                    onValueChange = { humidity = it },
                    label = { Text("Humidity") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = windSpeed,
                    onValueChange = { windSpeed = it },
                    label = { Text("Wind Speed") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = precipitation,
                    onValueChange = { precipitation = it },
                    label = { Text("Precipitation") }
                )
            }

            EntryType.HYDRO -> {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = riverName,
                    onValueChange = { riverName = it },
                    label = { Text("River Name") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterLevel,
                    onValueChange = { waterLevel = it },
                    label = { Text("Water Level") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterFlow,
                    onValueChange = { waterFlow = it },
                    label = { Text("Water Flow") }
                )
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

        Text(message)
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