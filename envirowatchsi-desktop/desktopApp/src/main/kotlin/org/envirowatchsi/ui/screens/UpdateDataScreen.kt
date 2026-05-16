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
    var recordsText by remember { mutableStateOf("Najprej naloži obstoječe zapise.") }
    var selectedId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
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
    var windDirection by remember { mutableStateOf("") }
    var precipitation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Posodabljanje podatkov",
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

        Text("Izbrana tabela: $selectedTable")

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    recordsText = "Nalagam podatke..."

                    recordsText = try {
                        withContext(Dispatchers.IO) {
                            ApiClient.getRaw("/api/$selectedTable")
                        }.let { response ->
                            if (response == "[]") {
                                "Ni zapisov v izbrani tabeli."
                            } else {
                                response
                            }
                        }
                    } catch (e: Exception) {
                        "Napaka pri nalaganju podatkov: ${e.message}"
                    }
                }
            }
        ) {
            Text("Naloži obstoječe zapise")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(recordsText)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = selectedId,
            onValueChange = { selectedId = it },
            label = { Text("ID zapisa za urejanje") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                message = if (selectedId.trim().isBlank()) {
                    "Vnesi veljaven ID zapisa."
                } else {
                    "Izbran zapis z ID: $selectedId"
                }
            }
        ) {
            Text("Izberi zapis")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Urejanje vrednosti",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Novo ime postaje") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Nova latitude") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Nova longitude") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTable) {

            "air-quality" -> {

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
                    label = { Text("Nova AQI vrednost") }
                )
            }

            "meteo" -> {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Nova temperatura") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = humidity,
                    onValueChange = { humidity = it },
                    label = { Text("Nova vlažnost") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = windSpeed,
                    onValueChange = { windSpeed = it },
                    label = { Text("Hitrost vetra") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = windDirection,
                    onValueChange = { windDirection = it },
                    label = { Text("Smer vetra") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = precipitation,
                    onValueChange = { precipitation = it },
                    label = { Text("Padavine") }
                )
            }

            "hydro" -> {
                OutlinedTextField(
                    value = riverName,
                    onValueChange = { riverName = it },
                    label = { Text("Novo ime reke") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterLevel,
                    onValueChange = { waterLevel = it },
                    label = { Text("Nov vodostaj") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterFlow,
                    onValueChange = { waterFlow = it },
                    label = { Text("Nov pretok") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val id = selectedId.trim()

                if (id.isBlank()) {
                    message = "Vnesi veljaven ID zapisa."
                    return@Button
                }

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
                                val aqiValue = aqi.toDoubleOrNull()

                                if (aqiValue == null) {
                                    message = "AQI mora biti številka."
                                    return@launch
                                }

                                endpoint = "air-quality"

                                jsonBody = buildAirQualityJson(
                                    stationName = stationName,
                                    latitude = latitude,
                                    longitude = longitude,
                                    aqi = aqi
                                )
                            }

                            "meteo" -> {
                                val tempValue = temperature.toDoubleOrNull()
                                val humidityValue = humidity.toDoubleOrNull()

                                if (tempValue == null || humidityValue == null) {
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

                        "Spremembe so uspešno shranjene."
                    } catch (e: Exception) {
                        "Napaka pri shranjevanju sprememb: ${e.message}"
                    }
                }
            }
        ) {
            Text("Shrani spremembe")
        }

        Text(message)
    }
}