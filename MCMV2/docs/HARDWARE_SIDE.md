# MCMV2 Hardware Side (from `gidrotester_pico_protocol_new_add_pause.ino`)

Scope: only `hardware/gidrotester_pico_protocol_new_add_pause.ino`.

## 1) Runtime Snapshot

- serial baud: `500000`
- SPI read: 3 ADC blocks, each `16` bytes
- loop path:

```text
readData() -> parseRawBytes() -> parsing(commands)
  + sensread() if sendingflag == 1
  + pwmsend()  if pwmflag == 1
```

## 2) Firmware Input Byte Arrays (Host/App -> Firmware)

Input buffer is fixed:

- `indata[14]`
- filled by `Serial.readBytes(indata, 14)`

### 2.1 Command selector

| `indata[0]` | Meaning |
|---:|---|
| `0x74` | start telemetry sending |
| `0x22` | resume scenario after pause |
| `0x54` | stop/reset telemetry state |
| `0x78` | scenario start/stop control |
| `0x71` | direct PWM channels |
| `0x51` | analog values |
| `0x73` | scenario part A |
| `0x72` | scenario part B |
| `0x68` | frequency |

### 2.2 Horizontal command array maps (`indata[0..13]`)

`0x71` direct PWM update:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `0x71` | `pwm0` | `pwm1` | `pwm2` | `pwm3` | `pwm4` | `pwm5` | `pwm6` | `pwm7` | `pwm8` | `pwm9` | `pwm10` | `pwm11` | `n/a` |

`0x51` analog update:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `0x51` | `analog1` | `analog2` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` |

`0x73` scenario part A:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `0x73` | `stepLow` | `stepHigh` | `ch0` | `ch1` | `ch2` | `ch3` | `ch4` | `ch5` | `ch6` | `ch7` | `timeLow` | `timeHigh` | `n/a` |

`0x72` scenario part B:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `0x72` | `stepLow` | `stepHigh` | `out8` | `out9` | `out10` | `out11` | `analog1` | `analog2` | `gradLow` | `gradHigh` | `pauseFlag` | `n/a` | `n/a` |

`0x68` frequency:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `0x68` | `hzLow` | `hzHigh` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` |

`0x78` control checks in code:

| idx | 0 | 1 | 2 |
|---:|---|---|---|
| start branch | `0x78` | `0` | `0` |
| stop branch | `0x78` | `138` | `2` |

## 3) Firmware Output Byte Arrays (Firmware -> Host/App)

Firmware sends framed telemetry (`29` bytes):

| idx | 0 | 1 | 2 | 3 | 4..27 | 28 |
|---:|---|---|---|---|---|---|
| val | `0xA5` | `0x5A` | `type` | `seq` | `payload(24)` | `crc8` |

Frame `type` values used by sketch:

| Type | Meaning |
|---|---|
| `0x01` | pressure payload |
| `0x02` | current payload |
| `0x10` | experiment start marker |
| `0x11` | experiment end marker |
| `0x12` | pause marker (emitted once on pause entry) |

CRC8 parameters:

- poly: `0x07`
- init: `0x00`
- range: bytes `[2..27]` (`type + seq + payload`)

### 3.1 Telemetry payload packing slots (`pressures[]` / `currents[]`)

Packing rule in code:

```text
slot = channelIndex * 2
[slot] = low
[slot+1] = high
```

For currents, high byte is tagged as `high + 16`.

Horizontal slot map:

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| pressure slots | `ch0L` | `ch0H` | `ch1L` | `ch1H` | `ch2L` | `ch2H` | `ch3L` | `ch3H` | `ch4L` | `ch4H` | `ch5L` | `ch5H` | `ch6L` | `ch6H` | `ch7L` | `ch7H` | `ch8L` | `ch8H` | `ch9L` | `ch9H` | `ch10L` | `ch10H` | `ch11L` | `ch11H` |
| current slots | `ch0L` | `ch0H+16` | `ch1L` | `ch1H+16` | `ch2L` | `ch2H+16` | `ch3L` | `ch3H+16` | `ch4L` | `ch4H+16` | `ch5L` | `ch5H+16` | `ch6L` | `ch6H+16` | `ch7L` | `ch7H+16` | `ch8L` | `ch8H+16` | `ch9L` | `ch9H+16` | `ch10L` | `ch10H+16` | `ch11L` | `ch11H+16` |

### 3.2 Outgoing write order in sender functions

`sendingpress()` output order:

| out byte order | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| source | `pressures[0]` | `pressures[1]` | `pressures[2]` | `pressures[3]` | `pressures[4]` | `pressures[5]` | `pressures[6]` | `pressures[7]` | `pressures[8]` | `pressures[9]` | `pressures[10]` | `pressures[11]` | `pressures[12]` | `pressures[13]` | `pressures[14]` | `pressures[15]` | `pressures[16]` | `pressures[17]` | `pressures[18]` | `pressures[19]` | `pressures[20]` | `pressures[21]` | `pressures[22]` | `pressures[23]` |

`sendingcurr()` output order:

| out byte order | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| source | `currents[0]` | `currents[1]` | `currents[2]` | `currents[3]` | `currents[4]` | `currents[5]` | `currents[6]` | `currents[7]` | `currents[8]` | `currents[9]` | `currents[10]` | `currents[11]` | `currents[12]` | `currents[13]` | `currents[14]` | `currents[15]` | `currents[16]` | `currents[17]` | `currents[18]` | `currents[19]` | `currents[20]` | `currents[21]` | `currents[22]` | `currents[23]` |

### 3.3 Marker output arrays

Start marker output (sent on `0x78,0,0`): framed packet with `type=0x10`, zero payload.

Stop marker output (stop branch): framed packet with `type=0x11`, zero payload.

## 4) Pause Behavior (new)

- Pause flag is stored per scenario step from command `0x72` byte index `11` (`pauseFlags[step] = indata[11]`).
- When active step has `pauseFlags[stepscounter] == 1`:
  - firmware sets `pauseflag = 1`
  - scenario step index stops incrementing
  - telemetry sending is disabled (`sendingflag = 0`)
  - framed pause marker is emitted once: `type=0x12`, zero payload
- Resume is handled by command `0x22` (`indata[0] == 34`):
  - `pauseflag = 0`
  - telemetry sending is re-enabled (`sendingflag = 1`)
  - scenario timing continues; pause duration is accumulated into `totalpausetime`
- Experiment stop condition includes accumulated pause duration:

```text
millis() - previousstarttimer >= totaltime + totalpausetime
```

## 5) Timing/Behavior Notes

- send interval in telemetry: `2000` microseconds
- send pattern: 3 pressure packets, then 1 current packet

## 6) Bring-up Checklist (App <-> Hardware)

Use this order during field debugging after firmware/app changes:

1. Start app listener (`0x74`) and verify framed RX (`A5 5A`) with valid CRC.
2. Send `0x68` and verify PWM base frequency changes on controller.
3. Upload 2-3 scenario steps via `0x73` + `0x72` and confirm step index/timers are populated.
4. Ensure one step has `pauseFlag=1` in `0x72[11]`.
5. Start experiment with `0x78 00 00 ...` and verify controller emits `type=0x10`.
6. At pause step verify controller emits `type=0x12` once and telemetry pauses.
7. Send resume `0x22 00 00 ...` and verify telemetry resumes.
8. On completion verify controller emits `type=0x11` and app closes recording state.
