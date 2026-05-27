package parser

import ast.IntervalNode
import ast.MeteoStationNode
import ast.WeatherMeasurementNode
import lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {
    @Test
    fun `stores interval and measurement times as datetime nodes`() {
        val program = Parser(
            Lexer(
                """
                    city "Maribor" {
                        meteoStation "Weather" at (15.6,46.5) {
                            interval from 2026-05-20T00:00 to 2026-05-20T03:00 step "1h" {
                                temperature 21.4 unit "C" at 2026-05-20T01:00;
                            }
                        };
                    }
                """.trimIndent()
            ).tokenize()
        ).parse()

        val station = assertIs<MeteoStationNode>(program.cities.single().items.single())
        val interval = assertIs<IntervalNode>(station.items.single())
        val measurement = assertIs<WeatherMeasurementNode>(interval.measurements.single())

        assertEquals("2026-05-20T00:00", interval.from.value)
        assertEquals("2026-05-20T03:00", interval.to.value)
        assertEquals("2026-05-20T01:00", measurement.time?.value)
    }
}
