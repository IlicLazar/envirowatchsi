package org.envirowatchsi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    val cellWidth = 148.dp

    EnviroPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .background(EnviroColors.LeafSoft)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier
                            .width(cellWidth)
                            .padding(8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = EnviroColors.ForestDark
                    )
                }
            }

            HorizontalDivider(color = EnviroColors.Line)

            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else EnviroColors.Mist)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            modifier = Modifier
                                .width(cellWidth)
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (cell == "-") EnviroColors.Muted else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = EnviroColors.Line.copy(alpha = 0.75f))
            }
        }
    }
}
