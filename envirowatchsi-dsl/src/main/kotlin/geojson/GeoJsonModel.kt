package geojson

data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>
)

data class GeoJsonFeature(
    val geometry: GeoJsonGeometry,
    val properties: Map<String, String> = emptyMap()
)

sealed interface GeoJsonGeometry
