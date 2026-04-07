# MCMV2 Hardware Side (from `gidrotester_pico.ino`)

Scope: only `hardware/gidrotester_pico.ino`.

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
| val | `0x71` | `pwm0` | `pwm1` | `pwm2` | `pwm3` | `pwm4` | `pwm5` | `pwm6` | `pwm7` | `n/a` | `n/a` | `n/a` | `n/a` | `n/a` |

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
| val | `0x72` | `stepLow` | `stepHigh` | `out8` | `out9` | `out10` | `out11` | `out12` | `out13` | `gradLow` | `gradHigh` | `n/a` | `n/a` | `n/a` |

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

### 3.1 Telemetry packing slots (`pressures[]` / `currents[]`)

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
| source | `currents[0]` | `currents[1]` | `currents[2]` | `currents[3]` | `currents[4]` | `currents[5]` | `currents[6]` | `currents[7]` | `currents[8]` | `currents[9]` | `currents[10]` | `currents[11]` | `currents[12]` | `currents[13]` | `currents[14]` | `currents[15]` | `pressures[16]` | `pressures[17]` | `pressures[18]` | `pressures[19]` | `pressures[20]` | `pressures[21]` | `pressures[22]` | `pressures[23]` |

### 3.3 Marker output arrays

Start marker output (sent on `0x78,0,0`):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` | `FE` | `FF` |

Stop marker output (stop branch):

| idx | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| val | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` | `FF` |

## 4) Timing/Behavior Notes

- send interval in telemetry: `2000` microseconds
- send pattern: 3 pressure packets, then 1 current packet
- declared array length mismatch in file: arrays are declared `16`, but code accesses up to index `23`
