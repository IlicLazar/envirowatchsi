package geojson

data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>
)

data class GeoJsonFeature(
    val geometry: GeoJsonGeometry,
    val properties: Map<String, String> = emptyMap()
)

sealed interface GeoJsonGeometry

data class GeoJsonPoint(
    val coordinates: List<Double>
) : GeoJsonGeometry

data class GeoJsonPolygon(
    val coordinates: List<List<List<Double>>>
) : GeoJsonGeometry
