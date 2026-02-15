package uk.ac.ucl.eof.android.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.AppSettings
import uk.ac.ucl.eof.android.model.AppState
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.model.PixelFitSummary
import uk.ac.ucl.eof.android.model.SourceConfig
import uk.ac.ucl.eof.android.service.PhenologyService
import uk.ac.ucl.eof.android.service.SyntheticEOService

class EORepository {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateAoi(newAoi: AoiConfig) {
        _state.value = _state.value.copy(aoi = newAoi)
    }

    fun updateSettings(newSettings: AppSettings) {
        _state.value = _state.value.copy(settings = newSettings)
    }

    fun updateSource(source: SourceConfig) {
        _state.value = _state.value.copy(
            sources = _state.value.sources.map { if (it.type == source.type) source else it }
        )
    }

    suspend fun fetchFromBestSources() {
        val s = _state.value
        val enabled = s.sources.filter { it.enabled && it.type.supportsPixels }
        if (enabled.isEmpty()) {
            _state.value = s.copy(status = "No enabled pixel-capable source")
            return
        }
        _state.value = s.copy(loading = true, status = "Fetching synthetic frames...")
        delay(300)

        val merged = enabled.flatMap { src ->
            SyntheticEOService.fetchObservations(s.aoi, src.type)
        }.sortedBy { it.date }

        _state.value = _state.value.copy(
            loading = false,
            observations = merged,
            status = "Fetched ${merged.size} observations from ${enabled.size} sources"
        )
    }

    suspend fun compareFirstTwoEnabledSources() {
        val s = _state.value
        val enabled = s.sources.filter { it.enabled && it.type.supportsPixels }
        if (enabled.size < 2) {
            _state.value = s.copy(status = "Enable at least two pixel-capable sources")
            return
        }
        _state.value = s.copy(loading = true, status = "Running source comparison...")
        delay(250)

        val a = SyntheticEOService.fetchObservations(s.aoi, enabled[0].type)
        val b = SyntheticEOService.fetchObservations(s.aoi, enabled[1].type)
        val cmp = SyntheticEOService.compare(a, b)

        _state.value = _state.value.copy(
            loading = false,
            comparison = cmp,
            status = if (cmp == null) "Not enough paired observations" else "Compared ${cmp.sourceA.label} vs ${cmp.sourceB.label}"
        )
    }

    suspend fun fitPhenology() {
        val s = _state.value
        val ts = s.observations.groupBy { it.date }.map { (date, samples) ->
            samples.first().copy(ndvi = samples.map { it.ndvi }.average())
        }.sortedBy { it.date }

        if (ts.size < s.settings.minObservations) {
            _state.value = s.copy(status = "Need at least ${s.settings.minObservations} observations")
            return
        }

        _state.value = s.copy(loading = true, status = "Fitting double logistic...")
        delay(250)
        val fit = PhenologyService.fitMedian(ts, s.settings.ensembleRuns)

        val total = 640
        val good = (total * 0.82).toInt()
        val poor = (total * 0.14).toInt()
        val skipped = total - good - poor

        _state.value = _state.value.copy(
            loading = false,
            phenology = fit,
            pixelSummary = PixelFitSummary(good = good, poor = poor, skipped = skipped),
            status = if (fit == null) "Fit failed" else "Phenology fit completed"
        )
    }

    fun toggleSource(type: DataSourceType, enabled: Boolean) {
        updateSource(_state.value.sources.first { it.type == type }.copy(enabled = enabled))
    }
}
