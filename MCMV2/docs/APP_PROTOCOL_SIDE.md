# MCMV2 App Protocol (Concise)

Scope: Kotlin/JVM app side only.

## Flow

```text
UI/Intents -> RouterCommunication(PROTOCOL_TYPE) -> CommMachineV1|V2 -> Serial write (14 bytes)
Serial listener (16/24 fixed packet) -> parser -> RAW/pressure/current flows
```

## 1) App Output Byte Arrays (App -> Controller)

All outbound commands are 14-byte arrays.

| Cmd | Purpose |
|---|---|
| `0x74` | start telemetry stream |
| `0x54` | reset communication |
| `0x78` | start experiment |
| `0x68` | set main frequency |
| `0x73` | scenario part A |
| `0x72` | scenario part B (V2) |
| `0x71` | solenoid payload A |
| `0x51` | solenoid payload B / analog |

Byte order primitive used by app:

- `to2ByteArray()` = `[lowByte, highByte]`

### 1.1 Fixed command arrays (horizontal)

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0x74` | `0x74` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |
| `0x54` | `0x54` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |
| `0x78` | `0x78` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |

### 1.2 Scenario/frequency arrays (horizontal)

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0x68` | `0x68` | `freqLow` | `freqHigh` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |
| `0x73` | `0x73` | `stepLow` | `stepHigh` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `ch8` | `timeLow` | `timeHigh` | `00` |
| `0x72` | `0x72` | `stepLow` | `stepHigh` | `ch9` | `ch10` | `ch11` | `ch12` | `analog1` | `analog2` | `gradLow` | `gradHigh` | `00` | `00` | `00` |

### 1.3 Control arrays used by app (horizontal)

V1 split/interleaved format (`CommMachineV1`, plus V2 methods `comparatorToSolenoid()` and `sendZerosToSolenoid()`):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0x71` split | `0x71` | `ch1` | `00` | `ch2` | `00` | `ch3` | `00` | `ch4` | `00` | `00` | `00` | `00` | `00` | `00` |
| `0x51` split | `0x51` | `ch5` | `00` | `ch6` | `00` | `ch7` | `00` | `ch8` | `00` | `00` | `00` | `00` | `00` | `00` |

V2 compact format (`CommMachineV2.solenoidControl()`):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `0x71` compact | `0x71` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `ch8` | `ch9` | `ch10` | `ch11` | `ch12` | `00` |
| `0x51` compact | `0x51` | `analog1` | `analog2` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` | `00` |

## 2) App Input Byte Arrays (Controller -> App)

Packet size selected by protocol mode:

- `OLD_AUG_2025` -> 16 bytes
- `NEW` -> 24 bytes

### 2.1 Marker arrays (horizontal)

Start marker (`NEW`, 24-byte packet):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` |

Start marker (`OLD_AUG_2025`, 16-byte packet):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` |

End marker: all bytes in packet are `FF`.

### 2.2 Pressure/current data arrays (horizontal)

Base map for bytes `0..15`:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| pressure | `ch1L` | `ch1H` | `ch2L` | `ch2H` | `ch3L` | `ch3H` | `ch4L` | `ch4H` | `ch5L` | `ch5H` | `ch6L` | `ch6H` | `ch7L` | `ch7H` | `ch8L` | `ch8H` |
| current | `ch1L` | `ch1H+16` | `ch2L` | `ch2H+16` | `ch3L` | `ch3H+16` | `ch4L` | `ch4H+16` | `ch5L` | `ch5H+16` | `ch6L` | `ch6H+16` | `ch7L` | `ch7H+16` | `ch8L` | `ch8H+16` |

Extra map for bytes `16..23` (24-byte mode):

| idx | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|
| pressure | `ch9L` | `ch9H` | `ch10L` | `ch10H` | `ch11L` | `ch11H` | `ch12L` | `ch12H` |
| current | `ch9L` | `ch9H+16` | `ch10L` | `ch10H+16` | `ch11L` | `ch11H+16` | `ch12L` | `ch12H+16` |

Parser checks:

- pressure packet: `b1,b3,b5,b7 < 16`
- current packet: `b1,b3,b5,b7 in 16..31`

Decode formulas:

```text
pressureValue = low + high * 256
currentValue  = low + (highTagged - 16) * 256
```

## 3) V1 vs V2 Quick Table

| Item | V1 (`OLD_AUG_2025`) | V2 (`NEW`) |
|---|---|---|
| listener packet size | 16 | 24 |
| scenario upload | `0x73` only | `0x73` + `0x72` |
| live control style | split/interleaved | split + compact |
| start marker length | 16 | 24 |
