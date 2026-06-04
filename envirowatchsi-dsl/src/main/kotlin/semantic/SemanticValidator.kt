package semantic

import ast.*

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
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, declaredRivers, errors)
                is IfNode -> validateIf(city, item, declaredRivers, errors)
                is WhileNode -> validateWhile(city, item, declaredRivers, errors)
                is RiverNode,
                is DateNode -> Unit
            }
        }
    }

    private fun validateList(city: CityNode, list: ListNode, errors: MutableList<SemanticError>) {
        if (list.name.isBlank()) {
            errors.add(SemanticError("Lista v mestu '${city.name}' mora imeti ime."))
        }

        list.values.filterIsInstance<PointValueNode>().forEach { value ->
            validatePoint(value.value, "lista '${list.name}' v mestu '${city.name}'", errors)
        }
    }

    private fun validateFor(
        city: CityNode,
        loop: ForNode,
        declaredRivers: Set<String>,
        errors: MutableList<SemanticError>,
        visibleLists: Set<String> = emptySet()
    ) {
        if (loop.variable.isBlank()) {
            errors.add(SemanticError("For zanka v mestu '${city.name}' mora imeti ime spremenljivke."))
        }

        val declaredLists = city.items.filterIsInstance<ListNode>().map { it.name }.toSet() + visibleLists
        if (loop.iterable !in declaredLists) {
            errors.add(
                SemanticError(
                    "For zanka v mestu '${city.name}' se sklicuje na listo '${loop.iterable}', ki ni deklarirana."
                )
            )
        }

        loop.items.forEach { item ->
            when (item) {
                is AreaNode -> validateArea(city, item, errors)
                is RuleNode -> validateRule(city, item, errors)
                is GenericStationNode -> validateGenericStation(city, item, errors)
                is AirStationNode -> validateAirStation(city, item, errors)
                is MeteoStationNode -> validateMeteoStation(city, item, errors)
                is HydroStationNode -> validateHydroStation(city, item, declaredRivers, errors)
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, declaredRivers, errors)
                is IfNode -> validateIf(city, item, declaredRivers, errors)
                is WhileNode -> validateWhile(city, item, declaredRivers, errors)
                is MeasurementNode,
                is AqiNode,
                is WeatherMeasurementNode,
                is WindMeasurementNode,
                is HydroMeasurementNode,
                is ThresholdNode,
                is FloodThresholdNode,
                is SourceNode,
                is StatusNode,
                is PollutantNode,
                is ConditionNode,
                is StringValueNode,
                is NumberValueNode,
                is IdentifierValueNode,
                is PointValueNode,
                is PointNode,
                is ProgramNode,
                is CityNode,
                is IntervalNode -> Unit
                is RiverNode,
                is DateNode -> Unit
            }
        }
    }

    private fun validateIf(
        city: CityNode,
        statement: IfNode,
        declaredRivers: Set<String>,
        errors: MutableList<SemanticError>
    ) {
        validateCondition(statement.condition, "if stavek v mestu '${city.name}'", errors)
        validateStatementItems(city, statement.thenItems, declaredRivers, errors)
        validateStatementItems(city, statement.elseItems, declaredRivers, errors)
    }

    private fun validateWhile(
        city: CityNode,
        statement: WhileNode,
        declaredRivers: Set<String>,
        errors: MutableList<SemanticError>
    ) {
        validateCondition(statement.condition, "while stavek v mestu '${city.name}'", errors)
        validateStatementItems(city, statement.items, declaredRivers, errors)
    }

    private fun validateStatementItems(
        city: CityNode,
        items: List<AstNode>,
        declaredRivers: Set<String>,
        errors: MutableList<SemanticError>
    ) {
        items.forEach { item ->
            when (item) {
                is ThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "kompleksni stavek v mestu '${city.name}'",
                    errors
                )
                is FloodThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "kompleksni stavek v mestu '${city.name}'",
                    errors
                )
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, declaredRivers, errors)
                is IfNode -> validateIf(city, item, declaredRivers, errors)
                is WhileNode -> validateWhile(city, item, declaredRivers, errors)
                is MeasurementNode,
                is AqiNode,
                is WeatherMeasurementNode,
                is WindMeasurementNode,
                is HydroMeasurementNode,
                is StatusNode -> Unit
                else -> Unit
            }
        }
    }

    private fun validateCondition(
        condition: ConditionNode,
        context: String,
        errors: MutableList<SemanticError>
    ) {
        val validOperators = setOf(">", "<", ">=", "<=", "==", "!=")
        if (condition.operator !in validOperators) {
            errors.add(SemanticError("Pogoj v $context ima neveljaven operator '${condition.operator}'."))
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
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, emptySet(), errors)
                is IfNode -> validateIf(city, item, emptySet(), errors)
                is WhileNode -> validateWhile(city, item, emptySet(), errors)
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

        val measurements = station.items.filter { it is MeasurementNode || it is AqiNode }
        if (measurements.isEmpty()) {
            errors.add(
                SemanticError(
                    "AirStation '${station.name}' v mestu '${city.name}' mora vsebovati vsaj eno meritev ali AQI zapis."
                )
            )
        }

        val localLists = station.items.filterIsInstance<ListNode>().map { it.name }.toSet()
        station.items.forEach { item ->
            when (item) {
                is MeasurementNode -> Unit
                is ThresholdNode -> validateThreshold(
                    item.warning,
                    item.critical,
                    "airStation '${station.name}' v mestu '${city.name}'",
                    errors
                )
                is SourceNode,
                is StatusNode,
                is PollutantNode,
                is AqiNode -> Unit
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, emptySet(), errors, localLists)
                is IfNode -> validateIf(city, item, emptySet(), errors)
                is WhileNode -> validateWhile(city, item, emptySet(), errors)
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

        val localLists = station.items.filterIsInstance<ListNode>().map { it.name }.toSet()
        station.items.forEach { item ->
            when (item) {
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, emptySet(), errors, localLists)
                is IfNode -> validateIf(city, item, emptySet(), errors)
                is WhileNode -> validateWhile(city, item, emptySet(), errors)
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

        val localLists = station.items.filterIsInstance<ListNode>().map { it.name }.toSet()
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
                is SourceNode,
                is StatusNode,
                is HydroMeasurementNode -> Unit
                is ListNode -> validateList(city, item, errors)
                is ForNode -> validateFor(city, item, declaredRivers, errors, localLists)
                is IfNode -> validateIf(city, item, declaredRivers, errors)
                is WhileNode -> validateWhile(city, item, declaredRivers, errors)
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

        interval.measurements.forEach { measurement ->
            if (!isAllowedMeasurement(measurement)) {
                errors.add(
                    SemanticError(
                        "Interval v $context vsebuje meritev neustreznega tipa: ${measurementTypeName(measurement)}."
                    )
                )
            }
        }
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
