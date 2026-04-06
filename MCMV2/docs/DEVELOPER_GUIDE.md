# Developer Guide

## Setting Up Development Environment

### Prerequisites
- JDK 17 or later
- Gradle 8.x
- IntelliJ IDEA (recommended)

### Build Commands

```bash
# Clean and build
./gradlew clean build

# Run in development mode
./gradlew run

# Compile Kotlin only
./gradlew compileKotlinJvm

# Run tests
./gradlew test

# Create distribution
./gradlew installDist

# Package for distribution
./gradlew jpackage
```

## Project Conventions

### Code Style
- **Package structure**: Follows `ru.atrs.mcm.{module}` pattern
- **Naming**: camelCase for variables/functions, PascalCase for classes
- **Composable functions**: Prefix with capital letter (Kotlin convention)
- **No comments**: Unless explicitly required by user

### File Organization
```
{module}/
├── {FeatureName}.kt           # Main feature file
├── {FeatureName}Model.kt      # Data models
├── {FeatureName}View.kt       # View layer
├── {FeatureName}Controller.kt # Controller/logic (if needed)
└── package.kt                 # Package declaration only
```

### State Management
- Use `mutableStateOf()` for UI state
- Use `MutableSharedFlow` for async data streams
- Global state in `utils/GlobalVariables.kt`

## Key Patterns

### Serial Communication

All serial I/O **must** run on `Dispatchers.IO`:

```kotlin
CoroutineScope(Dispatchers.IO + CoroutineName("...")).launch {
    // Serial operations here
}
```

### Async Data Loading

Use `produceState` for async chart file loading:

```kotlin
val data by produceState<ResultOrNull>(initialValue = ResultOrNull.Loading, path) {
    value = loadData(path)
}
```

### Chart Axis Range

Use `useNiceRange = false` to avoid "nice number" rounding:

```kotlin
val xRange = data.autoScaleXRange(useNiceRange = false)
```

### PDF Export

Use `PdfExporter` object methods:

```kotlin
val pdfPath = PdfExporter.getPdfPathFromChartPaths(listOf(path1, path2))

CoroutineScope(Dispatchers.IO).launch {
    PdfExporter.exportToPdf(config, onProgress = { progress ->
        // Update UI
    })
}
```

## Serial Protocol Commands

### V2 Protocol (Current)

| Byte | Purpose | Payload |
|------|---------|---------|
| `0x71` | Channels 1-4 | ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00 |
| `0x51` | Channels 5-8 | ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00 |
| `0x73` | Scenario part 1 | index, ch1-4, time |
| `0x72` | Scenario part 2 | index, ch5-12, analog1, analog2, gradientTime |
| `0x68` | Set frequency | mainFreq bytes |
| `0x74` | Start receive | - |
| `0x54` | Reset | - |
| `0x78` | Start recording | - |

## Logging

Use appropriate log levels:

```kotlin
logError("message")    // Errors only - always log
logInfo("message")     // General info
logGarbage("message")  // Debug verbose
logAct("message")      // Action logging
```

## File Paths

Always use `File()` constructor for cross-platform compatibility:

```kotlin
val configFile = File(Dir1Configs, "config.json")
val reportFile = File(Dir2Reports, "$name.txt")
```

## Important Notes

1. **Protocol Selection**: Check `PROTOCOL_TYPE` before modifying serial code
2. **12-Channel Mode**: `TWELVE_CHANNELS_MODE` affects data structures
3. **State Consistency**: Use `allowManipulationWithUI` during critical operations
4. **Single Instance**: Main.kt uses file locking
5. **Sound Resources**: Sound files must exist in config directory
