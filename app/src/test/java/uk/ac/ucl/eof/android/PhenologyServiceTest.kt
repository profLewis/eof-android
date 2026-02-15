package uk.ac.ucl.eof.android

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.model.Observation
import uk.ac.ucl.eof.android.service.PhenologyService
import java.time.LocalDate

class PhenologyServiceTest {
    @Test
    fun fitMedian_returnsParametersForSeasonalSignal() {
        val start = LocalDate.of(2025, 1, 1)
        val observations = (0 until 24).map { i ->
            val d = start.plusDays((i * 14).toLong())
            val day = d.dayOfYear.toDouble()
            val ndvi = 0.15 + 0.65 * kotlin.math.sin(((day / 365.0) - 0.2) * Math.PI).coerceAtLeast(0.0)
            Observation(d, ndvi, red = 0.2, nir = 0.3, source = DataSourceType.AWS)
        }
        val fit = PhenologyService.fitMedian(observations, runs = 4)
        assertNotNull(fit)
        assertTrue(fit!!.mx > fit.mn)
        assertTrue(fit.rmse < 0.2)
    }
}
