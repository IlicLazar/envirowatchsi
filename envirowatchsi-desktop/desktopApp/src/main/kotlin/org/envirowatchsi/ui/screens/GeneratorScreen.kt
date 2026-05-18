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
import kotlin.random.Random
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.ui.components.GeneratedDataTable
import org.envirowatchsi.ui.components.RangeInputRow

@Composable
fun GeneratorScreen() {
    val scope = rememberCoroutineScope()
    var recordCount by remember { mutableStateOf("10") }
    var minTemperature by remember { mutableStateOf("-10") }
    var maxTemperature by remember { mutableStateOf("35") }
    var minHumidity by remember { mutableStateOf("20") }
    var maxHumidity by remember { mutableStateOf("100") }
    var minWaterLevel by remember { mutableStateOf("20") }
    var maxWaterLevel by remember { mutableStateOf("500") }
    var minAirQuality by remember { mutableStateOf("5") }
    var maxAirQuality by remember { mutableStateOf("120") }
    var message by remember { mutableStateOf("Vnesi število zapisov za generiranje.") }
    var generatedHeaders by remember { mutableStateOf(emptyList<String>()) }
    var generatedRows by remember { mutableStateOf(emptyList<List<String>>()) }
    var selectedGeneratedRows by remember {
        mutableStateOf(setOf<Int>())
    }
    var selectedGeneratorType by remember { mutableStateOf("meteo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Generator naključnih podatkov",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
//            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nastavitve generiranja",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = recordCount,
                    onValueChange = { recordCount = it },
                    label = { Text("Število generiranih zapisov") },
                    modifier = Modifier.width(280.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tip podatkov")

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Button(onClick = { selectedGeneratorType = "meteo" }) {
                        Text("Meteo")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = { selectedGeneratorType = "hydro" }) {
                        Text("Hidro")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = { selectedGeneratorType = "air" }) {
                        Text("Kakovost zraka")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
//            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Območja vrednosti",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedGeneratorType) {
                    "meteo" -> {
                        RangeInputRow(
                            title = "Temperatura",
                            minValue = minTemperature,
                            onMinChange = { minTemperature = it },
                            maxValue = maxTemperature,
                            onMaxChange = { maxTemperature = it }
                        )

                        RangeInputRow(
                            title = "Vlaga",
                            minValue = minHumidity,
                            onMinChange = { minHumidity = it },
                            maxValue = maxHumidity,
                            onMaxChange = { maxHumidity = it }
                        )
                    }

                    "hydro" -> {
                        RangeInputRow(
                            title = "Vodostaj",
                            minValue = minWaterLevel,
                            onMinChange = { minWaterLevel = it },
                            maxValue = maxWaterLevel,
                            onMaxChange = { maxWaterLevel = it }
                        )
                    }

                    "air" -> {
                        RangeInputRow(
                            title = "Kakovost zraka",
                            minValue = minAirQuality,
                            onMinChange = { minAirQuality = it },
                            maxValue = maxAirQuality,
                            onMaxChange = { maxAirQuality = it }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
//            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generiranje podatkov",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val count = recordCount.toIntOrNull()

                        if (count == null || count <= 0) {
                            message = "Vnesi veljavno pozitivno število."
                            return@Button
                        }
                        val validationError = when (selectedGeneratorType) {

                            "meteo" -> {
                                val minTemp = minTemperature.toDoubleOrNull()
                                val maxTemp = maxTemperature.toDoubleOrNull()

                                val minHum = minHumidity.toDoubleOrNull()
                                val maxHum = maxHumidity.toDoubleOrNull()

                                when {
                                    minTemp == null || maxTemp == null ->
                                        "Temperatura mora biti številka."

                                    minTemp >= maxTemp ->
                                        "Minimalna temperatura mora biti manjša od maksimalne."

                                    minHum == null || maxHum == null ->
                                        "Vlaga mora biti številka."

                                    minHum >= maxHum ->
                                        "Minimalna vlaga mora biti manjša od maksimalne."

                                    else -> null
                                }
                            }

                            "hydro" -> {
                                val minLevel = minWaterLevel.toDoubleOrNull()
                                val maxLevel = maxWaterLevel.toDoubleOrNull()

                                when {
                                    minLevel == null || maxLevel == null ->
                                        "Vodostaj mora biti številka."

                                    minLevel >= maxLevel ->
                                        "Minimalni vodostaj mora biti manjši od maksimalnega."

                                    else -> null
                                }
                            }

                            "air" -> {
                                val minAq = minAirQuality.toDoubleOrNull()
                                val maxAq = maxAirQuality.toDoubleOrNull()

                                when {
                                    minAq == null || maxAq == null ->
                                        "Kakovost zraka mora biti številka."

                                    minAq >= maxAq ->
                                        "Minimalna kakovost zraka mora biti manjša od maksimalne."

                                    else -> null
                                }
                            }

                            else -> null
                        }

                        if (validationError != null) {
                            message = validationError
                            generatedRows = emptyList()
                            return@Button
                        }

                        when (selectedGeneratorType) {
                            "meteo" -> {
                                generatedHeaders = listOf(
                                    "Postaja",
                                    "Temperatura",
                                    "Vlaga",
                                    "Veter",
                                    "Padavine"
                                )

                                generatedRows = List(count) { index ->

                                    val stationName = "Meteo postaja ${index + 1}"

                                    val minTemp = minTemperature.toDoubleOrNull() ?: -10.0
                                    val maxTemp = maxTemperature.toDoubleOrNull() ?: 35.0
                                    val temperature = Random.nextDouble(minTemp, maxTemp)

                                    val minHum = minHumidity.toDoubleOrNull() ?: 20.0
                                    val maxHum = maxHumidity.toDoubleOrNull() ?: 100.0
                                    val humidity = Random.nextDouble(minHum, maxHum)

                                    val windSpeed = Random.nextDouble(0.0, 20.0)
                                    val precipitation = Random.nextDouble(0.0, 30.0)

                                    listOf(
                                        stationName,
                                        "%.1f °C".format(temperature),
                                        "%.1f %%".format(humidity),
                                        "%.1f km/h".format(windSpeed),
                                        "%.1f mm".format(precipitation)
                                    )
                                }
                                selectedGeneratedRows = generatedRows.indices.toSet()
                                message = "Uspešno generiranih meteo zapisov: $count"
                            }

                            "hydro" -> {
                                generatedHeaders = listOf(
                                    "Postaja",
                                    "Reka",
                                    "Vodostaj",
                                    "Pretok"
                                )

                                generatedRows = List(count) { index ->
                                    val stationName = "Hidro postaja ${index + 1}"
                                    val riverName = listOf("Sava", "Drava", "Soča", "Mura", "Krka", "Savinja").random()

                                    val minLevel = minWaterLevel.toDoubleOrNull() ?: 20.0
                                    val maxLevel = maxWaterLevel.toDoubleOrNull() ?: 500.0
                                    val waterLevel = Random.nextDouble(minLevel, maxLevel)

                                    val waterFlow = Random.nextDouble(1.0, 300.0)

                                    listOf(
                                        stationName,
                                        riverName,
                                        "%.1f cm".format(waterLevel),
                                        "%.1f m³/s".format(waterFlow)
                                    )
                                }
                                selectedGeneratedRows = generatedRows.indices.toSet()

                                message = "Uspešno generiranih hidro zapisov: $count"
                            }

                            "air" -> {
                                generatedHeaders = listOf(
                                    "Postaja",
                                    "PM10",
                                    "PM2.5",
                                    "O3",
                                    "AQI"
                                )

                                generatedRows = List(count) { index ->
                                    val stationName = "Zrak postaja ${index + 1}"

                                    val minAq = minAirQuality.toDoubleOrNull() ?: 5.0
                                    val maxAq = maxAirQuality.toDoubleOrNull() ?: 120.0

                                    val pm10 = Random.nextDouble(minAq, maxAq)
                                    val pm25 = Random.nextDouble(minAq, maxAq)
                                    val o3 = Random.nextDouble(minAq, maxAq)
                                    val aqi = listOf(pm10, pm25, o3).maxOrNull() ?: 0.0

                                    listOf(
                                        stationName,
                                        "%.1f".format(pm10),
                                        "%.1f".format(pm25),
                                        "%.1f".format(o3),
                                        "%.1f".format(aqi)
                                    )
                                }
                                selectedGeneratedRows = generatedRows.indices.toSet()

                                message = "Uspešno generiranih zapisov kakovosti zraka: $count"
                            }
                        }
                    }
                ) {
                    Text("Generiraj podatke")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(message)
        Spacer(modifier = Modifier.height(12.dp))



        Spacer(modifier = Modifier.height(16.dp))

        if (generatedRows.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Predogled generiranih podatkov",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            GeneratedDataTable(
                headers = generatedHeaders,
                rows = generatedRows,
                selectedRows = selectedGeneratedRows,
                onSelectionChange = { rowIndex, selected ->
                    selectedGeneratedRows =
                        if (selected) {
                            selectedGeneratedRows + rowIndex
                        } else {
                            selectedGeneratedRows - rowIndex
                        }
                },
                onDeleteRow = { rowIndex ->
                    generatedRows = generatedRows.filterIndexed { index, _ ->
                        index != rowIndex
                    }

                    selectedGeneratedRows = generatedRows.indices.toSet()
                    message = "Zapis je odstranjen iz predogleda."
                }
            )
            Button(
                onClick = {
                    scope.launch{
                        val selectedRowsData = generatedRows.filterIndexed { index, _ ->
                            selectedGeneratedRows.contains(index)
                        }

                        if (selectedRowsData.isEmpty()) {
                            message = "Izberi vsaj en zapis za shranjevanje."
                            return@launch
                        }

                        try {

                            selectedRowsData.forEach { row ->

                                val endpoint: String
                                val jsonBody: String

                                when (selectedGeneratorType) {

                                    "meteo" -> {
                                        endpoint = "meteo"

                                        jsonBody = buildMeteoJson(
                                            stationName = row[0],
                                            latitude = Random.nextDouble(45.4, 46.9).toString(),
                                            longitude = Random.nextDouble(13.4, 16.6).toString(),
                                            temperature = row[1].replace(" °C", ""),
                                            humidity = row[2].replace(" %", ""),
                                            windSpeed = row[3].replace(" km/h", ""),
                                            precipitation = row[4].replace(" mm", "")
                                        )
                                    }

                                    "hydro" -> {
                                        endpoint = "hydro"

                                        jsonBody = buildHydroJson(
                                            stationName = row[0],
                                            riverName = row[1],
                                            latitude = Random.nextDouble(45.4, 46.9).toString(),
                                            longitude = Random.nextDouble(13.4, 16.6).toString(),
                                            waterLevel = row[2].replace(" cm", ""),
                                            waterFlow = row[3].replace(" m³/s", "")
                                        )
                                    }

                                    else -> {
                                        endpoint = "air-quality"

                                        jsonBody = buildAirQualityJson(
                                            stationName = row[0],
                                            latitude = Random.nextDouble(45.4, 46.9).toString(),
                                            longitude = Random.nextDouble(13.4, 16.6).toString(),
                                            aqi = row[4].toDoubleOrNull()?.toInt()?.toString() ?: "0",
                                            pm10 = row[1],
                                            pm2_5 = row[2],
                                            o3 = row[3]
                                        )
                                    }
                                }

                                withContext(Dispatchers.IO) {
                                    ApiClient.postJson("/api/$endpoint", jsonBody)
                                }
                            }

                            message =
                                "Uspešno shranjenih zapisov: ${selectedRowsData.size}"

                            generatedRows = generatedRows.filterIndexed { index, _ ->
                                !selectedGeneratedRows.contains(index)
                            }

                            selectedGeneratedRows = generatedRows.indices.toSet()

                        } catch (e: Exception) {

                            message = "Napaka pri shranjevanju: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Shrani izbrane podatke")
            }
        }
    }
}