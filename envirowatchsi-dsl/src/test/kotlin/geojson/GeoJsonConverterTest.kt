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

    @Test
    fun `area becomes polygon feature with closed ring`() {
        val program = parse(
            """
                city "Koper" {
                    area "Center" polygon ((13.72,45.54), (13.73,45.54), (13.73,45.55));
                }
            """.trimIndent()
        )

        val featureCollection = GeoJsonConverter.convert(program)
        val feature = featureCollection.features.single()
        val geometry = assertIs<GeoJsonPolygon>(feature.geometry)

        assertEquals(
            listOf(
                listOf(
                    listOf(13.72, 45.54),
                    listOf(13.73, 45.54),
                    listOf(13.73, 45.55),
                    listOf(13.72, 45.54)
                )
            ),
            geometry.coordinates
        )
        assertEquals("Koper", feature.properties["city"])
        assertEquals("area", feature.properties["kind"])
        assertEquals("Center", feature.properties["name"])
    }

    private fun parse(program: String) =
        Parser(Lexer(program).tokenize()).parse()
}
