package org.envirowatchsi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GeneratedDataTable(
    headers: List<String>,
    rows: List<List<String>>,
    selectedRows: Set<Int>,
    onSelectionChange: (Int, Boolean) -> Unit,
    onDeleteRow: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Shrani",
                modifier = Modifier
                    .weight(1f)
//                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                style = MaterialTheme.typography.titleSmall
            )

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

            Text(
                text = "Akcija",
                modifier = Modifier
                    .weight(1f)
//                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                style = MaterialTheme.typography.titleSmall
            )
        }

        Divider()

        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth()
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

                Button(
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

            Divider()
        }
    }
}