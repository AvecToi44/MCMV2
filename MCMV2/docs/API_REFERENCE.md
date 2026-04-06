# API Reference

## Global Variables (`utils/GlobalVariables.kt`)

### Configuration State
```kotlin
var COM_PORT: String = "COM0"                    // Serial port name
var BAUD_RATE: Int = 500000                      // Baud rate
var TWELVE_CHANNELS_MODE: Boolean = false        // 8 or 12 channel mode
var PROTOCOL_TYPE: ProtocolType = ProtocolType.OLD_AUG_2025
var SHOW_FULLSCREEN: Boolean = false
var SHOW_BOTTOM_PANEL: Boolean = true
var GAUGES_IN_THE_ROW: Int = 6
var SOLENOID_MAIN_FREQ: Int? = 0
var SOUND_ENABLED: Int = 1
var DELAY_BEFORE_CHART: Int = 2000
var LOG_LEVEL: LogLevel = LogLevel.ERRORS
```

### Experiment State
```kotlin
var GLOBAL_STATE: MutableState<StateParseBytes>  // Current parsing state
var STATE_EXPERIMENT: MutableState<StateExperiments>  // Experiment state
var EXPLORER_MODE: MutableState<ExplorerMode>     // AUTO, MANUAL, etc.
var indexOfScenario: MutableState<Int>             // Current scenario step
var txtOfScenario: MutableState<String>            // Scenario text
var commentOfScenario: MutableState<String>        // Scenario comment
```

### Data Flows
```kotlin
var dataChunkRAW: MutableSharedFlow<ByteArray>               // Raw serial bytes
var pressuresChunkGauges: MutableSharedFlow<DataChunkG>        // Parsed gauge data
var dataGauges: MutableSharedFlow<UIGaugesData>                // UI gauge data
var dataChunkCurrents: MutableSharedFlow<DataChunkCurrent>     // Current readings
```

### Directory Paths
```kotlin
val Dir1Configs: File          // %USERPROFILE%/mcm/config
val Dir2Reports: File          // %USERPROFILE%/mcm/reports
val Dir3Scenarios: File        // %USERPROFILE%/mcm/scenarios
val Dir7ReportsStandard: File  // %USERPROFILE%/mcm/reports/standard
```

## Data Classes

### DataChunkG
```kotlin
data class DataChunkG(
    var isExperiment: Boolean = false,
    var firstGaugeData: Float,
    var secondGaugeData: Float,
    var thirdGaugeData: Float,
    var fourthGaugeData: Float,
    var fifthGaugeData: Float,
    var sixthGaugeData: Float,
    var seventhGaugeData: Float,
    var eighthGaugeData: Float,
    var ninthGaugeData: Float? = null,
    var tenthGaugeData: Float? = null,
    var eleventhGaugeData: Float? = null,
    var twelfthGaugeData: Float? = null
)
```

### DataChunkCurrent
```kotlin
data class DataChunkCurrent(
    var firstCurrentData: Int,
    var secondCurrentData: Int,
    var thirdCurrentData: Int,
    var fourthCurrentData: Int,
    var fifthCurrentData: Int,
    var sixthCurrentData: Int,
    var seventhCurrentData: Int,
    var eighthCurrentData: Int,
    var ninthCurrentData: Int? = null,
    var tenthCurrentData: Int? = null,
    var eleventhCurrentData: Int? = null,
    var twelfthCurrentData: Int? = null
)
```

### PressuresHolder
```kotlin
data class PressuresHolder(
    val displayName: String,
    val index: Int,
    val minValue: Float,
    val maxValue: Float,
    val tolerance: Int,
    val unit: String,
    val commentString: String,
    val prefferedColor: String,
    val isVisible: Boolean
)
```

### SolenoidHolder
```kotlin
data class SolenoidHolder(
    val displayName: String,
    val index: Int,
    val maxPWM: Int,
    val step: Int,
    val ditherAmplitude: String,
    val ditherFrequency: Int,
    val currentMinValue: Int,
    val currentMaxValue: Int,
    val isVisible: Boolean
)
```

### ScenarioStep
```kotlin
data class ScenarioStep(
    val time: Int,           // Duration in ms
    val channels: List<Int>, // PWM values for each channel
    val analog1: Int,
    val analog2: Int,
    val gradientTime: Int,
    val comment: String
)
```

## Enums

### StateParseBytes
```kotlin
enum class StateParseBytes {
    INIT,
    RECEIVING,
    READY,
    ERROR
}
```

### StateExperiments
```kotlin
enum class StateExperiments {
    NONE,
    PLAY,
    PAUSE,
    STOP
}
```

### ExplorerMode
```kotlin
enum class ExplorerMode {
    AUTO,
    MANUAL
}
```

### ProtocolType
```kotlin
enum class ProtocolType(val id: Int) {
    OLD_AUG_2025(0),
    NEW(1)
}
```

### LogLevel
```kotlin
enum class LogLevel {
    ERRORS,
    DEBUG
}
```

### ChartFileNameEnding
```kotlin
enum class ChartFileNameEnding {
    COMMENT_AND_TIMESTAMP,
    TIMESTAMP,
    COMMENT
}
```

## Chart Viewer V3 Types

### ResultOrNull
```kotlin
sealed class ResultOrNull {
    object Loading : ResultOrNull()
    data class Success(val chartData: ChartData) : ResultOrNull()
    data class Failure(val message: String) : ResultOrNull()
}
```

### ChartData
```kotlin
data class ChartData(
    val fileName: String,
    val series: List<List<Point<Float, Float>>>,  // Per-channel point lists
    val visibility: List<Boolean>,                // Per-channel visibility
    val pathEffect: PathEffect? = null            // Line style
)
```

### LineStyleIndicator
```kotlin
enum class LineStyleIndicator {
    SOLID,
    LONG_DASH,
    SHORT_DASH
}
```

### HeaderSlot
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

### ToggleSpec
```kotlin
data class ToggleSpec(
    val label: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val info: String
)
```

### PdfExportConfig
```kotlin
data class PdfExportConfig(
    val datasets: List<ChartData>,
    val colors: List<Color>,
    val outputPath: File,
    val chartTitle: String? = null
)
```

## Public Functions

### PdfExporter
```kotlin
object PdfExporter {
    fun getPdfPathFromChartPaths(paths: List<String>): File
    
    suspend fun exportToPdf(
        config: PdfExportConfig,
        onProgress: (Float) -> Unit
    ): Result<File>
}
```

### parseChartFileStrict
```kotlin
suspend fun parseChartFileStrict(
    path: String,
    pathEffect: PathEffect? = null
): ParseOutcome
```

### autoScaleXRange / autoScaleYRange
```kotlin
public fun <Y> List<Point<Float, Y>>.autoScaleXRange(
    useNiceRange: Boolean = true
): ClosedFloatingPointRange<Float>

public fun <X> List<Point<X, Float>>.autoScaleYRange(
    useNiceRange: Boolean = true
): ClosedFloatingPointRange<Float>
```

## Logging Functions

```kotlin
fun logError(message: String)    // Errors only
fun logInfo(message: String)     // General info
fun logGarbage(message: String)  // Debug verbose
fun logAct(message: String)      // Action logging
```

## Sound Functions

```kotlin
fun sound_On()       // Experiment started
fun sound_Error()    // Error occurred
fun sound_End()     // Experiment ended
fun sound_Run()     // Run sound
```
