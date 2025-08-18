package ru.atrs.mcm.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.CHART_FILE_NAME_ENDING
import ru.atrs.mcm.utils.ChartFileNameEnding
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.generateTimestampLastUpdate
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.toBin
import ru.atrs.mcm.utils.Dir11ForTargetingSaveNewExperiment
import ru.atrs.mcm.utils.NAME_OF_NEW_SCENARIO
import ru.atrs.mcm.utils.COMMENT_OF_EXPERIMENT
import ru.atrs.mcm.utils.NAME_OF_NEW_CHART_LOG_FILE
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import ru.atrs.mcm.utils.to5Decimals
import java.io.*


//

fun generateNewChartLogName() {
    println("Generate new chart file")
    val endingOfName = when(CHART_FILE_NAME_ENDING) {
        ChartFileNameEnding.COMMENT_AND_TIMESTAMP -> "${generateTimestampLastUpdate()}_${COMMENT_OF_EXPERIMENT}"
        ChartFileNameEnding.TIMESTAMP -> "${generateTimestampLastUpdate()}"
        ChartFileNameEnding.COMMENT -> "${COMMENT_OF_EXPERIMENT}"
    }
    NAME_OF_NEW_CHART_LOG_FILE = File(Dir11ForTargetingSaveNewExperiment,"${NAME_OF_NEW_SCENARIO}_${endingOfName}"+".txt")
    NAME_OF_NEW_CHART_LOG_FILE?.createNewFile()
}

data class NewPointerLine(
    val incrementTime: Long,
    val ch1: Float,
    val ch2: Float,
    val ch3: Float,
    val ch4: Float,
    val ch5: Float,
    val ch6: Float,
    val ch7: Float,
    val ch8: Float,
    val ch9: Float,
    val ch10: Float,
    val ch11: Float,
    val ch12: Float,
)

suspend fun addNewLineForChart(newLine: NewPointerLine, isRecordingExperiment: Boolean) {
    if (NAME_OF_NEW_CHART_LOG_FILE == null || !NAME_OF_NEW_CHART_LOG_FILE!!.exists()) {
        generateNewChartLogName()
    }

    val file = NAME_OF_NEW_CHART_LOG_FILE!!
    val fileIsEmpty = file.length() <= 0

    try {
        BufferedWriter(FileWriter(file, true)).use { bw ->
            println("RECORD! isRecordingExperiment $isRecordingExperiment ${newLine.toString()}")

            if (fileIsEmpty) {
                bw.append("#standard#${chartFileStandard.value?.name}\n")
                val twelveChannels = if (TWELVE_CHANNELS_MODE) {
                    "#${pressures[8].isVisible.toBin()}#${pressures[9].isVisible.toBin()}#${pressures[10].isVisible.toBin()}#${pressures[11].isVisible.toBin()}"
                } else ""
                bw.append(
                    "#visibility#${pressures[0].isVisible.toBin()}#${pressures[1].isVisible.toBin()}#${pressures[2].isVisible.toBin()}#${pressures[3].isVisible.toBin()}" +
                            "#${pressures[4].isVisible.toBin()}#${pressures[5].isVisible.toBin()}#${pressures[6].isVisible.toBin()}#${pressures[7].isVisible.toBin()}$twelveChannels\n"
                )
                bw.append("#\n")
            }

            val time = newLine.incrementTime
            var newStroke = "$time;${newLine.ch1.to5Decimals()}|$time;${newLine.ch2.to5Decimals()}|$time;${newLine.ch3.to5Decimals()}|$time;${newLine.ch4.to5Decimals()}|" +
                    "$time;${newLine.ch5.to5Decimals()}|$time;${newLine.ch6.to5Decimals()}|$time;${newLine.ch7.to5Decimals()}|$time;${newLine.ch8.to5Decimals()}|"

            if (TWELVE_CHANNELS_MODE) {
                newStroke += "$time;${newLine.ch9.to5Decimals()}|$time;${newLine.ch10.to5Decimals()}|$time;${newLine.ch11.to5Decimals()}|$time;${newLine.ch12.to5Decimals()}|"
            }

            bw.append("$newStroke\n")
        }
    } catch (e: Exception) {
        logError("addNewLineForChart error: ${e.message}")
        showMeSnackBar("Error! ${e.message}")
        STATE_EXPERIMENT.value = StateExperiments.NONE
    }
}



//fun createMeasureExperiment() {
//    logGarbage("createMeasureExperiment() ${arr1Measure.size}")
//    if (arr1Measure.isEmpty())
//        return
//    val endingOfName = when(CHART_FILE_NAME_ENDING) {
//        ChartFileNameEnding.COMMENT_AND_TIMESTAMP -> "${generateTimestampLastUpdate()}_${COMMENT_OF_EXPERIMENT}"
//        ChartFileNameEnding.TIMESTAMP -> "${generateTimestampLastUpdate()}"
//        ChartFileNameEnding.COMMENT -> "${COMMENT_OF_EXPERIMENT}"
//    }
//    val fl = File(Dir11ForTargetingSaveNewExperiment,"${NAME_OF_NEW_SCENARIO}_${endingOfName}"+".txt")
//    CoroutineScope(Dispatchers.Default).launch {
//        //logInfo("createMeasureExperiment ${arr8Measure.joinToString()}")
//
//
//        fl.createNewFile()
//        val bw = fl.bufferedWriter()
//        try {
//            // write lines in txt by Bufferreader
//            // standard file
//            bw.write("#standard#${chartFileStandard.value?.name}\n")
//            // visibility
//            val twelveChannels = if (TWELVE_CHANNELS_MODE) {
//                "#${pressures[8].isVisible.toBin()}#${pressures[9].isVisible.toBin()}#${pressures[10].isVisible.toBin()}#${pressures[11].isVisible.toBin()}"
//            } else ""
//
//            bw.write(
//                "#visibility#${pressures[0].isVisible.toBin()}#${pressures[1].isVisible.toBin()}#${pressures[2].isVisible.toBin()}#${pressures[3].isVisible.toBin()}"+
//                    "#${pressures[4].isVisible.toBin()}#${pressures[5].isVisible.toBin()}#${pressures[6].isVisible.toBin()}#${pressures[7].isVisible.toBin()}${twelveChannels}\n"
//            )
//            bw.write("#\n")
//            repeat(arr1Measure.size-1) {
//                val newStroke =
//                    "${arr1Measure[it].x};${arr1Measure[it].y}|"+
//                    "${arr2Measure[it].x};${arr2Measure[it].y}|"+
//                    "${arr3Measure[it].x};${arr3Measure[it].y}|"+
//                    "${arr4Measure[it].x};${arr4Measure[it].y}|"+
//                    "${arr5Measure[it].x};${arr5Measure[it].y}|"+
//                    "${arr6Measure[it].x};${arr6Measure[it].y}|"+
//                    "${arr7Measure[it].x};${arr7Measure[it].y}|"+
//                    "${arr8Measure[it].x};${arr8Measure[it].y}|"
//
//
//                var newStrokeFor12 = ""
//
//                if (TWELVE_CHANNELS_MODE) {
//                    newStrokeFor12 =
//                        "${arr9Measure[it].x};${arr9Measure[it].y}|" +
//                        "${arr10Measure[it].x};${arr10Measure[it].y}|" +
//                        "${arr11Measure[it].x};${arr11Measure[it].y}|" +
//                        "${arr12Measure[it].x};${arr12Measure[it].y}|"
//                }
//
//                //logGarbage("newStroke= ${newStroke}")
//                bw.write("${newStroke}${newStrokeFor12}\n")
//            }
//            bw.close()
//
//            arr1Measure.clear()
//            arr2Measure.clear()
//            arr3Measure.clear()
//            arr4Measure.clear()
//            arr5Measure.clear()
//            arr6Measure.clear()
//            arr7Measure.clear()
//            arr8Measure.clear()
//            arr9Measure.clear()
//            arr10Measure.clear()
//            arr11Measure.clear()
//            arr12Measure.clear()
//
//        }catch (e: Exception){
//            showMeSnackBar("Error! ${e.message}")
//            STATE_EXPERIMENT.value = StateExperiments.NONE
//        }
//        delay(1200)
//        chartFileAfterExperiment.value = fl
//        doOpen_First_ChartWindow.value = true
//        STATE_EXPERIMENT.value = StateExperiments.NONE
//    }
//}
//suspend fun addNewLineForChart(newLine: NewPointerLine, isRecordingExperiment: Boolean) {
//    if (NAME_OF_NEW_CHART_LOG_FILE == null || NAME_OF_NEW_CHART_LOG_FILE?.exists() == false) {
//        generateNewChartLogName()
//    }
//
//    val bw = NAME_OF_NEW_CHART_LOG_FILE?.bufferedWriter()
//    try {
//        println("RECORD! isRecordingExperiment ${isRecordingExperiment} ${newLine.toString()}")
//        val fileIsEmpty = (NAME_OF_NEW_CHART_LOG_FILE?.length() ?: 0L) <= 0
//
//        if (fileIsEmpty) {
//            // standard file
//            bw?.append("#standard#${chartFileStandard.value?.name}\n")
//            // visibility
//            val twelveChannels = if (TWELVE_CHANNELS_MODE) {
//                "#${pressures[8].isVisible.toBin()}#${pressures[9].isVisible.toBin()}#${pressures[10].isVisible.toBin()}#${pressures[11].isVisible.toBin()}"
//            } else ""
//
//            bw?.append(
//                "#visibility#${pressures[0].isVisible.toBin()}#${pressures[1].isVisible.toBin()}#${pressures[2].isVisible.toBin()}#${pressures[3].isVisible.toBin()}"+
//                        "#${pressures[4].isVisible.toBin()}#${pressures[5].isVisible.toBin()}#${pressures[6].isVisible.toBin()}#${pressures[7].isVisible.toBin()}${twelveChannels}\n"
//            )
//            bw?.append("#\n")
//        }
//
//        val newStroke =
//            "${newLine.incrementTime};${newLine.ch1}|"+
//            "${newLine.incrementTime};${newLine.ch2}|"+
//            "${newLine.incrementTime};${newLine.ch3}|"+
//            "${newLine.incrementTime};${newLine.ch4}|"+
//            "${newLine.incrementTime};${newLine.ch5}|"+
//            "${newLine.incrementTime};${newLine.ch6}|"+
//            "${newLine.incrementTime};${newLine.ch7}|"+
//            "${newLine.incrementTime};${newLine.ch8}|"
//
//
//        var newStrokeFor12 = ""
//
//        if (TWELVE_CHANNELS_MODE) {
//            newStrokeFor12 =
//                "${newLine.incrementTime};${newLine.ch9}|" +
//                "${newLine.incrementTime};${newLine.ch10}|" +
//                "${newLine.incrementTime};${newLine.ch11}|" +
//                "${newLine.incrementTime};${newLine.ch12}|"
//        }
//
//        //logGarbage("newStroke= ${newStroke}")
//        bw?.append("${newStroke}${newStrokeFor12}\n")
//        bw?.close()
//
//    }catch (e: Exception){
//        showMeSnackBar("Error! ${e.message}")
//        STATE_EXPERIMENT.value = StateExperiments.NONE
//    }
//
//}



fun readMeasuredExperiment(file: File) {
    try {
        val br = BufferedReader(FileReader(file))
        var line: String?
        var countOfLine = 0
        while (br.readLine().also { line = it } != null) {
            if(line != ""|| line != " "){
                val items = line?.split(";","|")?.toTypedArray()
                if (items != null ) {

                }
            }
            countOfLine++
        }
        br.close()
    } catch (e: Exception) {
        logError("error +${e.message}")
    }
}


fun writeToFile(msg: String, fl: File) {

    //IF first launch
    //val fl = Dir4MainConfig_Log
    if (!fl.exists()) {
        fl.createNewFile()
//        )
    }
    val fileOutputStream = FileOutputStream(fl,true)
    val outputStreamWriter = OutputStreamWriter(fileOutputStream)
    try {

        outputStreamWriter.append(msg+"\n")


    }catch (e:Exception) {
        logError("ERROR ${e.message}")
    } finally {
        outputStreamWriter.close()
        fileOutputStream.close()

    }

//    val bw = fl.bufferedWriter()
//
//    try {
//        bw.append(msg)
//        bw.close()
//    }catch (e: Exception){
//        showMeSnackBar("Error! ${e.message}")
//    }

}
