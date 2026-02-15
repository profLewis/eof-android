package uk.ac.ucl.eof.android.service

import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.DataSourceType
import uk.ac.ucl.eof.android.model.Observation
import uk.ac.ucl.eof.android.model.SourceComparison
import uk.ac.ucl.eof.android.util.NdviMath
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

object SyntheticEOService {
    fun fetchObservations(aoi: AoiConfig, source: DataSourceType): List<Observation> {
        val days = max(1L, ChronoUnit.DAYS.between(aoi.dateStart, aoi.dateEnd))
        val step = max(5L, days / 24L)
        return generateSequence(aoi.dateStart) { d -> d.plusDays(step) }
            .takeWhile { !it.isAfter(aoi.dateEnd) }
            .map { date ->
                val t = date.dayOfYear.toDouble() / 365.0
                val peak = when (source) {
                    DataSourceType.AWS -> 0.76
                    DataSourceType.PLANETARY -> 0.75
                    DataSourceType.CDSE -> 0.74
                    DataSourceType.EARTHDATA -> 0.72
                    DataSourceType.GEE -> 0.75
                }
                val ndviTrue = 0.15 + peak * kotlin.math.sin((t - 0.2) * Math.PI).coerceAtLeast(0.0)
                val noise = Random.nextDouble(-0.02, 0.02)
                val ndvi = (ndviTrue + noise).coerceIn(-0.2, 0.95)
                val nir = (0.25 + ndvi * 0.5 + Random.nextDouble(-0.02, 0.02)).coerceIn(0.01, 1.2)
                val red = (nir * (1 - ndvi) / (1 + ndvi)).coerceIn(0.01, 1.2)
                Observation(
                    date = date,
                    ndvi = NdviMath.ndvi(nir, red),
                    red = red,
                    nir = nir,
                    source = source
                )
            }
            .toList()
    }

    fun compare(a: List<Observation>, b: List<Observation>): SourceComparison? {
        if (a.isEmpty() || b.isEmpty()) return null
        val byDateB = b.associateBy { it.date }
        val pairs = a.mapNotNull { oa -> byDateB[oa.date]?.let { ob -> oa.ndvi to ob.ndvi } }
        if (pairs.size < 5) return null

        val diffs = pairs.map { it.first - it.second }
        val bias = diffs.average()
        val rmse = kotlin.math.sqrt(diffs.map { it * it }.average())

        val ys = pairs.map { it.second }
        val meanY = ys.average()
        val ssRes = pairs.sumOf { (it.second - it.first).pow(2.0) }
        val ssTot = ys.sumOf { (it - meanY).pow(2.0) }.coerceAtLeast(1e-9)
        val r2 = (1.0 - ssRes / ssTot).coerceIn(-1.0, 1.0)

        return SourceComparison(
            sourceA = a.first().source,
            sourceB = b.first().source,
            ndviBias = bias,
            ndviRmse = rmse,
            ndviR2 = r2,
            sampleCount = pairs.size
        )
    }
}
