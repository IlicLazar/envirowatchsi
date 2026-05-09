package com.envirowatchsi

import com.envirowatchsi.parser.parseMeteoData
import com.envirowatchsi.parser.parseAirQualityData
import com.envirowatchsi.parser.parseHydroData
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.envirowatchsi.network.fetchRawMeteoXml
import com.envirowatchsi.network.fetchRawAirQualityXml
import com.envirowatchsi.network.fetchRawXml
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import kotlin.random.Random
import java.time.LocalDateTime

enum class Screen {
    DASHBOARD,
    AIR_QUALITY,
    METEO,
    HYDRO,
    DATABASE,
    GENERATOR,
    DATA_ENTRY,
    UPDATE,
    DELETE
}

fun main() = application {
    com.envirowatchsi.server.ApiServer.start()
    Window(
        onCloseRequest = ::exitApplication,
        title = "EnviroWatch SI"
    ) {
        App()
    }
}
@Composable
@Preview
fun App() {
    var selectedScreen by remember{ mutableStateOf(Screen.DASHBOARD) }
    MaterialTheme{
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ){
            Sidebar(
                selectedScreen = selectedScreen,
                onScreenSelected = {selectedScreen = it}
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ){
                when(selectedScreen){
                    Screen.DASHBOARD -> DashboardScreen()
                    Screen.AIR_QUALITY -> AirQualityScreen()
                    Screen.METEO -> MeteoScreen()
                    Screen.HYDRO -> HydroScreen()
                    Screen.DATABASE -> DatabaseScreen()
                    Screen.GENERATOR -> GeneratorScreen()
                    Screen.DATA_ENTRY -> DataEntryScreen()
                    Screen.UPDATE -> UpdateDataScreen()
                    Screen.DELETE -> DeleteDataScreen()
                }
            }
        }
    }
}

@Composable
fun Sidebar(
    selectedScreen: Screen,
    onScreenSelected: (Screen)->Unit
){
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "EnviroWatch SI",
            style = MaterialTheme.typography.h5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Navigacija",
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(8.dp))

        SidebarButton("Nadzorna plošča", Screen.DASHBOARD, selectedScreen, onScreenSelected)
        SidebarButton("Kakovost zraka", Screen.AIR_QUALITY, selectedScreen, onScreenSelected)
        SidebarButton("Meteorološki podatki", Screen.METEO, selectedScreen, onScreenSelected)
        SidebarButton("Hidrološki podatki", Screen.HYDRO, selectedScreen, onScreenSelected)
        SidebarButton("Podatkovna baza", Screen.DATABASE, selectedScreen, onScreenSelected)
        SidebarButton("Generator podatkov", Screen.GENERATOR, selectedScreen, onScreenSelected)
        SidebarButton("Vnos podatkov", Screen.DATA_ENTRY, selectedScreen, onScreenSelected)
        SidebarButton("Posodabljanje podatkov", Screen.UPDATE, selectedScreen, onScreenSelected)
        SidebarButton("Brisanje podatkov", Screen.DELETE, selectedScreen, onScreenSelected)
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Projektna naloga 2",
            style = MaterialTheme.typography.caption
        )
    }
}
@Composable
fun SidebarButton(
    text: String,
    screen: Screen,
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
){
    Button(
        onClick = {onScreenSelected(screen)},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (screen == selectedScreen)
                MaterialTheme.colors.primary
            else
                MaterialTheme.colors.surface
        )
    ){
        Text(text)
    }
}
@Composable
fun DashboardScreen(){
    val scope = rememberCoroutineScope()
    var connectionStatus by remember { mutableStateOf("Povezava še ni testirana.") }
    Column {
        Text(
            text = "Digitalni dvojček okoljskega stanja v Sloveniji",
            style = MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Aplikacija bo omogočala prikaz, urejanje in shranjevanje podatkov o kakovosti zraka, vremenskih razmerah in hidrološkem stanju v Sloveniji."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Načrtovani moduli:", style = MaterialTheme.typography.h6)
        Text("- Kakovost zraka")
        Text("- Meteorološki podatki")
        Text("- Hidrološki podatki")
        Text("- Upravljanje podatkovne baze")
        Text("- Generator namišljenih podatkov")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    connectionStatus = "Testiranje povezave..."

                    connectionStatus = try {
                        val xml = fetchRawMeteoXml()
                        "Povezava uspešna. Prejeto znakov: ${xml.length}"
                    } catch (e: Exception) {
                        "Napaka pri povezavi: ${e.message}"
                    }
                }
            }
        ) {
            Text("Test povezave s spletnim servisom")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(connectionStatus)
    }
}
@Composable
fun PlaceholderScreen(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Trenutno izbran zaslon: $title",
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Preklapljanje med zasloni poteka preko navigacijskega menija na levi strani aplikacije.")
    }
}
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
            style = MaterialTheme.typography.h4
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
            style = MaterialTheme.typography.subtitle1

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
            style = MaterialTheme.typography.h6
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
@Composable
fun SortDropdown(
    label: String,
    selectedText: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)

        Spacer(modifier = Modifier.height(4.dp))

        Box {
            Button(
                onClick = { expanded = true }
            ) {
                Text("$selectedText ▼")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

suspend fun fetchDatabaseRecords(table: String): String {
    return withContext(Dispatchers.IO) {
        URL("http://localhost:8080/api/$table")
            .openStream()
            .bufferedReader(Charsets.UTF_8)
            .readText()
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

enum class EntryType {
    AIR_QUALITY,
    METEO,
    HYDRO
}
@Composable
fun DataEntryScreen() {
    val scope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf(EntryType.AIR_QUALITY) }

    var stationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    var aqi by remember { mutableStateOf("") }

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
            style = MaterialTheme.typography.h4
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
                                    aqi
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

                            val connection = URL("http://localhost:8080/api/$endpoint")
                                .openConnection() as java.net.HttpURLConnection

                            connection.requestMethod = "POST"
                            connection.doOutput = true

                            connection.setRequestProperty(
                                "Content-Type",
                                "application/json"
                            )

                            connection.outputStream.use {
                                it.write(jsonBody.toByteArray(Charsets.UTF_8))
                            }

                            connection.inputStream
                                .bufferedReader()
                                .readText()
                        }

                        stationName = ""
                        latitude = ""
                        longitude = ""
                        aqi = ""
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
    aqi: String
): String {
    return com.google.gson.Gson().toJson(
        mapOf(
            "stationName" to stationName,
            "latitude" to latitude.toDoubleOrNull(),
            "longitude" to longitude.toDoubleOrNull(),
            "aqi" to aqi.toIntOrNull()
        )
    )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Posodabljanje podatkov",
            style = MaterialTheme.typography.h4
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
                            URL("http://localhost:8080/api/$selectedTable")
                                .openStream()
                                .bufferedReader(Charsets.UTF_8)
                                .readText()
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
                message = if (selectedId.toIntOrNull() == null) {
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
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Novo ime postaje") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTable) {
            "air-quality" -> {
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
                val id = selectedId.toIntOrNull()

                if (id == null) {
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
                        val postData: String

                        when (selectedTable) {
                            "air-quality" -> {
                                val aqiValue = aqi.toIntOrNull()
                                if (aqiValue == null) {
                                    message = "AQI mora biti številka."
                                    return@launch
                                }

                                endpoint = "air-quality"
                                postData = "stationName=$stationName&aqi=$aqiValue"
                            }

                            "meteo" -> {
                                val tempValue = temperature.toDoubleOrNull()
                                val humidityValue = humidity.toDoubleOrNull()

                                if (tempValue == null || humidityValue == null) {
                                    message = "Temperatura in vlažnost morata biti številki."
                                    return@launch
                                }

                                endpoint = "meteo"
                                postData = "stationName=$stationName&temperature=$tempValue&humidity=$humidityValue"
                            }

                            else -> {
                                if (riverName.isBlank()) {
                                    message = "Ime reke je obvezno."
                                    return@launch
                                }

                                endpoint = "hydro"
                                postData = "stationName=$stationName&riverName=$riverName&waterLevel=$waterLevel&waterFlow=$waterFlow"
                            }
                        }

                        withContext(Dispatchers.IO) {
                            val connection = URL("http://localhost:8080/api/$endpoint/$id")
                                .openConnection() as java.net.HttpURLConnection

                            connection.requestMethod = "PUT"
                            connection.doOutput = true
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                            connection.outputStream.use {
                                it.write(postData.toByteArray())
                            }

                            connection.inputStream.bufferedReader().readText()
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
            style = MaterialTheme.typography.h4
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
                            URL("http://localhost:8080/api/$selectedTable")
                                .openStream()
                                .bufferedReader()
                                .readText()
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
                style = MaterialTheme.typography.h6
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
                                    val connection = URL("http://localhost:8080/api/$selectedTable/$id")
                                        .openConnection() as java.net.HttpURLConnection

                                    connection.requestMethod = "DELETE"

                                    connection.inputStream
                                        .bufferedReader()
                                        .readText()
                                }

                                recordsText = withContext(Dispatchers.IO) {
                                    URL("http://localhost:8080/api/$selectedTable")
                                        .openStream()
                                        .bufferedReader()
                                        .readText()
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

@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }

        Divider()

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        style = when {
                            cell.contains("null") -> MaterialTheme.typography.caption
                            else -> MaterialTheme.typography.body2
                        }
                    )
                }
            }

            Divider()
        }
    }
}
@Composable
fun MeteoScreen() {
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf(emptyList<com.envirowatchsi.model.MeteoStation>()) }
    var selectedRecord by remember { mutableStateOf<com.envirowatchsi.model.MeteoStation?>(null) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev meteo podatkov.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Meteorološki podatki",
            style = MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = "Pridobivanje in razčlenjevanje meteo podatkov..."

                    try {
                        val parsed = withContext(Dispatchers.IO) {
                            val xml = fetchRawMeteoXml()
                            parseMeteoData(xml).take(10)
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
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "Podatki so pripravljeni za shranjevanje v bazo.",
                style = MaterialTheme.typography.subtitle1
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
                                val connection = URL("http://localhost:8080/api/meteo")
                                    .openConnection() as java.net.HttpURLConnection

                                connection.requestMethod = "POST"
                                connection.doOutput = true
                                connection.setRequestProperty("Content-Type", "application/json")

                                connection.outputStream.use {
                                    it.write(jsonBody.toByteArray(Charsets.UTF_8))
                                }

                                connection.inputStream.bufferedReader().readText()
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
@Composable
fun AirQualityScreen() {
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf(emptyList<com.envirowatchsi.model.AirQualityStation>()) }
    var selectedRecord by remember { mutableStateOf<com.envirowatchsi.model.AirQualityStation?>(null) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev podatkov o kakovosti zraka.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Kakovost zraka", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = "Pridobivanje in razčlenjevanje podatkov o kakovosti zraka..."

                    try {
                        val parsed = withContext(Dispatchers.IO) {
                            val xml = fetchRawAirQualityXml()
                            parseAirQualityData(xml).take(10)
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
            Text("Pridobi podatke o kakovosti zraka")
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
                    text = "${record.stationName} | AQI: ${record.airQualityIndex}",
                    modifier = Modifier.padding(8.dp)
                )
            }

            Divider()
        }

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Text("Pregled izbranega zapisa", style = MaterialTheme.typography.h6)
            Text(
                text = "Podatki so pripravljeni za shranjevanje v bazo.",
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Postaja: ${record.stationName}")
            Text("Latitude: ${record.latitude}")
            Text("Longitude: ${record.longitude}")
            Text("PM10: ${record.pm10}")
            Text("PM2.5: ${record.pm2_5}")
            Text("O3: ${record.o3}")
            Text("CO: ${record.co}")
            Text("SO2: ${record.so2}")
            Text("AQI: ${record.airQualityIndex}")
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        message = "Shranjevanje izbranega zapisa..."

                        message = try {
                            val jsonBody = buildAirQualityJson(
                                record.stationName,
                                record.latitude.toString(),
                                record.longitude.toString(),
                                record.airQualityIndex?.toInt()?.toString() ?: "0"
                            )

                            withContext(Dispatchers.IO) {
                                val connection = URL("http://localhost:8080/api/air-quality")
                                    .openConnection() as java.net.HttpURLConnection

                                connection.requestMethod = "POST"
                                connection.doOutput = true
                                connection.setRequestProperty("Content-Type", "application/json")

                                connection.outputStream.use {
                                    it.write(jsonBody.toByteArray(Charsets.UTF_8))
                                }

                                connection.inputStream.bufferedReader().readText()
                            }

                            "Izbran zapis kakovosti zraka je shranjen v bazo."
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
@Composable
fun HydroScreen() {
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf(emptyList<com.envirowatchsi.model.HydroStation>()) }
    var selectedRecord by remember { mutableStateOf<com.envirowatchsi.model.HydroStation?>(null) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev hidroloških podatkov.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Hidrološki podatki", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = "Pridobivanje in razčlenjevanje hidroloških podatkov..."

                    try {
                        val parsed = withContext(Dispatchers.IO) {
                            val xml = fetchRawXml()
                            parseHydroData(xml).take(10)
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
            Text("Pridobi hidrološke podatke")
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
                    text = "${record.stationName} | ${record.riverName} | ${record.waterLevel}",
                    modifier = Modifier.padding(8.dp)
                )
            }

            Divider()
        }

        selectedRecord?.let { record ->
            Spacer(modifier = Modifier.height(24.dp))

            Text("Pregled izbranega zapisa", style = MaterialTheme.typography.h6)
            Text(
                text = "Podatki so pripravljeni za shranjevanje v bazo.",
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Postaja: ${record.stationName}")
            Text("Reka: ${record.riverName}")
            Text("Latitude: ${record.latitude}")
            Text("Longitude: ${record.longitude}")
            Text("Vodostaj: ${record.waterLevel}")
            Text("Pretok: ${record.waterFlow}")
            Text("Čas meritve: ${record.measuredAt}")
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        message = "Shranjevanje izbranega zapisa..."

                        message = try {
                            val jsonBody = buildHydroJson(
                                record.stationName,
                                record.riverName,
                                record.latitude?.toString() ?: "",
                                record.longitude?.toString() ?: "",
                                record.waterLevel?.toString() ?: "",
                                record.waterFlow?.toString() ?: ""
                            )

                            withContext(Dispatchers.IO) {
                                val connection = URL("http://localhost:8080/api/hydro")
                                    .openConnection() as java.net.HttpURLConnection

                                connection.requestMethod = "POST"
                                connection.doOutput = true
                                connection.setRequestProperty("Content-Type", "application/json")

                                connection.outputStream.use {
                                    it.write(jsonBody.toByteArray(Charsets.UTF_8))
                                }

                                connection.inputStream.bufferedReader().readText()
                            }

                            "Izbran hidro zapis je shranjen v bazo."
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

@Composable
fun GeneratorScreen() {
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
    var generatedMeteoRecords by remember {
        mutableStateOf(emptyList<String>())
    }
    var generatedHydroRecords by remember {
        mutableStateOf(emptyList<String>())
    }
    var generatedAirQualityRecords by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectedGeneratorType by remember { mutableStateOf("meteo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Generator naključnih podatkov",
            style = MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nastavitve generiranja",
                    style = MaterialTheme.typography.h6
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
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Območja vrednosti",
                    style = MaterialTheme.typography.h6
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
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generiranje podatkov",
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val count = recordCount.toIntOrNull()

                        if (count == null || count <= 0) {
                            message = "Vnesi veljavno pozitivno število."
                            generatedMeteoRecords = emptyList()
                            generatedHydroRecords = emptyList()
                            generatedAirQualityRecords = emptyList()
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

                                generatedMeteoRecords = emptyList()
                                generatedHydroRecords = emptyList()
                                generatedAirQualityRecords = emptyList()

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

                                generatedMeteoRecords = emptyList()
                                generatedHydroRecords = emptyList()
                                generatedAirQualityRecords = emptyList()

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

                                generatedMeteoRecords = emptyList()
                                generatedHydroRecords = emptyList()
                                generatedAirQualityRecords = emptyList()

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

        Spacer(modifier = Modifier.height(16.dp))

        if (generatedRows.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Predogled generiranih podatkov",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            DataTable(
                headers = generatedHeaders,
                rows = generatedRows
            )
        }
    }
}

@Composable
fun RangeInputRow(
    title: String,
    minValue: String,
    onMinChange: (String) -> Unit,
    maxValue: String,
    onMaxChange: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                label = { Text("Min") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = { Text("Max") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun GeneratedRecordCard(record: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Text(
            text = record,
            modifier = Modifier.padding(12.dp)
        )
    }
}