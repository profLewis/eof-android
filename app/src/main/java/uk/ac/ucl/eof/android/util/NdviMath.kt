package uk.ac.ucl.eof.android.util

object NdviMath {
    fun ndvi(nir: Double, red: Double): Double {
        val denom = nir + red
        if (denom == 0.0) return 0.0
        return (nir - red) / denom
    }

    fun dvi(nir: Double, red: Double): Double = nir - red
}
