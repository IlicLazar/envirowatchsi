package org.envirowatchsi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.Screen

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
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Navigacija",
            style = MaterialTheme.typography.titleSmall
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
            style = MaterialTheme.typography.bodySmall
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
//            backgroundColor = if (screen == selectedScreen)
//                MaterialTheme.colors.primary
//            else
//                MaterialTheme.colors.surface
        )
    ){
        Text(text)
    }
}