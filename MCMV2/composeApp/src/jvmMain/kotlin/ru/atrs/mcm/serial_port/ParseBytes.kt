package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.*
import ru.atrs.mcm.enums.StateExperiments
import kotlinx.coroutines.*
import ru.atrs.mcm.enums.ExplorerMode
import ru.atrs.mcm.serial_port.RouterCommunication.stopSerialCommunication
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
import ru.atrs.mcm.utils.pressuresChunkGauges
import ru.atrs.mcm.utils.dataChunkRAW
import ru.atrs.mcm.utils.dataGauges
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.incrementTime
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.isExperimentStarts
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.mapFloat
import ru.atrs.mcm.utils.onesAndTensFloat
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.toHexString


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
//        println("${newData.toHexString()}")
        if (!dataChunkRAW.tryEmit(newData)) {
            runBlocking {
                dataChunkRAW.emit(newData)
            }
        }
//        flowRawComparatorMachine(updData = newData)
    }
}

private const val FRAME_SOF1 = 0xA5
private const val FRAME_SOF2 = 0x5A
private const val FRAME_SIZE = 29
private const val FRAME_PAYLOAD_SIZE = 24
private const val FRAME_DATA_FOR_CRC = 26

private const val FRAME_TYPE_PRESSURE = 0x01
private const val FRAME_TYPE_CURRENT = 0x02
private const val FRAME_TYPE_START = 0x10
private const val FRAME_TYPE_END = 0x11

private data class ParsedFrame(
    val type: Int,
    val seq: Int,
    val payload: ByteArray
)

private var counter = 0
private var counter2 = 0

var incrementExperiment = 0
private var lastGauge : DataChunkG? = null

private var COUNTER = 0L
private var GARBAGECOUNTER = 0L

private var pendingRxBytes = ByteArray(0)
private var lastSeq = -1
private var rxFramesOk = 0L
private var rxCrcFail = 0L
private var rxResyncCount = 0L
private var rxSeqDropCount = 0L

private fun crc8(data: ByteArray, offset: Int, length: Int): Int {
    var crc = 0
    var i = offset
    while (i < offset + length) {
        crc = crc xor byteToInt(data[i])
        repeat(8) {
            crc = if ((crc and 0x80) != 0) {
                ((crc shl 1) xor 0x07) and 0xFF
            } else {
                (crc shl 1) and 0xFF
            }
        }
        i++
    }
    return crc and 0xFF
}

private fun appendPendingBytes(chunk: ByteArray) {
    if (chunk.isEmpty()) {
        return
    }
    val merged = ByteArray(pendingRxBytes.size + chunk.size)
    pendingRxBytes.copyInto(merged, destinationOffset = 0)
    chunk.copyInto(merged, destinationOffset = pendingRxBytes.size)
    pendingRxBytes = merged
}

private fun extractFramesFromChunk(chunk: ByteArray): List<ParsedFrame> {
    appendPendingBytes(chunk)

    val frames = mutableListOf<ParsedFrame>()

    while (true) {
        if (pendingRxBytes.size < 2) {
            return frames
        }

        var sofIndex = -1
        var i = 0
        while (i <= pendingRxBytes.size - 2) {
            if (byteToInt(pendingRxBytes[i]) == FRAME_SOF1 && byteToInt(pendingRxBytes[i + 1]) == FRAME_SOF2) {
                sofIndex = i
                break
            }
            i++
        }

        if (sofIndex == -1) {
            val keepTail = pendingRxBytes.lastOrNull()?.let { byteToInt(it) == FRAME_SOF1 } == true
            pendingRxBytes = if (keepTail) byteArrayOf(pendingRxBytes.last()) else byteArrayOf()
            rxResyncCount++
            return frames
        }

        if (sofIndex > 0) {
            pendingRxBytes = pendingRxBytes.copyOfRange(sofIndex, pendingRxBytes.size)
            rxResyncCount++
        }

        if (pendingRxBytes.size < FRAME_SIZE) {
            return frames
        }

        val crcExpected = byteToInt(pendingRxBytes[FRAME_SIZE - 1])
        val crcActual = crc8(pendingRxBytes, 2, FRAME_DATA_FOR_CRC)

        if (crcExpected != crcActual) {
            rxCrcFail++
            pendingRxBytes = pendingRxBytes.copyOfRange(1, pendingRxBytes.size)
            rxResyncCount++
            continue
        }

        val type = byteToInt(pendingRxBytes[2])
        val seq = byteToInt(pendingRxBytes[3])
        val payload = pendingRxBytes.copyOfRange(4, 4 + FRAME_PAYLOAD_SIZE)
        frames.add(ParsedFrame(type = type, seq = seq, payload = payload))
        rxFramesOk++

        pendingRxBytes = if (pendingRxBytes.size > FRAME_SIZE) {
            pendingRxBytes.copyOfRange(FRAME_SIZE, pendingRxBytes.size)
        } else {
            byteArrayOf()
        }
    }
}

private fun trackSeq(seq: Int) {
    if (lastSeq != -1) {
        val expected = (lastSeq + 1) and 0xFF
        if (seq != expected) {
            val missed = (seq - expected + 256) and 0xFF
            rxSeqDropCount += missed.toLong()
            logError("Telemetry seq gap detected: expected=$expected got=$seq missed=$missed")
        }
    }
    lastSeq = seq
}

suspend fun flowRawComparatorMachine() {
    //logInfo("Flow Receiver Machine, packet size: 24, protocol type: NEW")
    dataChunkRAW.collect { updData ->
        var dch: DataChunkG? = null
        var dchCurr: DataChunkCurrent? = null

//        if (incrementTime >= 100_000 && !isExperimentStarts) {
//            incrementTime = 0
//        }
        if (GARBAGECOUNTER <= 10 && READY_FOR_LISTENING_OF_PAYLOAD) {
            logGarbage("bytesReceiverMachine ${updData.toHexString()}")
            GARBAGECOUNTER++
        }

        //writeToFile(">> ${updData.toHexString()}", MainConfig_LogFile)

        val frames = extractFramesFromChunk(updData)
        for (frame in frames) {
            trackSeq(frame.seq)
            val payload = frame.payload
            when (frame.type) {
                FRAME_TYPE_START -> {
                    generateNewChartLogFile()
                    isExperimentStarts = true
                    STATE_EXPERIMENT.value = StateExperiments.RECORDING
                    println("isStartExperiment frame seq=${frame.seq}")
                    logInfo("Start Experiment! ${counter} ${isExperimentStarts}__${incrementExperiment}")
                }
                FRAME_TYPE_END -> {
                    println("isEndOfExperiment frame seq=${frame.seq}")
                    isExperimentStarts = false
                    STATE_EXPERIMENT.value = StateExperiments.ENDING_OF_EXPERIMENT

                    logInfo("End Experiment! ${counter} ; ${counter2}| frame seq=${frame.seq} ${isExperimentStarts}. count of packets of experiment: ${incrementExperiment}, COUNTER ${COUNTER}. rxOk=$rxFramesOk crcFail=$rxCrcFail seqDrops=$rxSeqDropCount resync=$rxResyncCount")
                    counter = 0
                    incrementExperiment = 0
                    COUNTER = 0
                }
                FRAME_TYPE_PRESSURE -> {
                    if (isExperimentStarts) {
                        incrementExperiment++
                    }
                    dch = DataChunkG(
                        isExperiment = isExperimentStarts,
                        onesAndTensFloat(byteToInt(payload[0]).toUInt(), byteToInt(payload[1]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[2]).toUInt(), byteToInt(payload[3]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[4]).toUInt(), byteToInt(payload[5]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[6]).toUInt(), byteToInt(payload[7]).toUInt()),

                        onesAndTensFloat(byteToInt(payload[8]).toUInt(), byteToInt(payload[9]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[10]).toUInt(), byteToInt(payload[11]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[12]).toUInt(), byteToInt(payload[13]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[14]).toUInt(), byteToInt(payload[15]).toUInt()),

                        onesAndTensFloat(byteToInt(payload[16]).toUInt(), byteToInt(payload[17]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[18]).toUInt(), byteToInt(payload[19]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[20]).toUInt(), byteToInt(payload[21]).toUInt()),
                        onesAndTensFloat(byteToInt(payload[22]).toUInt(), byteToInt(payload[23]).toUInt())
                    )

                    pressuresChunkGauges.emit(dch)
                    lastGauge = dch
                }
                FRAME_TYPE_CURRENT -> {
                    if (isExperimentStarts) {
                        incrementExperiment++
                    }
                    dchCurr = DataChunkCurrent(
                        onesAndTensFloat(byteToInt(payload[0]).toUInt(), byteToInt(payload[1]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[2]).toUInt(), byteToInt(payload[3]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[4]).toUInt(), byteToInt(payload[5]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[6]).toUInt(), byteToInt(payload[7]).toUInt() - 16u).toInt(),

                        onesAndTensFloat(byteToInt(payload[8]).toUInt(), byteToInt(payload[9]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[10]).toUInt(), byteToInt(payload[11]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[12]).toUInt(), byteToInt(payload[13]).toUInt() - 16u).toInt(),
                        onesAndTensFloat(byteToInt(payload[14]).toUInt(), byteToInt(payload[15]).toUInt() - 16u).toInt(),

                        ninthCurrentData = onesAndTensFloat(byteToInt(payload[16]).toUInt(), byteToInt(payload[17]).toUInt() - 16u).toInt(),
                        tenthCurrentData = onesAndTensFloat(byteToInt(payload[18]).toUInt(), byteToInt(payload[19]).toUInt() - 16u).toInt(),
                        eleventhCurrentData = onesAndTensFloat(byteToInt(payload[20]).toUInt(), byteToInt(payload[21]).toUInt() - 16u).toInt(),
                        twelfthCurrentData = onesAndTensFloat(byteToInt(payload[22]).toUInt(), byteToInt(payload[23]).toUInt() - 16u).toInt(),
                    )
                    dataChunkCurrents.emit(dchCurr)
                    lastGauge?.let { pressuresChunkGauges.emit(it) }
                }
                else -> {
                    logError("Unknown telemetry frame type: ${frame.type}, seq=${frame.seq}")
                }
            }
        }
        if (isExperimentStarts) {
            COUNTER++
        }
    }
}

//var jobFlowWriter =

var limiterForUI = 0
fun flowWriterMachine() {
    println("payloadWriterMachine")
    CoroutineScope(Dispatchers.IO).launch {
        var pressure1X =  0f
        var pressure2X =  0f
        var pressure3X =  0f
        var pressure4X =  0f
        var pressure5X =  0f
        var pressure6X =  0f
        var pressure7X =  0f
        var pressure8X =  0f
        var pressure9X  = 0f
        var pressure10X = 0f
        var pressure11X = 0f
        var pressure12X = 0f
        //EXPLORER_MODE.value = ExplorerMode.MANUAL
        //reInitSolenoids()
        indexOfScenario.value = 0

        //sound_On()

//        scenario.clear()
        pressuresChunkGauges.collect {
//            println("|<<<<<<<<<<<<<<<<<<<${it.isExperiment} [${pressuresChunkGauges.subscriptionCount.value} ]")
            if (pressures.isEmpty() || pressures.size < 7) {
                return@collect
            }

            // FILLING PRESSURES
            mapFloat(it.firstGaugeData, 0f, 4095f,   (pressures[0].minValue), (pressures[0].maxValue)).let { it1 -> pressure1X = it1 }//.takeIf { pressures.size >= 1 }
            mapFloat(it.secondGaugeData, 0f, 4095f, (pressures[1].minValue), (pressures[1].maxValue )).let { it1 -> pressure2X = it1 }//.takeIf { pressures.size >= 2 }
            mapFloat(it.thirdGaugeData, 0f, 4095f, (pressures[2].minValue), (pressures[2].maxValue  )).let { it1 -> pressure3X = it1 }//.takeIf { pressures.size >= 3 }
            mapFloat(it.fourthGaugeData, 0f, 4095f, (pressures[3].minValue), (pressures[3].maxValue )).let { it1 -> pressure4X = it1 }//.takeIf { pressures.size >= 4 }
            mapFloat(it.fifthGaugeData, 0f, 4095f, (pressures[4].minValue), (pressures[4].maxValue  )).let { it1 -> pressure5X = it1 }//.takeIf { pressures.size >= 5 }
            mapFloat(it.sixthGaugeData, 0f, 4095f, (pressures[5].minValue), (pressures[5].maxValue  )).let { it1 -> pressure6X = it1 }//.takeIf { pressures.size >= 6 }
            mapFloat(it.seventhGaugeData, 0f, 4095f, (pressures[6].minValue), (pressures[6].maxValue)).let { it1 -> pressure7X = it1 }//.takeIf { pressures.size >= 7 }
            mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[7].minValue), (pressures[7].maxValue )).let { it1 -> pressure8X = it1 }//.takeIf { pressures.size >= 8 }

            if (TWELVE_CHANNELS_MODE) {
                if (pressures.getOrNull(8)  != null && it.ninthGaugeData   != null) {(mapFloat(it.ninthGaugeData   !!   , 0f, 4095f, (pressures[8].minValue),    (pressures[8].maxValue)).let { pressure9X = it })}
                if (pressures.getOrNull(9)  != null && it.tenthGaugeData   != null) {(mapFloat(it.tenthGaugeData   !!   , 0f, 4095f, (pressures[9].minValue),   (pressures[9].maxValue)).let { pressure10X = it })}
                if (pressures.getOrNull(10) != null && it.eleventhGaugeData!= null) {(mapFloat(it.eleventhGaugeData!!, 0f, 4095f, (pressures[10].minValue), (pressures[10].maxValue)).let { pressure11X = it })}
                if (pressures.getOrNull(11) != null && it.twelfthGaugeData != null) {(mapFloat(it.twelfthGaugeData !! , 0f, 4095f, (pressures[11].minValue), (pressures[11].maxValue)).let { pressure12X = it })}
            }

            limiterForUI++
            if (limiterForUI%5L == 0L) {
                dataGauges.emit(
                    UIGaugesData(
                        pressure1  = pressure1X,
                        pressure2  = pressure2X,
                        pressure3  = pressure3X,
                        pressure4  = pressure4X,
                        pressure5  = pressure5X,
                        pressure6  = pressure6X,
                        pressure7  = pressure7X,
                        pressure8  = pressure8X,
                        pressure9  = pressure9X,
                        pressure10 = pressure10X,
                        pressure11 = pressure11X,
                        pressure12 = pressure12X
                    )
                )
                limiterForUI = 0
            }

            when (EXPLORER_MODE.value) {
                ExplorerMode.AUTO -> {
                    //logGarbage("konec ${}")
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
                                ch9 = pressure9X ,
                                ch10 =pressure10X,
                                ch11 =pressure11X,
                                ch12 =pressure12X
                            ),
                            isRecordingExperiment = it.isExperiment
                        )
                        incrementTime += 2L

                    } else if (STATE_EXPERIMENT.value == StateExperiments.ENDING_OF_EXPERIMENT) {
                        // logGarbage("Output: |${incrementExperiment}|=>|${count}|  | ${arr1Measure.size} ${arr1Measure[arr1Measure.lastIndex]}")

                        STATE_EXPERIMENT.value = StateExperiments.PREPARE_CHART

                        if (!isAlreadyReceivedBytesForChart.value) {
                            isAlreadyReceivedBytesForChart.value = true

                            //createMeasureExperiment()
                            delay(1200)
                            doOpen_First_ChartWindow.value = true
                            STATE_EXPERIMENT.value = StateExperiments.NONE
//                            chartFileAfterExperiment.value = null
                            incrementTime = 0
                            counter2 = 0
                        }
                    }
                }
                ExplorerMode.MANUAL -> { /** without recording */ }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////
