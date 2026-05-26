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
        assertEquals("air", feature.properties["stationType"])
        assertEquals("14.512", feature.properties["longitude"])
        assertEquals("46.065", feature.properties["latitude"])
    }

    @Test
    fun `typed station exports station attributes`() {
        val program = parse(
            """
                city "Celje" {
                    river "Savinja";
                    hydroStation "Lasko" river "Savinja" at (15.2,46.1) {
                        waterLevel 142 unit "cm";
                    };
                }
            """.trimIndent()
        )

        val featureCollection = GeoJsonConverter.convert(program)
        val feature = featureCollection.features.single()

        assertEquals("hydroStation", feature.properties["kind"])
        assertEquals("hydro", feature.properties["stationType"])
        assertEquals("Savinja", feature.properties["river"])
        assertEquals("15.2", feature.properties["longitude"])
        assertEquals("46.1", feature.properties["latitude"])
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
