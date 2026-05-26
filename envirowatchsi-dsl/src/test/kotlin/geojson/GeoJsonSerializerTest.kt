package geojson

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoJsonSerializerTest {
    @Test
    fun `serializes feature collection with point and polygon`() {
        val featureCollection = GeoJsonFeatureCollection(
            features = listOf(
                GeoJsonFeature(
                    geometry = GeoJsonPoint(listOf(14.512, 46.065)),
                    properties = mapOf("city" to "Ljubljana", "name" to "Center")
                ),
                GeoJsonFeature(
                    geometry = GeoJsonPolygon(
                        listOf(
                            listOf(
                                listOf(13.72, 45.54),
                                listOf(13.73, 45.54),
                                listOf(13.73, 45.55),
                                listOf(13.72, 45.54)
                            )
                        )
                    ),
                    properties = mapOf("city" to "Koper", "kind" to "area")
                )
            )
        )

        val json = GeoJsonSerializer.serialize(featureCollection)

        assertEquals(
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[14.512,46.065]},"properties":{"city":"Ljubljana","name":"Center"}},{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[13.72,45.54],[13.73,45.54],[13.73,45.55],[13.72,45.54]]]},"properties":{"city":"Koper","kind":"area"}}]}""",
            json
        )
    }

    @Test
    fun `escapes property strings`() {
        val featureCollection = GeoJsonFeatureCollection(
            features = listOf(
                GeoJsonFeature(
                    geometry = GeoJsonPoint(listOf(14.512, 46.065)),
                    properties = mapOf("name" to "Station \"Center\"")
                )
            )
        )

        val json = GeoJsonSerializer.serialize(featureCollection)

        assertEquals(
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[14.512,46.065]},"properties":{"name":"Station \"Center\""}}]}""",
            json
        )
    }
}
