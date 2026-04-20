package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortPacketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.enums.ExplorerMode
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.storage.NewPointerLine
import ru.atrs.mcm.storage.addNewLineForChart
import ru.atrs.mcm.storage.generateNewChartLogFile
import ru.atrs.mcm.storage.models.UIGaugesData
import ru.atrs.mcm.utils.DataChunkCurrent
import ru.atrs.mcm.utils.DataChunkG
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.READY_FOR_LISTENING_OF_PAYLOAD
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import ru.atrs.mcm.utils.byteToInt
import ru.atrs.mcm.utils.dataChunkCurrents
import ru.atrs.mcm.utils.dataChunkRAW
import ru.atrs.mcm.utils.dataGauges
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.incrementTime
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.isExperimentStarts
import ru.atrs.mcm.utils.isOperatorPauseActive
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.logSerialRxFrame
import ru.atrs.mcm.utils.mapFloat
import ru.atrs.mcm.utils.onesAndTensFloat
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.pressuresChunkGauges
import ru.atrs.mcm.utils.operatorPauseDialogRequests
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.isSerialLogFullMode
import ru.atrs.mcm.utils.toHexString

private val DEBUG_PARSING = false

private const val FRAME_SIZE = 29
private const val PAYLOAD_SIZE = 24
private const val SOF1: Byte = 0xA5.toByte()
private const val SOF2: Byte = 0x5A
private const val TYPE_PRESSURE: Byte = 0x01
private const val TYPE_CURRENT: Byte = 0x02
private const val TYPE_START: Byte = 0x10
private const val TYPE_END: Byte = 0x11
private const val TYPE_PAUSE: Byte = 0x12

var rxFramesOk = 0L
var rxCrcFail = 0L
var rxResyncCount = 0L
var rxSeqDropCount = 0L
var lastSeq: Int? = null

private val framedRxBuffer = ArrayList<Byte>(FRAME_SIZE * 4)

internal data class FramedTelemetryFrame(
    val type: Byte,
    val seq: Int,
    val payload: ByteArray,
)

class PacketListener : SerialPortPacketListener {
    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun getPacketSize(): Int {
        return FRAME_SIZE
    }

    override fun serialEvent(event: SerialPortEvent) {
        if (isExperimentStarts) {
            counter++
        }

        val newData = event.receivedData
        if (!dataChunkRAW.tryEmit(newData)) {
            CoroutineScope(Dispatchers.IO).launch {
                dataChunkRAW.emit(newData)
            }
        }
        if (DEBUG_PARSING) {
            logGarbage("RX chunk: ${newData.toHexString()}")
        }
        //        flowRawComparatorMachine(updData = newData)
    }
}

private var counter = 0
private var counter2 = 0

var incrementExperiment = 0
private var lastGauge: DataChunkG? = null
private var pauseMessageCursor = 0

private var COUNTER = 0L
private var GARBAGECOUNTER = 0L

suspend fun flowRawComparatorMachine() {
    dataChunkRAW.collect { updData ->
        if (isSerialLogFullMode() && GARBAGECOUNTER <= 10 && READY_FOR_LISTENING_OF_PAYLOAD) {
            logGarbage("bytesReceiverMachine ${updData.toHexString()}")
            GARBAGECOUNTER++
        }

        val frames = consumeFramedTelemetryBytes(updData)
        for (frame in frames) {
            processFramedFrame(frame)
            if (isExperimentStarts) {
                COUNTER++
            }
        }
    }
}

private suspend fun processFramedFrame(frame: FramedTelemetryFrame) {
    logSerialRxFrame(
        type = frame.type.toInt() and 0xFF,
        seq = frame.seq,
        payloadSize = frame.payload.size,
    )

    when (frame.type) {
        TYPE_START -> {
            handleStartExperiment("framed seq=${frame.seq}")
        }

        TYPE_END -> {
            handleEndExperiment("framed seq=${frame.seq}")
        }

        TYPE_PAUSE -> {
            handlePauseExperiment("framed seq=${frame.seq}")
        }

        TYPE_PRESSURE -> {
            processPressurePayload(frame.payload)
        }

        TYPE_CURRENT -> {
            processCurrentPayload(frame.payload)
        }

        else -> {
            logError("Unknown framed type=0x${String.format("%02X", frame.type.toInt() and 0xFF)} seq=${frame.seq}")
        }
    }
}

private fun handleStartExperiment(marker: String) {
    generateNewChartLogFile()
    isExperimentStarts = true
    isOperatorPauseActive.value = false
    pauseMessageCursor = 0
    STATE_EXPERIMENT.value = StateExperiments.RECORDING
    println("isStartExperiment $marker")
    logInfo("Start Experiment! ${counter} ${isExperimentStarts}__${incrementExperiment}")
}

private suspend fun handlePauseExperiment(marker: String) {
    isOperatorPauseActive.value = true

    val pauseMessages = scenario
        .map { it.operatorCommand.trim() }
        .filter { it.isNotBlank() }

    val message = pauseMessages.getOrNull(pauseMessageCursor)
        ?: "Pause detected. Press OK to continue."

    if (pauseMessageCursor < pauseMessages.size) {
        pauseMessageCursor++
    }

    operatorPauseDialogRequests.emit(message)
    logInfo("Pause marker received: $marker")
}

private fun handleEndExperiment(marker: String) {
    println("isEndOfExperiment $marker")
    isExperimentStarts = false
    isOperatorPauseActive.value = false
    STATE_EXPERIMENT.value = StateExperiments.ENDING_OF_EXPERIMENT

    logInfo("End Experiment! ${counter} ; ${counter2}| all it == 0xFF ${isExperimentStarts}. count of packets of experiment: ${incrementExperiment}, COUNTER ${COUNTER}")
    counter = 0
    incrementExperiment = 0
    COUNTER = 0
}

private suspend fun processPressurePayload(payload: ByteArray) {
    if (payload.size < PAYLOAD_SIZE) {
        logError("Invalid pressure payload size=${payload.size}")
        return
    }

    if (isExperimentStarts && !isOperatorPauseActive.value) {
        incrementExperiment++
    }

    val isRecordingNow = isExperimentStarts && !isOperatorPauseActive.value

    val dch = DataChunkG(
        isExperiment = isRecordingNow,
        onesAndTensFloat(byteToInt(payload[0]).toUInt(), byteToInt(payload[1]).toUInt()),
        onesAndTensFloat(byteToInt(payload[2]).toUInt(), byteToInt(payload[3]).toUInt()),
        onesAndTensFloat(byteToInt(payload[4]).toUInt(), byteToInt(payload[5]).toUInt()),
        onesAndTensFloat(byteToInt(payload[6]).toUInt(), byteToInt(payload[7]).toUInt()),
        onesAndTensFloat(byteToInt(payload[8]).toUInt(), byteToInt(payload[9]).toUInt()),
        onesAndTensFloat(byteToInt(payload[10]).toUInt(), byteToInt(payload[11]).toUInt()),
        onesAndTensFloat(byteToInt(payload[12]).toUInt(), byteToInt(payload[13]).toUInt()),
        onesAndTensFloat(byteToInt(payload[14]).toUInt(), byteToInt(payload[15]).toUInt()),
        payload.getOrNull(17)?.let { onesAndTensFloat(byteToInt(payload[16]).toUInt(), byteToInt(payload[17]).toUInt()) },
        payload.getOrNull(19)?.let { onesAndTensFloat(byteToInt(payload[18]).toUInt(), byteToInt(payload[19]).toUInt()) },
        payload.getOrNull(21)?.let { onesAndTensFloat(byteToInt(payload[20]).toUInt(), byteToInt(payload[21]).toUInt()) },
        payload.getOrNull(23)?.let { onesAndTensFloat(byteToInt(payload[22]).toUInt(), byteToInt(payload[23]).toUInt()) },
    )

    pressuresChunkGauges.emit(dch)
    lastGauge = dch
}

private suspend fun processCurrentPayload(payload: ByteArray) {
    if (payload.size < PAYLOAD_SIZE) {
        logError("Invalid current payload size=${payload.size}")
        return
    }

    if (isExperimentStarts && !isOperatorPauseActive.value) {
        incrementExperiment++
    }

    val dchCurr = DataChunkCurrent(
        onesAndTensFloat(byteToInt(payload[0]).toUInt(), byteToInt(payload[1]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[2]).toUInt(), byteToInt(payload[3]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[4]).toUInt(), byteToInt(payload[5]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[6]).toUInt(), byteToInt(payload[7]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[8]).toUInt(), byteToInt(payload[9]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[10]).toUInt(), byteToInt(payload[11]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[12]).toUInt(), byteToInt(payload[13]).toUInt() - 16u).toInt(),
        onesAndTensFloat(byteToInt(payload[14]).toUInt(), byteToInt(payload[15]).toUInt() - 16u).toInt(),
        ninthCurrentData = payload.getOrNull(17)?.let { onesAndTensFloat(byteToInt(payload[16]).toUInt(), byteToInt(payload[17]).toUInt() - 16u).toInt() },
        tenthCurrentData = payload.getOrNull(19)?.let { onesAndTensFloat(byteToInt(payload[18]).toUInt(), byteToInt(payload[19]).toUInt() - 16u).toInt() },
        eleventhCurrentData = payload.getOrNull(21)?.let { onesAndTensFloat(byteToInt(payload[20]).toUInt(), byteToInt(payload[21]).toUInt() - 16u).toInt() },
        twelfthCurrentData = payload.getOrNull(23)?.let { onesAndTensFloat(byteToInt(payload[22]).toUInt(), byteToInt(payload[23]).toUInt() - 16u).toInt() },
    )

    dataChunkCurrents.emit(dchCurr)
    lastGauge?.let {
        pressuresChunkGauges.emit(it.copy(isExperiment = isExperimentStarts && !isOperatorPauseActive.value))
    }
}

internal fun consumeFramedTelemetryBytes(chunk: ByteArray): List<FramedTelemetryFrame> {
    if (chunk.isEmpty()) {
        return emptyList()
    }

    framedRxBuffer.addAll(chunk.toList())
    val parsedFrames = mutableListOf<FramedTelemetryFrame>()

    while (framedRxBuffer.size >= 2) {
        val sofIndex = findSofIndex()
        if (sofIndex < 0) {
            val bytesToKeep = if (framedRxBuffer.last() == SOF1) 1 else 0
            val garbageCount = framedRxBuffer.size - bytesToKeep
            if (garbageCount > 0) {
                framedRxBuffer.subList(0, garbageCount).clear()
                rxResyncCount++
            }
            break
        }

        if (sofIndex > 0) {
            framedRxBuffer.subList(0, sofIndex).clear()
            rxResyncCount++
        }

        if (framedRxBuffer.size < FRAME_SIZE) {
            break
        }

        val frame = ByteArray(FRAME_SIZE) { idx -> framedRxBuffer[idx] }
        val expectedCrc = crc8Telemetry(frame, 2, FRAME_SIZE - 1)
        val receivedCrc = frame[FRAME_SIZE - 1]

        if (expectedCrc != receivedCrc) {
            rxCrcFail++
            rxResyncCount++
            framedRxBuffer.removeAt(0)
            continue
        }

        val seq = byteToInt(frame[3])
        trackSeqGap(seq)
        rxFramesOk++

        parsedFrames += FramedTelemetryFrame(
            type = frame[2],
            seq = seq,
            payload = frame.copyOfRange(4, 4 + PAYLOAD_SIZE),
        )

        framedRxBuffer.subList(0, FRAME_SIZE).clear()
    }

    return parsedFrames
}

private fun findSofIndex(): Int {
    for (i in 0 until framedRxBuffer.size - 1) {
        if (framedRxBuffer[i] == SOF1 && framedRxBuffer[i + 1] == SOF2) {
            return i
        }
    }
    return -1
}

private fun trackSeqGap(seq: Int) {
    val previous = lastSeq
    if (previous != null) {
        val expected = (previous + 1) and 0xFF
        if (seq != expected) {
            rxSeqDropCount++
        }
    }
    lastSeq = seq
}

internal fun crc8Telemetry(data: ByteArray): Byte {
    return crc8Telemetry(data, 0, data.size)
}

private fun crc8Telemetry(data: ByteArray, offset: Int, endExclusive: Int): Byte {
    var crc = 0x00
    for (idx in offset until endExclusive) {
        crc = crc xor byteToInt(data[idx])
        repeat(8) {
            crc = if ((crc and 0x80) != 0) {
                ((crc shl 1) xor 0x07) and 0xFF
            } else {
                (crc shl 1) and 0xFF
            }
        }
    }
    return crc.toByte()
}

internal fun resetFramedTelemetryParserState() {
    framedRxBuffer.clear()
    rxFramesOk = 0L
    rxCrcFail = 0L
    rxResyncCount = 0L
    rxSeqDropCount = 0L
    lastSeq = null
}

var limiterForUI = 0
fun flowWriterMachine() {
    println("payloadWriterMachine")
    CoroutineScope(Dispatchers.IO).launch {
        var pressure1X = 0f
        var pressure2X = 0f
        var pressure3X = 0f
        var pressure4X = 0f
        var pressure5X = 0f
        var pressure6X = 0f
        var pressure7X = 0f
        var pressure8X = 0f
        var pressure9X = 0f
        var pressure10X = 0f
        var pressure11X = 0f
        var pressure12X = 0f
        indexOfScenario.value = 0

        pressuresChunkGauges.collect {
            if (pressures.isEmpty() || pressures.size < 7) {
                return@collect
            }

            mapFloat(it.firstGaugeData, 0f, 4095f, pressures[0].minValue, pressures[0].maxValue).let { it1 -> pressure1X = it1 }
            mapFloat(it.secondGaugeData, 0f, 4095f, pressures[1].minValue, pressures[1].maxValue).let { it1 -> pressure2X = it1 }
            mapFloat(it.thirdGaugeData, 0f, 4095f, pressures[2].minValue, pressures[2].maxValue).let { it1 -> pressure3X = it1 }
            mapFloat(it.fourthGaugeData, 0f, 4095f, pressures[3].minValue, pressures[3].maxValue).let { it1 -> pressure4X = it1 }
            mapFloat(it.fifthGaugeData, 0f, 4095f, pressures[4].minValue, pressures[4].maxValue).let { it1 -> pressure5X = it1 }
            mapFloat(it.sixthGaugeData, 0f, 4095f, pressures[5].minValue, pressures[5].maxValue).let { it1 -> pressure6X = it1 }
            mapFloat(it.seventhGaugeData, 0f, 4095f, pressures[6].minValue, pressures[6].maxValue).let { it1 -> pressure7X = it1 }
            mapFloat(it.eighthGaugeData, 0f, 4095f, pressures[7].minValue, pressures[7].maxValue).let { it1 -> pressure8X = it1 }

            if (TWELVE_CHANNELS_MODE) {
                if (pressures.getOrNull(8) != null && it.ninthGaugeData != null) {
                    mapFloat(it.ninthGaugeData!!, 0f, 4095f, pressures[8].minValue, pressures[8].maxValue).let { pressure9X = it }
                }
                if (pressures.getOrNull(9) != null && it.tenthGaugeData != null) {
                    mapFloat(it.tenthGaugeData!!, 0f, 4095f, pressures[9].minValue, pressures[9].maxValue).let { pressure10X = it }
                }
                if (pressures.getOrNull(10) != null && it.eleventhGaugeData != null) {
                    mapFloat(it.eleventhGaugeData!!, 0f, 4095f, pressures[10].minValue, pressures[10].maxValue).let { pressure11X = it }
                }
                if (pressures.getOrNull(11) != null && it.twelfthGaugeData != null) {
                    mapFloat(it.twelfthGaugeData!!, 0f, 4095f, pressures[11].minValue, pressures[11].maxValue).let { pressure12X = it }
                }
            }

            limiterForUI++
            if (limiterForUI % 5L == 0L) {
                dataGauges.emit(
                    UIGaugesData(
                        pressure1 = pressure1X,
                        pressure2 = pressure2X,
                        pressure3 = pressure3X,
                        pressure4 = pressure4X,
                        pressure5 = pressure5X,
                        pressure6 = pressure6X,
                        pressure7 = pressure7X,
                        pressure8 = pressure8X,
                        pressure9 = pressure9X,
                        pressure10 = pressure10X,
                        pressure11 = pressure11X,
                        pressure12 = pressure12X,
                    )
                )
                limiterForUI = 0
            }

            when (EXPLORER_MODE.value) {
                ExplorerMode.AUTO -> {
                    if (it.isExperiment) {
                        counter2++
                        addNewLineForChart(
                            newLine = NewPointerLine(
                                incrementTime = incrementTime,
                                ch1 = pressure1X,
                                ch2 = pressure2X,
                                ch3 = pressure3X,
                                ch4 = pressure4X,
                                ch5 = pressure5X,
                                ch6 = pressure6X,
                                ch7 = pressure7X,
                                ch8 = pressure8X,
                                ch9 = pressure9X,
                                ch10 = pressure10X,
                                ch11 = pressure11X,
                                ch12 = pressure12X,
                            ),
                            isRecordingExperiment = it.isExperiment,
                        )
                        incrementTime += 2L
                    } else if (STATE_EXPERIMENT.value == StateExperiments.ENDING_OF_EXPERIMENT) {
                        STATE_EXPERIMENT.value = StateExperiments.PREPARE_CHART

                        if (!isAlreadyReceivedBytesForChart.value) {
                            isAlreadyReceivedBytesForChart.value = true
                            delay(1200)
                            doOpen_First_ChartWindow.value = true
                            STATE_EXPERIMENT.value = StateExperiments.NONE
                            incrementTime = 0
                            counter2 = 0
                        }
                    }
                }

                ExplorerMode.MANUAL -> {
                    /** without recording */
                }
            }
        }
    }
}

