package lexer
import geojson.GeoJsonConverter
import geojson.GeoJsonSerializer
import parser.Parser
import ast.AstPrinter
import semantic.SemanticValidator
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    if (args.firstOrNull() == "geojson") {
        printGeoJson(args)
        return
    }

    val program = """
        city "TestCity" {
            // comment that the lexer should ignore

            station "Generic Air Station" type air at (-14.512,46.065);

            airStation "Air Quality" at (14.512,46.065) {
                source "ARSO";
                status active;
                pollutant pm10 unit "ug/m3";
                aqi 64 at "2026-05-21T12:00";
                threshold pm10 warning 50 critical 100;
            };

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

    try {
        val tokens = lexer.tokenize()

        val parser = Parser(tokens)
        val ast = parser.parse()
        SemanticValidator.validateOrThrow(ast)

        tokens.forEach {
            println("${it.type} '${it.lexeme}' [${it.line}:${it.column}]")
        }

        println("\nProgram je sintaktično in semantično pravilen.\n")

        AstPrinter.print(ast)
    } catch (error: RuntimeException) {
        println("Program ni veljaven:")
        println(error.message)
    }
}

private fun printGeoJson(args: Array<String>) {
    if (args.size != 2) {
        throw IllegalArgumentException("Uporaba: geojson <pot-do-dsl-datoteke>")
    }

    val source = Files.readString(Path.of(args[1]))
    val ast = Parser(Lexer(source).tokenize()).parse()
    SemanticValidator.validateOrThrow(ast)

    val featureCollection = GeoJsonConverter.convert(ast)
    println(GeoJsonSerializer.serialize(featureCollection))
}
