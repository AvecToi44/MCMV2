# MCMV2 App Specs (Common, Concise)

Scope: high-level application spec from Kotlin code.

## 1) Runtime Snapshot

| Item | Value |
|---|---|
| App type | Compose Desktop (JVM) |
| Package root | `ru.atrs.mcm` |
| Entry point | `Main.kt` |
| Window title | `MCM 1.2.25` |
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
| `dataChunkRAW` | `ByteArray` | raw serial packets |
| `pressuresChunkGauges` | `DataChunkG` | parsed pressure chunk |
| `dataChunkCurrents` | `DataChunkCurrent` | parsed current chunk |
| `dataGauges` | `UIGaugesData` | throttled UI gauge updates |

## 5) Scenario Parsing (`ParseScenario.kt`)

Current parser settings:

- fixed `NUMBER_OF_GAUGES = 12`
- pressure metadata rows: `2..10`
- solenoid metadata rows: `14..22`
- scenario rows start at `27`

Scenario columns:

| Column | Meaning |
|---:|---|
| `0` | `time` |
| `1..12` | channels |
| `13` | `analog1` |
| `14` | `analog2` |
| `15` | `gradientTime` |
| `16` | `comment` (optional) |

## 6) Config Persistence (`JsonWorker.kt`)

Saved in `mcm/config/config.json`.

Main keys:

- serial: `comport`, `baudrate`, `protocolType`
- mode/UI: `is12ChannelsMode`, `isFullscreenEnabled`, `isBottomPanelShow`, `GAUGES_IN_THE_ROW`
- run/chart: `last_scenario`, `delay_before_chart`, `CHART_FILE_NAME_ENDING`
- operator/log/sound: `last_operator_id`, `LOG_LEVEL`, `sound_enabled`

## 7) Chart Log File (`FileWorker.kt`)

Header:

```text
#standard#<file>
#visibility#<ch1>...<ch8>[...<ch12>]
#
```

Data row format:

```text
time;ch1|time;ch2|...|time;ch8|[time;ch9|...|time;ch12|]
```

## 8) Filesystem Layout

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
