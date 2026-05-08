package com.envirowatchsi

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
                    Screen.AIR_QUALITY -> PlaceholderScreen("Kakovost zraka")
                    Screen.METEO -> PlaceholderScreen("Meteorološki podatki")
                    Screen.HYDRO -> PlaceholderScreen("Hidrološki podatki")
                    Screen.DATABASE -> DatabaseScreen()
                    Screen.GENERATOR -> PlaceholderScreen("Generator namišljenih podatkov")
                    Screen.DATA_ENTRY -> DataEntryScreen()
                    Screen.UPDATE -> UpdateDataScreen()
                    Screen.DELETE -> DeleteDataScreen()
                }
            }
        }
    }
}
@Composable
fun ContentArea(selectedScreen: Screen) {
    ContentArea(selectedScreen)
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
            Button(onClick = { selectedTable = "air-quality" }) {
                Text("Kakovost zraka")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedTable = "meteo" }) {
                Text("Meteo")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { selectedTable = "hydro" }) {
                Text("Hidro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    message = "Pridobivanje podatkov..."

                    try {
                        val response = withContext(Dispatchers.IO) {
                            URL("http://localhost:8080/api/$selectedTable")
                                .openStream()
                                .bufferedReader(Charsets.UTF_8)
                                .readText()
                        }

                        val parsedRows = parseRowsForTable(selectedTable, response)

                        headers = headersForTable(selectedTable)
                        rows = parsedRows

                        message = if (parsedRows.isEmpty()) {
                            "Ni zapisov v izbrani tabeli."
                        } else {
                            "Podatki so uspešno naloženi."
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(message)

        Spacer(modifier = Modifier.height(16.dp))

        DataTable(
            headers = headers,
            rows = rows
        )
    }
}

fun headersForTable(table: String): List<String> {
    return when (table) {
        "air-quality" -> listOf("ID", "Postaja", "AQI")
        "meteo" -> listOf("ID", "Postaja", "Temp.", "Vlažnost")
        "hydro" -> listOf("ID", "Postaja", "Reka", "Vodostaj", "Pretok")
        else -> listOf("ID", "Postaja")
    }
}

fun parseRowsForTable(table: String, response: String): List<List<String>> {
    if (response == "[]") return emptyList()

    val gson = com.google.gson.Gson()
    val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
    val records: List<Map<String, Any?>> = gson.fromJson(response, listType)

    return records.map { record ->
        when (table) {
            "air-quality" -> listOf(
                record["id"].toString(),
                record["stationName"].toString(),
                record["airQualityIndex"].toString()
            )

            "meteo" -> listOf(
                record["id"].toString(),
                record["stationName"].toString(),
                record["temperature"].toString(),
                record["humidity"].toString()
            )

            "hydro" -> listOf(
                record["id"].toString(),
                record["stationName"].toString(),
                record["riverName"].toString(),
                record["waterLevel"].toString(),
                record["waterFlow"].toString()
            )

            else -> emptyList()
        }
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
                        val postData: String

                        when (selectedType) {
                            EntryType.AIR_QUALITY -> {
                                val aqiValue = aqi.toDoubleOrNull()

                                if (aqiValue == null) {
                                    message = "AQI mora biti številka."
                                    return@launch
                                }

                                endpoint = "air-quality"
                                postData =
                                    "stationName=$stationName" +
                                            "&latitude=$latitude" +
                                            "&longitude=$longitude" +
                                            "&aqi=${aqiValue.toInt()}"
                            }

                            EntryType.METEO -> {
                                val temperatureValue = temperature.toDoubleOrNull()
                                val humidityValue = humidity.toDoubleOrNull()

                                if (temperatureValue == null || humidityValue == null) {
                                    message = "Temperature in humidity morata biti številki."
                                    return@launch
                                }

                                endpoint = "meteo"
                                postData =
                                    "stationName=$stationName" +
                                            "&latitude=$latitude" +
                                            "&longitude=$longitude" +
                                            "&temperature=$temperature" +
                                            "&humidity=$humidity" +
                                            "&windSpeed=$windSpeed" +
                                            "&precipitation=$precipitation"
                            }

                            EntryType.HYDRO -> {
                                if (riverName.isBlank()) {
                                    message = "Ime reke je obvezno."
                                    return@launch
                                }

                                endpoint = "hydro"
                                postData =
                                    "stationName=$stationName" +
                                            "&riverName=$riverName" +
                                            "&latitude=$latitude" +
                                            "&longitude=$longitude" +
                                            "&waterLevel=$waterLevel" +
                                            "&waterFlow=$waterFlow"
                            }
                        }

                        withContext(Dispatchers.IO) {
                            URL("http://localhost:8080/api/$endpoint")
                                .openConnection()
                                .apply {
                                    doOutput = true
                                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                                    outputStream.use { it.write(postData.toByteArray()) }
                                }
                                .getInputStream()
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
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(1f)
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
                            .padding(8.dp)
                    )
                }
            }

            Divider()
        }
    }
}