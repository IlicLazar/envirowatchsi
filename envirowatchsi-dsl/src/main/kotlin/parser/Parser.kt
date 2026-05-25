package parser

import lexer.Token
import lexer.TokenType

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse() {
        parseCities()
        consume(TokenType.EOF, "Pričakovan konec datoteke.")
    }

    private fun parseCities() {
        parseCity()
        while (!check(TokenType.EOF)) {
            parseCity()
        }
    }

    private fun parseCity() {
        consume(TokenType.CITY, "Pričakovana ključna beseda 'city'.")
        consume(TokenType.STRING, "Pričakovano ime mesta.")
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            parseCityItem()
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
    }

    private fun parseCityItem() {
        when {
            check(TokenType.AREA) -> parseArea()
            check(TokenType.RIVER) -> parseRiver()
            check(TokenType.DATE) -> parseDate()
            check(TokenType.RULE) -> parseRule()
            isStationStart() -> parseStation()
            else -> error(peek(), "Nepričakovan element v bloku city.")
        }
    }

    private fun parseArea() {
        consume(TokenType.AREA, "Pričakovana ključna beseda 'area'.")
        consume(TokenType.STRING, "Pričakovano ime območja.")
        consume(TokenType.POLYGON, "Pričakovana ključna beseda 'polygon'.")
        parsePoints()
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseRiver() {
        consume(TokenType.RIVER, "Pričakovana ključna beseda 'river'.")
        consume(TokenType.STRING, "Pričakovano ime reke.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseDate() {
        consume(TokenType.DATE, "Pričakovana ključna beseda 'date'.")
        consume(TokenType.STRING, "Pričakovan datum kot string.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseRule() {
        consume(TokenType.RULE, "Pričakovana ključna beseda 'rule'.")
        consume(TokenType.ID, "Pričakovano ime pravila.")
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            when {
                check(TokenType.THRESHOLD) -> parseThreshold()
                check(TokenType.FLOOD_THRESHOLD) -> parseFloodThreshold()
                else -> error(peek(), "Pričakovan threshold ali floodThreshold.")
            }
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseStation() {
        when {
            check(TokenType.STATION) -> parseGenericStation()
            check(TokenType.AIR_STATION) -> parseAirStation()
            check(TokenType.METEO_STATION) -> parseMeteoStation()
            check(TokenType.HYDRO_STATION) -> parseHydroStation()
            else -> error(peek(), "Pričakovana postaja.")
        }
    }

    private fun parseGenericStation() {
        consume(TokenType.STATION, "Pričakovana ključna beseda 'station'.")
        consume(TokenType.STRING, "Pričakovano ime postaje.")
        consume(TokenType.TYPE, "Pričakovana ključna beseda 'type'.")
        parseStationType()
        parseLocation()
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseStationType() {
        if (!match(TokenType.AIR, TokenType.METEO, TokenType.HYDRO, TokenType.MIXED)) {
            error(peek(), "Pričakovan tip postaje: air, meteo, hydro ali mixed.")
        }
    }

    private fun parseAirStation() {
        consume(TokenType.AIR_STATION, "Pričakovana ključna beseda 'airStation'.")
        consume(TokenType.STRING, "Pričakovano ime postaje.")
        parseLocation()
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            parseAirItem()
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseMeteoStation() {
        consume(TokenType.METEO_STATION, "Pričakovana ključna beseda 'meteoStation'.")
        consume(TokenType.STRING, "Pričakovano ime postaje.")
        parseLocation()
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            parseMeteoItem()
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseHydroStation() {
        consume(TokenType.HYDRO_STATION, "Pričakovana ključna beseda 'hydroStation'.")
        consume(TokenType.STRING, "Pričakovano ime postaje.")
        consume(TokenType.RIVER, "Pričakovana ključna beseda 'river'.")
        consume(TokenType.STRING, "Pričakovano ime reke.")
        parseLocation()
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            parseHydroItem()
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseAirItem() {
        when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            check(TokenType.POLLUTANT) -> parsePollutant()
            check(TokenType.MEASUREMENT) || check(TokenType.AQI) -> parseAirMeasurement()
            check(TokenType.THRESHOLD) -> parseThreshold()
            else -> error(peek(), "Neveljaven element v airStation.")
        }
    }

    private fun parseMeteoItem() {
        when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            isWeatherMeasurementStart() -> parseWeatherMeasurement()
            check(TokenType.INTERVAL) -> parseInterval()
            else -> error(peek(), "Neveljaven element v meteoStation.")
        }
    }

    private fun parseHydroItem() {
        when {
            check(TokenType.SOURCE) -> parseSource()
            check(TokenType.STATUS) -> parseStatus()
            isHydroMeasurementStart() -> parseHydroMeasurement()
            check(TokenType.FLOOD_THRESHOLD) -> parseFloodThreshold()
            check(TokenType.INTERVAL) -> parseInterval()
            else -> error(peek(), "Neveljaven element v hydroStation.")
        }
    }

    private fun parseSource() {
        consume(TokenType.SOURCE, "Pričakovana ključna beseda 'source'.")
        consume(TokenType.STRING, "Pričakovan vir podatkov.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseStatus() {
        consume(TokenType.STATUS, "Pričakovana ključna beseda 'status'.")
        if (!match(TokenType.ACTIVE, TokenType.INACTIVE, TokenType.TEST)) {
            error(peek(), "Pričakovan status: active, inactive ali test.")
        }
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parsePollutant() {
        consume(TokenType.POLLUTANT, "Pričakovana ključna beseda 'pollutant'.")
        parsePollutantType()
        consume(TokenType.UNIT, "Pričakovana ključna beseda 'unit'.")
        consume(TokenType.STRING, "Pričakovana enota.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parsePollutantType() {
        if (!match(TokenType.PM10, TokenType.PM2_5, TokenType.O3, TokenType.CO, TokenType.SO2, TokenType.ID)) {
            error(peek(), "Pričakovan tip onesnaževala.")
        }
    }

    private fun parseAirMeasurement() {
        if (match(TokenType.MEASUREMENT)) {
            consume(TokenType.ID, "Pričakovan identifikator meritve.")
            consume(TokenType.EQUALS, "Pričakovan znak '='.")
            consume(TokenType.NUMBER, "Pričakovana številčna vrednost.")
            parseUnitOptional()
            parseTimeOptional()
            consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
        } else {
            consume(TokenType.AQI, "Pričakovana ključna beseda 'aqi'.")
            consume(TokenType.NUMBER, "Pričakovana AQI vrednost.")
            parseTimeOptional()
            consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
        }
    }

    private fun parseWeatherMeasurement() {
        when {
            match(TokenType.TEMPERATURE, TokenType.HUMIDITY, TokenType.PRECIPITATION) -> {
                consume(TokenType.NUMBER, "Pričakovana številčna vrednost.")
                parseUnitOptional()
                parseTimeOptional()
                consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
            }
            match(TokenType.WIND) -> {
                consume(TokenType.SPEED, "Pričakovana ključna beseda 'speed'.")
                consume(TokenType.NUMBER, "Pričakovana hitrost vetra.")
                consume(TokenType.DIRECTION, "Pričakovana ključna beseda 'direction'.")
                consume(TokenType.STRING, "Pričakovana smer vetra.")
                parseTimeOptional()
                consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
            }
            else -> error(peek(), "Pričakovana vremenska meritev.")
        }
    }

    private fun parseHydroMeasurement() {
        if (!match(TokenType.WATER_LEVEL, TokenType.WATER_FLOW)) {
            error(peek(), "Pričakovana hidrološka meritev.")
        }

        consume(TokenType.NUMBER, "Pričakovana številčna vrednost.")
        parseUnitOptional()
        parseTimeOptional()
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseThreshold() {
        consume(TokenType.THRESHOLD, "Pričakovana ključna beseda 'threshold'.")
        consumeAnyPollutantOrId()
        consume(TokenType.WARNING, "Pričakovana ključna beseda 'warning'.")
        consume(TokenType.NUMBER, "Pričakovana opozorilna vrednost.")
        consume(TokenType.CRITICAL, "Pričakovana ključna beseda 'critical'.")
        consume(TokenType.NUMBER, "Pričakovana kritična vrednost.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun consumeAnyPollutantOrId() {
        if (!match(TokenType.PM10, TokenType.PM2_5, TokenType.O3, TokenType.CO, TokenType.SO2, TokenType.ID)) {
            error(peek(), "Pričakovan identifikator parametra.")
        }
    }

    private fun parseFloodThreshold() {
        consume(TokenType.FLOOD_THRESHOLD, "Pričakovana ključna beseda 'floodThreshold'.")
        consume(TokenType.WARNING, "Pričakovana ključna beseda 'warning'.")
        consume(TokenType.NUMBER, "Pričakovana opozorilna vrednost.")
        consume(TokenType.CRITICAL, "Pričakovana ključna beseda 'critical'.")
        consume(TokenType.NUMBER, "Pričakovana kritična vrednost.")
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseInterval() {
        consume(TokenType.INTERVAL, "Pričakovana ključna beseda 'interval'.")
        consume(TokenType.FROM, "Pričakovana ključna beseda 'from'.")
        consume(TokenType.STRING, "Pričakovan začetni čas.")
        consume(TokenType.TO, "Pričakovana ključna beseda 'to'.")
        consume(TokenType.STRING, "Pričakovan končni čas.")
        consume(TokenType.STEP, "Pričakovana ključna beseda 'step'.")
        consume(TokenType.STRING, "Pričakovan korak intervala.")
        consume(TokenType.LBRACE, "Pričakovan znak '{'.")

        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            when {
                check(TokenType.MEASUREMENT) -> parseMeasurement()
                isWeatherMeasurementStart() -> parseWeatherMeasurement()
                isHydroMeasurementStart() -> parseHydroMeasurement()
                else -> error(peek(), "Neveljavna meritev v intervalu.")
            }
        }

        consume(TokenType.RBRACE, "Pričakovan znak '}'.")
    }

    private fun parseMeasurement() {
        consume(TokenType.MEASUREMENT, "Pričakovana ključna beseda 'measurement'.")
        consume(TokenType.ID, "Pričakovan identifikator meritve.")
        consume(TokenType.EQUALS, "Pričakovan znak '='.")
        consume(TokenType.NUMBER, "Pričakovana številčna vrednost.")
        parseUnitOptional()
        parseTimeOptional()
        consume(TokenType.SEMICOLON, "Pričakovan znak ';'.")
    }

    private fun parseUnitOptional() {
        if (match(TokenType.UNIT)) {
            consume(TokenType.STRING, "Pričakovana enota.")
        }
    }

    private fun parseTimeOptional() {
        if (match(TokenType.AT)) {
            consume(TokenType.STRING, "Pričakovan časovni zapis.")
        }
    }

    private fun parseLocation() {
        consume(TokenType.AT, "Pričakovana ključna beseda 'at'.")
        parsePoint()
    }

    private fun parsePoints() {
        consume(TokenType.LPAREN, "Pričakovan znak '('.")
        parsePoint()

        while (match(TokenType.COMMA)) {
            parsePoint()
        }

        consume(TokenType.RPAREN, "Pričakovan znak ')'.")
    }

    private fun parsePoint() {
        consume(TokenType.LPAREN, "Pričakovan znak '('.")
        consume(TokenType.NUMBER, "Pričakovana prva koordinata.")
        consume(TokenType.COMMA, "Pričakovan znak ','.")
        consume(TokenType.NUMBER, "Pričakovana druga koordinata.")
        consume(TokenType.RPAREN, "Pričakovan znak ')'.")
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