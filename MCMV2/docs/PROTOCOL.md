# MCMV2 Serial Communication Protocol

## Overview

MCMV2 communicates with hardware controllers via RS-232 serial communication. The application sends commands to control solenoids, run scenarios, and receives pressure/current data.

**Priority**: This documentation focuses on the **application-side (Kotlin)** protocol implementation.

---

## Communication Parameters

| Parameter | Value |
|-----------|-------|
| **Baud Rate** | 500,000 |
| **Data Bits** | 8 |
| **Stop Bits** | 1 |
| **Parity** | None |
| **Flow Control** | None |
| **Timeout** | `TIMEOUT_READ_SEMI_BLOCKING` |

---

## Command Protocol (App → Hardware)

All commands are 14 bytes with the command byte as the first byte, followed by payload and padding.

### 1. Start Receiving (0x74)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x74│ 0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Start continuous data reception from hardware.

---

### 2. Reset (0x54)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x54│ 0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Reset the hardware controller and stop all outputs.

---

### 3. Set Frequency (0x68)
```
┌────┬────────┬────────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x68│FREQ_LO │FREQ_HI│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────────┴────────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Set PWM frequency for solenoid control.

**Frequency Calculation**:
```
frequency = (FREQ_HI << 8) | FREQ_LO
```

---

### 4. Set PWM Channels 1-4 (0x71)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x71│ CH1│0x00│ CH2│0x00│ CH3│0x00│ CH4│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Set PWM duty cycle for channels 1-4 (0-255).

---

### 5. Set PWM Channels 5-8 (0x51)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x51│ CH5│0x00│ CH6│0x00│ CH7│0x00│ CH8│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Set PWM duty cycle for channels 5-8 (0-255).

---

### 6. Scenario Step Part 1 (0x73)
```
┌────┬──────┬──────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────┬────┬────┐
│0x73│ IDX0 │ IDX1 │  CH1   │  CH2   │  CH3   │  CH4   │  CH5   │  CH6   │  CH7   │  CH8   │ T0 │ T1 │0x00│
└────┴──────┴──────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────┴────┴────┘
```
**Purpose**: Define a scenario step with channels 1-8 values and duration.

**Fields**:
- `IDX0, IDX1`: Step index (little-endian 16-bit)
- `CH1-CH8`: PWM values for each channel (0-255)
- `T0, T1`: Duration in milliseconds (little-endian 16-bit)

---

### 7. Scenario Step Part 2 (0x72)
```
┌────┬──────┬──────┬────────┬────────┬────────┬────────┬──────┬──────┬────┬────────┬────────┬────┬────┬────┐
│0x72│ IDX0 │ IDX1 │  CH9   │ CH10   │ CH11   │ CH12   │ ANA1 │ ANA2 │0x00│ GRAD0  │ GRAD1  │0x00│0x00│0x00│
└────┴──────┴──────┴────────┴────────┴────────┴────────┴──────┴──────┴────┴────────┴────────┴────┴────┴────┘
```
**Purpose**: Define a scenario step with channels 9-12 values, analog inputs, and gradient time.

**Fields**:
- `IDX0, IDX1`: Step index (must match 0x73 command)
- `CH9-CH12`: PWM values for channels 9-12
- `ANA1, ANA2`: Analog input values (0-255)
- `GRAD0, GRAD1`: Gradient transition time (little-endian 16-bit)

---

### 8. Start Recording (0x78)
```
┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
│0x78│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│0x00│
└────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┴────┘
```
**Purpose**: Start experiment recording. Hardware sends `FE FF` pattern followed by data.

---

## Data Protocol (Hardware → App)

### Data Packet Format

Hardware sends **24 bytes** per packet (12 channels × 2 bytes).

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    DATA PACKET (24 bytes)                               │
├────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│ VALUE_0│ VALUE_1│ VALUE_2│ VALUE_3│ VALUE_4│ VALUE_5│ VALUE_6│ VALUE_7│ VALUE_8│ VALUE_9│VALUE_10│VALUE_11│
│  (Hi)  │  (Lo)  │  (Hi)  │  (Lo)  │  (Hi)  │  (Lo)  │  (Hi)  │  (Lo)  │  (Hi)  │  (Lo)  │  (Hi)  │  (Lo)  │
└────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┘
```

---

### Value Decoding

Each value is a 12-bit number encoded in 2 bytes.

**High Byte Structure**:
```
┌────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│   X    │   X    │   X    │   X    │  D11   │  D10   │  D9    │  D8    │
└────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┘
         Upper 4 bits (unused)              Lower 4 bits (high nibble of 12-bit value)
```

**12-bit Value Calculation**:
```kotlin
// Combine high and low bytes
val highByte = data[i + 1].toInt() and 0x0F  // Lower 4 bits only
val lowByte = data[i].toInt()
val rawValue = (highByte shl 8) or lowByte   // 0-4095
```

**Current Value Adjustment** (for current readings):
```kotlin
// High byte has +16 offset for currents
val currentValue = rawValue  // Hardware handles offset internally
```

---

### Special Packets

| Packet Type | Pattern | Purpose |
|-------------|---------|---------|
| **Start Experiment** | `FE FF FE FF ...` (24 bytes) | Indicates experiment started |
| **End Experiment** | `FF FF FF FF ...` (24 bytes) | Indicates experiment ended |

**Detection Logic**:
```kotlin
fun isStartExperiment(data: ByteArray): Boolean {
    return data.size >= 24 && data.all { it == 0xFE.toByte() || it == 0xFF.toByte() }
}

fun isEndExperiment(data: ByteArray): Boolean {
    return data.all { it == 0xFF.toByte() }
}
```

---

## Data Flow Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              HARDWARE CONTROLLER                              │
│                           RS-232 @ 500000 baud                               │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            APPLICATION LAYER                                 │
│                                                                             │
│  ┌──────────────────┐     ┌───────────────────┐     ┌─────────────────┐  │
│  │  PacketListener   │────▶│  dataChunkRAW      │────▶│ flowRawCompar-  │  │
│  │  (jSerialComm)   │     │  MutableSharedFlow │     │ atorMachine()   │  │
│  └──────────────────┘     └───────────────────┘     └────────┬────────┘  │
│                                                             │              │
│                                         ┌───────────────────┴───────────┐  │
│                                         ▼                               ▼  │
│                           ┌───────────────────────┐     ┌───────────────────────┐
│                           │  pressuresChunkGauges  │     │   dataChunkCurrents   │
│                           │  SharedFlow<DataChunkG>│     │ SharedFlow<DataChunkCurrent>
│                           └───────────┬───────────┘     └───────────┬───────────┘
│                                       │                             │
└───────────────────────────────────────┼─────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FLOW WRITER MACHINE                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                         flowWriterMachine()                          │  │
│  │                                                                     │  │
│  │  1. Collect pressuresChunkGauges                                    │  │
│  │  2. Map raw ADC values (0-4095) → physical units                    │  │
│  │  3. Throttle UI updates (every 5th packet)                          │  │
│  │  4. Emit to dataGauges for UI                                       │  │
│  │  5. Write to chart file if recording                                │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────┬────────────────────────────────┘
                                             │
                          ┌──────────────────┴──────────────────┐
                          ▼                                     ▼
              ┌───────────────────────┐             ┌───────────────────────┐
              │     dataGauges         │             │    Chart File Writer   │
              │  SharedFlow<UIGaugesData>│             │  addNewLineForChart() │
              └───────────────────────┘             └───────────────────────┘
```

---

## State Machine

```
┌─────────────┐
│    INIT     │ (Application start)
└──────┬──────┘
       │ startReceiveFullData()
       ▼
┌─────────────┐
│   RECORDING  │◀────────────────────┐
└──────┬──────┘                    │
       │ End packet (all 0xFF)     │ Start packet (all 0xFE)
       ▼                            │
┌─────────────────────┐            │
│  ENDING_OF_EXPERIMENT │──────────┘
└──────────┬──────────┘
           │ Delay 1.2s
           ▼
┌─────────────────────┐
│    PREPARE_CHART     │ (Open chart window)
└──────────┬──────────┘
           │
           ▼
┌─────────────┐
│    NONE     │ (Idle state)
└─────────────┘
```

---

## Key Source Files

| Component | File | Description |
|-----------|------|-------------|
| Protocol Interface | `serial_port/COMProtocol.kt` | Abstract interface for serial communication |
| V2 Implementation | `serial_port/CommMachineV2.kt` | Current protocol implementation |
| Byte Parser | `serial_port/ParseBytes.kt` | Data packet parsing and state machine |
| Global State | `utils/GlobalVariables.kt` | SharedFlows and data structures |
| Router | `serial_port/RouterCommunication.kt` | Protocol selection router |

---

## Kotlin Implementation Reference

### Sending Commands

```kotlin
// Via CommMachineV2
writeToSerialPort(
    byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00, 
                0x00, 0x00, 0x00, 0x00, 0x00),
    delay = 100L
)

// Via RouterCommunication
RouterCommunication.writeToSerialPort(bytes, flush = false, delay = 0)
```

### Receiving Data

```kotlin
// Collect raw bytes
dataChunkRAW.collect { bytes ->
    // bytes is ByteArray received from hardware
}

// Collect parsed pressure data
pressuresChunkGauges.collect { chunk: DataChunkG ->
    val ch1Pressure = chunk.firstGaugeData  // Already decoded Float
}

// Collect parsed current data
dataChunkCurrents.collect { chunk: DataChunkCurrent ->
    val ch1Current = chunk.firstCurrentData  // Int value
}
```

---

## Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `PRESSURE_MAX_RAW` | 4095 | Maximum raw ADC value (12-bit) |
| `CURRENT_MAX_RAW` | 255 | Maximum raw current value |
| `PACKET_SIZE_V1` | 16 | Old protocol packet size |
| `PACKET_SIZE_V2` | 24 | New protocol packet size |

---

## Troubleshooting

| Issue | Possible Cause | Solution |
|-------|---------------|----------|
| No data received | Serial port not open | Check `serialPort.isOpen` |
| Garbage data | Baud rate mismatch | Verify 500000 baud on both sides |
| Intermittent data | Timeout too short | Increase timeout in `setComPortTimeouts` |
| Wrong values | Protocol version mismatch | Check `PROTOCOL_TYPE` setting |
