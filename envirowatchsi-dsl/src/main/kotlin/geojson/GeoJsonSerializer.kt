package geojson

object GeoJsonSerializer {
    fun serialize(featureCollection: GeoJsonFeatureCollection): String =
        buildString {
            append("{")
            appendJsonProperty("type", "FeatureCollection")
            append(",\"features\":[")
            featureCollection.features.forEachIndexed { index, feature ->
                if (index > 0) append(",")
                appendFeature(feature)
            }
            append("]}")
        }

    private fun StringBuilder.appendFeature(feature: GeoJsonFeature) {
        append("{")
        appendJsonProperty("type", "Feature")
        append(",\"geometry\":")
        appendGeometry(feature.geometry)
        append(",\"properties\":")
        appendProperties(feature.properties)
        append("}")
    }

    private fun StringBuilder.appendGeometry(geometry: GeoJsonGeometry) {
        when (geometry) {
            is GeoJsonPoint -> {
                append("{")
                appendJsonProperty("type", "Point")
                append(",\"coordinates\":")
                appendCoordinates(geometry.coordinates)
                append("}")
            }

            is GeoJsonPolygon -> {
                append("{")
                appendJsonProperty("type", "Polygon")
                append(",\"coordinates\":")
                appendPolygonCoordinates(geometry.coordinates)
                append("}")
            }
        }
    }

    private fun StringBuilder.appendProperties(properties: Map<String, String>) {
        append("{")
        properties.entries.forEachIndexed { index, entry ->
            if (index > 0) append(",")
            appendJsonProperty(entry.key, entry.value)
        }
        append("}")
    }

    private fun StringBuilder.appendPolygonCoordinates(coordinates: List<List<List<Double>>>) {
        append("[")
        coordinates.forEachIndexed { ringIndex, ring ->
            if (ringIndex > 0) append(",")
            append("[")
            ring.forEachIndexed { pointIndex, point ->
                if (pointIndex > 0) append(",")
                appendCoordinates(point)
            }
            append("]")
        }
        append("]")
    }

    private fun StringBuilder.appendCoordinates(coordinates: List<Double>) {
        append("[")
        coordinates.forEachIndexed { index, coordinate ->
            if (index > 0) append(",")
            append(coordinate)
        }
        append("]")
    }

    private fun StringBuilder.appendJsonProperty(name: String, value: String) {
        append("\"")
        append(name.escapeJson())
        append("\":\"")
        append(value.escapeJson())
        append("\"")
    }

    private fun String.escapeJson(): String =
        buildString {
            this@escapeJson.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
}
