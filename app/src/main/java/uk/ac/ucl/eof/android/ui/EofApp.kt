package uk.ac.ucl.eof.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.ac.ucl.eof.android.model.AoiConfig
import uk.ac.ucl.eof.android.model.AppSettings
import uk.ac.ucl.eof.android.model.AppState
import kotlin.math.roundToInt

private enum class Tab(val label: String) {
    FETCH("Fetch"),
    SOURCES("Sources"),
    PHENOLOGY("Phenology"),
    SETTINGS("Settings")
}

@Composable
fun EofApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.FETCH) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Tab.FETCH to Icons.Default.CloudDownload,
                    Tab.SOURCES to Icons.Default.Storage,
                    Tab.PHENOLOGY to Icons.Default.Analytics,
                    Tab.SETTINGS to Icons.Default.Settings
                ).forEach { (t, icon) ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("EOF Android", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(state.status, style = MaterialTheme.typography.bodyMedium)
            }

            when (tab) {
                Tab.FETCH -> fetchScreen(state, vm)
                Tab.SOURCES -> sourcesScreen(state, vm)
                Tab.PHENOLOGY -> phenologyScreen(state, vm)
                Tab.SETTINGS -> settingsScreen(state, vm)
            }
        }
    }
}

private fun LazyListScope.fetchScreen(state: AppState, vm: MainViewModel) {
    item {
        AoiCard(state.aoi) { vm.updateAoi(it) }
    }
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::fetch, enabled = !state.loading) {
                Text("Fetch")
            }
            Button(onClick = vm::compare, enabled = !state.loading) {
                Text("Compare Sources")
            }
        }
    }
    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Median NDVI Time Series", fontWeight = FontWeight.Medium)
                if (state.observations.isEmpty()) {
                    Text("No observations yet")
                } else {
                    val medianSeries = state.observations.groupBy { it.date }
                        .toSortedMap()
                        .values
                        .map { pts -> pts.map { it.ndvi }.average() }
                    LineChart(points = medianSeries)
                }
            }
        }
    }
    state.comparison?.let { cmp ->
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${cmp.sourceA.label} vs ${cmp.sourceB.label}", fontWeight = FontWeight.Medium)
                    Text("Bias: ${"%.4f".format(cmp.ndviBias)}")
                    Text("RMSE: ${"%.4f".format(cmp.ndviRmse)}")
                    Text("R²: ${"%.3f".format(cmp.ndviR2)}")
                    Text("Samples: ${cmp.sampleCount}")
                }
            }
        }
    }
}

private fun LazyListScope.sourcesScreen(state: AppState, vm: MainViewModel) {
    item {
        Text("Data Sources", style = MaterialTheme.typography.titleMedium)
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

private fun LazyListScope.phenologyScreen(state: AppState, vm: MainViewModel) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::fitPhenology, enabled = !state.loading) { Text("Fit Phenology") }
        }
    }
    item {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Double Logistic", fontWeight = FontWeight.Medium)
                val p = state.phenology
                if (p == null) {
                    Text("No fit yet")
                } else {
                    Text("mn=${"%.3f".format(p.mn)} mx=${"%.3f".format(p.mx)}")
                    Text("SOS=${p.sos.roundToInt()} EOS=${p.eos.roundToInt()}")
                    Text("rsp=${"%.3f".format(p.rsp)} rau=${"%.3f".format(p.rau)}")
                    Text("RMSE=${"%.4f".format(p.rmse)}")
                }
            }
        }
    }
    state.pixelSummary?.let { s ->
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Per-Pixel Fit Summary", fontWeight = FontWeight.Medium)
                    Text("Good: ${s.good}")
                    Text("Poor: ${s.poor}")
                    Text("Skipped: ${s.skipped}")
                }
            }
        }
    }
}

private fun LazyListScope.settingsScreen(state: AppState, vm: MainViewModel) {
    item {
        SettingsCard(state.settings, vm::updateSettings)
    }
    item {
        Text(
            "This Android baseline mirrors the iOS architecture (AOI, sources, comparison, phenology) and is ready for real STAC/COG service adapters.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AoiCard(aoi: AoiConfig, onChange: (AoiConfig) -> Unit) {
    var lat by remember(aoi.latitude) { mutableStateOf(aoi.latitude.toString()) }
    var lon by remember(aoi.longitude) { mutableStateOf(aoi.longitude.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
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
        }
    }
}

@Composable
private fun LineChart(points: List<Double>, modifier: Modifier = Modifier) {
    if (points.size < 2) {
        Text("Insufficient data")
        return
    }
    val minY = points.minOrNull() ?: 0.0
    val maxY = points.maxOrNull() ?: 1.0
    val span = (maxY - minY).takeIf { it > 1e-9 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val w = size.width
        val h = size.height

        drawLine(Color.Gray, Offset(0f, h - 1), Offset(w, h - 1), 1f)
        drawLine(Color.Gray, Offset(1f, 0f), Offset(1f, h), 1f)

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
