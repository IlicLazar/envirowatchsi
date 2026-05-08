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

enum class Screen {
    DASHBOARD,
    AIR_QUALITY,
    METEO,
    HYDRO,
    DATABASE,
    GENERATOR,
    DATA_ENTRY
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
    var recordsText by remember { mutableStateOf("Klikni gumb za pridobitev podatkov.") }

    Column {
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
                    recordsText = "Pridobivanje podatkov..."

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
                        "Napaka pri pridobivanju podatkov: ${e.message}"
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

        Text(recordsText)
    }
}
@Composable
fun DataEntryScreen() {

    var stationName by remember { mutableStateOf("") }
    var aqi by remember { mutableStateOf("") }

    Column {

        Text(
            text = "Vnos podatkov",
            style = MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Ime postaje") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = aqi,
            onValueChange = { aqi = it },
            label = { Text("AQI vrednost") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                println("Shrani podatke")
            }
        ) {
            Text("Shrani")
        }
    }
}