package semantic

import ast.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class SemanticError(
    val message: String
)

class SemanticValidationException(
    val errors: List<SemanticError>
) : RuntimeException(
    errors.joinToString(separator = "\n") { "- ${it.message}" }
)

object SemanticValidator {
    private val validStationTypes = setOf("air", "meteo", "hydro", "mixed")

    fun validate(program: ProgramNode): List<SemanticError> {
        val errors = mutableListOf<SemanticError>()

        program.cities.forEach { city ->
            validateCity(city, errors)
        }

        return errors
    }

    fun validateOrThrow(program: ProgramNode) {
        val errors = validate(program)
        if (errors.isNotEmpty()) {
            throw SemanticValidationException(errors)
        }
    }

    private fun validateCity(city: CityNode, errors: MutableList<SemanticError>) {
        val declaredRivers = city.items
            .filterIsInstance<RiverNode>()
            .map { it.name }
            .toSet()

        city.items.forEach { item ->
            when (item) {
                is AreaNode -> validateArea(city, item, errors)
                is RuleNode -> validateRule(city, item, errors)
                is GenericStationNode -> validateGenericStation(city, item, errors)
                is AirStationNode -> validateAirStation(city, item, errors)
                is MeteoStationNode -> validateMeteoStation(city, item, errors)
                is HydroStationNode -> validateHydroStation(city, item, declaredRivers, errors)
                is RiverNode,
                is DateNode -> Unit
            }
        }
    }

    private fun validateArea(city: CityNode, area: AreaNode, errors: MutableList<SemanticError>) {
        if (area.points.size < 3) {
            errors.add(
                SemanticError(
                    "Območje '${area.name}' v mestu '${city.name}' mora imeti vsaj tri točke poligona."
                )
            )
        }

        area.points.forEach { point ->
            validatePoint(point, "območje '${area.name}' v mestu '${city.name}'", errors)
        }
    }

    private fun validateRule(city: CityNode, rule: RuleNode, errors: MutableList<SemanticError>) {
        rule.items.forEach { item ->
            when (item) {
                is ThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "pravilo '${rule.name}' v mestu '${city.name}'",
                    errors
                )
                is FloodThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "pravilo '${rule.name}' v mestu '${city.name}'",
                    errors
                )
            }
        }
    }

    private fun validateGenericStation(
        city: CityNode,
        station: GenericStationNode,
        errors: MutableList<SemanticError>
    ) {
        validateStationName(station.name, "generična postaja", city, errors)

        if (station.type !in validStationTypes) {
            errors.add(
                SemanticError(
                    "Postaja '${station.name}' v mestu '${city.name}' ima neveljaven tip '${station.type}'. " +
                            "Dovoljeni tipi so: ${validStationTypes.joinToString(", ")}."
                )
            )
        }

        validatePoint(station.location, "postaja '${station.name}' v mestu '${city.name}'", errors)
    }

    private fun validateAirStation(
        city: CityNode,
        station: AirStationNode,
        errors: MutableList<SemanticError>
    ) {
        validateStationName(station.name, "airStation", city, errors)
        validatePoint(station.location, "airStation '${station.name}' v mestu '${city.name}'", errors)

        val measurements = station.items.filter { it is MeasurementNode || it is AqiNode || it is IntervalNode }
        if (measurements.isEmpty()) {
            errors.add(
                SemanticError(
                    "AirStation '${station.name}' v mestu '${city.name}' mora vsebovati vsaj eno meritev ali AQI zapis."
                )
            )
        }

        station.items.forEach { item ->
            when (item) {
                is MeasurementNode -> validateDateTime(
                    item.time,
                    "meritev '${item.name}' v airStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is ThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "airStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is SourceNode,
                is StatusNode,
                is PollutantNode -> Unit
                is AqiNode -> validateDateTime(
                    item.time,
                    "AQI meritev v airStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is IntervalNode -> validateInterval(
                    interval = item,
                    context = "airStation '${station.name}' v mestu '${city.name}'",
                    errors = errors,
                    isAllowedMeasurement = { it is MeasurementNode || it is AqiNode }
                )
            }
        }
    }

    private fun validateMeteoStation(
        city: CityNode,
        station: MeteoStationNode,
        errors: MutableList<SemanticError>
    ) {
        validateStationName(station.name, "meteoStation", city, errors)
        validatePoint(station.location, "meteoStation '${station.name}' v mestu '${city.name}'", errors)

        val measurements = station.items.filter {
            it is WeatherMeasurementNode || it is WindMeasurementNode || it is IntervalNode
        }
        if (measurements.isEmpty()) {
            errors.add(
                SemanticError(
                    "MeteoStation '${station.name}' v mestu '${city.name}' mora vsebovati vsaj eno vremensko meritev."
                )
            )
        }

        station.items.filterIsInstance<IntervalNode>().forEach { interval ->
            validateInterval(
                interval = interval,
                context = "meteoStation '${station.name}' v mestu '${city.name}'",
                errors = errors,
                isAllowedMeasurement = { it is WeatherMeasurementNode || it is WindMeasurementNode }
            )
        }

        station.items.forEach { item ->
            when (item) {
                is WeatherMeasurementNode -> validateDateTime(
                    item.time,
                    "meritev '${item.type}' v meteoStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is WindMeasurementNode -> validateDateTime(
                    item.time,
                    "meritev 'wind' v meteoStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                else -> Unit
            }
        }
    }

    private fun validateHydroStation(
        city: CityNode,
        station: HydroStationNode,
        declaredRivers: Set<String>,
        errors: MutableList<SemanticError>
    ) {
        validateStationName(station.name, "hydroStation", city, errors)
        validatePoint(station.location, "hydroStation '${station.name}' v mestu '${city.name}'", errors)

        if (station.river.isBlank()) {
            errors.add(
                SemanticError(
                    "HydroStation '${station.name}' v mestu '${city.name}' mora imeti določeno reko."
                )
            )
        } else if (declaredRivers.isNotEmpty() && station.river !in declaredRivers) {
            errors.add(
                SemanticError(
                    "HydroStation '${station.name}' v mestu '${city.name}' se sklicuje na reko '${station.river}', " +
                            "ki v mestu ni deklarirana."
                )
            )
        }

        val measurements = station.items.filter { it is HydroMeasurementNode || it is IntervalNode }
        if (measurements.isEmpty()) {
            errors.add(
                SemanticError(
                    "HydroStation '${station.name}' v mestu '${city.name}' mora vsebovati vsaj eno hidrološko meritev."
                )
            )
        }

        station.items.forEach { item ->
            when (item) {
                is FloodThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "hydroStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is IntervalNode -> validateInterval(
                    interval = item,
                    context = "hydroStation '${station.name}' v mestu '${city.name}'",
                    errors = errors,
                    isAllowedMeasurement = { it is HydroMeasurementNode }
                )
                is MeasurementNode -> errors.add(
                    SemanticError(
                        "HydroStation '${station.name}' v mestu '${city.name}' vsebuje generično meritev " +
                                "'${item.name}', dovoljena sta samo waterLevel in waterFlow."
                    )
                )
                is HydroMeasurementNode -> validateDateTime(
                    item.time,
                    "meritev '${item.type}' v hydroStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is SourceNode,
                is StatusNode -> Unit
            }
        }
    }

    private fun validateInterval(
        interval: IntervalNode,
        context: String,
        errors: MutableList<SemanticError>,
        isAllowedMeasurement: (AstNode) -> Boolean
    ) {
        if (interval.measurements.isEmpty()) {
            errors.add(SemanticError("Interval v $context mora vsebovati vsaj eno meritev."))
        }

        val from = parseDateTime(interval.from, "začetek intervala v $context", errors)
        val to = parseDateTime(interval.to, "konec intervala v $context", errors)
        val step = parseStep(interval.step, "interval v $context", errors)

        if (from != null && to != null) {
            if (!from.isBefore(to)) {
                errors.add(SemanticError("Začetek intervala v $context mora biti pred koncem intervala."))
            } else if (step != null && step > Duration.between(from, to)) {
                errors.add(SemanticError("Korak intervala v $context ne sme biti daljši od trajanja intervala."))
            }
        }

        interval.measurements.forEach { measurement ->
            if (!isAllowedMeasurement(measurement)) {
                errors.add(
                    SemanticError(
                        "Interval v $context vsebuje meritev neustreznega tipa: ${measurementTypeName(measurement)}."
                    )
                )
            }

            val measurementTime = measurementDateTime(measurement)
            validateDateTime(measurementTime, "meritev v intervalu v $context", errors)

            if (from != null && to != null && measurementTime != null) {
                parseDateTime(measurementTime, "meritev v intervalu v $context", errors)?.let { parsedTime ->
                    if (parsedTime.isBefore(from) || parsedTime.isAfter(to)) {
                        errors.add(
                            SemanticError(
                                "Čas meritve (${measurementTime.value}) v $context mora biti znotraj intervala " +
                                        "[${interval.from.value}, ${interval.to.value}]."
                            )
                        )
                    }
                }
            }
        }
    }

    private fun parseDateTime(
        dateTime: DateTimeNode,
        context: String,
        errors: MutableList<SemanticError>
    ): LocalDateTime? =
        try {
            LocalDateTime.parse(dateTime.value)
        } catch (_: DateTimeParseException) {
            errors.add(
                SemanticError(
                    "Časovni zapis za $context mora biti v ISO-8601 obliki yyyy-MM-ddTHH:mm, najdeno: ${dateTime.value}."
                )
            )
            null
        }

    private fun validateDateTime(
        dateTime: DateTimeNode?,
        context: String,
        errors: MutableList<SemanticError>
    ) {
        if (dateTime != null) {
            parseDateTime(dateTime, context, errors)
        }
    }

    private fun parseStep(
        step: String,
        context: String,
        errors: MutableList<SemanticError>
    ): Duration? {
        val match = Regex("""^(\d+)(m|min|h|d)$""").matchEntire(step)
        if (match == null) {
            errors.add(SemanticError("Korak za $context mora biti zapisan kot pozitivno trajanje, npr. 15m, 1h ali 1d."))
            return null
        }

        val amount = match.groupValues[1].toLong()
        if (amount <= 0) {
            errors.add(SemanticError("Korak za $context mora biti večji od 0."))
            return null
        }

        return when (match.groupValues[2]) {
            "m",
            "min" -> Duration.ofMinutes(amount)
            "h" -> Duration.ofHours(amount)
            "d" -> Duration.ofDays(amount)
            else -> null
        }
    }

    private fun measurementDateTime(measurement: AstNode): DateTimeNode? =
        when (measurement) {
            is MeasurementNode -> measurement.time
            is WeatherMeasurementNode -> measurement.time
            is WindMeasurementNode -> measurement.time
            is HydroMeasurementNode -> measurement.time
            is AqiNode -> measurement.time
            else -> null
        }

    private fun validateThreshold(
        warning: String,
        critical: String,
        context: String,
        errors: MutableList<SemanticError>
    ) {
        val warningValue = warning.toDoubleOrNull()
        val criticalValue = critical.toDoubleOrNull()

        if (warningValue == null || criticalValue == null) {
            errors.add(SemanticError("Pragovi v $context morajo biti številske vrednosti."))
            return
        }

        if (warningValue >= criticalValue) {
            errors.add(
                SemanticError(
                    "V $context mora biti warning ($warning) manjši od critical ($critical)."
                )
            )
        }
    }

    private fun validateStationName(
        name: String,
        stationType: String,
        city: CityNode,
        errors: MutableList<SemanticError>
    ) {
        if (name.isBlank()) {
            errors.add(SemanticError("$stationType v mestu '${city.name}' mora imeti ime."))
        }
    }

    private fun validatePoint(point: PointNode, context: String, errors: MutableList<SemanticError>) {
        val longitude = point.longitude.toDoubleOrNull()
        val latitude = point.latitude.toDoubleOrNull()

        if (longitude == null || latitude == null) {
            errors.add(SemanticError("Koordinate za $context morajo biti številske vrednosti."))
            return
        }

        if (!isDecimalCoordinate(point.longitude) || !isDecimalCoordinate(point.latitude)) {
            errors.add(
                SemanticError(
                    "Koordinate za $context morajo biti zapisane v decimalni obliki, najdeno: " +
                            "(${point.longitude}, ${point.latitude})."
                )
            )
        }

        if (longitude !in -180.0..180.0) {
            errors.add(
                SemanticError(
                    "Longituda za $context mora biti v intervalu [-180, 180], najdeno: ${point.longitude}."
                )
            )
        }

        if (latitude !in -90.0..90.0) {
            errors.add(
                SemanticError(
                    "Latituda za $context mora biti v intervalu [-90, 90], najdeno: ${point.latitude}."
                )
            )
        }
    }

    private fun isDecimalCoordinate(value: String): Boolean =
        value.contains(".")

    private fun measurementTypeName(measurement: AstNode): String =
        when (measurement) {
            is MeasurementNode -> "measurement"
            is WeatherMeasurementNode -> measurement.type
            is WindMeasurementNode -> "wind"
            is HydroMeasurementNode -> measurement.type
            is AqiNode -> "aqi"
            else -> measurement.toString()
        }
}
