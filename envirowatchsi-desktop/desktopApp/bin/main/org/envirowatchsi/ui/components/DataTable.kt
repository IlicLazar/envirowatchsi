package org.envirowatchsi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(1f)
//                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    style = MaterialTheme.typography.titleSmall
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
                            .padding(8.dp),
                        style = when {
                            cell.contains("null") -> MaterialTheme.typography.titleSmall
                            else -> MaterialTheme.typography.bodySmall
                        }
                    )
                }
            }

            Divider()
        }
    }
}