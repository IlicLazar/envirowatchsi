package geojson

import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GeoJsonConverterTest {
    @Test
    fun `station becomes point feature`() {
        val program = parse(
            """
                city "Ljubljana" {
                    station "Center" type air at (14.512,46.065);
                }
            """.trimIndent()
        )

        val featureCollection = GeoJsonConverter.convert(program)
        val feature = featureCollection.features.single()
        val geometry = assertIs<GeoJsonPoint>(feature.geometry)

        assertEquals(listOf(14.512, 46.065), geometry.coordinates)
        assertEquals("Ljubljana", feature.properties["city"])
        assertEquals("station", feature.properties["kind"])
        assertEquals("Center", feature.properties["name"])
        assertEquals("air", feature.properties["type"])
    }

    private fun parse(program: String) =
        Parser(Lexer(program).tokenize()).parse()
}
