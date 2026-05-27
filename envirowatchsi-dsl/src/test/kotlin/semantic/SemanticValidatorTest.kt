package semantic

import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SemanticValidatorTest {
    @Test
    fun `valid program passes semantic validation`() {
        val program = """
            city "Ljubljana" {
                airStation "Center" at (14.5,46.0) {
                    source "ARSO";
                    status active;
                    pollutant pm10 unit "ug/m3";
                    aqi 64;
                };
            }
        """.trimIndent()

        SemanticValidator.validateOrThrow(parse(program))
    }

    @Test
    fun `longitude must be inside valid range`() {
        val error = validateInvalidProgram(
            """
                city "Ljubljana" {
                    airStation "Center" at (200.0,46.0) {
                        aqi 64;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "Longituda")
    }

    @Test
    fun `latitude must be inside valid range`() {
        val error = validateInvalidProgram(
            """
                city "Ljubljana" {
                    airStation "Center" at (14.5,95.0) {
                        aqi 64;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "Latituda")
    }

    @Test
    fun `coordinates must be decimal`() {
        val error = validateInvalidProgram(
            """
                city "Ljubljana" {
                    airStation "Center" at (14,46) {
                        aqi 64;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "decimalni obliki")
    }

    @Test
    fun `polygon must contain at least three points`() {
        val error = validateInvalidProgram(
            """
                city "Koper" {
                    area "Center" polygon ((13.72,45.54), (13.73,45.54));
                }
            """.trimIndent()
        )

        assertContainsError(error, "vsaj tri točke")
    }

    @Test
    fun `threshold warning must be lower than critical`() {
        val error = validateInvalidProgram(
            """
                city "Ljubljana" {
                    airStation "Center" at (14.5,46.0) {
                        aqi 64;
                        threshold pm10 warning 100 critical 50;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "warning (100) manjši od critical (50)")
    }

    @Test
    fun `air station must contain at least one measurement or aqi`() {
        val error = validateInvalidProgram(
            """
                city "Ljubljana" {
                    airStation "Center" at (14.5,46.0) {
                        source "ARSO";
                        status active;
                        pollutant pm10 unit "ug/m3";
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "vsaj eno meritev ali AQI")
    }

    @Test
    fun `meteo station must contain at least one weather measurement`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        source "ARSO";
                        status active;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "vsaj eno vremensko meritev")
    }

    @Test
    fun `air station can contain interval measurements`() {
        val program = """
            city "Ljubljana" {
                airStation "Center" at (14.5,46.0) {
                    interval from "2026-05-20T00:00" to "2026-05-20T03:00" step "1h" {
                        measurement pm10 = 42 unit "ug/m3";
                        aqi 64;
                    }
                };
            }
        """.trimIndent()

        SemanticValidator.validateOrThrow(parse(program))
    }

    @Test
    fun `interval accepts unquoted datetime literals`() {
        val program = """
            city "Maribor" {
                meteoStation "Weather" at (15.6,46.5) {
                    interval from 2026-05-20T00:00 to 2026-05-20T03:00 step "1h" {
                        temperature 21.4 unit "C" at 2026-05-20T01:00;
                    }
                };
            }
        """.trimIndent()

        SemanticValidator.validateOrThrow(parse(program))
    }

    @Test
    fun `hydro station must contain at least one hydro measurement`() {
        val error = validateInvalidProgram(
            """
                city "Celje" {
                    hydroStation "Lasko" river "Savinja" at (15.2,46.1) {
                        source "ARSO";
                        status active;
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "vsaj eno hidrološko meritev")
    }

    @Test
    fun `hydro station river must be declared when city declares rivers`() {
        val error = validateInvalidProgram(
            """
                city "Celje" {
                    river "Drava";

                    hydroStation "Lasko" river "Savinja" at (15.2,46.1) {
                        waterLevel 142 unit "cm";
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "ni deklarirana")
    }

    @Test
    fun `meteo interval cannot contain hydro measurements`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        interval from "2026-05-20T00:00" to "2026-05-20T03:00" step "1h" {
                            waterLevel 142 unit "cm";
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "neustreznega tipa: waterLevel")
    }

    @Test
    fun `hydro interval cannot contain weather measurements`() {
        val error = validateInvalidProgram(
            """
                city "Celje" {
                    hydroStation "Lasko" river "Savinja" at (15.2,46.1) {
                        interval from "2026-05-20T00:00" to "2026-05-20T03:00" step "1h" {
                            temperature 21.4 unit "C";
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "neustreznega tipa: temperature")
    }

    @Test
    fun `interval start must be before end`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        interval from 2026-05-20T03:00 to 2026-05-20T00:00 step "1h" {
                            temperature 21.4 unit "C";
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "mora biti pred koncem")
    }

    @Test
    fun `interval datetime must use iso format`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        interval from "20-05-2026 00:00" to 2026-05-20T03:00 step "1h" {
                            temperature 21.4 unit "C";
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "ISO-8601")
    }

    @Test
    fun `measurement time must be inside interval`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        interval from 2026-05-20T00:00 to 2026-05-20T03:00 step "1h" {
                            temperature 21.4 unit "C" at 2026-05-20T04:00;
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "znotraj intervala")
    }

    @Test
    fun `interval step must be positive duration`() {
        val error = validateInvalidProgram(
            """
                city "Maribor" {
                    meteoStation "Weather" at (15.6,46.5) {
                        interval from 2026-05-20T00:00 to 2026-05-20T03:00 step "one hour" {
                            temperature 21.4 unit "C";
                        }
                    };
                }
            """.trimIndent()
        )

        assertContainsError(error, "Korak")
    }

    private fun validateInvalidProgram(program: String): SemanticValidationException =
        assertFailsWith<SemanticValidationException> {
            SemanticValidator.validateOrThrow(parse(program))
        }

    private fun parse(program: String) =
        Parser(Lexer(program).tokenize()).parse()

    private fun assertContainsError(error: SemanticValidationException, expected: String) {
        assertTrue(
            actual = error.errors.any { expected in it.message },
            message = "Expected semantic error containing '$expected', got: ${error.message}"
        )
    }
}
