package org.envirowatchsi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.ui.components.Sidebar
import org.envirowatchsi.ui.screens.LoginScreen
import org.envirowatchsi.ui.screens.DatabaseScreen
import org.envirowatchsi.ui.screens.DataEntryScreen
import org.envirowatchsi.ui.screens.UpdateDataScreen
import org.envirowatchsi.ui.screens.DeleteDataScreen
import org.envirowatchsi.ui.screens.MeteoScreen
import org.envirowatchsi.ui.screens.AirQualityScreen
import org.envirowatchsi.ui.screens.HydroScreen
import org.envirowatchsi.ui.screens.GeneratorScreen
import org.envirowatchsi.ui.theme.EnviroWatchTheme

@Composable
@Preview
fun App() {
    var selectedScreen by remember{ mutableStateOf(Screen.LOGIN) }
    var loggedInUser by remember { mutableStateOf<String?>(null) }
    val isAdmin = !loggedInUser.isNullOrBlank()
    EnviroWatchTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ){
            Sidebar(
                selectedScreen = selectedScreen,
                loggedInUser = loggedInUser,
                onScreenSelected = {selectedScreen = it},
                onLogout = {
                    ApiClient.logout()
                    loggedInUser = null
                    selectedScreen = Screen.LOGIN
                }
            )
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                contentAlignment = Alignment.TopCenter
            ){
                Box(
                    modifier = Modifier
                        .widthIn(max = 1280.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    when(selectedScreen){
                        Screen.LOGIN -> LoginScreen(
                            onLoginSuccess = { result ->
                                loggedInUser = result.email ?: result.username ?: "Prijavljen uporabnik"
                                selectedScreen = Screen.DATA_ENTRY
                            }
                        )
                    Screen.AIR_QUALITY -> AirQualityScreen(canManageData = isAdmin)
                    Screen.METEO -> MeteoScreen(canManageData = isAdmin)
                    Screen.HYDRO -> HydroScreen(canManageData = isAdmin)
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
}
