# MCMV2 App Specs (Common, Concise)

Scope: high-level application spec from current Kotlin/JVM code.

## 1) Runtime Snapshot

| Item | Value |
|---|---|
| App type | Compose Desktop (JVM) |
| Package root | `ru.atrs.mcm` |
| Entry point | `Main.kt` |
| Window title | `MCM <BuildConfig.APP_VERSION>` |
| Version source | `gradle.properties` -> `app.version` |
| Serial lib | `jSerialComm` |
| Scenario format | `.xls` via Apache POI |

## 2) Main Architecture

```text
Main.kt
  -> initialize(config.json)
  -> start raw parser flow
  -> start writer/UI flow
  -> App() navigation

App()
  STARTER -> StarterScreen
  MAIN    -> CenterPiece
  EASTER  -> Represent
```

## 3) Core Runtime State

| Group | Key vars |
|---|---|
| Serial/config | `COM_PORT`, `BAUD_RATE`, `PROTOCOL_TYPE`, `TWELVE_CHANNELS_MODE` |
| Experiment | `STATE_EXPERIMENT`, `GLOBAL_STATE`, `EXPLORER_MODE` |
| Scenario cursor | `indexOfScenario`, `txtOfScenario` |
| UI safety | `allowManipulationWithUI` |

## 4) Data Flows

| Flow | Payload | Purpose |
|---|---|---|
| `dataChunkRAW` | `ByteArray` | raw serial packets/chunks |
| `pressuresChunkGauges` | `DataChunkG` | parsed pressure chunk |
| `dataChunkCurrents` | `DataChunkCurrent` | parsed current chunk |
| `dataGauges` | `UIGaugesData` | throttled UI gauge updates |
| `operatorPauseDialogRequests` | `String` | pause dialog text for operator |

## 5) Scenario Parsing (`ParseScenario.kt`)

- fixed `NUMBER_OF_GAUGES = 12`
- pressure metadata rows: `2..10`
- solenoid metadata rows: `14..23`
- scenario rows start at `28` (`A28`)
- comment column: `Q`
- operator command column: `R`

Scenario columns:

| Column | Meaning |
|---:|---|
| `A` | `time` (ms) |
| `B..M` | channels 1..12 |
| `N` | `analog1` |
| `O` | `analog2` |
| `P` | `gradientTime` |
| `Q` | `comment` |
| `R` | `commands for operator` (if non-empty -> controller pause flag + operator popup) |

## 6) Config Persistence (`JsonWorker.kt`)

Saved in `mcm/config/config.json`.

Main keys:

- serial: `comport`, `baudrate`, `protocolType`
- mode/UI: `is12ChannelsMode`, `isFullscreenEnabled`, `isBottomPanelShow`, `GAUGES_IN_THE_ROW`
- run/chart: `last_scenario`, `delay_before_chart`, `CHART_FILE_NAME_ENDING`
- operator/log/sound: `last_operator_id`, `LOG_LEVEL`, `sound_enabled`

## 7) Chart Log File (`FileWorker.kt`)

Header (new logs):

```text
#standard#<file>
#visibility#<ch1>...<ch8>[...<ch12>]
#channels#<name1>#<name2>#...<nameN>
#steps#<time_ms;comment>#<time_ms;comment>#...
#
```

Data row format:

```text
time;ch1|time;ch2|...|time;ch8|[time;ch9|...|time;ch12|]
```

Compatibility notes:

- old logs without `#channels#` and/or `#steps#` are supported
- `#channels#` drives channel labels in ChartViewer and PDF chips
- `#steps#` drives timeline sections in ChartViewer and PDF

## 8) ChartViewer V3 Notes

- `Hide`/`Show` restores previous per-channel visibility mask (not "all true")
- `View options` panel is in layout flow and does not overlay picker controls
- timeline axis is above chart area and does not overlap plot lines
- PDF export includes:
  - render path aligned with UI data preparation (`prepareChartRender`)
  - channel names (`#channels#`)
  - timeline sections (`#steps#`)
  - footer (light gray): `<host>; <user>; <home>`

## 9) Filesystem Layout

```text
Documents/mcm/
  config/
    config.json
    logs/
  reports/
    <scenario_name>/
    standard/
  scenarios/
    *.xls
```
