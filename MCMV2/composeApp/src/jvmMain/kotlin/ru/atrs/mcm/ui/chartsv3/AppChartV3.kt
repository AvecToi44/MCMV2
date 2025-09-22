package ru.atrs.mcm.ui.chartsv3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.atrs.mcm.ui.snackBarShow
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow
import ru.atrsx.chartviewer.koala.ExperimentalKoalaPlotApi
import ru.atrsx.chartviewer.koala.gestures.GestureConfig
import ru.atrsx.chartviewer.koala.line.LinePlot
import ru.atrsx.chartviewer.koala.style.LineStyle
import ru.atrsx.chartviewer.koala.xygraph.Point
import ru.atrsx.chartviewer.koala.xygraph.XYGraph
import ru.atrsx.chartviewer.koala.xygraph.autoScaleXRange
import ru.atrsx.chartviewer.koala.xygraph.autoScaleYRange
import ru.atrsx.chartviewer.koala.xygraph.rememberFloatLinearAxisModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

// ==============================
// Data & parsing
// ==============================

/** Parsing outcome with human-readable reasons for snackbars and UI */
sealed class ParseOutcome {
    data class Success(val data: ChartData) : ParseOutcome()
    data class Error(val reason: String) : ParseOutcome()
}

/** Immutable, stable chart data bundle */
data class ChartData(
    val fileName: String,
    val series: List<List<Point<Float, Float>>>,  // size = channelCount
    val visibility: List<Boolean>,                // size = channelCount
    val pathEffect: PathEffect? = null
)

/** Pattern guard: strict header validation + robust data parsing. */
suspend fun parseChartFileStrict(
    path: String,
    pathEffect: PathEffect? = null
): ParseOutcome = withContext(Dispatchers.IO) {
    val f = File(path)
    if (!f.exists()) return@withContext ParseOutcome.Error("File does not exist")

    // Read all lines once (file sizes are moderate for plots; change to streaming if huge)
    val lines = try {
        f.bufferedReader().useLines { it.toList() }
    } catch (e: Exception) {
        return@withContext ParseOutcome.Error("Failed to read file: ${e.message}")
    }
    if (lines.isEmpty()) return@withContext ParseOutcome.Error("File is empty")

    // --- Validate headers
    // 0: #standard#<name_or_anything>
    // 1: #visibility#1#1#1#... (0/1 flags)
    // 2: #  (separator)
    if (lines.size < 4) return@withContext ParseOutcome.Error("Too few lines for header")

    val header0 = lines[0].trim()
    if (!header0.startsWith("#standard#"))
        return@withContext ParseOutcome.Error("Line 1 must start with #standard#")

    val header1 = lines[1].trim()
    if (!header1.startsWith("#visibility#"))
        return@withContext ParseOutcome.Error("Line 2 must start with #visibility#")

    val header2 = lines[2].trim()
    if (header2 != "#")
        return@withContext ParseOutcome.Error("Line 3 must be exactly '#'")

    // visibility flags (after '#visibility#')
    val rawVis = header1.split('#').drop(2) // skip ["", "visibility"]
    val visFlags = rawVis.mapNotNull {
        when (it) {
            "0" -> false
            "1" -> true
            else -> null // ignore garbage
        }
    }

    // --- Find first data line (first line not starting with '#')
    val dataStartIdx = lines.indexOfFirst { it.isNotBlank() && !it.trim().startsWith("#") }
    if (dataStartIdx == -1) return@withContext ParseOutcome.Error("No data rows after header")

    // Infer channelCount from the first data line
    val firstDataLine = lines[dataStartIdx].trim()
    val channelCount = countPairsSafe(firstDataLine)
    if (channelCount <= 0) return@withContext ParseOutcome.Error("Cannot infer channels from first data row")

    // Pad or trim visibility to channelCount
    val visibility = if (visFlags.isEmpty()) {
        List(channelCount) { true }
    } else {
        when {
            visFlags.size == channelCount -> visFlags
            visFlags.size > channelCount  -> visFlags.take(channelCount)
            else                          -> visFlags + List(channelCount - visFlags.size) { true }
        }
    }

    // Parse data rows
    val series = List(channelCount) { mutableListOf<Point<Float, Float>>() }
    var anyPoint = false

    fun parseRow(row: String) {
        // row example: "2;0.00000|2;0.00000|...|2;24.99389|"
        // tolerate trailing '|' and spaces
        val pieces = row.split('|').filter { it.isNotBlank() }
        // Each piece must be "x;y"
        pieces.forEachIndexed { idx, seg ->
            if (idx >= channelCount) return@forEachIndexed
            val xy = seg.split(';')
            if (xy.size == 2) {
                val x = xy[0].toFloatOrNull()
                val y = xy[1].toFloatOrNull()
                if (x != null && y != null) {
                    series[idx].add(Point(x, y))
                    anyPoint = true
                }
            }
        }
    }

    for (i in dataStartIdx until lines.size) {
        val row = lines[i].trim()
        if (row.isEmpty() || row.startsWith("#")) continue
        parseRow(row)
    }

    if (!anyPoint) return@withContext ParseOutcome.Error("No numeric points parsed")

    ParseOutcome.Success(
        ChartData(
            fileName = f.name,
            series = series.map { it.toList() },
            visibility = visibility,
            pathEffect = pathEffect
        )
    )
}

/** Counts "x;y" entries in a line safely. */
private fun countPairsSafe(line: String): Int =
    line.split('|')
        .count { seg ->
            val trimmed = seg.trim()
            val parts = trimmed.split(';')
            parts.size == 2 && parts[0].toFloatOrNull() != null && parts[1].toFloatOrNull() != null
        }

// ==============================
// Window & Screen
// ==============================

class AppChartV3 {
    @Composable
    fun WindowChartsV3(analysisAfterExperiment: Boolean = false) {
        Window(
            title = "ChartViewer V3 [Для зума зажми: Ctrl + Колесо мыши, для горизонтальной прокрутки: Shift + Колесо мыши]",
            state = WindowState(size = DpSize(1200.dp, 800.dp)),
            onCloseRequest = {
                if (doOpen_First_ChartWindow.value && analysisAfterExperiment) {
                    doOpen_First_ChartWindow.value = false
                }
                if (doOpen_Second_ChartWindow.value && !analysisAfterExperiment) {
                    doOpen_Second_ChartWindow.value = false
                }
            }
        ) {
            Box {
                App(analysisAfterExperiment = analysisAfterExperiment)
                snackBarShow()
            }
        }
    }
}

@Composable
fun App(analysisAfterExperiment: Boolean = false) {
    // Initial paths (if provided by your external state)
    var path1 by remember { mutableStateOf(chartFileAfterExperiment.value?.absolutePath?.takeIf { analysisAfterExperiment }) }
    var path2 by remember { mutableStateOf(chartFileStandard.value?.absolutePath?.takeIf { analysisAfterExperiment }) }
    var path3 by remember { mutableStateOf<String?>(null) }

    // Parse 3 files independently
    val data1 by remember(path1) { mutableStateOf<ResultOrNull>(ResultOrNull.Loading) }
        .let { _ ->
            produceState<ResultOrNull>(initialValue = ResultOrNull.Loading, path1) {
                value = loadData(path1, null) // file 1: solid
            }
        }

    val data2 by produceState<ResultOrNull>(initialValue = ResultOrNull.Loading, path2) {
        value = loadData(path2, PathEffect.dashPathEffect(floatArrayOf(10f, 10f))) // file 2: long dash
    }

    val data3 by produceState<ResultOrNull>(initialValue = ResultOrNull.Loading, path3) {
        value = loadData(path3, PathEffect.dashPathEffect(floatArrayOf(2f, 6f)))   // file 3: short dash
    }

    // Visibility per dataset (bound to actual channel counts)
    var vis1 by remember { mutableStateOf<List<Boolean>>(emptyList()) }
    var vis2 by remember { mutableStateOf<List<Boolean>>(emptyList()) }
    var vis3 by remember { mutableStateOf<List<Boolean>>(emptyList()) }

    // Sync visibility arrays whenever parse results change
    LaunchedEffect(data1) { vis1 = (data1 as? ResultOrNull.Success)?.chartData?.visibility ?: emptyList() }
    LaunchedEffect(data2) { vis2 = (data2 as? ResultOrNull.Success)?.chartData?.visibility ?: emptyList() }
    LaunchedEffect(data3) { vis3 = (data3 as? ResultOrNull.Success)?.chartData?.visibility ?: emptyList() }

    val seriesColors = remember {
        listOf(
            Color(0xFF2E7D32),  // Dark Green
            Color(0xFF5D4037),  // Brown
            Color(0xFF101010),  // Dark Gray
            Color(0xFF1976D2),  // Blue
            Color(0xFFD32F2F),  // Red
            Color(0xFFFBC02D),  // Yellow
            Color(0xFFDE4504),  // Orange-ish
            Color(0xFF7F7F7F),  // Light Gray
            Color(0xFF9C27B0),  // Purple
            Color(0xFFE91E63),  // Pink
            Color(0xFFBE9FDA),  // Light Purple
            Color(0xFF4CAF50),  // Green
            Color(0xFF8D6E63),  // Light Brown
            Color(0xFFAB47BC),  // Medium Purple
            Color(0xFF66BB6A),  // Light Green
            Color(0xFFF06292)   // Light Pink
        )
    }

    var overlapHalves by remember { mutableStateOf(false) } // <— NEW
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Compact header (put this inside Column, instead of 3 FilePickerRow calls) ---
            HeaderBar(
                slots = listOf(
                    HeaderSlot(
                        label = "File 1",
                        path = path1,
                        result = data1,
                        visibility = vis1,
                        onPick = { path1 = it },
                        onClear = { path1 = null },
                        onToggleAll = { vis1 = toggleAll(vis1) },
                        onToggleIdx = { idx -> vis1 = vis1.updateIndex(idx) }
                    ),
                    HeaderSlot(
                        label = "File 2",
                        path = path2,
                        result = data2,
                        visibility = vis2,
                        onPick = { path2 = it },
                        onClear = { path2 = null },
                        onToggleAll = { vis2 = toggleAll(vis2) },
                        onToggleIdx = { idx -> vis2 = vis2.updateIndex(idx) }
                    ),
                    HeaderSlot(
                        label = "File 3",
                        path = path3,
                        result = data3,
                        visibility = vis3,
                        onPick = { path3 = it },
                        onClear = { path3 = null },
                        onToggleAll = { vis3 = toggleAll(vis3) },
                        onToggleIdx = { idx -> vis3 = vis3.updateIndex(idx) }
                    ),
                ),
                colors = seriesColors
            )


            // Chart area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val ds1 = (data1 as? ResultOrNull.Success)?.chartData
                val ds2 = (data2 as? ResultOrNull.Success)?.chartData
                val ds3 = (data3 as? ResultOrNull.Success)?.chartData

                val datasets = listOfNotNull(ds1, ds2, ds3)
                val vis = listOfNotNull(
                    ds1?.let { vis1 },
                    ds2?.let { vis2 },
                    ds3?.let { vis3 }
                )
                if (datasets.isNotEmpty()) {
                    ChartView(datasets = datasets, visibilityStates = vis, colors = seriesColors, overlapHalves = overlapHalves)
                }



//            Box(Modifier.align(Alignment.TopStart).padding(10.dp)) {
//                StickyHint()
//            }
            }
        }

        // NEW: top-right plate for toggles
        TogglesPlate(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            overlapHalves = overlapHalves,
            onOverlapChanged = { overlapHalves = it }
        )
    }

}

// Helper sealed for produceState
sealed class ResultOrNull {
    object Loading : ResultOrNull()
    data class Success(val chartData: ChartData) : ResultOrNull()
    data class Failure(val message: String) : ResultOrNull()
}

private suspend fun loadData(path: String?, effect: PathEffect?): ResultOrNull {
    if (path.isNullOrBlank()) return ResultOrNull.Failure("No file chosen")
    return when (val out = parseChartFileStrict(path, effect)) {
        is ParseOutcome.Error -> {
            showMeSnackBar("[$path]: ${out.reason}")
            ResultOrNull.Failure(out.reason)
        }
        is ParseOutcome.Success -> ResultOrNull.Success(out.data)
    }
}

// ==============================
// UI bits
// ==============================

@Composable
private fun FilePickerRow(
    label: String,
    path: String?,
    result: ResultOrNull,
    visibility: List<Boolean>,
    onToggleAll: () -> Unit,
    onToggleIdx: (Int) -> Unit,
    onClear: () -> Unit,
    onPick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileButton(label, onPick)

        when (result) {
            is ResultOrNull.Loading -> Text("Loading...", fontSize = 12.sp)
            is ResultOrNull.Failure -> {
                val name = path?.let { File(it).name } ?: "(none)"
                Text("$name — ${result.message}", fontSize = 12.sp, color = Color(0xFFB00020))
            }
            is ResultOrNull.Success -> {
                val cd = result.chartData
                FileControl(
                    fileName = cd.fileName,
                    color = Color.Black,
                    visible = visibility.any { it },
                    onToggle = onToggleAll,
                    onClear = onClear
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cd.series.forEachIndexed { idx, series ->
                        if (series.isNotEmpty()) {
                            val on = visibility.getOrNull(idx) ?: false
                            val bg = if (on) Color(0xFF222222) else Color(0xFFBBBBBB)
                            Box(
                                modifier = Modifier
                                    .background(bg, RoundedCornerShape(4.dp))
                                    .clickable { onToggleIdx(idx) }
                                    .border(width = 2.dp, color = if (on) Color.Black else Color.White)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Ch${idx + 1}", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileControl(
    fileName: String,
    color: Color,
    visible: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(fileName, fontSize = 12.sp)
        SmallBtn(if (visible) "Hide" else "Show", onToggle)
        SmallBtn("Clear", onClear)
    }
}

@Composable
fun SmallBtn(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp, pressedElevation = 8.dp
        )
    ) { Text(text, fontSize = 12.sp) }
}

@Composable
private fun FileButton(
    label: String,
    onFileSelected: (String) -> Unit
) {
    Button(
        onClick = {
            FileDialog(null as Frame?, "Select $label", FileDialog.LOAD).apply {
                isVisible = true
                file?.let { filePath ->
                    val dir = directory ?: ""
                    val full = if (dir.endsWith(File.separator)) "$dir$filePath" else "$dir${File.separator}$filePath"
                    onFileSelected(full)
                }
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
    ) {
        Text(text = label, style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

// ==============================
// Chart view
// ==============================

@OptIn(ru.atrsx.chartviewer.koala.ExperimentalKoalaPlotApi::class)
@Composable
fun ChartView(
    datasets: List<ChartData>,
    visibilityStates: List<List<Boolean>>,
    colors: List<Color>,
    overlapHalves: Boolean
) {
    val maxPoints = 6000
    val minPoints = 200

    fun splitAndShift(series: List<Point<Float, Float>>): Pair<List<Point<Float, Float>>, List<Point<Float, Float>>> {
        if (series.size < 2) return series to emptyList()
        val mid = series.size / 2
        val first = series.subList(0, mid)
        val second = series.subList(mid, series.size)
        if (first.isEmpty() || second.isEmpty()) return first to emptyList()
        val dx = second.first().x - first.first().x
        val shiftedSecond = second.map { Point(it.x - dx, it.y) }
        return first to shiftedSecond
    }

    val downsampledAll by remember(datasets, visibilityStates, overlapHalves) {
        derivedStateOf {
            datasets.flatMapIndexed { di, cd ->
                cd.series.flatMapIndexed { si, series ->
                    if (visibilityStates.getOrNull(di)?.getOrNull(si) != true) emptyList()
                    else {
                        val seqs = if (!overlapHalves) listOf(series) else {
                            val (first, secondShifted) = splitAndShift(series)
                            listOf(first, secondShifted)
                        }
                        seqs.flatMap { seq ->
                            val step = (seq.size / minPoints).coerceAtLeast(1)
                            if (seq.size > maxPoints) seq.filterIndexed { idx, _ -> idx % step == 0 } else seq
                        }
                    }
                }
            }
        }
    }

    val xRange = downsampledAll.takeIf { it.isNotEmpty() }?.autoScaleXRange() ?: (0f..1f)
    val yRange = downsampledAll.takeIf { it.isNotEmpty() }?.autoScaleYRange() ?: (0f..1f)

    val xModel = rememberFloatLinearAxisModel(
        range = xRange,
        minViewExtent = (xRange.endInclusive - xRange.start) * 0.01f,
        maxViewExtent = xRange.endInclusive - xRange.start,
        minimumMajorTickIncrement = (xRange.endInclusive - xRange.start) * 0.005f,
        minimumMajorTickSpacing = 30.dp,
        minorTickCount = 4
    )
    val yModel = rememberFloatLinearAxisModel(
        range = yRange,
        minViewExtent = (yRange.endInclusive - yRange.start) * 0.01f,
        maxViewExtent = yRange.endInclusive - yRange.start,
        minimumMajorTickIncrement = (yRange.endInclusive - yRange.start) * 0.005f,
        minimumMajorTickSpacing = 30.dp,
        minorTickCount = 4
    )

    Box(Modifier.fillMaxSize()) {
        XYGraph(
            xAxisModel = xModel,
            yAxisModel = yModel,
            modifier = Modifier.fillMaxSize(),
            gestureConfig = GestureConfig(
                panXEnabled = true,
                panYEnabled = true,
                zoomXEnabled = true,
                zoomYEnabled = true,
                independentZoomEnabled = false
            )
        ) {
            datasets.forEachIndexed { di, cd ->
                cd.series.forEachIndexed { si, series ->
                    if (visibilityStates.getOrNull(di)?.getOrNull(si) != true) return@forEachIndexed

                    val baseColor = colors[si % colors.size]

                    @Composable
                    fun plot(seq: List<Point<Float, Float>>, isSecondHalf: Boolean) {
                        val step = (seq.size / minPoints).coerceAtLeast(1)
                        val plotData =
                            if (seq.size > maxPoints) seq.filterIndexed { idx, _ -> idx % step == 0 } else seq

                        val dotted = if (isSecondHalf) PathEffect.dashPathEffect(floatArrayOf(4f, 6f)) else null
                        val combinedEffect = when {
                            cd.pathEffect != null && dotted != null -> dotted
                            else -> cd.pathEffect ?: dotted
                        }

                        val style = LineStyle(
                            brush = SolidColor(if (isSecondHalf) baseColor.copy(alpha = 0.65f) else baseColor),
                            strokeWidth = 2.dp,
                            pathEffect = combinedEffect
                        )

                        LinePlot(
                            data = plotData,
                            lineStyle = style,
                            symbol = null
                        )
                    }

                    if (!overlapHalves) {
                        plot(series, isSecondHalf = false)
                    } else {
                        val (first, secondShifted) = splitAndShift(series)
                        if (first.isNotEmpty()) plot(first, isSecondHalf = false)
                        if (secondShifted.isNotEmpty()) plot(secondShifted, isSecondHalf = true)
                    }
                }
            }
        }
    }
}



// ==============================
// Small helpers
// ==============================

private fun toggleAll(current: List<Boolean>): List<Boolean> {
    if (current.isEmpty()) return current
    val newState = !current.any { it }
    return List(current.size) { newState }
}

private fun List<Boolean>.updateIndex(i: Int): List<Boolean> =
    mapIndexed { idx, v -> if (idx == i) !v else v }
