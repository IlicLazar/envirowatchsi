package org.envirowatchsi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun EnviroPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EnviroColors.Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = EnviroColors.Ink
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            content()
        }
    }
}

@Composable
fun StatusText(
    text: String,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return

    val displayText = displayStatusText(text)
    val type = statusTypeFor(displayText)
    val colors = statusColors(type)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.container,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.dot)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (type == StatusType.ERROR) FontWeight.SemiBold else FontWeight.Normal,
                color = colors.content
            )
        }
    }
}

private fun displayStatusText(text: String): String {
    val normalized = text.lowercase()
    return when {
        normalized.contains("administratorski token manjka") ||
            normalized.contains("admin token") ||
            normalized.contains("jwt token") -> "Najprej se prijavi kot admin."
        else -> text
    }
}

private enum class StatusType {
    ERROR,
    SUCCESS,
    NEUTRAL
}

private data class StatusColors(
    val container: Color,
    val border: Color,
    val dot: Color,
    val content: Color
)

private fun statusTypeFor(text: String): StatusType {
    val normalized = text.trim().lowercase()
    return when {
        normalized.startsWith("napaka") ||
            normalized.contains("manjka") ||
            normalized.contains("prijavi kot admin") ||
            normalized.contains("ni uspela") -> StatusType.ERROR
        normalized.contains("uspe") ||
            normalized.contains("shranjen") ||
            normalized.contains("prikazanih") ||
            normalized.contains("pridobljenih") -> StatusType.SUCCESS
        else -> StatusType.NEUTRAL
    }
}

@Composable
private fun statusColors(type: StatusType): StatusColors {
    return when (type) {
        StatusType.ERROR -> StatusColors(
            container = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
            border = MaterialTheme.colorScheme.error.copy(alpha = 0.32f),
            dot = MaterialTheme.colorScheme.error,
            content = MaterialTheme.colorScheme.onErrorContainer
        )
        StatusType.SUCCESS -> StatusColors(
            container = EnviroColors.LeafSoft,
            border = EnviroColors.Forest.copy(alpha = 0.25f),
            dot = EnviroColors.Forest,
            content = EnviroColors.ForestDark
        )
        StatusType.NEUTRAL -> StatusColors(
            container = EnviroColors.Sky,
            border = EnviroColors.Water.copy(alpha = 0.22f),
            dot = EnviroColors.Water,
            content = EnviroColors.WaterDark
        )
    }
}
