# MCMV2 - Multi-Channel Machine Controller

**Version:** 1.2.23

A Kotlin Multiplatform Desktop (JVM) application for controlling multi-channel industrial machines and conducting scientific experiments. The application interfaces with hardware controllers via RS-232 serial communication and provides real-time visualization of sensor data.

## Overview

MCMV2 enables precise control of up to 12 solenoid channels with PWM modulation, loading experiment scenarios from Excel files and recording experimental data for analysis.

## Tech Stack

- **Language:** Kotlin 2.2.0
- **UI Framework:** Jetpack Compose Multiplatform 1.8.2
- **Target Platform:** Desktop (JVM only)
- **Build System:** Gradle with Kotlin DSL
- **Architecture:** MVVM with reactive state management

## Dependencies

| Library | Purpose |
|---------|---------|
| jSerialComm 2.9.3 | Serial port (COM/RS-232) communication |
| Apache POI 5.0.0 | Excel (.xls) scenario file parsing |
| kotlinx-serialization-json | JSON configuration persistence |
| JFreeChart 1.5.3 | Chart generation |
| Koala Chart | Interactive chart visualization with zoom/pan |
| Feather Icons | UI iconography |

## Key Features

- **Multi-Channel Control:** Support for 8 or 12 solenoid channels with independent PWM control
- **Scenario Management:** Load experiment parameters from Excel (.xls) files
- **Real-time Monitoring:** Live pressure and current gauges with configurable thresholds
- **Data Recording:** Export experiment data to timestamped .txt files
- **Chart Analysis:** Compare up to 3 chart files with interactive zoom/pan
- **Protocol Support:** V2 protocol (NEW)
- **Sound Alerts:** Audio notifications for experiment state changes
- **Flexible Display:** Configurable gauge layouts (1-12 per row)

## Project Structure

```
MCMV2/
├── composeApp/
│   └── src/
│       └── jvmMain/kotlin/ru/atrs/mcm/
│           ├── Main.kt                    # Application entry point
│           ├── Intents.kt                 # User action handlers
│           ├── serial_port/                # Serial communication
│           │   ├── CommMachineV2.kt       # New protocol implementation
│           │   ├── RouterCommunication.kt  # V2 communication router
│           │   ├── COMProtocol.kt         # Communication interface
│           │   └── ParseBytes.kt          # Byte stream parsing
│           ├── ui/                        # Compose UI components
│           │   ├── MainScreen.kt          # Main application screen
│           │   ├── chartsv3/               # Chart viewer V3
│           │   ├── main_screen/           # Dashboard components
│           │   └── custom/                # Custom UI elements
│           ├── parsing_excel/             # Excel scenario parser
│           │   ├── ParseScenario.kt       # Main parser
│           │   └── models/                # Data models
│           ├── storage/                   # File I/O operations
│           │   ├── FileWorker.kt          # Chart file operations
│           │   ├── JsonWorker.kt          # JSON config persistence
│           │   └── FilePicker.kt          # File selection dialogs
│           ├── koala/                     # Chart visualization library
│           ├── utils/                     # Utilities and global state
│           └── enums/                     # State enumerations
└── gradle/
    └── libs.versions.toml                 # Version catalog
```

## Data Storage Paths

All application data is stored in user-specific directories:

| Type | Path |
|------|------|
| Configuration | `%USERPROFILE%/mcm/config/` |
| Reports | `%USERPROFILE%/mcm/reports/` |
| Scenarios | `%USERPROFILE%/mcm/scenarios/` |
| Logs | `%USERPROFILE%/mcm/config/logs/` |

## Configuration

On first launch, the application creates a `config.json` file with the following parameters:

- `comport` - Serial port name (e.g., COM0)
- `baudrate` - Communication speed (default: 500000)
- `last_scenario` - Path to last used scenario file
- `isFullscreenEnabled` - Fullscreen mode toggle
- `is12ChannelsMode` - Channel count mode
- `protocolType` - Serial protocol version
- `CHART_FILE_NAME_ENDING` - Chart file naming convention

## Building

```bash
./gradlew build
./gradlew run
```

Build outputs are created in `build/distributions/`.

## Serial Protocol

The application communicates with hardware via binary serial commands:

| Command | Description |
|---------|-------------|
| `0x71` | Set channels 1-12 |
| `0x51` | Set analog1/analog2 |
| `0x73` | Send scenario step (part 1) |
| `0x72` | Send scenario step (part 2) |
| `0x68` | Set main frequency |
| `0x74` | Start receiving data |
| `0x54` | Reset communication |
| `0x78` | Start recording |

## License

Copyright © ATRS
