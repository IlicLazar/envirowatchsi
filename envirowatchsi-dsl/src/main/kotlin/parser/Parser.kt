package parser

import lexer.Token
import lexer.TokenType
import ast.*

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): ProgramNode {
        val program = parseCities()
        consume(TokenType.EOF, "Pričakovan konec datoteke.")
        return program
    }

    private fun parseCities(): ProgramNode {
        val cities = mutableListOf<CityNode>()

        cities.add(parseCity())

        while (!check(TokenType.EOF)) {
            cities.add(parseCity())
        }

        return ProgramNode(cities)
    }

    private fun parseCity(): CityNode {
        consume(TokenType.CITY, "Pričakovana ključna beseda 'city'.")
        val name = consume(TokenType.STRING, "Pričakovano ime mesta.").lexeme
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val items = mutableListOf<CityItemNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            items.add(parseCityItem())
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        return CityNode(name, items)
    }

    private fun parseCityItem(): CityItemNode {
        return when {
            check(TokenType.AREA) -> parseArea()
            check(TokenType.RIVER) -> parseRiver()
            check(TokenType.DATE) -> parseDate()
            check(TokenType.RULE) -> parseRule()
            isStationStart() -> parseStation()
            else -> error(peek(), "Nepričakovan element v bloku city.")
        }
    }

    private fun parseArea(): AreaNode {
        consume(TokenType.AREA, "Pričakovana ključna beseda 'area'.")
        val name = consume(TokenType.STRING, "Pričakovano ime območja.").lexeme
        consume(TokenType.POLYGON, "Pričakovana ključna beseda 'polygon'.")

        val points = parsePoints()

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return AreaNode(name, points)
    }

    private fun parseRiver(): RiverNode {
        consume(TokenType.RIVER, "Pričakovana ključna beseda 'river'.")
        val name = consume(TokenType.STRING, "Pričakovano ime reke.").lexeme
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return RiverNode(name)
    }

    private fun parseDate(): DateNode {
        consume(TokenType.DATE, "Pričakovana ključna beseda 'date'.")
        val value = consume(TokenType.STRING, "Pričakovan datum kot string.").lexeme
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return DateNode(value)
    }

    private fun parseRule(): RuleNode {
        consume(TokenType.RULE, "Pričakovana ključna beseda 'rule'.")
        val name = consume(TokenType.ID, "Pričakovano ime pravila.").lexeme
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val items = mutableListOf<RuleItemNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            val item = when {
                check(TokenType.THRESHOLD) -> parseThreshold()
                check(TokenType.FLOOD_THRESHOLD) -> parseFloodThreshold()
                else -> error(peek(), "Pričakovan threshold ali floodThreshold.")
            }
            items.add(item)
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return RuleNode(name, items)
    }

    private fun parseStation(): StationNode {
        return when {
            check(TokenType.STATION) -> parseGenericStation()
            check(TokenType.AIR_STATION) -> parseAirStation()
            check(TokenType.METEO_STATION) -> parseMeteoStation()
            check(TokenType.HYDRO_STATION) -> parseHydroStation()
            else -> error(peek(), "Pričakovana postaja.")
        }
    }

    private fun parseGenericStation(): GenericStationNode {
        consume(TokenType.STATION, "Pričakovana ključna beseda 'station'.")

        val name = consume(
            TokenType.STRING,
            "Pričakovano ime postaje."
        ).lexeme

        consume(TokenType.TYPE, "Pričakovana ključna beseda 'type'.")

        parseStationType()
        val type = previous().lexeme

        val location = parseLocation()

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return GenericStationNode(name, type, location)
    }

    private fun parseStationType() {
        if (!match(TokenType.AIR, TokenType.METEO, TokenType.HYDRO, TokenType.MIXED)) {
            error(peek(), "Pričakovan tip postaje: air, meteo, hydro ali mixed.")
        }
    }

    private fun parseAirStation(): AirStationNode {
        consume(TokenType.AIR_STATION, "Pričakovana ključna beseda 'airStation'.")

        val name = consume(
            TokenType.STRING,
            "Pričakovano ime postaje."
        ).lexeme

        val location = parseLocation()

        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val items = mutableListOf<AirItemNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            items.add(parseAirItem())
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return AirStationNode(name, location, items)
    }

    private fun parseMeteoStation(): MeteoStationNode {
        consume(TokenType.METEO_STATION, "Pričakovana ključna beseda 'meteoStation'.")

        val name = consume(
            TokenType.STRING,
            "Pričakovano ime postaje."
        ).lexeme

        val location = parseLocation()

        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val items = mutableListOf<MeteoItemNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            items.add(parseMeteoItem())
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return MeteoStationNode(name, location, items)
    }

    private fun parseHydroStation(): HydroStationNode {
        consume(TokenType.HYDRO_STATION, "Pričakovana ključna beseda 'hydroStation'.")

        val name = consume(
            TokenType.STRING,
            "Pričakovano ime postaje."
        ).lexeme

        consume(TokenType.RIVER, "Pričakovana ključna beseda 'river'.")

        val river = consume(
            TokenType.STRING,
            "Pričakovano ime reke."
        ).lexeme

        val location = parseLocation()

        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val items = mutableListOf<HydroItemNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            items.add(parseHydroItem())
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return HydroStationNode(name, river, location, items)
    }

    private fun parseAirItem(): AirItemNode {
        return when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            check(TokenType.POLLUTANT) -> parsePollutant()
            check(TokenType.MEASUREMENT) || check(TokenType.AQI) -> parseAirMeasurement()
            check(TokenType.THRESHOLD) -> parseThreshold()
            check(TokenType.INTERVAL) -> parseInterval()
            else -> error(peek(), "Neveljaven element v airStation.")
        }
    }

    private fun parseMeteoItem(): MeteoItemNode {
        return when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            isWeatherMeasurementStart() -> parseWeatherMeasurement()
            check(TokenType.INTERVAL) -> parseInterval()
            else -> error(peek(), "Neveljaven element v meteoStation.")
        }
    }

    private fun parseHydroItem(): HydroItemNode {
        return when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            isHydroMeasurementStart() -> parseHydroMeasurement()
            check(TokenType.FLOOD_THRESHOLD) -> parseFloodThreshold()
            check(TokenType.INTERVAL) -> parseInterval()
            else -> error(peek(), "Neveljaven element v hydroStation.")
        }
    }

    private fun parseSource(): SourceNode {
        consume(TokenType.SOURCE, "Pričakovana ključna beseda 'source'.")
        val value = consume(TokenType.STRING, "Pričakovan vir podatkov.").lexeme
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
        return SourceNode(value)
    }

    private fun parseStatus(): StatusNode {
        consume(TokenType.STATUS, "Pričakovana ključna beseda 'status'.")

        val value = if (match(TokenType.ACTIVE, TokenType.INACTIVE, TokenType.TEST)) {
            previous().lexeme
        } else {
            error(peek(), "Pričakovan status: active, inactive ali test.")
        }

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
        return StatusNode(value)
    }

    private fun parsePollutant(): PollutantNode {
        consume(TokenType.POLLUTANT, "Pričakovana ključna beseda 'pollutant'.")

        val type = parsePollutantType()

        consume(TokenType.UNIT, "Pričakovana ključna beseda 'unit'.")
        val unit = consume(TokenType.STRING, "Pričakovana enota.").lexeme

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return PollutantNode(type, unit)
    }

    private fun parsePollutantType(): String {
        return if (match(TokenType.PM10, TokenType.PM2_5, TokenType.O3, TokenType.CO, TokenType.SO2, TokenType.ID)) {
            previous().lexeme
        } else {
            error(peek(), "Pričakovan tip onesnaževala.")
        }
    }

    private fun parseAirMeasurement(): AirItemNode {
        return if (match(TokenType.MEASUREMENT)) {
            val name = consumeAnyPollutantOrId()
            consume(TokenType.EQUALS, "Pričakovan znak '='.")
            val value = consume(TokenType.NUMBER, "Pričakovana številčna vrednost.").lexeme
            val unit = parseUnitOptional()
            val time = parseTimeOptional()
            consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
            MeasurementNode(name, value, unit, time)
        } else {
            consume(TokenType.AQI, "Pričakovana ključna beseda 'aqi'.")
            val value = consume(TokenType.NUMBER, "Pričakovana AQI vrednost.").lexeme
            val time = parseTimeOptional()
            consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
            AqiNode(value, time)
        }
    }

    private fun parseWeatherMeasurement(): MeteoItemNode {
        return when {
            match(TokenType.TEMPERATURE, TokenType.HUMIDITY, TokenType.PRECIPITATION) -> {
                val type = previous().lexeme
                val value = consume(TokenType.NUMBER, "Pričakovana številčna vrednost.").lexeme
                val unit = parseUnitOptional()
                val time = parseTimeOptional()
                consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
                WeatherMeasurementNode(type, value, unit, time)
            }

            match(TokenType.WIND) -> {
                consume(TokenType.SPEED, "Pričakovana ključna beseda 'speed'.")
                val speed = consume(TokenType.NUMBER, "Pričakovana hitrost vetra.").lexeme
                consume(TokenType.DIRECTION, "Pričakovana ključna beseda 'direction'.")
                val direction = consume(TokenType.STRING, "Pričakovana smer vetra.").lexeme
                val time = parseTimeOptional()
                consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
                WindMeasurementNode(speed, direction, time)
            }

            else -> error(peek(), "Pričakovana vremenska meritev.")
        }
    }

    private fun parseHydroMeasurement(): HydroMeasurementNode {
        val type = if (match(TokenType.WATER_LEVEL, TokenType.WATER_FLOW)) {
            previous().lexeme
        } else {
            error(peek(), "Pričakovana hidrološka meritev.")
        }

        val value = consume(TokenType.NUMBER, "Pričakovana številčna vrednost.").lexeme
        val unit = parseUnitOptional()
        val time = parseTimeOptional()

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return HydroMeasurementNode(type, value, unit, time)
    }

    private fun parseThreshold(): ThresholdNode {
        consume(TokenType.THRESHOLD, "Pričakovana ključna beseda 'threshold'.")
        val parameter = consumeAnyPollutantOrId()

        consume(TokenType.WARNING, "Pričakovana ključna beseda 'warning'.")
        val warning = consume(TokenType.NUMBER, "Pričakovana opozorilna vrednost.").lexeme

        consume(TokenType.CRITICAL, "Pričakovana ključna beseda 'critical'.")
        val critical = consume(TokenType.NUMBER, "Pričakovana kritična vrednost.").lexeme

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return ThresholdNode(parameter, warning, critical)
    }

    private fun consumeAnyPollutantOrId(): String {
        return if (match(TokenType.PM10, TokenType.PM2_5, TokenType.O3, TokenType.CO, TokenType.SO2, TokenType.ID)) {
            previous().lexeme
        } else {
            error(peek(), "Pričakovan identifikator parametra.")
        }
    }

    private fun parseFloodThreshold(): FloodThresholdNode {
        consume(TokenType.FLOOD_THRESHOLD, "Pričakovana ključna beseda 'floodThreshold'.")

        consume(TokenType.WARNING, "Pričakovana ključna beseda 'warning'.")
        val warning = consume(TokenType.NUMBER, "Pričakovana opozorilna vrednost.").lexeme

        consume(TokenType.CRITICAL, "Pričakovana ključna beseda 'critical'.")
        val critical = consume(TokenType.NUMBER, "Pričakovana kritična vrednost.").lexeme

        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return FloodThresholdNode(warning, critical)
    }

    private fun parseInterval(): IntervalNode {
        consume(TokenType.INTERVAL, "Pričakovana ključna beseda 'interval'.")
        consume(TokenType.FROM, "Pričakovana ključna beseda 'from'.")
        val from = consume(TokenType.STRING, "Pričakovan začetni čas.").lexeme
        consume(TokenType.TO, "Pričakovana ključna beseda 'to'.")
        val to = consume(TokenType.STRING, "Pričakovan končni čas.").lexeme
        consume(TokenType.STEP, "Pričakovana ključna beseda 'step'.")
        val step = consume(TokenType.STRING, "Pričakovan korak intervala.").lexeme
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        val measurements = mutableListOf<AstNode>()

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            val measurement = when {
                check(TokenType.MEASUREMENT) -> parseMeasurement()
                check(TokenType.AQI) -> parseAirMeasurement()
                isWeatherMeasurementStart() -> parseWeatherMeasurement()
                isHydroMeasurementStart() -> parseHydroMeasurement()
                else -> error(peek(), "Neveljavna meritev v intervalu.")
            }
            measurements.add(measurement)
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")

        return IntervalNode(from, to, step, measurements)
    }

    private fun parseMeasurement(): MeasurementNode {
        consume(TokenType.MEASUREMENT, "Pričakovana ključna beseda 'measurement'.")
        val name = consumeAnyPollutantOrId()
        consume(TokenType.EQUALS, "Pričakovan znak '='.")
        val value = consume(TokenType.NUMBER, "Pričakovana številčna vrednost.").lexeme
        val unit = parseUnitOptional()
        val time = parseTimeOptional()
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")

        return MeasurementNode(name, value, unit, time)
    }

    private fun parseUnitOptional(): String? {
        return if (match(TokenType.UNIT)) {
            consume(TokenType.STRING, "Pričakovana enota.").lexeme
        } else {
            null
        }
    }

    private fun parseTimeOptional(): String? {
        return if (match(TokenType.AT)) {
            consume(TokenType.STRING, "Pričakovan časovni zapis.").lexeme
        } else {
            null
        }
    }

    private fun parseLocation(): PointNode {
        consume(TokenType.AT, "Pričakovana ključna beseda 'at'.")
        return parsePoint()
    }

    private fun parsePoints(): List<PointNode> {
        consume(TokenType.LPAREN, "Pričakovan znak '('.")

        val points = mutableListOf<PointNode>()

        points.add(parsePoint())

        while (match(TokenType.COMMA)) {
            points.add(parsePoint())
        }

        consume(TokenType.RPAREN, "Pričakovan znak ')'.")

        return points
    }

    private fun parsePoint(): PointNode {
        consume(TokenType.LPAREN, "Pričakovan znak '('.")

        val longitude = consume(TokenType.NUMBER, "Pričakovana prva koordinata.").lexeme

        consume(TokenType.COMMA, "Pričakovan znak ','.")

        val latitude = consume(TokenType.NUMBER, "Pričakovana druga koordinata.").lexeme

        consume(TokenType.RPAREN, "Pričakovan znak ')'.")

        return PointNode(longitude, latitude)
    }

    private fun isStationStart(): Boolean =
        check(TokenType.STATION) || check(TokenType.AIR_STATION) ||
                check(TokenType.METEO_STATION) || check(TokenType.HYDRO_STATION)

    private fun isWeatherMeasurementStart(): Boolean =
        check(TokenType.TEMPERATURE) || check(TokenType.HUMIDITY) ||
                check(TokenType.PRECIPITATION) || check(TokenType.WIND)

    private fun isHydroMeasurementStart(): Boolean =
        check(TokenType.WATER_LEVEL) || check(TokenType.WATER_FLOW)

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return type == TokenType.EOF
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): Nothing {
        throw RuntimeException(
            "Sintaktična napaka na vrstici ${token.line}, stolpec ${token.column}: $message " +
                    "Najden token: ${token.type} '${token.lexeme}'"
        )
    }
}
