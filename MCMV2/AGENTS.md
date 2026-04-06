# MCMV2 - AI Agent Documentation

This document provides detailed technical documentation for AI agents working with the MCMV2 codebase.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [State Management](#state-management)
4. [Serial Communication](#serial-communication)
5. [Data Models](#data-models)
6. [UI Components](#ui-components)
7. [Feature Toggles](#feature-toggles)
8. [Common Patterns](#common-patterns)
9. [Testing](#testing)
10. [Build & Run](#build--run)

---

## Project Overview

**MCMV2** (Multi-Channel Machine V2) is a desktop application for controlling industrial machinery with up to 12 independent solenoid channels. It reads experiment scenarios from Excel files, communicates with hardware via RS-232, and provides real-time visualization.

**Package root:** `ru.atrs.mcm`

---

## Architecture

### Entry Point
- `Main.kt` - Application bootstrap, window creation, single-instance enforcement
- Uses file locking to prevent multiple instances

### Navigation
- `ui/navigation/Screens.kt` - Screen enum (STARTER, MAIN, EASTER_EGG)
- `screenNav` mutableStateOf controls current screen

### User Actions
- `Intents.kt` - Intent functions (launchPlay, openNewScenario, openLastScenario, openChartViewer)

---

## State Management

All global state is in `utils/GlobalVariables.kt`. Key variables:

### Configuration State
```kotlin
var COM_PORT = "COM0"
var BAUD_RATE = 500000
var TWELVE_CHANNELS_MODE = false
var PROTOCOL_TYPE: ProtocolType = ProtocolType.OLD_AUG_2025
var SHOW_FULLSCREEN = false
var SHOW_BOTTOM_PANEL = true
var GAUGES_IN_THE_ROW = 6
```

### Experiment State
```kotlin
var GLOBAL_STATE = mutableStateOf(StateParseBytes.INIT)
var STATE_EXPERIMENT = mutableStateOf(StateExperiments.NONE)
var EXPLORER_MODE = mutableStateOf(ExplorerMode.AUTO)
var indexOfScenario = mutableStateOf(0)
var txtOfScenario = mutableStateOf("")
```

### Data Flows (Kotlin Coroutines)
```kotlin
var dataChunkRAW = MutableSharedFlow<ByteArray>(...)        // Raw serial bytes
var pressuresChunkGauges = MutableSharedFlow<DataChunkG>(...)  // Parsed gauge data
var dataGauges = MutableSharedFlow<UIGaugesData>(...)       // UI gauge data
var dataChunkCurrents = MutableSharedFlow<DataChunkCurrent>(...) // Current readings
```

---

## Serial Communication

### Protocol Interface
`serial_port/COMProtocol.kt` - Defines communication contract:
```kotlin
interface COMProtocol {
    suspend fun initSerialCommunication()
    suspend fun startReceiveFullData()
    fun stopSerialCommunication()
    fun cleanCOMPort()
    suspend fun resetSerialComm()
    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long)
    suspend fun comparatorToSolenoid(newIndex: Int)
    suspend fun sendScenarioToController()
    suspend fun reInitSolenoids()
    fun sendFrequency()
    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean)
}
```

### Protocol Router
`serial_port/RouterCommunication.kt` - Routes to V1 or V2 based on `PROTOCOL_TYPE`:
```kotlin
object RouterCommunication {
    // All functions delegate to CommMachineV1 or CommMachineV2
}
```

### Protocol V1 (Legacy)
`serial_port/CommMachineV1.kt` - Old August 2025 protocol implementation

### Protocol V2 (New)
`serial_port/CommMachineV2.kt` - Current protocol implementation with:
- 12-channel support
- Separate byte commands for channels 1-4 (0x71) and 5-8 (0x51)
- Scenario transfer with gradient time support

### Serial Commands Reference

| Byte | Purpose | Payload |
|------|---------|---------|
| `0x71` | Channels 1-4 | ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00... |
| `0x51` | Channels 5-8 | ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00... |
| `0x73` | Scenario part 1 | index, ch1-4, time |
| `0x72` | Scenario part 2 | index, ch5-12, analog1, analog2, gradientTime |
| `0x68` | Set frequency | mainFreq bytes |
| `0x74` | Start receive | - |
| `0x54` | Reset | - |
| `0x78` | Start recording | - |

### Byte Parsing
`serial_port/ParseBytes.kt` - Handles incoming serial data streams and splits into channel data.

---

## Data Models

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

### DataChunkG
```kotlin
data class DataChunkG(
    var isExperiment: Boolean = false,
    var firstGaugeData: Float,
    var secondGaugeData: Float,
    // ... channels 1-12 (8 mandatory, 4 optional)
)
```

---

## UI Components

### Main Screen
`ui/MainScreen.kt`:
- `App()` - Main composable with navigation
- `snackBarShow()` - Toast notifications
- Navigation via `screenNav` state

### Center Piece (Dashboard)
`ui/main_screen/center/CenterPart.kt`:
- `CenterPiece()` - Main dashboard with gauges grid
- LazyVerticalGrid for responsive gauge layout
- Bottom panel with controls (Play/Pause/Home/Settings)

### Gauges
`ui/custom/GaugeX.kt` - Custom circular gauge component

### Chart Viewer V3
`ui/chartsv3/AppChartV3.kt`:
- `WindowChartsV3()` - Opens chart in new window
- `App()` - Chart UI with 3 file slots
- `ChartView()` - Koala-based interactive chart
- Supports zoom (Ctrl+scroll), pan, channel toggles

### Support Elements
`ui/main_screen/center/support_elements/`:
- `BottomSlidersCurr.kt` - Current sliders
- `SolenoidControl.kt` - Solenoid PWM controls
- `SolenoidsPanel.kt` - Panel container

---

## Feature Toggles

`featureToggles/FeatureToggles.kt`:
```kotlin
object FeatureToggles {
    const val CONTROL = true  // Hardware control feature
}
```

---

## Common Patterns

### Logging
```kotlin
logError("message")    // Errors only
logInfo("message")     // General info
logGarbage("message")  // Debug verbose
logAct("message")      // Action logging
```

### Sound Notifications
```kotlin
sound_On()           // Experiment started
sound_Error()        // Error occurred
```

### Async Operations
All serial I/O runs on Dispatchers.IO:
```kotlin
CoroutineScope(Dispatchers.IO + CoroutineName("...")).launch { ... }
```

### Configuration Persistence
```kotlin
// Read
val params = readParametersJson()

// Initialize on startup
initialize(params)

// Update and save
refreshJsonParameters()
```

---

## Excel Scenario Parsing

`parsing_excel/ParseScenario.kt`:
- `targetParseScenario(File)` - Main parsing function
- Reads .xls format using Apache POI
- Expects specific row structure:
  - Row 2: Pressure display names
  - Row 3: Pressure indices
  - Row 4-8: Pressure min/max/tolerance/unit/comment
  - Row 14-22: Solenoid configuration
  - Row 27+: Scenario steps

### Scenario Step Structure
Columns: time | ch1 | ch2 | ... | ch8 | ch9-ch12? | analog1 | analog2 | gradientTime | comment

---

## File Operations

### Chart File Format (.txt)
```
#standard#<standard_file_name>
#visibility#1#1#1#1#1#1#1#1#1#1#1#1
#
<time>;<ch1_val>|<time>;<ch2_val>|...
```

### Directory Structure
```
%USERPROFILE%/mcm/
├── config/
│   ├── config.json
│   ├── logs/
│   └── *.wav (sounds)
├── reports/
│   ├── <scenario_name>/
│   │   └── *.txt (chart files)
│   └── standard/
└── scenarios/
    └── *.xls (scenario files)
```

---

## Testing

Tests located in `composeApp/src/jvmTest/kotlin/ru/atrs/mcm/`

Run tests:
```bash
./gradlew test
```

---

## Build & Run

### Development
```bash
./gradlew run
```

### Build
```bash
./gradlew build
```

### Create Distribution
```bash
./gradlew installDist
# or
./gradlew jpackage
```

### Gradle Properties
- `./gradle/gradle.properties` - JVM args, Kotlin options

---

## Important Notes for Agents

1. **Protocol Selection**: Always check `PROTOCOL_TYPE` before modifying serial communication code
2. **12-Channel Mode**: `TWELVE_CHANNELS_MODE` affects data structures and UI layout
3. **State Consistency**: Use `allowManipulationWithUI` to prevent UI conflicts during critical operations
4. **File Paths**: Always use `File()` constructor for cross-platform compatibility
5. **Coroutines**: Serial operations MUST run on Dispatchers.IO
6. **Single Instance**: Main.kt uses file locking to prevent multiple instances
7. **Sound Resources**: Sound files must exist in config directory or app will silently fail
