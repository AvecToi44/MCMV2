package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.*
import ru.atrs.mcm.enums.StateExperiments
import kotlinx.coroutines.*
import ru.atrs.mcm.enums.ExplorerMode
import ru.atrs.mcm.serial_port.RouterCommunication.comparatorToSolenoid
import ru.atrs.mcm.serial_port.RouterCommunication.sendScenarioToController
import ru.atrs.mcm.serial_port.RouterCommunication.startReceiveFullData
import ru.atrs.mcm.serial_port.RouterCommunication.stopSerialCommunication
import ru.atrs.mcm.storage.NewPointerLine
import ru.atrs.mcm.storage.addNewLineForChart
import ru.atrs.mcm.storage.models.UIGaugesData
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.DataChunkCurrent
import ru.atrs.mcm.utils.DataChunkG
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.NAME_OF_NEW_CHART_LOG_FILE
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import ru.atrs.mcm.utils.byteToInt
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.dataChunkCurrents
import ru.atrs.mcm.utils.dataChunkGauges
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
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.toHexString


private val DEBUG_PARSING = false



class PacketListener : SerialPortPacketListener {
    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun getPacketSize(): Int {
        return 16 // 24?
    }

    override fun serialEvent(event: SerialPortEvent) {

        CoroutineScope(Dispatchers.IO).launch {
            val newData = event.receivedData
            //println("${newData.toHexString()}")
            dataChunkRAW.emit(newData)
        }
    }
}

private var arrCurrRaw  = arrayListOf<ByteArray>()
private var arrPressRaw = arrayListOf<ByteArray>()

private var arrCurr =  arrayListOf<ArrayList<Int>>()
private var arrPress = arrayListOf<ArrayList<Float>>()
private var start_time = 0L
private var incr = 0
var incrementExperiment = 0
private var lastGauge : DataChunkG? = null

private var COUNTER = 0L
suspend fun bytesReceiverMachine() {
    println("bytesReceiverMachine")
    dataChunkRAW.collect { updData ->
        var dch: DataChunkG? = null
        var dchCurr: DataChunkCurrent? = null

//        if (incrementTime >= 100_000 && !isExperimentStarts) {
//            incrementTime = 0
//        }

        when {
            isStartExperiment(updData)  -> {
                isExperimentStarts = true
                STATE_EXPERIMENT.value = StateExperiments.START

                logInfo("Start Experiment! ${isExperimentStarts}__${incrementExperiment}")

            }
            isEndOfExperiment(updData) -> {
                isExperimentStarts = false
                STATE_EXPERIMENT.value = StateExperiments.ENDING_OF_EXPERIMENT

                logInfo("End Experiment! all it == 0xFF ${isExperimentStarts}. count of packets of experiment: ${incrementExperiment}, COUNTER ${COUNTER}")
            }

            //pressure
            isPressureType(updData) -> { // 24???
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
                    onesAndTensFloat(byteToInt(updData[14]).toUInt(), byteToInt(updData[15]).toUInt())

                    //onesAndTensFloat(byteToInt(updData[16]).toUInt(), byteToInt(updData[17]).toUInt()),
                    //onesAndTensFloat(byteToInt(updData[18]).toUInt(), byteToInt(updData[19]).toUInt()),
                    //onesAndTensFloat(byteToInt(updData[20]).toUInt(), byteToInt(updData[21]).toUInt())
                    //onesAndTensFloat(byteToInt(updData[22]).toUInt(), byteToInt(updData[23]).toUInt())
                )

                //logGarbage(">>> ${dch.toString()}")
                //println("PRES ${dch.toString()}")

                dataChunkGauges.emit(dch)
                lastGauge = dch

                if (DEBUG_PARSING) {
                    arrPressRaw.add(updData)

                    arrPress.add(arrayListOf(
                        dch.firstGaugeData,
                        dch.secondGaugeData,
                        dch.thirdGaugeData,
                        dch.fourthGaugeData,
                        dch.fifthGaugeData,
                        dch.sixthGaugeData,
                        dch.seventhGaugeData,
                        dch.eighthGaugeData
                    ))
                }
            }

            //currency
            isCurrencyType(updData) -> {
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
                    onesAndTensFloat(byteToInt(updData[14]).toUInt(), byteToInt(updData[15]).toUInt() - 16u).toInt()
                )
                //println("CURR  ${updData.joinToString()}||${dchCurr.toString()}")
                dataChunkCurrents.emit(dchCurr)
                lastGauge?.let { dataChunkGauges.emit(it) }

                if (DEBUG_PARSING) {
                    arrCurrRaw.add(updData)

                    arrCurr.add(arrayListOf(
                        dchCurr.firstCurrentData,
                        dchCurr.secondCurrentData,
                        dchCurr.thirdCurrentData,
                        dchCurr.fourthCurrentData,
                        dchCurr.fifthCurrentData,
                        dchCurr.sixthCurrentData,
                        dchCurr.seventhCurrentData,
                        dchCurr.eighthCurrentData,
                    ))
                }
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
        if (DEBUG_PARSING) {
            // print clear results:
            if (arrPressRaw.size > 9) {
                stopSerialCommunication()
                println("_______current:")
                repeat(arrCurrRaw.size) {
                    println(arrCurrRaw[it].toHexString())
                }
                println("_______pressure:")
                repeat(arrPressRaw.size) {
                    println(arrPressRaw[it].toHexString())
                }
                ///
                println("********************************")

                repeat(arrCurr.size) {
                    println(arrCurr[it])
                }
                repeat(arrPress.size) {
                    println(arrPress[it])
                }
            }
            arrCurr.clear()
            arrPress.clear()
            arrCurrRaw.clear()
            arrPressRaw.clear()
        }
    }
}

var limiterForUI = 0
fun payloadWriterMachine() {
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
        startReceiveFullData()
        comparatorToSolenoid(indexOfScenario.value)
        sendScenarioToController()
//        scenario.clear()
        dataChunkGauges.collect {

            //delay(DELAY_FOR_GET_DATA)
            //logGarbage(">>>> ${it.toString()}")

            //logGarbage("dataChunkGauges> ${it.toString()} ||sizes:${arr1Measure.size} ${dataChunkGauges.replayCache.size} ${solenoids.size} ${pressures.size} ${scenario.size}")

            //println("|<<<<<<<<<<<<<<<<<<<${it.isExperiment} [${it.firstGaugeData}]")
            // in_max 65535 instead of 4095
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
                            chartFileAfterExperiment.value = NAME_OF_NEW_CHART_LOG_FILE!!
                            doOpen_First_ChartWindow.value = true
                            STATE_EXPERIMENT.value = StateExperiments.NONE
                            NAME_OF_NEW_CHART_LOG_FILE = null
                            incrementTime = 0
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
    return updData.size >= 14 &&
            updData[0] == 0xFE.toByte() && updData[1] == 0xFF.toByte() &&
            updData[2] == 0xFE.toByte() && updData[3] == 0xFF.toByte() &&
            updData[4] == 0xFE.toByte() && updData[5] == 0xFF.toByte() &&
            updData[6] == 0xFE.toByte() && updData[7] == 0xFF.toByte() &&
            updData[8] == 0xFE.toByte() && updData[9] == 0xFF.toByte() &&
            updData[10] == 0xFE.toByte() && updData[11] == 0xFF.toByte() &&
            updData[12] == 0xFE.toByte() && updData[13] == 0xFF.toByte()
}

fun isPressureType(updData: ByteArray): Boolean  = updData[1] < 16 && updData[3] < 16 && updData[5] < 16 && updData[7] < 16

fun isCurrencyType(updData: ByteArray): Boolean  =
    updData[1] in 16..31 && updData[3] in 16..31 &&
            updData[5] in 16..31 && updData[7] in 16..31