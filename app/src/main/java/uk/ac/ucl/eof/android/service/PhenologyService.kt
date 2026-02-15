package uk.ac.ucl.eof.android.service

import uk.ac.ucl.eof.android.model.Observation
import uk.ac.ucl.eof.android.model.PhenologyParams
import java.time.temporal.ChronoField
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

object PhenologyService {
    fun evaluate(p: PhenologyParams, dayOfYear: Double): Double {
        val spring = 1.0 / (1.0 + exp(-p.rsp * (dayOfYear - p.sos)))
        val autumn = 1.0 / (1.0 + exp(p.rau * (dayOfYear - p.eos)))
        return p.mn + (p.mx - p.mn) * (spring + autumn - 1.0)
    }

    fun fitMedian(observations: List<Observation>, runs: Int = 8): PhenologyParams? {
        if (observations.size < 4) return null
        val ys = observations.map { it.ndvi }.sorted()
        val mnGuess = ys[(ys.size * 0.1).toInt().coerceIn(0, ys.lastIndex)]
        val mxGuess = ys[(ys.size * 0.9).toInt().coerceIn(0, ys.lastIndex)]
        val base = PhenologyParams(
            mn = mnGuess,
            mx = mxGuess.coerceAtLeast(mnGuess + 0.1),
            sos = 80.0,
            rsp = 0.05,
            eos = 280.0,
            rau = 0.05,
            rmse = Double.POSITIVE_INFINITY
        )

        var best = base
        repeat(runs.coerceAtLeast(1)) { idx ->
            val start = if (idx == 0) base else perturb(base)
            val fit = localSearch(start, observations)
            if (fit.rmse < best.rmse) best = fit
        }
        return best
    }

    private fun perturb(base: PhenologyParams): PhenologyParams {
        fun p(v: Double, pct: Double): Double {
            val f = 1.0 + Random.nextDouble(-pct, pct)
            return v * f
        }
        return base.copy(
            mn = p(base.mn, 0.5).coerceIn(-0.5, 0.8),
            mx = p(base.mx, 0.5).coerceIn(0.0, 1.2),
            sos = p(base.sos, 0.25).coerceIn(1.0, 250.0),
            rsp = p(base.rsp, 0.2).coerceIn(0.001, 0.5),
            eos = p(base.eos, 0.25).coerceIn(100.0, 366.0),
            rau = p(base.rau, 0.2).coerceIn(0.001, 0.5)
        )
    }

    private fun localSearch(start: PhenologyParams, obs: List<Observation>): PhenologyParams {
        var current = start.copy(rmse = rmse(start, obs))
        val step = doubleArrayOf(0.02, 0.02, 6.0, 0.01, 6.0, 0.01)
        repeat(120) {
            var improved = false
            for (i in 0..5) {
                val candidates = listOf(-1.0, 1.0).map { dir ->
                    tweak(current, i, dir * step[i])
                }
                val better = candidates.minBy { rmse(it, obs) }
                val betterRmse = rmse(better, obs)
                if (betterRmse + 1e-9 < current.rmse) {
                    current = better.copy(rmse = betterRmse)
                    improved = true
                }
            }
            if (!improved) {
                for (i in step.indices) step[i] *= 0.8
            }
        }
        return current
    }

    private fun tweak(p: PhenologyParams, idx: Int, d: Double): PhenologyParams = when (idx) {
        0 -> p.copy(mn = (p.mn + d).coerceIn(-0.5, 0.8))
        1 -> p.copy(mx = (p.mx + d).coerceIn(0.0, 1.2))
        2 -> p.copy(sos = (p.sos + d).coerceIn(1.0, 250.0))
        3 -> p.copy(rsp = (p.rsp + d).coerceIn(0.001, 0.5))
        4 -> p.copy(eos = (p.eos + d).coerceIn(100.0, 366.0))
        else -> p.copy(rau = (p.rau + d).coerceIn(0.001, 0.5))
    }

    private fun rmse(p: PhenologyParams, obs: List<Observation>): Double {
        var sum = 0.0
        for (o in obs) {
            val doy = o.date.getLong(ChronoField.DAY_OF_YEAR).toDouble()
            val e = evaluate(p, doy) - o.ndvi
            sum += e * e
        }
        return sqrt(sum / obs.size)
    }
}
