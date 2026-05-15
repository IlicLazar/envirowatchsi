package org.envirowatchsi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RangeInputRow(
    title: String,
    minValue: String,
    onMinChange: (String) -> Unit,
    maxValue: String,
    onMaxChange: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                label = { Text("Min") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = { Text("Max") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}