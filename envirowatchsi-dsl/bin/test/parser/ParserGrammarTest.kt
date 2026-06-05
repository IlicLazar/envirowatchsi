package parser

import ast.ForNode
import ast.IfNode
import ast.ListNode
import ast.PointValueNode
import ast.StringValueNode
import ast.WhileNode
import lexer.Lexer
import semantic.SemanticValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserGrammarTest {
    @Test
    fun `parses list and for statement items`() {
        val program = Parser(
            Lexer(
                """
                    city "Ljubljana" {
                        list pollutants = [pm10, pm2_5, o3, (14.5,46.0)];

                        for p in pollutants {
                            threshold p warning 50 critical 100;
                        }
                    }
                """.trimIndent()
            ).tokenize()
        ).parse()

        val city = program.cities.single()
        val list = assertIs<ListNode>(city.items[0])
        val loop = assertIs<ForNode>(city.items[1])

        assertEquals("pollutants", list.name)
        assertIs<PointValueNode>(list.values[3])
        assertEquals("p", loop.variable)
        assertEquals("pollutants", loop.iterable)
        assertEquals(1, loop.items.size)
    }

    @Test
    fun `parses complex statements inside station block`() {
        val program = Parser(
            Lexer(
                """
                    city "Ljubljana" {
                        airStation "Ljubljana Bezigrad" at (14.512,46.065) {
                            aqi 42;
                            list pollutants = [pm10, pm2_5, o3];
                            for p in pollutants {
                                threshold p warning 50 critical 100;
                            }
                            if pm10 > 50 {
                                status test;
                            } else {
                                status active;
                            }
                            while aqi > 100 {
                                threshold aqi warning 100 critical 150;
                            }
                        };
                    }
                """.trimIndent()
            ).tokenize()
        ).parse()

        val station = assertIs<ast.AirStationNode>(program.cities.single().items.single())

        assertIs<ListNode>(station.items[1])
        assertIs<ForNode>(station.items[2])
        assertIs<IfNode>(station.items[3])
        assertIs<WhileNode>(station.items[4])
        SemanticValidator.validateOrThrow(program)
    }
}
