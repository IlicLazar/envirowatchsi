package org.envirowatchsi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun GeneratedDataTable(
    headers: List<String>,
    rows: List<List<String>>,
    selectedRows: Set<Int>,
    onSelectionChange: (Int, Boolean) -> Unit,
    onDeleteRow: (Int) -> Unit
) {
    EnviroPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EnviroColors.LeafSoft)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Shrani",
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = EnviroColors.ForestDark
            )

            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = EnviroColors.ForestDark
                )
            }

            Text(
                text = "Akcija",
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = EnviroColors.ForestDark
            )
        }

        HorizontalDivider(color = EnviroColors.Line)

        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else EnviroColors.Mist)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedRows.contains(index),
                    onCheckedChange = { checked ->
                        onSelectionChange(index, checked)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                )

                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = {
                        onDeleteRow(index)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text("Odstrani")
                }
            }

            HorizontalDivider(color = EnviroColors.Line.copy(alpha = 0.75f))
        }
    }
}
