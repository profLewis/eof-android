package uk.ac.ucl.eof.android

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.model.Observation
import uk.ac.ucl.eof.android.service.SyntheticEOService
import java.time.LocalDate

class SyntheticEOServiceEdgeCaseTest {
    @Test
    fun compare_returnsNullWhenNoOverlap() {
        val a = listOf(
            Observation(LocalDate.of(2025, 1, 1), 0.2, 0.2, 0.3, DataSourceType.AWS)
        )
        val b = listOf(
            Observation(LocalDate.of(2025, 2, 1), 0.25, 0.2, 0.3, DataSourceType.PLANETARY)
        )

        val cmp = SyntheticEOService.compare(a, b)
        assertNull(cmp)
    }

    @Test
    fun compare_returnsNullWhenTooFewPairedSamples() {
        val dates = listOf(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 10),
            LocalDate.of(2025, 1, 20),
            LocalDate.of(2025, 1, 30)
        )
        val a = dates.map { Observation(it, 0.2, 0.2, 0.3, DataSourceType.AWS) }
        val b = dates.map { Observation(it, 0.22, 0.2, 0.3, DataSourceType.PLANETARY) }

        val cmp = SyntheticEOService.compare(a, b)
        assertNull(cmp)
    }

    @Test
    fun fetchObservations_singleDayWindowStillProducesAtLeastOneSample() {
        val day = LocalDate.of(2025, 3, 15)
        val aoi = uk.ac.ucl.eof.android.model.AoiConfig(dateStart = day, dateEnd = day)

        val obs = SyntheticEOService.fetchObservations(aoi, DataSourceType.AWS)

        assertTrue(obs.isNotEmpty())
        assertTrue(obs.all { it.date == day })
    }
}
