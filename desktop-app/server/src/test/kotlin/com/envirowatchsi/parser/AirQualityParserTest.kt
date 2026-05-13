package com.envirowatchsi.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AirQualityParserTest {

    private val validXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
            <postaja sifra="MB1" wgs84_sirina="46.5547" wgs84_dolzina="15.6459">
                <merilno_mesto>Maribor</merilno_mesto>
                <datum_od>2024-01-15T12:00:00</datum_od>
                <pm10>35.0</pm10>
                <pm2.5>18.0</pm2.5>
                <o3>80.0</o3>
                <co>0.5</co>
                <so2>5.0</so2>
            </postaja>
        </arsopodatki>
    """.trimIndent()

    private val emptyXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
        </arsopodatki>
    """.trimIndent()

    private val missingPollutantsXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
            <postaja sifra="LJ1" wgs84_sirina="46.0651" wgs84_dolzina="14.5124">
                <merilno_mesto>Ljubljana</merilno_mesto>
                <datum_od>2024-01-15T08:00:00</datum_od>
                <pm10></pm10>
                <pm2.5></pm2.5>
                <o3></o3>
                <co></co>
                <so2></so2>
            </postaja>
        </arsopodatki>
    """.trimIndent()

    private val invalidValueXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
            <postaja sifra="KP1" wgs84_sirina="45.5469" wgs84_dolzina="13.7299">
                <merilno_mesto>Koper</merilno_mesto>
                <datum_od>2024-01-15T10:00:00</datum_od>
                <pm10>0.5</pm10>
                <pm2.5>12.0</pm2.5>
                <o3></o3>
                <co></co>
                <so2></so2>
            </postaja>
        </arsopodatki>
    """.trimIndent()

    @Test
    fun `parseAirQualityData returns correct station`() {
        val result = parseAirQualityData(validXml)

        assertEquals(1, result.size)
        assertEquals("MB1", result[0].stationId)
        assertEquals("Maribor", result[0].stationName)
        assertEquals(46.5547, result[0].latitude, 0.0001)
        assertEquals(15.6459, result[0].longitude, 0.0001)
        assertEquals(35.0, result[0].pm10!!, 0.0001)
        assertEquals(18.0, result[0].pm2_5!!, 0.0001)
        assertEquals(80.0, result[0].airQualityIndex!!, 0.0001)
    }

    @Test
    fun `parseAirQualityData returns empty list for empty XML`() {
        val result = parseAirQualityData(emptyXml)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseAirQualityData sets null for empty pollutants`() {
        val result = parseAirQualityData(missingPollutantsXml)

        assertEquals(1, result.size)
        assertNull(result[0].pm10)
        assertNull(result[0].pm2_5)
        assertNull(result[0].o3)
        assertNull(result[0].airQualityIndex)
    }

    @Test
    fun `parseAirQualityData ignores value less than 1`() {
        val result = parseAirQualityData(invalidValueXml)

        assertEquals(1, result.size)
        assertNull(result[0].pm10)
        assertNotNull(result[0].pm2_5)
    }
}