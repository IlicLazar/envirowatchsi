package com.envirowatchsi.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MeteoParserTest {

    private val validXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <meteoSI>
            <metData>
                <domain_meteosiId>LJUBL</domain_meteosiId>
                <domain_shortTitle>Ljubljana</domain_shortTitle>
                <domain_lat>46.0651</domain_lat>
                <domain_lon>14.5124</domain_lon>
                <tsValid_issued>2024-01-15T12:00:00</tsValid_issued>
                <t>18.5</t>
                <rh>65.0</rh>
                <ff_val>3.2</ff_val>
                <dd_shortText>NE</dd_shortText>
                <tp_acc>0.0</tp_acc>
            </metData>
        </meteoSI>
    """.trimIndent()

    private val emptyXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <meteoSI>
        </meteoSI>
    """.trimIndent()

    private val missingTempXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <meteoSI>
            <metData>
                <domain_meteosiId>MARIB</domain_meteosiId>
                <domain_shortTitle>Maribor</domain_shortTitle>
                <domain_lat>46.5547</domain_lat>
                <domain_lon>15.6459</domain_lon>
                <tsValid_issued>2024-01-15T08:00:00</tsValid_issued>
                <t></t>
                <rh></rh>
                <ff_val></ff_val>
                <dd_shortText></dd_shortText>
                <tp_acc></tp_acc>
            </metData>
        </meteoSI>
    """.trimIndent()

    private val optionalFieldsXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <meteoSI>
            <metData>
                <domain_meteosiId>KOPER</domain_meteosiId>
                <domain_shortTitle>Koper</domain_shortTitle>
                <domain_lat>45.5469</domain_lat>
                <domain_lon>13.7299</domain_lon>
                <tsValid_issued>2024-01-15T10:00:00</tsValid_issued>
                <t>22.0</t>
                <rh>70.0</rh>
                <ff_val></ff_val>
                <dd_shortText></dd_shortText>
                <tp_acc></tp_acc>
            </metData>
        </meteoSI>
    """.trimIndent()

    @Test
    fun `parseMeteoData returns correct station`() {
        val result = parseMeteoData(validXml)

        assertEquals(1, result.size)
        assertEquals("LJUBL", result[0].stationId)
        assertEquals("Ljubljana", result[0].stationName)
        assertEquals(46.0651, result[0].latitude!!, 0.0001)
        assertEquals(14.5124, result[0].longitude!!, 0.0001)
        assertEquals(18.5, result[0].temperature, 0.0001)
        assertEquals(65.0, result[0].humidity, 0.0001)
        assertEquals(3.2, result[0].windSpeed!!, 0.0001)
        assertEquals("NE", result[0].windDirection)
    }

    @Test
    fun `parseMeteoData returns empty list for empty XML`() {
        val result = parseMeteoData(emptyXml)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseMeteoData skips station without temperature and humidity`() {
        val result = parseMeteoData(missingTempXml)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseMeteoData sets null for optional fields`() {
        val result = parseMeteoData(optionalFieldsXml)

        assertEquals(1, result.size)
        assertNull(result[0].windSpeed)
        assertNull(result[0].windDirection)
        assertNull(result[0].precipitation)
    }
}