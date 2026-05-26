package geojson

import ast.AirStationNode
import ast.AreaNode
import ast.AqiNode
import ast.CityItemNode
import ast.CityNode
import ast.FloodThresholdNode
import ast.GenericStationNode
import ast.HydroStationNode
import ast.HydroMeasurementNode
import ast.IntervalNode
import ast.MeasurementNode
import ast.MeteoStationNode
import ast.PointNode
import ast.PollutantNode
import ast.ProgramNode
import ast.SourceNode
import ast.StatusNode
import ast.ThresholdNode
import ast.WeatherMeasurementNode
import ast.WindMeasurementNode

object GeoJsonConverter {
    fun convert(program: ProgramNode): GeoJsonFeatureCollection =
        GeoJsonFeatureCollection(
            features = program.cities.flatMap { city ->
                city.items.mapNotNull { item -> convertCityItem(city, item) }
            }
        )

    private fun convertCityItem(city: CityNode, item: CityItemNode): GeoJsonFeature? =
        when (item) {
            is AreaNode -> GeoJsonFeature(
                geometry = item.points.toGeoJsonPolygon(),
                properties = mapOf(
                    "city" to city.name,
                    "kind" to "area",
                    "name" to item.name
                )
            )

            is GenericStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "station",
                stationType = item.type,
                geometry = item.location.toGeoJsonPoint(),
                properties = mapOf("type" to item.type)
            )

            is AirStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "airStation",
                stationType = "air",
                geometry = item.location.toGeoJsonPoint(),
                properties = measurementProperties(item.items) +
                        metadataProperties(item.items) +
                        thresholdProperties(item.items)
            )

            is MeteoStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "meteoStation",
                stationType = "meteo",
                geometry = item.location.toGeoJsonPoint(),
                properties = measurementProperties(item.items) + metadataProperties(item.items)
            )

            is HydroStationNode -> stationFeature(
                city = city,
                name = item.name,
                kind = "hydroStation",
                stationType = "hydro",
                geometry = item.location.toGeoJsonPoint(),
                properties = mapOf("river" to item.river) +
                        measurementProperties(item.items) +
                        metadataProperties(item.items) +
                        thresholdProperties(item.items)
            )

            else -> null
        }

    private fun stationFeature(
        city: CityNode,
        name: String,
        kind: String,
        stationType: String,
        geometry: GeoJsonPoint,
        properties: Map<String, String> = emptyMap()
    ): GeoJsonFeature =
        GeoJsonFeature(
            geometry = geometry,
            properties = mapOf(
                "city" to city.name,
                "kind" to kind,
                "name" to name,
                "stationType" to stationType,
                "longitude" to geometry.coordinates[0].toString(),
                "latitude" to geometry.coordinates[1].toString()
            ) + properties
        )

    private fun PointNode.toGeoJsonPoint(): GeoJsonPoint =
        GeoJsonPoint(
            coordinates = listOf(longitude.toDouble(), latitude.toDouble())
        )

    private fun List<PointNode>.toGeoJsonPolygon(): GeoJsonPolygon {
        val ring = map { it.toCoordinates() }.let { coordinates ->
            if (coordinates.firstOrNull() == coordinates.lastOrNull()) {
                coordinates
            } else {
                coordinates + listOf(coordinates.first())
            }
        }

        return GeoJsonPolygon(coordinates = listOf(ring))
    }

    private fun PointNode.toCoordinates(): List<Double> =
        listOf(longitude.toDouble(), latitude.toDouble())

    private fun measurementProperties(items: List<Any>): Map<String, String> {
        val measurementTypes = items.flatMap { item ->
            when (item) {
                is MeasurementNode -> listOf(item.name)
                is AqiNode -> listOf("aqi")
                is PollutantNode -> listOf(item.type)
                is WeatherMeasurementNode -> listOf(item.type)
                is WindMeasurementNode -> listOf("wind")
                is HydroMeasurementNode -> listOf(item.type)
                is IntervalNode -> measurementProperties(item.measurements)["measurementTypes"]
                    ?.split(",")
                    .orEmpty()
                else -> emptyList()
            }
        }.distinct()

        return if (measurementTypes.isEmpty()) {
            emptyMap()
        } else {
            mapOf("measurementTypes" to measurementTypes.joinToString(","))
        }
    }

    private fun metadataProperties(items: List<Any>): Map<String, String> =
        buildMap {
            items.filterIsInstance<SourceNode>().firstOrNull()?.let { source ->
                put("source", source.value)
            }
            items.filterIsInstance<StatusNode>().firstOrNull()?.let { status ->
                put("status", status.value)
            }
        }

    private fun thresholdProperties(items: List<Any>): Map<String, String> =
        buildMap {
            val thresholds = items.filterIsInstance<ThresholdNode>()
                .joinToString(";") { threshold ->
                    "${threshold.parameter}:warning=${threshold.warning},critical=${threshold.critical}"
                }

            if (thresholds.isNotEmpty()) {
                put("thresholds", thresholds)
            }

            items.filterIsInstance<FloodThresholdNode>().firstOrNull()?.let { threshold ->
                put("floodThreshold", "warning=${threshold.warning},critical=${threshold.critical}")
            }
        }
}
