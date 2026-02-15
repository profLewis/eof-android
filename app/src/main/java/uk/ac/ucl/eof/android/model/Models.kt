package uk.ac.ucl.eof.android.model

import java.time.LocalDate

enum class DataSourceType(val label: String, val supportsPixels: Boolean, val requiresAuth: Boolean) {
    AWS("AWS Earth Search", true, false),
    PLANETARY("Planetary Computer", true, false),
    CDSE("Copernicus Data Space", false, true),
    EARTHDATA("NASA Earthdata HLS", true, true),
    GEE("Google Earth Engine", true, true)
}

data class SourceConfig(
    val type: DataSourceType,
    val enabled: Boolean = type == DataSourceType.AWS || type == DataSourceType.PLANETARY,
    val username: String = "",
    val password: String = "",
    val notes: String = ""
)

data class AoiConfig(
    val latitude: Double = -32.12,
    val longitude: Double = 20.98,
    val geoJsonUrl: String = "",
    val dateStart: LocalDate = LocalDate.now().minusMonths(8),
    val dateEnd: LocalDate = LocalDate.now(),
    val cloudThresholdPct: Int = 40
)

data class AppSettings(
    val maxConcurrency: Int = 4,
    val useDvi: Boolean = false,
    val sclMaskEnabled: Boolean = true,
    val minObservations: Int = 4,
    val ensembleRuns: Int = 8,
    val showSpectralEnvelope: Boolean = true,
    val dynamicYScale: Boolean = true
)

data class Observation(
    val date: LocalDate,
    val ndvi: Double,
    val red: Double,
    val nir: Double,
    val source: DataSourceType
)

data class SourceComparison(
    val sourceA: DataSourceType,
    val sourceB: DataSourceType,
    val ndviBias: Double,
    val ndviRmse: Double,
    val ndviR2: Double,
    val sampleCount: Int
)

data class PhenologyParams(
    val mn: Double,
    val mx: Double,
    val sos: Double,
    val rsp: Double,
    val eos: Double,
    val rau: Double,
    val rmse: Double
)

data class PixelFitSummary(
    val good: Int,
    val poor: Int,
    val skipped: Int
)

data class AppState(
    val aoi: AoiConfig = AoiConfig(),
    val settings: AppSettings = AppSettings(),
    val sources: List<SourceConfig> = DataSourceType.entries.map { SourceConfig(it) },
    val observations: List<Observation> = emptyList(),
    val comparison: SourceComparison? = null,
    val phenology: PhenologyParams? = null,
    val pixelSummary: PixelFitSummary? = null,
    val loading: Boolean = false,
    val status: String = "Ready"
)
