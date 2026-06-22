package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
fun DatabaseScreen(canManageData: Boolean) {
    val scope = rememberCoroutineScope()

    var selectedTable by remember { mutableStateOf("air-quality") }
    var selectedValueMetric by remember { mutableStateOf(defaultValueMetricForTable(selectedTable)) }
    var message by remember { mutableStateOf("Klikni gumb za pridobitev podatkov.") }

    var headers by remember { mutableStateOf(headersForTable(selectedTable)) }
    var records by remember { mutableStateOf(emptyList<DatabaseRecordItem>()) }

    var stationFilter by remember { mutableStateOf("")}

    var minValueFilter by remember { mutableStateOf("")}
    var maxValueFilter by remember { mutableStateOf("")}

    var sortMode by remember { mutableStateOf("station") }
    var sortAscending by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf(DatabasePeriod.LAST_7_DAYS) }
    var selectedActionRecord by remember { mutableStateOf<DatabaseRecordItem?>(null) }
    var selectedAction by remember { mutableStateOf<DatabaseRowAction?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var stationName by remember { mutableStateOf("") }
    var aqi by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var riverName by remember { mutableStateOf("") }
    var waterLevel by remember { mutableStateOf("") }
    var waterFlow by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var pm10 by remember { mutableStateOf("") }
    var pm25 by remember { mutableStateOf("") }
    var o3 by remember { mutableStateOf("") }
    var co by remember { mutableStateOf("") }
    var so2 by remember { mutableStateOf("") }
    var windSpeed by remember { mutableStateOf("") }
    var precipitation by remember { mutableStateOf("") }

    val stationCollator = remember {
        Collator.getInstance(Locale.forLanguageTag("sl-SI")).apply {
            decomposition = Collator.CANONICAL_DECOMPOSITION
        }
    }

    fun clearInlineAction() {
        selectedActionRecord = null
        selectedAction = null
        showDeleteConfirmation = false
        stationName = ""
        aqi = ""
        temperature = ""
        humidity = ""
        riverName = ""
        waterLevel = ""
        waterFlow = ""
        latitude = ""
        longitude = ""
        pm10 = ""
        pm25 = ""
        o3 = ""
        co = ""
        so2 = ""
        windSpeed = ""
        precipitation = ""
    }

    fun fillEditForm(record: DatabaseRecordItem) {
        selectedActionRecord = record
        selectedAction = DatabaseRowAction.EDIT
        showDeleteConfirmation = false
        stationName = record.raw.stringValue("stationName")
        latitude = record.raw.stringValue("latitude")
        longitude = record.raw.stringValue("longitude")
        pm10 = record.raw.stringValue("pm10")
        pm25 = record.raw.stringValue("pm2_5")
        o3 = record.raw.stringValue("o3")
        co = record.raw.stringValue("co")
        so2 = record.raw.stringValue("so2")
        aqi = record.raw.stringValue("airQualityIndex")
        temperature = record.raw.stringValue("temperature")
        humidity = record.raw.stringValue("humidity")
        windSpeed = record.raw.stringValue("windSpeed")
        precipitation = record.raw.stringValue("precipitation")
        riverName = record.raw.stringValue("riverName")
        waterLevel = record.raw.stringValue("waterLevel")
        waterFlow = record.raw.stringValue("waterFlow")
    }

    fun selectDelete(record: DatabaseRecordItem) {
        selectedActionRecord = record
        selectedAction = DatabaseRowAction.DELETE
        showDeleteConfirmation = false
    }

    fun saveInlineChanges(record: DatabaseRecordItem) {
        if (stationName.isBlank()) {
            message = "Ime postaje je obvezno."
            return
        }

        scope.launch {
            message = "Shranjujem spremembe..."

            message = try {
                val endpoint: String
                val jsonBody: String

                when (selectedTable) {
                    "air-quality" -> {
                        if (aqi.toDoubleOrNull() == null) {
                            message = "AQI mora biti številka."
                            return@launch
                        }

                        endpoint = "air-quality"
                        jsonBody = buildAirQualityJson(
                            stationName = stationName,
                            latitude = latitude,
                            longitude = longitude,
                            aqi = aqi,
                            pm10 = pm10,
                            pm2_5 = pm25,
                            o3 = o3,
                            co = co,
                            so2 = so2
                        )
                    }

                    "meteo" -> {
                        if (temperature.toDoubleOrNull() == null || humidity.toDoubleOrNull() == null) {
                            message = "Temperatura in vlažnost morata biti številki."
                            return@launch
                        }

                        endpoint = "meteo"
                        jsonBody = buildMeteoJson(
                            stationName = stationName,
                            latitude = latitude,
                            longitude = longitude,
                            temperature = temperature,
                            humidity = humidity,
                            windSpeed = windSpeed,
                            precipitation = precipitation
                        )
                    }

                    else -> {
                        if (riverName.isBlank()) {
                            message = "Ime reke je obvezno."
                            return@launch
                        }

                        endpoint = "hydro"
                        jsonBody = buildHydroJson(
                            stationName = stationName,
                            riverName = riverName,
                            latitude = latitude,
                            longitude = longitude,
                            waterLevel = waterLevel,
                            waterFlow = waterFlow
                        )
                    }
                }

                withContext(Dispatchers.IO) {
                    ApiClient.putForm("/api/$endpoint/${record.id}", jsonBody)
                }

                val response = withContext(Dispatchers.IO) {
                    fetchDatabaseRecords(selectedTable, selectedPeriod)
                }
                records = parseDatabaseRecords(response, selectedTable)
                clearInlineAction()

                "Spremembe so shranjene."
            } catch (e: Exception) {
                "Napaka pri shranjevanju sprememb: ${e.message}"
            }
        }
    }

    fun deleteInlineRecord(record: DatabaseRecordItem) {
        scope.launch {
            message = "Brišem zapis..."

            message = try {
                withContext(Dispatchers.IO) {
                    ApiClient.delete("/api/$selectedTable/${record.id}")
                }

                val response = withContext(Dispatchers.IO) {
                    fetchDatabaseRecords(selectedTable, selectedPeriod)
                }
                records = parseDatabaseRecords(response, selectedTable)
                clearInlineAction()

                "Zapis je uspešno izbrisan."
            } catch (e: Exception) {
                showDeleteConfirmation = false
                "Napaka pri brisanju: ${e.message}"
            }
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
                            records = emptyList()
                            clearInlineAction()
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
                                    records = emptyList()
                                    clearInlineAction()
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

                                    val parsedRecords = parseDatabaseRecords(response, selectedTable)

                                    headers = headersForTable(selectedTable)
                                    records = parsedRecords
                                    clearInlineAction()

                                    message = if (parsedRecords.isEmpty()) {
                                        "Ni zapisov v izbrani tabeli."
                                    } else {
                                        "Prikazanih zapisov: ${parsedRecords.size}"
                                    }
                                } catch (e: Exception) {
                                    records = emptyList()
                                    clearInlineAction()
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

        val tableRows = records.map { record ->
            DatabaseDisplayRow(
                cells = tableRowForRecord(selectedTable, record),
                record = record
            )
        }

        val filteredRows = tableRows
            .filter { tableRow ->
                val stationMatches =
                    stationFilter.isBlank() || tableRow.cells.getOrNull(stationNameColumnIndex)
                        ?.contains(
                            stationFilter,
                            ignoreCase = true
                        ) == true

                val numericValue = tableRow.cells.getOrNull(numericValueColumnIndex)?.toDoubleOrNull()

                val minMatches = minValue == null || (numericValue != null && numericValue >= minValue)
                val maxMatches = maxValue == null || (numericValue != null && numericValue <= maxValue)
                stationMatches && minMatches && maxMatches
            }
            .let { filteredList ->
                val sortedRows =
                    when (sortMode) {
                        "date" -> filteredList.sortedBy {
                            it.cells.getOrNull(measuredAtColumnIndex)
                        }
                        "value" -> filteredList.sortedBy {
                            it.cells.getOrNull(numericValueColumnIndex)
                                ?.toDoubleOrNull()
                        }
                        "station" -> filteredList.sortedBy {
                            stationCollator.getCollationKey(it.cells.getOrNull(stationNameColumnIndex) ?: "")
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
            rows = filteredRows.map { it.cells },
            rowActions = if (canManageData) {
                { index ->
                    val record = filteredRows[index].record
                    IconButton(
                        onClick = {
                            if (selectedActionRecord?.id == record.id && selectedAction == DatabaseRowAction.EDIT) {
                                clearInlineAction()
                            } else {
                                fillEditForm(record)
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = EditRecordIcon,
                            contentDescription = "Uredi zapis",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (selectedActionRecord?.id == record.id && selectedAction == DatabaseRowAction.DELETE) {
                                clearInlineAction()
                            } else {
                                selectDelete(record)
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = DeleteRecordIcon,
                            contentDescription = "Obriši zapis",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                null
            },
            expandedRowContent = if (canManageData) {
                { index ->
                    val record = filteredRows[index].record
                    if (selectedActionRecord?.id == record.id) {
                        when (selectedAction) {
                            DatabaseRowAction.EDIT -> InlineDatabaseEditForm(
                                selectedTable = selectedTable,
                                stationName = stationName,
                                onStationNameChange = { stationName = it },
                                latitude = latitude,
                                onLatitudeChange = { latitude = it },
                                longitude = longitude,
                                onLongitudeChange = { longitude = it },
                                pm10 = pm10,
                                onPm10Change = { pm10 = it },
                                pm25 = pm25,
                                onPm25Change = { pm25 = it },
                                o3 = o3,
                                onO3Change = { o3 = it },
                                co = co,
                                onCoChange = { co = it },
                                so2 = so2,
                                onSo2Change = { so2 = it },
                                aqi = aqi,
                                onAqiChange = { aqi = it },
                                temperature = temperature,
                                onTemperatureChange = { temperature = it },
                                humidity = humidity,
                                onHumidityChange = { humidity = it },
                                windSpeed = windSpeed,
                                onWindSpeedChange = { windSpeed = it },
                                precipitation = precipitation,
                                onPrecipitationChange = { precipitation = it },
                                riverName = riverName,
                                onRiverNameChange = { riverName = it },
                                waterLevel = waterLevel,
                                onWaterLevelChange = { waterLevel = it },
                                waterFlow = waterFlow,
                                onWaterFlowChange = { waterFlow = it },
                                onSave = { saveInlineChanges(record) },
                                onCancel = { clearInlineAction() }
                            )

                            DatabaseRowAction.DELETE -> InlineDatabaseDeleteConfirmation(
                                record = record,
                                showConfirmation = showDeleteConfirmation,
                                onShowConfirmation = { showDeleteConfirmation = true },
                                onCancel = { clearInlineAction() },
                                onDelete = { deleteInlineRecord(record) }
                            )

                            null -> Unit
                        }
                    }
                }
            } else {
                null
            }
        )
    }
}

private enum class DatabaseRowAction {
    EDIT,
    DELETE
}

private val EditRecordIcon: ImageVector =
    ImageVector.Builder(
        name = "EditRecord",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 20f)
            lineTo(8.5f, 19f)
            lineTo(19f, 8.5f)
            lineTo(15.5f, 5f)
            lineTo(5f, 15.5f)
            lineTo(4f, 20f)
            close()
            moveTo(14f, 6.5f)
            lineTo(17.5f, 10f)
        }
    }.build()

private val DeleteRecordIcon: ImageVector =
    ImageVector.Builder(
        name = "DeleteRecord",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 7f)
            lineTo(20f, 7f)
            moveTo(10f, 11f)
            lineTo(10f, 17f)
            moveTo(14f, 11f)
            lineTo(14f, 17f)
            moveTo(6f, 7f)
            lineTo(7f, 21f)
            lineTo(17f, 21f)
            lineTo(18f, 7f)
            moveTo(9f, 7f)
            lineTo(9.5f, 4f)
            lineTo(14.5f, 4f)
            lineTo(15f, 7f)
        }
    }.build()

private data class DatabaseDisplayRow(
    val cells: List<String>,
    val record: DatabaseRecordItem
)

@Composable
private fun InlineDatabaseEditForm(
    selectedTable: String,
    stationName: String,
    onStationNameChange: (String) -> Unit,
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    pm10: String,
    onPm10Change: (String) -> Unit,
    pm25: String,
    onPm25Change: (String) -> Unit,
    o3: String,
    onO3Change: (String) -> Unit,
    co: String,
    onCoChange: (String) -> Unit,
    so2: String,
    onSo2Change: (String) -> Unit,
    aqi: String,
    onAqiChange: (String) -> Unit,
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    humidity: String,
    onHumidityChange: (String) -> Unit,
    windSpeed: String,
    onWindSpeedChange: (String) -> Unit,
    precipitation: String,
    onPrecipitationChange: (String) -> Unit,
    riverName: String,
    onRiverNameChange: (String) -> Unit,
    waterLevel: String,
    onWaterLevelChange: (String) -> Unit,
    waterFlow: String,
    onWaterFlowChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        EnviroPanel(
            modifier = Modifier.widthIn(max = 860.dp),
            title = "Urejanje zapisa"
        ) {
            OutlinedTextField(
                value = stationName,
                onValueChange = onStationNameChange,
                label = { Text("Ime postaje") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = onLatitudeChange,
                    label = { Text("Zemljepisna širina") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = onLongitudeChange,
                    label = { Text("Zemljepisna dolžina") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTable) {
                "air-quality" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(pm10, onPm10Change, label = { Text("PM10") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(pm25, onPm25Change, label = { Text("PM2.5") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(o3, onO3Change, label = { Text("O3") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(co, onCoChange, label = { Text("CO") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(so2, onSo2Change, label = { Text("SO2") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(aqi, onAqiChange, label = { Text("AQI") }, modifier = Modifier.weight(1f))
                    }
                }

                "meteo" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(temperature, onTemperatureChange, label = { Text("Temperatura") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(humidity, onHumidityChange, label = { Text("Vlažnost") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(windSpeed, onWindSpeedChange, label = { Text("Hitrost vetra") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(precipitation, onPrecipitationChange, label = { Text("Padavine") }, modifier = Modifier.weight(1f))
                    }
                }

                "hydro" -> {
                    OutlinedTextField(
                        value = riverName,
                        onValueChange = onRiverNameChange,
                        label = { Text("Ime reke") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(waterLevel, onWaterLevelChange, label = { Text("Vodostaj") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(waterFlow, onWaterFlowChange, label = { Text("Pretok") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave) {
                    Text("Shrani spremembe")
                }
                OutlinedButton(onClick = onCancel) {
                    Text("Prekliči")
                }
            }
        }
    }
}

@Composable
private fun InlineDatabaseDeleteConfirmation(
    record: DatabaseRecordItem,
    showConfirmation: Boolean,
    onShowConfirmation: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        EnviroPanel(modifier = Modifier.widthIn(max = 680.dp)) {
            if (!showConfirmation) {
                Text(
                    text = "Ali si prepričan, da želiš izbrisati zapis za postajo \"${record.stationName}\"?",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = onShowConfirmation
                    ) {
                        Text("Izbriši zapis")
                    }

                    OutlinedButton(onClick = onCancel) {
                        Text("Prekliči")
                    }
                }
            } else {
                Text(
                    text = "Potrdi brisanje. Tega dejanja ni mogoče razveljaviti.",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = onDelete
                    ) {
                        Text("Da, izbriši")
                    }

                    OutlinedButton(onClick = onCancel) {
                        Text("Prekliči")
                    }
                }
            }
        }
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

private fun tableRowForRecord(table: String, record: DatabaseRecordItem): List<String> {
    return when (table) {
        "air-quality" -> listOf(
            record.raw.displayValue("stationName"),
            formatDisplayDate(record.raw.displayValue("measuredAt")),
            record.raw.displayValue("latitude"),
            record.raw.displayValue("longitude"),
            record.raw.displayValue("pm10"),
            record.raw.displayValue("pm2_5"),
            record.raw.displayValue("o3"),
            record.raw.displayValue("co"),
            record.raw.displayValue("so2"),
            record.raw.displayValue("airQualityIndex")
        )

        "meteo" -> listOf(
            record.raw.displayValue("stationName"),
            formatDisplayDate(record.raw.displayValue("measuredAt")),
            record.raw.displayValue("latitude"),
            record.raw.displayValue("longitude"),
            record.raw.displayValue("temperature"),
            record.raw.displayValue("humidity"),
            record.raw.displayValue("windSpeed"),
            record.raw.displayValue("windDirection"),
            record.raw.displayValue("precipitation")
        )

        "hydro" -> listOf(
            record.raw.displayValue("stationName"),
            formatDisplayDate(record.raw.displayValue("measuredAt")),
            record.raw.displayValue("latitude"),
            record.raw.displayValue("longitude"),
            record.raw.displayValue("riverName"),
            record.raw.displayValue("waterLevel"),
            record.raw.displayValue("waterFlow")
        )

        else -> emptyList()
    }
}

private fun Map<String, Any?>.displayValue(field: String): String {
    val value = this[field] ?: return "-"
    val text = value.toString().trim()
    return if (text.isBlank() || text == "null") "-" else text
}

private fun com.google.gson.JsonObject.displayValue(name: String): String {
    return stringValue(name).ifBlank { "-" }
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
