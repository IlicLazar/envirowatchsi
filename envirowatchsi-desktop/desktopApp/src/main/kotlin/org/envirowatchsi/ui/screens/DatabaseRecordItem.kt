package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class DatabaseRecordItem(
    val id: String,
    val stationName: String,
    val detail: String,
    val raw: JsonObject
)

fun parseDatabaseRecords(json: String, table: String): List<DatabaseRecordItem> {
    val root = JsonParser.parseString(json)

    if (!root.isJsonArray) {
        return emptyList()
    }

    return root.asJsonArray.mapNotNull { element ->
        val obj = element.asJsonObject
        val id = obj.stringValue("_id")

        if (id.isBlank()) {
            null
        } else {
            DatabaseRecordItem(
                id = id,
                stationName = obj.stringValue("stationName").ifBlank { "Brez imena postaje" },
                detail = obj.recordDetail(table),
                raw = obj
            )
        }
    }
}

fun JsonObject.stringValue(name: String): String {
    val value = get(name) ?: return ""
    return if (value.isJsonNull) "" else value.asString
}

private fun JsonObject.recordDetail(table: String): String {
    return when (table) {
        "air-quality" -> "AQI: ${stringValue("airQualityIndex").ifBlank { "-" }} | PM10: ${stringValue("pm10").ifBlank { "-" }}"
        "meteo" -> "Temperatura: ${stringValue("temperature").ifBlank { "-" }} | Vlažnost: ${stringValue("humidity").ifBlank { "-" }}"
        "hydro" -> "Reka: ${stringValue("riverName").ifBlank { "-" }} | Vodostaj: ${stringValue("waterLevel").ifBlank { "-" }}"
        else -> ""
    }
}

@Composable
fun TableSelector(
    selectedTable: String,
    onSelected: (String) -> Unit
) {
    Row {
        TableButton("Kakovost zraka", "air-quality", selectedTable, onSelected)
        Spacer(modifier = Modifier.width(8.dp))
        TableButton("Meteo", "meteo", selectedTable, onSelected)
        Spacer(modifier = Modifier.width(8.dp))
        TableButton("Hydro", "hydro", selectedTable, onSelected)
    }
}

@Composable
private fun TableButton(
    text: String,
    table: String,
    selectedTable: String,
    onSelected: (String) -> Unit
) {
    if (table == selectedTable) {
        Button(onClick = { onSelected(table) }) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = { onSelected(table) }) {
            Text(text)
        }
    }
}

@Composable
fun RecordsTable(
    records: List<DatabaseRecordItem>,
    selectedId: String?,
    actionText: String,
    onRecordSelected: (DatabaseRecordItem) -> Unit
) {
    if (records.isEmpty()) {
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Postaja", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                Text("Podatki", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                Text("Akcija", modifier = Modifier.width(120.dp), style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            records.forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(record.stationName, modifier = Modifier.weight(2f))
                    Text(record.detail, modifier = Modifier.weight(2f))

                    if (record.id == selectedId) {
                        Button(
                            onClick = { onRecordSelected(record) },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text("Izbran")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onRecordSelected(record) },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(actionText)
                        }
                    }
                }

                HorizontalDivider()
            }
        }
    }
}
