package lexer

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun `geojson command prints geojson io compatible feature collection`() {
        val dslFile = Files.createTempFile("envirowatchsi", ".dsl")
        Files.writeString(
            dslFile,
            """
                city "Koper" {
                    area "Center" polygon ((13.72,45.54), (13.73,45.54), (13.73,45.55));
                    station "Port" type mixed at (13.73,45.55);
                }
            """.trimIndent()
        )

        val output = captureStdout {
            main(arrayOf("geojson", dslFile.toString()))
        }.trim()

        assertContains(output, """"type":"FeatureCollection"""")
        assertContains(output, """"type":"Polygon"""")
        assertContains(output, """"type":"Point"""")
        assertContains(output, """"coordinates":[13.73,45.55]""")
        assertEquals('{', output.first())
        assertEquals('}', output.last())
    }

    private fun captureStdout(block: () -> Unit): String {
        val originalOut = System.out
        val output = ByteArrayOutputStream()

        System.setOut(PrintStream(output))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }

        return output.toString()
    }
}
