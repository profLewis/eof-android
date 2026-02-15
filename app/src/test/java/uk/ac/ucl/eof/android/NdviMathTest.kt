package uk.ac.ucl.eof.android

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.ac.ucl.eof.android.util.NdviMath

class NdviMathTest {
    @Test
    fun ndvi_handlesNormalValues() {
        val ndvi = NdviMath.ndvi(nir = 0.42, red = 0.18)
        assertEquals(0.4, ndvi, 1e-6)
    }

    @Test
    fun ndvi_handlesZeroDenominator() {
        val ndvi = NdviMath.ndvi(nir = 0.0, red = 0.0)
        assertEquals(0.0, ndvi, 1e-9)
    }
}
