# Excel Parser (MCMV2)

This document describes the current Excel scenario parser behavior for future development and for other AI agents.

## Scope and source files

- Parser implementation: `composeApp/src/jvmMain/kotlin/ru/atrs/mcm/parsing_excel/ParseScenario.kt`
- Scenario model: `composeApp/src/jvmMain/kotlin/ru/atrs/mcm/parsing_excel/models/ScenarioStep.kt`
- Parsed globals: `composeApp/src/jvmMain/kotlin/ru/atrs/mcm/utils/GlobalVariables.kt`
- 0x68 send usage: `composeApp/src/jvmMain/kotlin/ru/atrs/mcm/serial_port/CommMachineV2.kt`

## Main behavior

- Input format: `.xls` (Apache POI `HSSFWorkbook`)
- Sheet used: first sheet (`sheet[0]`)
- Fixed channels count: `12`
- Parser reads by fixed row/column coordinates (not by compacted row values)
- Scenario rows are parsed from row 28 (A28, 1-based) to the last row
- A scenario row is skipped if `A` (`sleep/step time`) is empty/non-numeric

## Strict template validation

`STRICT_TEMPLATE_VALIDATION = true`

Before data parsing, the parser validates template labels and numeric ranges.

Validation includes:

- section labels (`Pressures`, `Solenoids`, `Scenario`)
- expected row labels (`DisplayName`, `index`, `maxPWM`, etc.)
- `Main Frequency (0x68)` and all extra bytes in row 14
- scenario header in `A27`: supports both `step time` and `sleep time`

If validation fails, parser throws with a detailed list of issues.

## Expected Excel layout (1-based)

### Pressure section

| Row | Column A label | Data columns |
|---:|---|---|
| 2 | `Pressures` | - |
| 3 | `DisplayName` | B..M |
| 4 | `index` | B..M |
| 5 | `minValue` | B..M |
| 6 | `maxValue` | B..M |
| 7 | `tolerance` | B..M |
| 8 | `unit` | B..M |
| 9 | `commentString` | B..M |
| 10 | `preferredColor` | B..M |
| 11 | `isVisible` | B..M |

### Solenoid + frequency section

| Row | Column A/B label | Data columns |
|---:|---|---|
| 14 | `Main Frequency (0x68)` (label in B) | C = main freq, D..M = bytes for 0x68 |
| 15 | `Solenoids` + `DisplayName` | B..M |
| 16 | `index` | B..M |
| 17 | `maxPWM` | B..M |
| 18 | `value of division` / `step` | B..M |
| 19 | `Dither Amplitude` | B..M |
| 20 | `Dither Frequency` | B..M |
| 21 | `Current MinValue` | B..M |
| 22 | `Current MaxValue` | B..M |
| 23 | `isVisible` | B..M |

### Scenario section

| Row | Column A |
|---:|---|
| 26 | `Scenario:` |
| 27 | `step time` or `sleep time` |
| 28+ | scenario data rows |

Scenario data columns (row 28+):

| Column | Meaning |
|---|---|
| A | time (ms) |
| B..M | channels 1..12 |
| N | analog1 |
| O | analog2 |
| P | gradientTime |
| Q | text/comment |

## Parsed outputs

- `pressures: MutableList<PressuresHolder>`
- `solenoids: MutableList<SolenoidHolder>`
- `scenario: MutableList<ScenarioStep>`
- `SOLENOID_MAIN_FREQ: Int?` from `C14`
- `SOLENOID_FREQ_PARAMS_0x68: IntArray(10)` from `D14..M14` (clamped to `0..255`)
- `limitTime`: sum of all parsed scenario times

Additional runtime side-effects:

- updates `LAST_SCENARIO`
- creates report directory `mcm/reports/<scenario_name>`
- updates `Dir11ForTargetingSaveNewExperiment`
- reads standard chart file path from `A1` if it ends with `.txt`

## 0x68 packet mapping

Parser values from row 14 map to `sendFrequency()` payload as:

| Byte index | Source |
|---:|---|
| 0 | `0x68` |
| 1..2 | `SOLENOID_MAIN_FREQ` low/high |
| 3..12 | `SOLENOID_FREQ_PARAMS_0x68[0..9]` (D..M) |
| 13 | `0x00` |

## Error handling and logs

On parse failure:

- full error + stack trace is printed to terminal
- full error is written via `logError(...)`
- top toast notification is shown (`showMeSnackBar(...)`) with short message

Error messages include field name and exact cell address (e.g. `F21`).

## Important constraints

- Parser is currently designed for exactly 12 channels.
- If you change template coordinates, update constants in `ParseScenario.kt`.
- Keep strict validation enabled unless you intentionally support loose templates.
