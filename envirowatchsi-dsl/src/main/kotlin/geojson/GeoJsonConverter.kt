package geojson

import ast.AirStationNode
import ast.CityItemNode
import ast.CityNode
import ast.GenericStationNode
import ast.HydroStationNode
import ast.MeteoStationNode
import ast.PointNode
import ast.ProgramNode

object GeoJsonConverter {
    fun convert(program: ProgramNode): GeoJsonFeatureCollection =
        GeoJsonFeatureCollection(
            features = program.cities.flatMap { city ->
                city.items.mapNotNull { item -> convertCityItem(city, item) }
            }
        )

    private fun convertCityItem(city: CityNode, item: CityItemNode): GeoJsonFeature? =
        when (item) {
            is GenericStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "station",
                geometry = item.location.toGeoJsonPoint(),
                properties = mapOf("type" to item.type)
            )

            is AirStationNode -> stationFeature(city, item.name, "airStation", item.location.toGeoJsonPoint())
            is MeteoStationNode -> stationFeature(city, item.name, "meteoStation", item.location.toGeoJsonPoint())
            is HydroStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "hydroStation",
                geometry = item.location.toGeoJsonPoint(),
                properties = mapOf("river" to item.river)
            )

            else -> null
        }

    private fun stationFeature(
        city: CityNode,
        name: String,
        kind: String,
        geometry: GeoJsonGeometry,
        properties: Map<String, String> = emptyMap()
    ): GeoJsonFeature =
        GeoJsonFeature(
            geometry = geometry,
            properties = mapOf(
                "city" to city.name,
                "kind" to kind,
                "name" to name
            ) + properties
        )

    private fun PointNode.toGeoJsonPoint(): GeoJsonPoint =
        GeoJsonPoint(
            coordinates = listOf(longitude.toDouble(), latitude.toDouble())
        )
}
