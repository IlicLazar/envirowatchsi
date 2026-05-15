package org.envirowatchsi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import org.envirowatchsi.ui.components.Sidebar
import org.envirowatchsi.ui.screens.DashboardScreen
import org.envirowatchsi.ui.screens.DatabaseScreen
import org.envirowatchsi.ui.screens.DataEntryScreen
import org.envirowatchsi.ui.screens.UpdateDataScreen
import org.envirowatchsi.ui.screens.DeleteDataScreen
import org.envirowatchsi.ui.screens.MeteoScreen
import org.envirowatchsi.ui.screens.AirQualityScreen
import org.envirowatchsi.ui.screens.HydroScreen
import org.envirowatchsi.ui.screens.GeneratorScreen
@Composable
@Preview
fun App() {
    var selectedScreen by remember{ mutableStateOf(Screen.DASHBOARD) }
    MaterialTheme{
        Row(
            modifier = Modifier
                .fillMaxSize()
//                .background(MaterialTheme.colors.background)
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