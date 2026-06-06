package org.envirowatchsi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun InlineRecordDetails(
    title: String,
    values: List<Pair<String, String>>,
    saveButtonText: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    showActionButton: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EnviroColors.LeafSoft.copy(alpha = 0.72f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, EnviroColors.Line)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = EnviroColors.Ink
            )

            Spacer(modifier = Modifier.height(10.dp))

            values.chunked(2).forEach { rowValues ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowValues.forEach { (label, value) ->
                        DetailValue(
                            label = label,
                            value = value,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowValues.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showActionButton) {
                Spacer(modifier = Modifier.height(6.dp))

                Button(onClick = onSave) {
                    Text(saveButtonText)
                }
            }
        }
    }
}

@Composable
private fun DetailValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = EnviroColors.Muted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = EnviroColors.Ink
        )
    }
}

fun readableValue(value: Any?): String {
    val text = value?.toString()?.trim().orEmpty()
    return if (text.isBlank() || text == "null") "-" else text
}
