# MCMV2 App Protocol (V2 Only)

Scope: Kotlin/JVM application side, protocol `NEW` only (`CommMachineV2`).

## Flow

```text
UI/Intents -> RouterCommunication -> CommMachineV2 -> Serial write (14 bytes)

Serial listener -> ParseBytes:
  - framed telemetry path (if FRAMED_TELEMETRY_ENABLED = true)
  - legacy 24-byte path (if FRAMED_TELEMETRY_ENABLED = false)
```

## 1) App Output Arrays (App -> Controller)

All outbound command frames are 14 bytes.

| Cmd | Purpose |
|---|---|
| `0x74` | start telemetry stream |
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
| `0x72` | `0x72` | `stepLow` | `stepHigh` | `ch9` | `ch10` | `ch11` | `ch12` | `analog1` | `analog2` | `gradLow` | `gradHigh` | `00` | `00` | `00` |
| `0x71` | `0x71` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `ch8` | `ch9` | `ch10` | `ch11` | `ch12` | `00` |
| `0x51` | `0x51` | `analog1` | `analog2` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |

## 2) App Input Arrays (Controller -> App)

Parser supports both framed and legacy input.

### 2.1 Framed telemetry mode

Enabled by feature toggle: `FeatureToggles.FRAMED_TELEMETRY_ENABLED`.

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

Parser behavior:

- SOF resync (`A5 5A`)
- CRC8 check over bytes `[2..27]`
- sequence gap tracking (`rxSeqDropCount`)

### 2.2 Legacy mode (24-byte packets)

Legacy packet size: `24` bytes.

Marker frames:

- start marker: `(FE FF)` repeated 12 times
- end marker: all bytes are `FF`

Telemetry map (horizontal):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| pressure | `ch1L` | `ch1H` | `ch2L` | `ch2H` | `ch3L` | `ch3H` | `ch4L` | `ch4H` | `ch5L` | `ch5H` | `ch6L` | `ch6H` | `ch7L` | `ch7H` | `ch8L` | `ch8H` | `ch9L` | `ch9H` | `ch10L` | `ch10H` | `ch11L` | `ch11H` | `ch12L` | `ch12H` |
| current | `ch1L` | `ch1H+16` | `ch2L` | `ch2H+16` | `ch3L` | `ch3H+16` | `ch4L` | `ch4H+16` | `ch5L` | `ch5H+16` | `ch6L` | `ch6H+16` | `ch7L` | `ch7H+16` | `ch8L` | `ch8H+16` | `ch9L` | `ch9H+16` | `ch10L` | `ch10H+16` | `ch11L` | `ch11H+16` | `ch12L` | `ch12H+16` |

Type checks:

- pressure: `b1,b3,b5,b7 < 16`
- current: `b1,b3,b5,b7 in 16..31`

Decode formulas:

```text
pressureValue = low + high * 256
currentValue  = low + (highTagged - 16) * 256
```
