package uk.ac.ucl.eof.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.AppSettings
import uk.ac.ucl.eof.android.model.AppState
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EofApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var showSources by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EOF", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = vm::fetch) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Fetch")
                    }
                    IconButton(onClick = { showSources = !showSources }) {
                        Icon(Icons.Default.Storage, contentDescription = "Sources")
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = vm::compare) {
                        Icon(Icons.Default.Refresh, contentDescription = "Compare")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5F7))
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { HeaderSection(state) }
            item { StatusSection(state, vm) }
            item { AoiCard(state.aoi) { vm.updateAoi(it) } }
            item { FramePanel(state) }
            item { NdviChartSection(state) }
            item { ComparisonSection(state) }
            item { PhenologySection(state, vm) }

            if (showSources) {
                item {
                    Text("Data Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                }
                items(items = state.sources, key = { it.type.name }) { source ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(source.type.label, fontWeight = FontWeight.Medium)
                                val flags = buildString {
                                    append(if (source.type.supportsPixels) "Pixel fetch" else "Search only")
                                    if (source.type.requiresAuth) append(" • Auth")
                                }
                                Text(flags, style = MaterialTheme.typography.bodySmall)
                            }
                            Checkbox(checked = source.enabled, onCheckedChange = { vm.toggleSource(source.type, it) })
                        }
                    }
                }
            }

            if (showSettings) {
                item {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                }
                item { SettingsCard(state.settings, vm::updateSettings) }
            }
        }
    }
}

@Composable
private fun HeaderSection(state: AppState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val enabled = state.sources.filter { it.enabled }.joinToString("+") { it.type.name }
            Text(
                "S2 NDVI | ${state.aoi.dateStart}–${state.aoi.dateEnd} | ${enabled.ifBlank { "No source" }}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "AOI: lat ${"%.3f".format(state.aoi.latitude)}, lon ${"%.3f".format(state.aoi.longitude)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusSection(state: AppState, vm: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp)
                }
                Text(state.status, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = vm::fetch, enabled = !state.loading) { Text("Fetch") }
                OutlinedButton(onClick = vm::compare, enabled = !state.loading) { Text("Compare") }
            }
        }
    }
}

@Composable
private fun FramePanel(state: AppState) {
    val frameCount = state.observations.groupBy { it.date }.size
    val latestDate = state.observations.maxByOrNull { it.date }?.date

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("NDVI Movie", fontWeight = FontWeight.Medium)
                Text(
                    if (latestDate == null) "No frames" else "$latestDate • ${frameCount} frames",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFFF7F8FA))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.88f)
                        .height(200.dp)
                        .border(1.dp, Color(0xFFCFD8DC))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFE8F5E9), Color(0xFFD7F0E9), Color(0xFFC6E7DF))
                            )
                        )
                ) {
                    Text(
                        if (state.observations.isEmpty()) "Fetch data to render frame panel"
                        else "Frame preview placeholder (map/raster renderer slot)",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF004D40)
                    )
                }
            }
        }
    }
}

@Composable
private fun NdviChartSection(state: AppState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Median NDVI Time Series", fontWeight = FontWeight.Medium)
            if (state.observations.isEmpty()) {
                Text("No observations yet")
            } else {
                val grouped = state.observations.groupBy { it.date }.toSortedMap().values
                val medianSeries = grouped.map { pts -> median(pts.map { it.ndvi }) }
                val lowSeries = grouped.map { pts -> pts.minOf { it.ndvi } }
                val highSeries = grouped.map { pts -> pts.maxOf { it.ndvi } }
                LineChart(
                    points = medianSeries,
                    lows = if (state.settings.showSpectralEnvelope) lowSeries else null,
                    highs = if (state.settings.showSpectralEnvelope) highSeries else null,
                    dynamicYScale = state.settings.dynamicYScale
                )
            }
        }
    }
}

@Composable
private fun ComparisonSection(state: AppState) {
    val cmp = state.comparison ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${cmp.sourceA.label} vs ${cmp.sourceB.label}", fontWeight = FontWeight.Medium)
            Text("Bias: ${"%.4f".format(cmp.ndviBias)}")
            Text("RMSE: ${"%.4f".format(cmp.ndviRmse)}")
            Text("R²: ${"%.3f".format(cmp.ndviR2)}")
            Text("Samples: ${cmp.sampleCount}")
        }
    }
}

@Composable
private fun PhenologySection(state: AppState, vm: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Phenology", fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Button(onClick = vm::fitPhenology, enabled = !state.loading) { Text("Fit") }
            }

            val p = state.phenology
            if (p == null) {
                Text("No fit yet")
            } else {
                Text("mn=${"%.3f".format(p.mn)} mx=${"%.3f".format(p.mx)}")
                Text("SOS=${p.sos.roundToInt()} EOS=${p.eos.roundToInt()}")
                Text("rsp=${"%.3f".format(p.rsp)} rau=${"%.3f".format(p.rau)}")
                Text("RMSE=${"%.4f".format(p.rmse)}")
            }

            state.pixelSummary?.let { s ->
                Text("Per-pixel: good ${s.good}, poor ${s.poor}, skipped ${s.skipped}")
            }
        }
    }
}

@Composable
private fun AoiCard(aoi: AoiConfig, onChange: (AoiConfig) -> Unit) {
    var lat by remember(aoi.latitude) { mutableStateOf(aoi.latitude.toString()) }
    var lon by remember(aoi.longitude) { mutableStateOf(aoi.longitude.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AOI", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = {
                        lat = it
                        it.toDoubleOrNull()?.let { v -> onChange(aoi.copy(latitude = v)) }
                    },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lon,
                    onValueChange = {
                        lon = it
                        it.toDoubleOrNull()?.let { v -> onChange(aoi.copy(longitude = v)) }
                    },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = aoi.geoJsonUrl,
                onValueChange = { onChange(aoi.copy(geoJsonUrl = it)) },
                label = { Text("GeoJSON URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Date range: ${aoi.dateStart} to ${aoi.dateEnd}")
            Text("Cloud threshold: ${aoi.cloudThresholdPct}%")
        }
    }
}

@Composable
private fun SettingsCard(settings: AppSettings, onChange: (AppSettings) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Pipeline Settings", fontWeight = FontWeight.Medium)

            Text("Max concurrency: ${settings.maxConcurrency}")
            Slider(
                value = settings.maxConcurrency.toFloat(),
                onValueChange = { onChange(settings.copy(maxConcurrency = it.roundToInt().coerceIn(1, 12))) },
                valueRange = 1f..12f
            )

            Text("Ensemble runs: ${settings.ensembleRuns}")
            Slider(
                value = settings.ensembleRuns.toFloat(),
                onValueChange = { onChange(settings.copy(ensembleRuns = it.roundToInt().coerceIn(1, 20))) },
                valueRange = 1f..20f
            )

            Text("Min observations: ${settings.minObservations}")
            Slider(
                value = settings.minObservations.toFloat(),
                onValueChange = { onChange(settings.copy(minObservations = it.roundToInt().coerceIn(3, 10))) },
                valueRange = 3f..10f
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = settings.sclMaskEnabled,
                    onCheckedChange = { onChange(settings.copy(sclMaskEnabled = it)) }
                )
                Spacer(Modifier.width(6.dp))
                Text("Enable SCL/Fmask cloud filtering")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = settings.useDvi,
                    onCheckedChange = { onChange(settings.copy(useDvi = it)) }
                )
                Spacer(Modifier.width(6.dp))
                Text("Use DVI instead of NDVI")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = settings.showSpectralEnvelope,
                    onCheckedChange = { onChange(settings.copy(showSpectralEnvelope = it)) }
                )
                Spacer(Modifier.width(6.dp))
                Text("Show NDVI envelope (min/max)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = settings.dynamicYScale,
                    onCheckedChange = { onChange(settings.copy(dynamicYScale = it)) }
                )
                Spacer(Modifier.width(6.dp))
                Text("Dynamic Y scale")
            }
        }
    }
}

@Composable
private fun LineChart(
    points: List<Double>,
    lows: List<Double>? = null,
    highs: List<Double>? = null,
    dynamicYScale: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Text("Insufficient data")
        return
    }
    val minY = if (dynamicYScale) {
        sequenceOf(points.minOrNull(), lows?.minOrNull(), highs?.minOrNull()).filterNotNull().minOrNull() ?: -0.2
    } else {
        -0.2
    }
    val maxY = if (dynamicYScale) {
        sequenceOf(points.maxOrNull(), lows?.maxOrNull(), highs?.maxOrNull()).filterNotNull().maxOrNull() ?: 1.0
    } else {
        1.0
    }
    val span = (maxY - minY).takeIf { it > 1e-9 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        val w = size.width
        val h = size.height

        drawLine(Color.Gray, Offset(0f, h - 1), Offset(w, h - 1), 1f)
        drawLine(Color.Gray, Offset(1f, 0f), Offset(1f, h), 1f)

        if (lows != null && highs != null && lows.size == points.size && highs.size == points.size) {
            for (i in lows.indices) {
                val x = i.toFloat() / (points.size - 1).toFloat() * w
                val yLow = h - (((lows[i] - minY) / span).toFloat() * h)
                val yHigh = h - (((highs[i] - minY) / span).toFloat() * h)
                drawLine(
                    color = Color(0x334A90E2),
                    start = Offset(x, yLow),
                    end = Offset(x, yHigh),
                    strokeWidth = 4f
                )
            }
        }

        for (i in 1 until points.size) {
            val x0 = (i - 1).toFloat() / (points.size - 1).toFloat() * w
            val x1 = i.toFloat() / (points.size - 1).toFloat() * w
            val y0 = h - (((points[i - 1] - minY) / span).toFloat() * h)
            val y1 = h - (((points[i] - minY) / span).toFloat() * h)
            drawLine(
                color = Color(0xFF00695C),
                start = Offset(x0, y0),
                end = Offset(x1, y1),
                strokeWidth = 4f
            )
        }
    }
}

private fun median(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0
    val sorted = values.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
}
