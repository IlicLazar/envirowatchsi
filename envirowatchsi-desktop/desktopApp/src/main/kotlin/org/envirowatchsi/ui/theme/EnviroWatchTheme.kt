package org.envirowatchsi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object EnviroColors {
    val Forest = Color(0xFF2F8A24)
    val ForestDark = Color(0xFF1E6318)
    val Water = Color(0xFF0A84C7)
    val WaterDark = Color(0xFF073246)
    val Sky = Color(0xFFE7F5FB)
    val Mist = Color(0xFFF4F8F6)
    val LeafSoft = Color(0xFFEAF6E7)
    val Sun = Color(0xFFF5B722)
    val Ink = Color(0xFF12313F)
    val Muted = Color(0xFF5E717A)
    val Line = Color(0xFFD8E6E1)
    val Panel = Color(0xFFFFFFFF)
    val Sidebar = Color(0xFF082C3A)
    val SidebarSelected = Color(0xFF104D58)
}

private val EnviroColorScheme = lightColorScheme(
    primary = EnviroColors.Forest,
    onPrimary = Color.White,
    primaryContainer = EnviroColors.LeafSoft,
    onPrimaryContainer = EnviroColors.ForestDark,
    secondary = EnviroColors.Water,
    onSecondary = Color.White,
    secondaryContainer = EnviroColors.Sky,
    onSecondaryContainer = EnviroColors.WaterDark,
    tertiary = EnviroColors.Sun,
    onTertiary = EnviroColors.Ink,
    background = EnviroColors.Mist,
    onBackground = EnviroColors.Ink,
    surface = EnviroColors.Panel,
    onSurface = EnviroColors.Ink,
    surfaceVariant = EnviroColors.LeafSoft,
    onSurfaceVariant = EnviroColors.Muted,
    outline = EnviroColors.Line,
    error = Color(0xFFB42318),
    errorContainer = Color(0xFFFFE5E1),
    onErrorContainer = Color(0xFF7A1B13)
)

private val EnviroShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun EnviroWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EnviroColorScheme,
        shapes = EnviroShapes,
        content = content
    )
}

val EnviroCardColors
    @Composable
    get() = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
