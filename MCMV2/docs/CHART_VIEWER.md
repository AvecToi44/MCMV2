# Chart Viewer V3 - Feature Documentation

## Overview

Chart Viewer V3 is a powerful chart analysis tool built with Jetpack Compose and the Koala charting library. It allows comparing up to 3 chart files simultaneously with full zoom/pan support.

## File Structure

```
ui/chartsv3/
├── AppChartV3.kt      # Main application composable
├── ChartUI.kt         # UI components (file slots, header bar)
├── ChartView.kt       # Koala-based chart rendering
├── PdfExporter.kt     # PDF export functionality
└── TogglesPlate.kt    # View options panel
```

## Core Features

### 1. Multi-File Support
- **3 File Slots**: Load up to 3 chart files for comparison
- **File Picker**: Native file dialog for selecting .txt chart files
- **Auto Line Styles**: Each file gets a distinct line style:
  - File 1: Solid line
  - File 2: Long dash (10f on, 6f off)
  - File 3: Short dash (4f on, 4f off)
- **Line Style Indicator**: Visual indicator under each filename showing the line style

### 2. Channel Visibility
- **Per-Channel Toggle**: Show/hide individual channels
- **Show/Hide All**: Quick toggle for all channels
- **Color Coding**: Each channel has a unique color
- **Channel Chips**: Visual chips showing channel status (Ch1, Ch2, etc.)

### 3. Chart Interaction
- **Zoom**: `Ctrl + Mouse Wheel` zooms in/out
- **Pan**: `Shift + Mouse Wheel` for horizontal panning
- **Independent Zoom**: Toggle X/Y axis zoom independently
- **Overlap Halves Mode**: First half uses normal style, second half uses dotted pattern

### 4. PDF Export
- **A4 Landscape Format**: Optimized for chart visualization
- **Auto Filename**: Uses most recent file's timestamp
- **Header**: Shows "MCMV2 Measurement from {timestamp}"
- **Cyrillic Support**: Uses Arial font for Russian text
- **Line Styles**: Preserves chart line styles in PDF
- **File + Channels Legend**: Filenames and channel chips displayed inline

## Key Classes

### AppChartV3.kt

**Main Application Composable**

```kotlin
@Composable
fun App(analysisAfterExperiment: Boolean = false)
```

- Manages 3 file paths (path1, path2, path3)
- Loads data with `produceState` for async loading
- Handles chart visibility state
- Coordinates PDF export

**Data Loading:**
```kotlin
private suspend fun loadData(path: String?, effect: PathEffect?): ResultOrNull
```

**PDF Export:**
```kotlin
fun exportPdf()
fun exportPdfTo1C()  // Placeholder for 1C integration
```

### ChartUI.kt

**HeaderSlot Data Class**
```kotlin
data class HeaderSlot(
    val label: String,
    val path: String?,
    val result: ResultOrNull,
    val visibility: List<Boolean>,
    val onPick: (String) -> Unit,
    val onClear: () -> Unit,
    val onToggleAll: () -> Unit,
    val onToggleIdx: (Int) -> Unit,
    val lineStyle: LineStyleIndicator = LineStyleIndicator.SOLID
)
```

**Line Style Indicator Enum**
```kotlin
enum class LineStyleIndicator {
    SOLID,
    LONG_DASH,
    SHORT_DASH
}
```

**Key Composables:**
- `HeaderBar(slots, colors)` - Renders all file slots
- `CompactFileSlot(slot, colors)` - Single file slot UI
- `CompactChip(label, color, active, onClick)` - Channel toggle chip
- `LineStyleIndicatorView(style)` - Visual line style under filename

### ChartView.kt

**Chart Rendering**

Uses Koala's XYGraph with custom configuration:
```kotlin
XYGraph(
    xAxisModel = xModel,
    yAxisModel = yModel,
    gestureConfig = GestureConfig(
        panXEnabled = true,
        panYEnabled = true,
        zoomXEnabled = true,
        zoomYEnabled = true,
        independentZoomEnabled = false
    )
)
```

**X-Axis Range Calculation:**
- Uses `autoScaleXRange(useNiceRange = false)` for exact data range
- Avoids "nice number" rounding that would extend the axis beyond data

### TogglesPlate.kt

**View Options Panel**

```kotlin
@Composable
fun TogglesPlate(
    modifier: Modifier = Modifier,
    toggles: List<ToggleSpec>,
    onExportPdf: (() -> Unit)? = null,
    onExportPdfTo1C: (() -> Unit)? = null,
    isExporting: Boolean = false,
    isExportingTo1C: Boolean = false
)
```

**Toggles:**
- "Наложение половин" (Overlap halves) - Toggles overlap visualization mode

**Buttons:**
- "Экспорт PDF" - Export chart to PDF
- "Экспорт PDF в 1С" - Placeholder for 1C server integration (disabled)

### PdfExporter.kt

**PDF Generation**

```kotlin
object PdfExporter {
    fun getPdfPathFromChartPaths(paths: List<String>): File
    
    suspend fun exportToPdf(
        config: PdfExportConfig,
        onProgress: (Float) -> Unit
    ): Result<File>
}
```

**PdfExportConfig:**
```kotlin
data class PdfExportConfig(
    val datasets: List<ChartData>,
    val colors: List<Color>,
    val outputPath: File,
    val chartTitle: String? = null
)
```

## Chart File Format

Chart files use a custom `.txt` format:

```
#standard#<standard_file_name>
#visibility#1#1#1#1#1#1#1#1#1#1#1#1
#
<time>;<ch1_val>|<time>;<ch2_val>|...
<time>;<ch1_val>|<time>;<ch2_val>|...
...
```

- `#standard#` - Header identifier
- `#visibility#` - Channel visibility flags (0=hidden, 1=visible)
- `#` - Separator
- Data rows: `<timestamp>;<value>` pairs separated by `|`

## Data Models

### ChartData
```kotlin
data class ChartData(
    val fileName: String,
    val series: List<List<Point<Float, Float>>>,  // Per-channel point lists
    val visibility: List<Boolean>,                // Per-channel visibility
    val pathEffect: PathEffect? = null           // Line style
)
```

### ResultOrNull
```kotlin
sealed class ResultOrNull {
    object Loading : ResultOrNull()
    data class Success(val chartData: ChartData) : ResultOrNull()
    data class Failure(val message: String) : ResultOrNull()
}
```

## Configuration

### Series Colors
Defined in `AppChartV3.kt`:
```kotlin
val seriesColors = listOf(
    Color(0xFF2E7D32),  // Dark Green
    Color(0xFF5D4037),  // Brown
    Color(0xFF101010),  // Dark Gray
    Color(0xFF1976D2),  // Blue
    Color(0xFFD32F2F),  // Red
    Color(0xFFFBC02D),  // Yellow
    // ... up to 16 colors
)
```

### Downsampling
- `maxPoints = 6000` - Maximum points per series for performance
- `minPoints = 200` - Minimum points for downsampling calculation

## Common Issues & Solutions

### X-Axis Range Too Large
**Problem**: Axis extends beyond actual data (e.g., 20K when data only goes to 12K)

**Cause**: `autoScaleRange()` uses "nice number" rounding

**Solution**: Use `autoScaleXRange(useNiceRange = false)` for exact range

### Cyrillic Not Displaying in PDF
**Cause**: PDFBox default fonts don't support Cyrillic

**Solution**: Use embedded TTF Arial font (implemented in PdfExporter.kt)
