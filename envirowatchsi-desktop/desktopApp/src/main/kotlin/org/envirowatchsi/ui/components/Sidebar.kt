package org.envirowatchsi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.Screen
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun Sidebar(
    selectedScreen: Screen,
    loggedInUser: String?,
    onScreenSelected: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(268.dp)
            .fillMaxHeight()
            .background(EnviroColors.Sidebar)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        BrandHeader()

        Spacer(modifier = Modifier.height(24.dp))

        SidebarSectionTitle("Dostop")

        Spacer(modifier = Modifier.height(8.dp))

        if (loggedInUser.isNullOrBlank()) {
            SidebarButton("Prijava", Screen.LOGIN, selectedScreen, onScreenSelected)
        } else {
            SidebarAction(
                text = "Odjava",
                onClick = onLogout,
                danger = false
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        SidebarSectionTitle("Meritve")
        Spacer(modifier = Modifier.height(8.dp))

        SidebarButton("Kakovost zraka", Screen.AIR_QUALITY, selectedScreen, onScreenSelected)
        SidebarButton("Meteorološki podatki", Screen.METEO, selectedScreen, onScreenSelected)
        SidebarButton("Hidrološki podatki", Screen.HYDRO, selectedScreen, onScreenSelected)

        Spacer(modifier = Modifier.height(18.dp))
        SidebarSectionTitle("Podatki")
        Spacer(modifier = Modifier.height(8.dp))

        SidebarButton("Podatkovna baza", Screen.DATABASE, selectedScreen, onScreenSelected)

        if (!loggedInUser.isNullOrBlank()) {
            SidebarButton("Generator podatkov", Screen.GENERATOR, selectedScreen, onScreenSelected)
            SidebarButton("Vnos podatkov", Screen.DATA_ENTRY, selectedScreen, onScreenSelected)
            SidebarButton("Posodabljanje podatkov", Screen.UPDATE, selectedScreen, onScreenSelected)
            SidebarButton("Brisanje podatkov", Screen.DELETE, selectedScreen, onScreenSelected)
        }
        Spacer(modifier = Modifier.weight(1f))

        if (!loggedInUser.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.08f),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Prijavljen uporabnik",
                        style = MaterialTheme.typography.labelMedium,
                        color = EnviroColors.Sky.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = loggedInUser,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

    }
}

@Composable
private fun BrandHeader() {
    Text(
        text = "EnviroWatch SI",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
private fun SidebarSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = EnviroColors.Sun.copy(alpha = 0.9f)
    )
}

@Composable
fun SidebarButton(
    text: String,
    screen: Screen,
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    val selected = screen == selectedScreen
    val background = if (selected) EnviroColors.SidebarSelected else Color.Transparent
    val textColor = if (selected) Color.White else EnviroColors.Sky.copy(alpha = 0.86f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { onScreenSelected(screen) },
        color = background,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (selected) EnviroColors.Sun else EnviroColors.Water.copy(alpha = 0.55f))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun SidebarAction(
    text: String,
    onClick: () -> Unit,
    danger: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (danger) MaterialTheme.colorScheme.error else EnviroColors.Sky
        )
    ) {
        Text(text)
    }
}
