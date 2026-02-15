package uk.ac.ucl.eof.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.AppSettings
import uk.ac.ucl.eof.android.repository.EORepository

class MainViewModel(
    private val repository: EORepository = EORepository()
) : ViewModel() {
    val state: StateFlow<uk.ac.ucl.eof.android.model.AppState> = repository.state

    fun updateAoi(aoi: AoiConfig) = repository.updateAoi(aoi)
    fun updateSettings(settings: AppSettings) = repository.updateSettings(settings)
    fun toggleSource(type: uk.ac.ucl.eof.android.model.DataSourceType, enabled: Boolean) = repository.toggleSource(type, enabled)

    fun fetch() = viewModelScope.launch { repository.fetchFromBestSources() }
    fun compare() = viewModelScope.launch { repository.compareFirstTwoEnabledSources() }
    fun fitPhenology() = viewModelScope.launch { repository.fitPhenology() }
}
