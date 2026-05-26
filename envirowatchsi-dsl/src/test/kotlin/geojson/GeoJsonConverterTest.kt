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
    fun `typed stations export measurement types`() {
        val program = parse(
            """
                city "Maribor" {
                    airStation "Air" at (15.6,46.5) {
                        pollutant pm10 unit "ug/m3";
                        measurement no2 = 18 unit "ug/m3";
                        aqi 42;
                    };

                    meteoStation "Weather" at (15.7,46.6) {
                        temperature 21 unit "C";
                        wind speed 4 direction "NE";
                    };

                    hydroStation "River" river "Drava" at (15.8,46.7) {
                        interval from "2026-05-20T00:00" to "2026-05-20T01:00" step "1h" {
                            waterLevel 142 unit "cm";
                            waterFlow 87 unit "m3/s";
                        }
                    };
                }
            """.trimIndent()
        )

        val featuresByName = GeoJsonConverter.convert(program)
            .features
            .associateBy { it.properties["name"] }

        assertEquals("pm10,no2,aqi", featuresByName["Air"]?.properties?.get("measurementTypes"))
        assertEquals("temperature,wind", featuresByName["Weather"]?.properties?.get("measurementTypes"))
        assertEquals("waterLevel,waterFlow", featuresByName["River"]?.properties?.get("measurementTypes"))
    }

    @Test
    fun `typed stations export source and status`() {
        val program = parse(
            """
                city "Ljubljana" {
                    meteoStation "Weather" at (14.5,46.0) {
                        source "ARSO";
                        status active;
                        temperature 21 unit "C";
                    };
                }
            """.trimIndent()
        )

        val feature = GeoJsonConverter.convert(program).features.single()

        assertEquals("ARSO", feature.properties["source"])
        assertEquals("active", feature.properties["status"])
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
