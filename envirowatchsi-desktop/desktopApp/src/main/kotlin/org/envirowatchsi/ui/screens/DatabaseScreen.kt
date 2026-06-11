package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.ui.components.DataTable
import org.envirowatchsi.ui.components.EnviroPanel
import org.envirowatchsi.ui.components.SortDropdown
import org.envirowatchsi.ui.components.StatusText
import java.text.Collator
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DatabaseScreen() {
    val scope = rememberCoroutineScope()

    var selectedTable by remember { mutableStateOf("air-quality") }
    var selectedValueMetric by remember { mutableStateOf(defaultValueMetricForTable(selectedTable)) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev podatkov.") }

    var headers by remember { mutableStateOf(headersForTable(selectedTable)) }
    var rows by remember { mutableStateOf(emptyList<List<String>>()) }

    var stationFilter by remember { mutableStateOf("")}

    var minValueFilter by remember { mutableStateOf("")}
    var maxValueFilter by remember { mutableStateOf("")}

    var sortMode by remember { mutableStateOf("station") }
    var sortAscending by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf(DatabasePeriod.LAST_7_DAYS) }
    val stationCollator = remember {
        Collator.getInstance(Locale.forLanguageTag("sl-SI")).apply {
            decomposition = Collator.CANONICAL_DECOMPOSITION
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Podatkovna baza",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        EnviroPanel(modifier = Modifier.fillMaxWidth(), title = "Izbor in filtriranje") {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val useTwoColumns = maxWidth >= 900.dp
                val leftContent: @Composable ColumnScope.() -> Unit = {
                    TableSelector(
                        selectedTable = selectedTable,
                        onSelected = {
                            selectedTable = it
                            selectedValueMetric = defaultValueMetricForTable(it)
                            headers = headersForTable(it)
                            stationFilter = ""
                            minValueFilter = ""
                            maxValueFilter = ""
                            rows = emptyList()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Obdobje prikaza",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        DatabasePeriod.entries.forEachIndexed { index, period ->
                            PeriodButton(
                                period = period,
                                selectedPeriod = selectedPeriod,
                                onSelected = {
                                    selectedPeriod = it
                                    rows = emptyList()
                                    message = "Klikni gumb za pridobitev podatkov."
                                }
                            )

                            if (index < DatabasePeriod.entries.lastIndex) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.widthIn(min = 180.dp),
                        onClick = {
                            scope.launch {
                                message = "Pridobivanje podatkov..."

                                try {
                                    val response = fetchDatabaseRecords(selectedTable, selectedPeriod)

                                    val parsedRows = parseRowsForTable(selectedTable, response)

                                    headers = headersForTable(selectedTable)
                                    rows = parsedRows

                                    message = if (parsedRows.isEmpty()) {
                                        "Ni zapisov v izbrani tabeli."
                                    } else {
                                        "Prikazanih zapisov: ${parsedRows.size}"
                                    }
                                } catch (e: Exception) {
                                    rows = emptyList()
                                    message = "Napaka pri pridobivanju podatkov: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text("Prikaži zapise")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    StatusText(message)
                }

                val rightContent: @Composable ColumnScope.() -> Unit = {
                    OutlinedTextField(
                        value = stationFilter,
                        onValueChange = { stationFilter = it },
                        label = { Text("Filtriraj po merilni postaji") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sortiranje",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        SortDropdown(
                            label = "Sortiraj po",
                            selectedText = when (sortMode) {
                                "station" -> "Postaja"
                                "date" -> "Datum meritve"
                                "value" -> "Izbrana vrednost"
                                else -> "Postaja"
                            },
                            options = listOf("Postaja", "Datum meritve", "Izbrana vrednost"),
                            onOptionSelected = { selected ->
                                sortMode = when (selected) {
                                    "Postaja" -> "station"
                                    "Datum meritve" -> "date"
                                    "Izbrana vrednost" -> "value"
                                    else -> "station"
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        SortDropdown(
                            label = "Smer",
                            selectedText = when (sortMode) {
                                "station" -> if (sortAscending) "A-Z" else "Z-A"
                                "date" -> if (sortAscending) "Najstarejši" else "Najnoviji"
                                "value" -> if (sortAscending) "Najmanjša" else "Največja"
                                else -> "A-Z"
                            },
                            options = when (sortMode) {
                                "station" -> listOf("A-Z", "Z-A")
                                "date" -> listOf("Najstarejši", "Najnoviji")
                                "value" -> listOf("Najmanjša", "Največja")
                                else -> listOf("A-Z", "Z-A")
                            },
                            onOptionSelected = { selected ->
                                sortAscending = when (selected) {
                                    "A-Z", "Najstarejši", "Najmanjša" -> true
                                    else -> false
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SortDropdown(
                        label = "Vrednost za filter",
                        selectedText = selectedValueMetric.label,
                        options = valueMetricsForTable(selectedTable).map { it.label },
                        onOptionSelected = { selected ->
                            selectedValueMetric = valueMetricsForTable(selectedTable)
                                .firstOrNull { it.label == selected }
                                ?: defaultValueMetricForTable(selectedTable)
                            minValueFilter = ""
                            maxValueFilter = ""
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        OutlinedTextField(
                            value = minValueFilter,
                            onValueChange = { minValueFilter = it },
                            label = { Text("Minimalna vrednost (${selectedValueMetric.label})") },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = maxValueFilter,
                            onValueChange = { maxValueFilter = it },
                            label = { Text("Maksimalna vrednost (${selectedValueMetric.label})") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (useTwoColumns) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(0.9f), content = leftContent)
                        Spacer(modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1.25f), content = rightContent)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        leftContent()
                        Spacer(modifier = Modifier.height(16.dp))
                        rightContent()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val minValue = minValueFilter.toDoubleOrNull()
        val maxValue = maxValueFilter.toDoubleOrNull()

        val stationNameColumnIndex = 0
        val measuredAtColumnIndex = 1
        val numericValueColumnIndex =
            headers.indexOf(selectedValueMetric.columnHeader).takeIf { it >= 0 } ?: headers.lastIndex

        val filteredRows = rows
            .filter { row ->
                val stationMatches =
                    stationFilter.isBlank() || row.getOrNull(stationNameColumnIndex)
                        ?.contains(
                            stationFilter,
                            ignoreCase = true
                        ) == true

                val numericValue = row.getOrNull(numericValueColumnIndex)?.toDoubleOrNull()

                val minMatches = minValue == null || (numericValue != null && numericValue >= minValue)
                val maxMatches = maxValue == null || (numericValue != null && numericValue <= maxValue)
                stationMatches && minMatches && maxMatches
            }
            .let { filteredList ->
                val sortedRows =
                    when (sortMode) {
                        "date" -> filteredList.sortedBy {
                            it.getOrNull(measuredAtColumnIndex)
                        }
                        "value" -> filteredList.sortedBy {
                            it.getOrNull(numericValueColumnIndex)
                                ?.toDoubleOrNull()
                        }
                        "station" -> filteredList.sortedBy {
                            stationCollator.getCollationKey(it.getOrNull(stationNameColumnIndex) ?: "")
                        }
                        else -> filteredList
                    }
                if (sortAscending) {
                    sortedRows
                } else {
                    sortedRows.reversed()
                }
            }
        DataTable(
            headers = headers,
            rows = filteredRows
        )
    }
}

suspend fun fetchDatabaseRecords(table: String, period: DatabasePeriod): String {
    return withContext(Dispatchers.IO) {
        ApiClient.getRaw("/api/$table${period.queryString()}")
    }
}

enum class DatabasePeriod(
    val label: String,
    private val duration: Duration?
) {
    LAST_HOUR("Zadnja ura", Duration.ofHours(1)),
    LAST_7_DAYS("Zadnjih 7 dni", Duration.ofDays(7)),
    ALL("Vse", null);

    fun queryString(): String {
        val filterDuration = duration ?: return ""
        val startDate = Instant.now().minus(filterDuration).toString()
        return "?startDate=$startDate"
    }
}

@Composable
fun PeriodButton(
    period: DatabasePeriod,
    selectedPeriod: DatabasePeriod,
    onSelected: (DatabasePeriod) -> Unit
) {
    if (period == selectedPeriod) {
        Button(onClick = { onSelected(period) }) {
            Text(period.label)
        }
    } else {
        OutlinedButton(onClick = { onSelected(period) }) {
            Text(period.label)
        }
    }
}

fun headersForTable(table: String): List<String> {
    return when (table) {
        "air-quality" -> listOf(
            "Postaja",
            "Datum meritve",
            "Lat",
            "Lon",
            "PM10",
            "PM2.5",
            "O3",
            "CO",
            "SO2",
            "AQI"
        )
        "meteo" -> listOf(
            "Postaja",
            "Datum meritve",
            "Lat",
            "Lon",
            "Temp.",
            "Vlažnost",
            "Veter",
            "Smer vetra",
            "Padavine"
        )
        "hydro" -> listOf(
            "Postaja",
            "Datum meritve",
            "Lat",
            "Lon",
            "Reka",
            "Vodostaj",
            "Pretok"
        )
        else -> listOf("Postaja", "Datum meritve")
    }
}

private data class DatabaseValueMetric(
    val label: String,
    val columnHeader: String
)

private fun defaultValueMetricForTable(table: String): DatabaseValueMetric {
    return valueMetricsForTable(table).first()
}

private fun valueMetricsForTable(table: String): List<DatabaseValueMetric> {
    return when (table) {
        "air-quality" -> listOf(
            DatabaseValueMetric("AQI", "AQI"),
            DatabaseValueMetric("PM10", "PM10"),
            DatabaseValueMetric("PM2.5", "PM2.5"),
            DatabaseValueMetric("O3", "O3"),
            DatabaseValueMetric("CO", "CO"),
            DatabaseValueMetric("SO2", "SO2")
        )

        "meteo" -> listOf(
            DatabaseValueMetric("Temperatura", "Temp."),
            DatabaseValueMetric("Vlažnost", "Vlažnost"),
            DatabaseValueMetric("Hitrost vetra", "Veter"),
            DatabaseValueMetric("Padavine", "Padavine")
        )

        "hydro" -> listOf(
            DatabaseValueMetric("Vodostaj", "Vodostaj"),
            DatabaseValueMetric("Pretok", "Pretok")
        )

        else -> listOf(DatabaseValueMetric("Vrednost", "Vrednost"))
    }
}

fun parseRowsForTable(table: String, response: String): List<List<String>> {
    if (response.isBlank() || response == "[]") return emptyList()

    return try {
        val gson = com.google.gson.Gson()
        val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
        val records: List<Map<String, Any?>> = gson.fromJson(response, listType)

        records.map { record ->
            when (table) {
                "air-quality" -> listOf(
                    record.displayValue("stationName"),
                    record.displayDate("measuredAt"),
                    record.displayValue("latitude"),
                    record.displayValue("longitude"),
                    record.displayValue("pm10"),
                    record.displayValue("pm2_5"),
                    record.displayValue("o3"),
                    record.displayValue("co"),
                    record.displayValue("so2"),
                    record.displayValue("airQualityIndex")
                )

                "meteo" -> listOf(
                    record.displayValue("stationName"),
                    record.displayDate("measuredAt"),
                    record.displayValue("latitude"),
                    record.displayValue("longitude"),
                    record.displayValue("temperature"),
                    record.displayValue("humidity"),
                    record.displayValue("windSpeed"),
                    record.displayValue("windDirection"),
                    record.displayValue("precipitation")
                )

                "hydro" -> listOf(
                    record.displayValue("stationName"),
                    record.displayDate("measuredAt"),
                    record.displayValue("latitude"),
                    record.displayValue("longitude"),
                    record.displayValue("riverName"),
                    record.displayValue("waterLevel"),
                    record.displayValue("waterFlow")
                )
                else -> emptyList()
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun Map<String, Any?>.displayValue(field: String): String {
    val value = this[field] ?: return "-"
    val text = value.toString().trim()
    return if (text.isBlank() || text == "null") "-" else text
}

val tableDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

fun formatDisplayDate(value: String): String {
    if (value.isBlank() || value == "-") return "-"

    return try {
        tableDateFormatter.format(Instant.parse(value))
    } catch (e: Exception) {
        value
    }
}

private fun Map<String, Any?>.displayDate(field: String): String {
    val value = displayValue(field)
    return formatDisplayDate(value)
}
