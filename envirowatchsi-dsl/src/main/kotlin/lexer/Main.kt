package lexer

fun main() {
    val program = """
    city "TestCity" {
        // komentar koji lexer treba ignorirati

        station "Generic Air Station" type air at (-14.512,46.065);

        meteoStation "Weather" at (15.646,46.554) {
            temperature -5.2 unit "C" at "2026-05-21T12:00";
            humidity 63 unit "%";
            wind speed 4.2 direction "NE";
        };

        hydroStation "River Station" river "Savinja" at (15.236,46.154) {
            waterLevel 142 unit "cm";
            waterFlow 87.5 unit "m3/s";
            floodThreshold warning 180 critical 240;
        };
    }
""".trimIndent()

    val lexer = Lexer(program)
    val tokens = lexer.tokenize()

    tokens.forEach {
        println("${it.type} '${it.lexeme}' [${it.line}:${it.column}]")
    }
}