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

enum class Screen {
    DASHBOARD,
    AIR_QUALITY,
    METEO,
    HYDRO,
    DATABASE,
    GENERATOR
}

fun main() = application {
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
                    Screen.DATABASE -> PlaceholderScreen("Upravljanje podatkovne baze")
                    Screen.GENERATOR -> PlaceholderScreen("Generator namišljenih podatkov")
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

        Text("Ta zaslon predstavlja osnovno postavitev za nadaljnjo implementacijo funkcionalnosti.")
    }
}
