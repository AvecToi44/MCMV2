# Serial Protocol Documentation

## Overview

MCMV2 communicates with hardware controllers via RS-232 serial communication. The application supports two protocol versions for backward compatibility.

## Protocol Selection

Protocol selection is controlled by the global variable:
```kotlin
var PROTOCOL_TYPE: ProtocolType = ProtocolType.OLD_AUG_2025
```

- `ProtocolType.OLD_AUG_2025` - Legacy protocol (V1)
- `ProtocolType.NEW` - New protocol with 12-channel support (V2)

## V2 Protocol (Current)

### Command Reference

| Command | Byte | Description | Payload Format |
|---------|------|-------------|----------------|
| Channels 1-4 | `0x71` | Set PWM for channels 1-4 | `ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00` |
| Channels 5-8 | `0x51` | Set PWM for channels 5-8 | `ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00` |
| Scenario Part 1 | `0x73` | Scenario step data | `index, ch1-4, time` |
| Scenario Part 2 | `0x72` | Scenario step data | `index, ch5-12, analog1, analog2, gradientTime` |
| Set Frequency | `0x68` | Set main frequency | `mainFreq` bytes |
| Start Receive | `0x74` | Start data reception | - |
| Reset | `0x54` | Reset controller | - |
| Start Recording | `0x78` | Start experiment recording | - |

### Protocol Interface (COMProtocol.kt)

```kotlin
interface COMProtocol {
    suspend fun initSerialCommunication()
    suspend fun startReceiveFullData()
    fun stopSerialCommunication()
    fun cleanCOMPort()
    suspend fun resetSerialComm()
    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long)
    suspend fun comparatorToSolenoid(newIndex: Int)
    suspend fun sendScenarioToController()
    suspend fun reInitSolenoids()
    fun sendFrequency()
    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean)
}
```

### Implementation Classes

- `CommMachineV1.kt` - Legacy August 2025 protocol
- `CommMachineV2.kt` - Current protocol with extended features
- `RouterCommunication.kt` - Routes calls to appropriate implementation

## Byte Parsing

The `ParseBytes.kt` module handles incoming serial data streams:

1. Receives raw bytes via `dataChunkRAW` SharedFlow
2. Parses byte arrays into channel data
3. Emits parsed data via `pressuresChunkGauges` SharedFlow
4. Handles current readings via `dataChunkCurrents` SharedFlow

### Parsing States

```kotlin
enum class StateParseBytes {
    INIT,       // Initial state
    RECEIVING,  // Actively receiving data
    READY,      // Data ready
    ERROR       // Parsing error
}
```

## Communication Configuration

```kotlin
var COM_PORT = "COM0"           // Serial port name
var BAUD_RATE = 500000          // Communication speed
```

## Data Flow

```
Serial Port → Byte Array → ParseBytes → pressuresChunkGauges (DataChunkG)
                                         ↓
                                   dataGauges (UIGaugesData)
                                         ↓
                                   UI Gauges Display
```

## Raw Data Format

Incoming bytes are parsed into:

```kotlin
data class DataChunkG(
    var isExperiment: Boolean = false,
    var firstGaugeData: Float,    // Channel 1
    var secondGaugeData: Float,   // Channel 2
    // ... channels 3-8 (mandatory)
    // ... channels 9-12 (optional, 12-channel mode)
)

data class DataChunkCurrent(
    var firstCurrentData: Int,    // Channel 1 current
    var secondCurrentData: Int,   // Channel 2 current
    // ... channels 3-8 (mandatory)
    // ... channels 9-12 (optional)
)
```

## Important Notes

1. All serial operations run on `Dispatchers.IO`
2. Protocol router selects implementation based on `PROTOCOL_TYPE`
3. Byte parsing handles stream fragmentation
4. Controller uses 12-bit ADC: max raw value = 4095
5. Current readings are 8-bit: max raw value = 255
