# MCMV2 App Protocol (V2 Only)

Scope: Kotlin/JVM application side, protocol `NEW` only (`CommMachineV2`).

## Flow

```text
UI/Intents -> RouterCommunication -> CommMachineV2 -> Serial write (14 bytes)

Serial listener -> ParseBytes:
  - framed telemetry path (`SOF A5 5A`, `type`, `seq`, `payload(24)`, `crc8`)
```

## 1) App Output Arrays (App -> Controller)

All outbound command frames are 14 bytes.

| Cmd | Purpose |
|---|---|
| `0x74` | start telemetry stream |
| `0x22` | resume scenario after pause |
| `0x54` | reset communication |
| `0x78` | start experiment |
| `0x68` | set main frequency |
| `0x73` | scenario part A |
| `0x72` | scenario part B |
| `0x71` | set channels 1..12 |
| `0x51` | set analog1/analog2 |

Byte order for packed integers: `[lowByte, highByte]`.

### 1.1 Core frames (horizontal)

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0x68` | `0x68` | `freqLow` | `freqHigh` | `p0` | `p1` | `p2` | `p3` | `p4` | `p5` | `p6` | `p7` | `p8` | `p9` | `00` |
| `0x73` | `0x73` | `stepLow` | `stepHigh` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `ch8` | `timeLow` | `timeHigh` | `00` |
| `0x72` | `0x72` | `stepLow` | `stepHigh` | `ch9` | `ch10` | `ch11` | `ch12` | `analog1` | `analog2` | `gradLow` | `gradHigh` | `pauseFlag` | `00` | `00` |
| `0x71` | `0x71` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `ch8` | `ch9` | `ch10` | `ch11` | `ch12` | `00` |
| `0x51` | `0x51` | `analog1` | `analog2` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |

## 2) App Input Arrays (Controller -> App)

Parser expects framed telemetry input.

### 2.1 Framed telemetry

Frame size: `29` bytes.

| idx | 0 | 1 | 2 | 3 | 4..27 | 28 |
|---:|---|---|---|---|---|---|
| val | `0xA5` | `0x5A` | `type` | `seq` | `payload(24)` | `crc8` |

Frame types:

| Type | Meaning |
|---|---|
| `0x01` | pressure payload |
| `0x02` | current payload |
| `0x10` | experiment start marker |
| `0x11` | experiment end marker |
| `0x12` | pause marker (controller entered pause state) |

Parser behavior:

- SOF resync (`A5 5A`)
- CRC8 check over bytes `[2..27]`
- sequence gap tracking (`rxSeqDropCount`)

## 3) Pause Extension (`gidrotester_pico_protocol_new_add_pause.ino`)

Scenario pause behavior is extended in hardware and should be reflected in the app protocol handling.

- `0x72[11]` carries a per-step pause flag (`0` = normal step, `1` = pause after applying this step).
- When pause flag is triggered on controller side, scenario progression is frozen until resume command arrives.
- Resume command from app: `0x22` in byte 0 (`byteArrayOf(0x22, 0x00, ... x14)`), same 14-byte command shape as other host commands.
- Telemetry stream may stop while paused (firmware sets internal sending flag to false during pause).

Current app behavior for pause flow:

- Pause trigger is derived from Excel column `R` (`commands for operator`): non-empty text => `pauseFlag=1` in `0x72[11]`.
- Parser handles framed type `0x12` as pause event from controller.
- On pause event, app shows operator dialog with step text and `OK` button.
- On `OK`, app sends resume command `0x22 00 00 00 00 00 00 00 00 00 00 00 00 00`.

## 4) Typical Protocol Use Case (ordered)

This is a practical order of messages for one normal run with a pause.

### Step 0: session init

1. App -> Controller: start telemetry listener

```text
0x74 00 00 00 00 00 00 00 00 00 00 00 00 00
```

2. Controller -> App: periodic framed telemetry starts

```text
A5 5A 01 seq p0..p23 crc   // pressure frame
A5 5A 02 seq c0..c23 crc   // current frame
```

### Step 1: send hardware configuration

3. App -> Controller: set PWM frequency (`0x68`)

```text
0x68 freqLow freqHigh p0 p1 p2 p3 p4 p5 p6 p7 p8 p9 00
```

Example for 2500 Hz (`0x09C4` => low/high = `C4 09`):

```text
0x68 C4 09 00 00 00 00 00 00 00 00 00 00 00
```

### Step 2: upload scenario (for each step index N)

4. App -> Controller: scenario part A (`0x73`)

```text
0x73 stepLow stepHigh ch1 ch2 ch3 ch4 ch5 ch6 ch7 ch8 timeLow timeHigh 00
```

5. App -> Controller: scenario part B (`0x72`)

```text
0x72 stepLow stepHigh ch9 ch10 ch11 ch12 analog1 analog2 gradLow gradHigh pauseFlag 00 00
```

Example (step 5, time=1000ms, grad=200ms, pause=1):

```text
0x73 05 00 10 20 30 40 50 60 70 80 E8 03 00
0x72 05 00 90 A0 B0 C0 00 00 C8 00 01 00 00
```

### Step 3: start experiment

6. App -> Controller: start record/play (`0x78`)

```text
0x78 00 00 00 00 00 00 00 00 00 00 00 00 00
```

7. Controller -> App: framed start marker

```text
A5 5A 10 seq 00..00 crc
```

8. Controller -> App: streaming pressure/current frames during execution

```text
A5 5A 01 seq payload(24) crc
A5 5A 02 seq payload(24) crc
```

### Step 4: pause and resume

9. Controller reaches step where `pauseFlag=1`:
   - scenario progression pauses,
   - telemetry may stop until resume.

10. App -> Controller: resume command (`0x22`)

```text
0x22 00 00 00 00 00 00 00 00 00 00 00 00 00
```

11. Controller -> App: pause marker on pause entry, then telemetry continues after resume.

```text
A5 5A 12 seq 00..00 crc
```

### Step 5: finish

12. Controller completes all steps (including pause time compensation) and sends end marker:

```text
A5 5A 11 seq 00..00 crc
```

13. App moves experiment state to end/prepare chart.
