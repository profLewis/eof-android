package uk.ac.ucl.eof.android

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.model.AppSettings
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.repository.EORepository

class EORepositoryEdgeCaseTest {
    @Test
    fun fetch_reportsWhenNoPixelCapableSourceEnabled() = runTest {
        val repo = EORepository()
        repo.toggleSource(DataSourceType.AWS, false)
        repo.toggleSource(DataSourceType.PLANETARY, false)
        repo.toggleSource(DataSourceType.EARTHDATA, false)
        repo.toggleSource(DataSourceType.GEE, false)

        repo.fetchFromBestSources()
        val state = repo.state.value

        assertEquals("No enabled pixel-capable source", state.status)
        assertTrue(state.observations.isEmpty())
    }

    @Test
    fun compare_reportsWhenFewerThanTwoSourcesEnabled() = runTest {
        val repo = EORepository()
        repo.toggleSource(DataSourceType.PLANETARY, false)

        repo.compareFirstTwoEnabledSources()
        val state = repo.state.value

        assertEquals("Enable at least two pixel-capable sources", state.status)
        assertNull(state.comparison)
    }

    @Test
    fun fitPhenology_reportsWhenObservationCountTooLow() = runTest {
        val repo = EORepository()
        repo.updateSettings(AppSettings(minObservations = 50, ensembleRuns = 4))
        repo.fetchFromBestSources()

        repo.fitPhenology()
        val state = repo.state.value

        assertEquals("Need at least 50 observations", state.status)
        assertNull(state.phenology)
        assertNull(state.pixelSummary)
    }
}
