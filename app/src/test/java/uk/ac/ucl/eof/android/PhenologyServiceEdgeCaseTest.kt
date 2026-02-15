package uk.ac.ucl.eof.android

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.model.Observation
import uk.ac.ucl.eof.android.model.PhenologyParams
import uk.ac.ucl.eof.android.service.PhenologyService
import java.time.LocalDate

class PhenologyServiceEdgeCaseTest {
    @Test
    fun fitMedian_returnsNullForInsufficientObservations() {
        val start = LocalDate.of(2025, 1, 1)
        val observations = (0 until 3).map { i ->
            Observation(
                date = start.plusDays((i * 20).toLong()),
                ndvi = 0.2 + i * 0.05,
                red = 0.2,
                nir = 0.3,
                source = DataSourceType.AWS
            )
        }

        val fit = PhenologyService.fitMedian(observations, runs = 4)
        assertNull(fit)
    }

    @Test
    fun evaluate_hasHigherGrowingSeasonValueThanDormantSeason() {
        val p = PhenologyParams(
            mn = 0.15,
            mx = 0.85,
            sos = 90.0,
            rsp = 0.08,
            eos = 280.0,
            rau = 0.08,
            rmse = 0.01
        )

        val dormant = PhenologyService.evaluate(p, dayOfYear = 20.0)
        val peak = PhenologyService.evaluate(p, dayOfYear = 180.0)

        assertTrue(peak > dormant)
        assertTrue(dormant in -0.5..1.2)
        assertTrue(peak in -0.5..1.2)
    }
}
