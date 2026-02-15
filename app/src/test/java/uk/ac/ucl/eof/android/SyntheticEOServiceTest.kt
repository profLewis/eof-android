package uk.ac.ucl.eof.android

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.service.SyntheticEOService
import java.time.LocalDate

class SyntheticEOServiceTest {
    @Test
    fun fetchObservations_respectsDateWindowAndReturnsSamples() {
        val aoi = AoiConfig(
            dateStart = LocalDate.of(2025, 1, 1),
            dateEnd = LocalDate.of(2025, 6, 1)
        )
        val obs = SyntheticEOService.fetchObservations(aoi, DataSourceType.AWS)

        assertTrue(obs.isNotEmpty())
        assertTrue(obs.all { !it.date.isBefore(aoi.dateStart) && !it.date.isAfter(aoi.dateEnd) })
    }

    @Test
    fun compare_returnsMetricsForOverlappingSeries() {
        val aoi = AoiConfig(
            dateStart = LocalDate.of(2025, 1, 1),
            dateEnd = LocalDate.of(2025, 9, 1)
        )
        val aws = SyntheticEOService.fetchObservations(aoi, DataSourceType.AWS)
        val pc = SyntheticEOService.fetchObservations(aoi, DataSourceType.PLANETARY)
        val cmp = SyntheticEOService.compare(aws, pc)

        assertNotNull(cmp)
        assertTrue(cmp!!.sampleCount >= 5)
        assertTrue(cmp.ndviRmse >= 0.0)
        assertTrue(cmp.ndviR2 in -1.0..1.0)
    }
}
