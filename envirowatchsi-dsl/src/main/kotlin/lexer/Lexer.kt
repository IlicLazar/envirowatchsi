package lexer

class Lexer(private val input: String) {

    private var position = 0
    private var line = 1
    private var column = 1

    private val keywords = mapOf(
        "city" to TokenType.CITY,
        "area" to TokenType.AREA,
        "polygon" to TokenType.POLYGON,
        "river" to TokenType.RIVER,
        "date" to TokenType.DATE,
        "rule" to TokenType.RULE,

        "station" to TokenType.STATION,
        "type" to TokenType.TYPE,
        "air" to TokenType.AIR,
        "meteo" to TokenType.METEO,
        "hydro" to TokenType.HYDRO,
        "mixed" to TokenType.MIXED,

        "airStation" to TokenType.AIR_STATION,
        "meteoStation" to TokenType.METEO_STATION,
        "hydroStation" to TokenType.HYDRO_STATION,

        "source" to TokenType.SOURCE,
        "status" to TokenType.STATUS,
        "active" to TokenType.ACTIVE,
        "inactive" to TokenType.INACTIVE,
        "test" to TokenType.TEST,

        "measurement" to TokenType.MEASUREMENT,
        "aqi" to TokenType.AQI,

        "temperature" to TokenType.TEMPERATURE,
        "humidity" to TokenType.HUMIDITY,
        "precipitation" to TokenType.PRECIPITATION,
        "wind" to TokenType.WIND,
        "speed" to TokenType.SPEED,
        "direction" to TokenType.DIRECTION,

        "waterLevel" to TokenType.WATER_LEVEL,
        "waterFlow" to TokenType.WATER_FLOW,

        "pollutant" to TokenType.POLLUTANT,
        "pm10" to TokenType.PM10,
        "pm2_5" to TokenType.PM2_5,
        "o3" to TokenType.O3,
        "co" to TokenType.CO,
        "so2" to TokenType.SO2,

        "threshold" to TokenType.THRESHOLD,
        "floodThreshold" to TokenType.FLOOD_THRESHOLD,
        "warning" to TokenType.WARNING,
        "critical" to TokenType.CRITICAL,

        "unit" to TokenType.UNIT,
        "at" to TokenType.AT,
        "interval" to TokenType.INTERVAL,
        "from" to TokenType.FROM,
        "to" to TokenType.TO,
        "step" to TokenType.STEP
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        while (!isAtEnd()) {
            skipWhitespaceAndComments()

            if (isAtEnd()) break

            val startLine = line
            val startColumn = column
            val char = peek()

            val token = when {
                char.isLetter() -> readIdentifier(startLine, startColumn)
                char.isDigit() || char == '-' -> readNumber(startLine, startColumn)
                char == '"' -> readString(startLine, startColumn)

                char == '{' -> simpleToken(TokenType.LBRACE, startLine, startColumn)
                char == '}' -> simpleToken(TokenType.RBRACE, startLine, startColumn)
                char == '(' -> simpleToken(TokenType.LPAREN, startLine, startColumn)
                char == ')' -> simpleToken(TokenType.RPAREN, startLine, startColumn)
                char == ',' -> simpleToken(TokenType.COMMA, startLine, startColumn)
                char == ';' -> simpleToken(TokenType.SEMICOLON, startLine, startColumn)
                char == '=' -> simpleToken(TokenType.EQUALS, startLine, startColumn)

                else -> throw RuntimeException("Neznan znak '$char' na vrstici $line, stolpec $column")
            }

            tokens.add(token)
        }

        tokens.add(Token(TokenType.EOF, "", line, column))
        return tokens
    }

    private fun readIdentifier(startLine: Int, startColumn: Int): Token {
        val start = position

        while (!isAtEnd() && (peek().isLetterOrDigit() || peek() == '_')) {
            advance()
        }

        val text = input.substring(start, position)
        val type = keywords[text] ?: TokenType.ID

        return Token(type, text, startLine, startColumn)
    }

    private fun readNumber(startLine: Int, startColumn: Int): Token {
        val start = position

        if (peek() == '-') {
            advance()
        }

        if (isAtEnd() || !peek().isDigit()) {
            throw RuntimeException("Napačen zapis števila na vrstici $startLine, stolpec $startColumn")
        }

        while (!isAtEnd() && peek().isDigit()) {
            advance()
        }

        if (!isAtEnd() && peek() == '.') {
            advance()

            if (isAtEnd() || !peek().isDigit()) {
                throw RuntimeException("Napačen decimalni zapis na vrstici $startLine, stolpec $startColumn")
            }

            while (!isAtEnd() && peek().isDigit()) {
                advance()
            }
        }

        val text = input.substring(start, position)
        return Token(TokenType.NUMBER, text, startLine, startColumn)
    }

    private fun readString(startLine: Int, startColumn: Int): Token {
        advance()

        val start = position

        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                throw RuntimeException("Niz ni zaključen na vrstici $startLine, stolpec $startColumn")
            }
            advance()
        }

        if (isAtEnd()) {
            throw RuntimeException("Niz ni zaključen na vrstici $startLine, stolpec $startColumn")
        }

        val text = input.substring(start, position)
        advance()

        return Token(TokenType.STRING, text, startLine, startColumn)
    }

    private fun simpleToken(type: TokenType, startLine: Int, startColumn: Int): Token {
        val text = advance().toString()
        return Token(type, text, startLine, startColumn)
    }

    private fun skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            when (peek()) {
                ' ', '\r', '\t' -> advance()
                '\n' -> advanceNewLine()
                '/' -> {
                    if (peekNext() == '/') {
                        while (!isAtEnd() && peek() != '\n') {
                            advance()
                        }
                    } else {
                        return
                    }
                }
                else -> return
            }
        }
    }

    private fun advance(): Char {
        val char = input[position]
        position++
        column++
        return char
    }

    private fun advanceNewLine() {
        position++
        line++
        column = 1
    }

    private fun peek(): Char = input[position]

    private fun peekNext(): Char {
        return if (position + 1 >= input.length) '\u0000' else input[position + 1]
    }

    private fun isAtEnd(): Boolean = position >= input.length
}