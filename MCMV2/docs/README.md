# MCMV2 - Multi-Channel Machine V2

## Overview

**MCMV2** is a Kotlin Multiplatform Desktop application for controlling industrial machinery with up to 12 independent solenoid channels. It reads experiment scenarios from Excel files, communicates with hardware via RS-232, and provides real-time visualization.

### Key Features

- **Multi-Channel Control**: Support for 8-12 independent solenoid channels
- **Scenario-Based Experiments**: Load experiment configurations from Excel files (.xls)
- **Real-Time Visualization**: Live pressure gauges and current monitoring
- **Chart Analysis**: Compare up to 3 chart files side-by-side with zoom/pan
- **PDF Export**: Export chart data to PDF for reporting
- **Dual Protocol Support**: Legacy (Aug 2025) and new protocol implementations

### Technology Stack

- **Language**: Kotlin 2.2.0
- **UI Framework**: Jetpack Compose (Desktop)
- **Charting**: Koala Chart Library (custom implementation)
- **PDF Generation**: Apache PDFBox 2.0.30
- **Excel Parsing**: Apache POI 5.0.0
- **Serial Communication**: jSerialComm 2.9.3
- **Coroutines**: kotlinx-coroutines 1.6.4

### Project Structure

```
composeApp/
├── build.gradle.kts          # Gradle build configuration
├── src/
│   ├── jvmMain/kotlin/ru/atrs/mcm/
│   │   ├── Main.kt                      # Application entry point
│   │   ├── Intents.kt                   # User action handlers
│   │   ├── enums/                       # Enumerations
│   │   │   ├── ExplorerMode.kt
│   │   │   ├── StateExperiments.kt
│   │   │   └── StateParseBytes.kt
│   │   ├── featureToggles/              # Feature flags
│   │   │   └── FeatureToggles.kt
│   │   ├── koala/                       # Custom Koala chart library
│   │   │   ├── xygraph/                 # XY graph components
│   │   │   │   ├── FloatLinearAxisModel.kt  # Axis model with autoScaleXRange
│   │   │   │   ├── Point.kt
│   │   │   │   └── XYGraph.kt
│   │   │   ├── gestures/                # Gesture handling
│   │   │   ├── line/                    # Line plotting
│   │   │   └── style/                   # Chart styling
│   │   ├── parsing_excel/              # Excel scenario parsing
│   │   │   ├── ParseScenario.kt
│   │   │   ├── WriteToExcel.kt
│   │   │   └── models/
│   │   │       ├── PressuresHolder.kt
│   │   │       ├── ScenarioStep.kt
│   │   │       └── SolenoidHolder.kt
│   │   ├── serial_port/                 # Hardware communication
│   │   │   ├── COMProtocol.kt          # Protocol interface
│   │   │   ├── RouterCommunication.kt  # Protocol router
│   │   │   ├── CommMachineV1.kt        # Legacy protocol
│   │   │   ├── CommMachineV2.kt        # New protocol
│   │   │   └── ParseBytes.kt           # Byte parsing
│   │   ├── storage/                     # File operations
│   │   │   ├── FileWorker.kt
│   │   │   ├── FilePicker.kt
│   │   │   ├── JsonWorker.kt
│   │   │   └── models/
│   │   │       └── UIGaugesData.kt
│   │   ├── ui/                         # User interface
│   │   │   ├── MainScreen.kt
│   │   │   ├── CustomElements.kt
│   │   │   ├── navigation/
│   │   │   │   └── Screens.kt
│   │   │   ├── chartsv3/               # Chart Viewer V3
│   │   │   │   ├── AppChartV3.kt      # Main chart app
│   │   │   │   ├── ChartUI.kt         # UI components
│   │   │   │   ├── PdfExporter.kt     # PDF export
│   │   │   │   └── TogglesPlate.kt    # View controls
│   │   │   ├── custom/
│   │   │   │   ├── GaugeX.kt          # Circular gauge
│   │   │   │   └── DefaultTrack.kt
│   │   │   ├── main_screen/
│   │   │   │   ├── center/
│   │   │   │   │   ├── CenterPart.kt
│   │   │   │   │   └── support_elements/
│   │   │   │   │       ├── BottomSlidersCurr.kt
│   │   │   │   │       └── SolenoidControl.kt
│   │   │   │   └── starter_screen/
│   │   │   │       └── starter.kt
│   │   │   ├── styles/
│   │   │   │   ├── Colors.kt
│   │   │   │   └── Fonts.kt
│   │   │   └── windows/
│   │   │       └── WindowTypes.kt
│   │   ├── utils/                      # Utilities
│   │   │   ├── GlobalVariables.kt     # Global state
│   │   │   ├── Logs.kt                # Logging
│   │   │   ├── Sounds.kt              # Sound effects
│   │   │   ├── Scenarios.kt           # Scenario helpers
│   │   │   └── Tools.kt
│   │   └── view_results/
│   │       └── ChartCustom.kt
│   └── jvmTest/                        # Tests
│       └── kotlin/ru/atrs/mcm/
└── compose-desktop.pro                  # ProGuard configuration
```

### Build & Run

```bash
# Development
./gradlew run

# Build
./gradlew build

# Create distribution
./gradlew installDist

# Package for distribution
./gradlew jpackage
```

### Version

Current version: **1.2.24**
