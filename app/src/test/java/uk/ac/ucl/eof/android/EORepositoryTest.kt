package uk.ac.ucl.eof.android

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.ac.ucl.eof.android.repository.EORepository

class EORepositoryTest {
    @Test
    fun fetchAndFit_updatesState() = runTest {
        val repo = EORepository()

        repo.fetchFromBestSources()
        val afterFetch = repo.state.value
        assertTrue(afterFetch.observations.isNotEmpty())

        repo.fitPhenology()
        val afterFit = repo.state.value
        assertNotNull(afterFit.phenology)
        assertNotNull(afterFit.pixelSummary)
    }

    @Test
    fun compare_requiresTwoSources() = runTest {
        val repo = EORepository()

        repo.compareFirstTwoEnabledSources()
        val state = repo.state.value
        assertNotNull(state.comparison)
        assertTrue(state.comparison!!.sampleCount >= 5)
    }
}
