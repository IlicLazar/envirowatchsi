package com.envirowatchsi.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HydroParserTest {

    private val validXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
            <postaja sifra="1234" wgs84_sirina="46.0512" wgs84_dolzina="14.5061">
                <merilno_mesto>Ljubljana</merilno_mesto>
                <reka>Sava</reka>
                <datum>2024-01-15</datum>
                <ura>12:00</ura>
                <vodostaj>150.5</vodostaj>
                <pretok>23.7</pretok>
            </postaja>
        </arsopodatki>
    """.trimIndent()

    private val emptyXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
        </arsopodatki>
    """.trimIndent()

    private val missingValuesXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <arsopodatki>
            <postaja sifra="5678" wgs84_sirina="" wgs84_dolzina="">
                <merilno_mesto>Maribor</merilno_mesto>
                <reka>Drava</reka>
                <datum>2024-01-15</datum>
                <ura>08:00</ura>
                <vodostaj></vodostaj>
                <pretok></pretok>
            </postaja>
        </arsopodatki>
    """.trimIndent()

    @Test
    fun `parseHydroData returns correct station`() {
        val result = parseHydroData(validXml)

        assertEquals(1, result.size)
        assertEquals("1234", result[0].stationId)
        assertEquals("Ljubljana", result[0].stationName)
        assertEquals("Sava", result[0].riverName)
        assertEquals(46.0512, result[0].latitude!!, 0.0001)
        assertEquals(14.5061, result[0].longitude!!, 0.0001)
        assertEquals("2024-01-15 12:00", result[0].measuredAt)
        assertEquals(150.5, result[0].waterLevel!!, 0.0001)
        assertEquals(23.7, result[0].waterFlow!!, 0.0001)
    }

    @Test
    fun `parseHydroData returns empty list for empty XML`() {
        val result = parseHydroData(emptyXml)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseHydroData sets null for missing values`() {
        val result = parseHydroData(missingValuesXml)

        assertEquals(1, result.size)
        assertNull(result[0].waterLevel)
        assertNull(result[0].waterFlow)
        assertNull(result[0].latitude)
        assertNull(result[0].longitude)
    }
}