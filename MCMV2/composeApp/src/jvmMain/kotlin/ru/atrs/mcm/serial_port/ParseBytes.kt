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
import ru.atrs.mcm.utils.PROTOCOL_TYPE
import ru.atrs.mcm.utils.ProtocolType
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
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.mapFloat
import ru.atrs.mcm.utils.onesAndTensFloat
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.toHexString


private val DEBUG_PARSING = false



class PacketListener : SerialPortPacketListener {
    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun getPacketSize(): Int {
        return if (PROTOCOL_TYPE == ProtocolType.NEW) 24 else 16
    }

    override fun serialEvent(event: SerialPortEvent) {
        if (isExperimentStarts) {
            counter++
        }

        CoroutineScope(Dispatchers.IO).launch {
            val newData = event.receivedData
//            println("${newData.toHexString()}")
            dataChunkRAW.emit(newData)
//            flowRawComparatorMachine(updData = newData)
        }
    }
}
private var counter = 0
private var counter2 = 0

var incrementExperiment = 0
private var lastGauge : DataChunkG? = null

private var COUNTER = 0L
suspend fun flowRawComparatorMachine() {
    //logInfo("Flow Receiver Machine, with packet size: ${if (PROTOCOL_TYPE == ProtocolType.NEW) 24 else 16}, protocol type: ${PROTOCOL_TYPE.name}")
    dataChunkRAW.collect { updData ->
        var dch: DataChunkG? = null
        var dchCurr: DataChunkCurrent? = null

//        if (incrementTime >= 100_000 && !isExperimentStarts) {
//            incrementTime = 0
//        }
//        logGarbage("bytesReceiverMachine ${updData.toHexString()}")
        //writeToFile(">> ${updData.toHexString()}", MainConfig_LogFile)

        when {
            PROTOCOL_TYPE == ProtocolType.NEW && isStartExperiment(updData)  -> {
                generateNewChartLogFile()
                isExperimentStarts = true
                STATE_EXPERIMENT.value = StateExperiments.RECORDING
                println("isStartExperiment ${updData.toHexString()}")
                logInfo("Start Experiment! ${counter} ${isExperimentStarts}__${incrementExperiment}")

            }
            PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025 && isStartExperimentOLD(updData)  -> {
                generateNewChartLogFile()
                isExperimentStarts = true
                STATE_EXPERIMENT.value = StateExperiments.RECORDING
                println("isStartExperiment ${updData.toHexString()}")
                logInfo("Start Experiment! ${counter} ${isExperimentStarts}__${incrementExperiment}")
                counter = 0
            }
            isEndOfExperiment(updData) -> {
                println("isEndOfExperiment ${updData.toHexString()}")
                isExperimentStarts = false
                STATE_EXPERIMENT.value = StateExperiments.ENDING_OF_EXPERIMENT

                logInfo("End Experiment! ${counter} ; ${counter2}| all it == 0xFF ${isExperimentStarts}. count of packets of experiment: ${incrementExperiment}, COUNTER ${COUNTER}")
                counter = 0
                incrementExperiment = 0
                COUNTER = 0
            }
            !isExperimentStarts && isEndOfExperiment(updData) -> {
                logError("No way to end not started experiment!")
            }

            //pressure
            isPressureTypeOld(updData) -> { // 24???
                //logGarbage("Pressure: ${updData.toHexString()} size:${updData.size}")
                //println("> ${updData.toHexString()} [size:${updData.size}]")
                if (isExperimentStarts) {
                    incrementExperiment++
                }
                dch = DataChunkG(
                    isExperiment = isExperimentStarts,
                    onesAndTensFloat(byteToInt(updData[0]).toUInt(), byteToInt(updData[1]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[2]).toUInt(), byteToInt(updData[3]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[4]).toUInt(), byteToInt(updData[5]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[6]).toUInt(), byteToInt(updData[7]).toUInt()),

                    onesAndTensFloat(byteToInt(updData[8]).toUInt(), byteToInt(updData[9]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[10]).toUInt(), byteToInt(updData[11]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[12]).toUInt(), byteToInt(updData[13]).toUInt()),
                    onesAndTensFloat(byteToInt(updData[14]).toUInt(), byteToInt(updData[15]).toUInt()),

                    updData.getOrNull(17)?.let { onesAndTensFloat(byteToInt(updData[16]).toUInt(), byteToInt(updData[17]).toUInt()) },
                    updData.getOrNull(19)?.let { onesAndTensFloat(byteToInt(updData[18]).toUInt(), byteToInt(updData[19]).toUInt()) },
                    updData.getOrNull(21)?.let {onesAndTensFloat(byteToInt(updData[20]).toUInt(), byteToInt(updData[21]).toUInt())},
                    updData.getOrNull(23)?.let {onesAndTensFloat(byteToInt(updData[22]).toUInt(), byteToInt(updData[23]).toUInt())}
                )

                //logGarbage(">>> ${dch.toString()}")
                //println("PRES ${dch.toString()}")

                pressuresChunkGauges.emit(dch)
                lastGauge = dch
            }

            //currency
            isCurrencyTypeOld(updData) -> {
                //logGarbage("Currency: ${updData.toHexString()} size:${updData.size}")
                if (isExperimentStarts) {
                    incrementExperiment++
                }
                dchCurr = DataChunkCurrent(
                    onesAndTensFloat(byteToInt(updData[0]).toUInt(), byteToInt(updData[1]).toUInt() - 16u).toInt(), // 24??
                    onesAndTensFloat(byteToInt(updData[2]).toUInt(), byteToInt(updData[3]).toUInt() - 16u).toInt(),
                    onesAndTensFloat(byteToInt(updData[4]).toUInt(), byteToInt(updData[5]).toUInt() - 16u).toInt(),
                    onesAndTensFloat(byteToInt(updData[6]).toUInt(), byteToInt(updData[7]).toUInt() - 16u).toInt(),

                    onesAndTensFloat(byteToInt(updData[8]).toUInt(), byteToInt(updData[9]).toUInt() - 16u).toInt(),
                    onesAndTensFloat(byteToInt(updData[10]).toUInt(), byteToInt(updData[11]).toUInt() - 16u).toInt(),
                    onesAndTensFloat(byteToInt(updData[12]).toUInt(), byteToInt(updData[13]).toUInt() - 16u).toInt(),
                    onesAndTensFloat(byteToInt(updData[14]).toUInt(), byteToInt(updData[15]).toUInt() - 16u).toInt(),

                        ninthCurrentData = updData.getOrNull(17)?.let { onesAndTensFloat(byteToInt(updData[16]).toUInt(), byteToInt(updData[17]).toUInt() - 16u).toInt() },
                       tenthCurrentData =  updData.getOrNull(19)?.let { onesAndTensFloat(byteToInt(updData[18]).toUInt(), byteToInt(updData[19]).toUInt() - 16u).toInt() },
                     eleventhCurrentData = updData.getOrNull(21)?.let { onesAndTensFloat(byteToInt(updData[20]).toUInt(), byteToInt(updData[21]).toUInt() - 16u).toInt() },
                     twelfthCurrentData =  updData.getOrNull(23)?.let { onesAndTensFloat(byteToInt(updData[22]).toUInt(), byteToInt(updData[23]).toUInt() - 16u).toInt() },
                )
                //println("CURR  ${updData.joinToString()}||${dchCurr.toString()}")
                dataChunkCurrents.emit(dchCurr)
                lastGauge?.let { pressuresChunkGauges.emit(it) }

            }
            else -> {
                // if not valid numbers - refresh connection
                if (STATE_EXPERIMENT.value == StateExperiments.NONE) {

                    logError("not valid numbers - refresh connection !!!")
//                    showMeSnackBar("not valid numbers - refresh connection")
                    //delay(1000)
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

fun isEndOfExperiment(updData: ByteArray): Boolean = updData.all { it == 0xFF.toByte() }

fun isStartExperiment(updData: ByteArray): Boolean {
    return updData.size >= 24 &&
            updData[0] == 0xFE.toByte() && updData[1] == 0xFF.toByte() &&
            updData[2] == 0xFE.toByte() && updData[3] == 0xFF.toByte() &&
            updData[4] == 0xFE.toByte() && updData[5] == 0xFF.toByte() &&
            updData[6] == 0xFE.toByte() && updData[7] == 0xFF.toByte() &&
            updData[8] == 0xFE.toByte() && updData[9] == 0xFF.toByte() &&
            updData[10] == 0xFE.toByte() && updData[11] == 0xFF.toByte() &&
            updData[12] == 0xFE.toByte() && updData[13] == 0xFF.toByte() &&
            updData[14] == 0xFE.toByte() && updData[15] == 0xFF.toByte() &&
            updData[16] == 0xFE.toByte() && updData[17] == 0xFF.toByte() &&
            updData[18] == 0xFE.toByte() && updData[19] == 0xFF.toByte() &&
            updData[20] == 0xFE.toByte() && updData[21] == 0xFF.toByte() &&
            updData[22] == 0xFE.toByte() && updData[23] == 0xFF.toByte()
}

fun isStartExperimentOLD(updData: ByteArray): Boolean {
    return updData.size >= 16 &&
            updData[0] == 0xFE.toByte() && updData[1] == 0xFF.toByte() &&
            updData[2] == 0xFE.toByte() && updData[3] == 0xFF.toByte() &&
            updData[4] == 0xFE.toByte() && updData[5] == 0xFF.toByte() &&
            updData[6] == 0xFE.toByte() && updData[7] == 0xFF.toByte() &&
            updData[8] == 0xFE.toByte() && updData[9] == 0xFF.toByte() &&
            updData[10] == 0xFE.toByte() && updData[11] == 0xFF.toByte() &&
            updData[12] == 0xFE.toByte() && updData[13] == 0xFF.toByte() &&
            updData[14] == 0xFE.toByte() && updData[15] == 0xFF.toByte()
}

//fun isPressureType(updData: ByteArray): Boolean  = updData[1] < 24 && updData[3] < 24 && updData[5] < 24 && updData[7] < 24
//
//fun isCurrencyType(updData: ByteArray): Boolean  =
//    updData[1] in 24..47 && updData[3] in 24..47 &&
//            updData[5] in 24..47 && updData[7] in 24..47


/////////////////////////////////////////////////////////////////////////


fun isPressureTypeOld(updData: ByteArray): Boolean  = updData[1] < 16 && updData[3] < 16 && updData[5] < 16 && updData[7] < 16

fun isCurrencyTypeOld(updData: ByteArray): Boolean  =
    updData[1] in 16..31 && updData[3] in 16..31 &&
            updData[5] in 16..31 && updData[7] in 16..31
