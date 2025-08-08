package ru.atrs.mcm.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.DELAY_BEFORE_CHART
import ru.atrs.mcm.utils.Dir1Configs
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir3Scenarios
import ru.atrs.mcm.utils.Dir4MainConfig_Txt
import ru.atrs.mcm.utils.Dir5Operators
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.OPERATOR_ID
import ru.atrs.mcm.utils.LOG_LEVEL
import ru.atrs.mcm.utils.SOUND_ENABLED
import ru.atrs.mcm.utils.arr1Measure
import ru.atrs.mcm.utils.arr2Measure
import ru.atrs.mcm.utils.arr3Measure
import ru.atrs.mcm.utils.arr4Measure
import ru.atrs.mcm.utils.arr5Measure
import ru.atrs.mcm.utils.arr6Measure
import ru.atrs.mcm.utils.arr7Measure
import ru.atrs.mcm.utils.arr8Measure
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.generateTimestampLastUpdate
import ru.atrs.mcm.utils.logAct
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.toBin
import ru.atrs.mcm.storage.models.ParameterCommon
import ru.atrs.mcm.utils.Dir11ForTargetingSaveNewExperiment
import ru.atrs.mcm.utils.Dir_10_ScenarioForChart
import ru.atrs.mcm.utils.NAME_OF_NEW_EXPERIMENT
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import java.io.*


fun createMeasureExperiment() {
    logGarbage("createMeasureExperiment() ${arr1Measure.size}")
    if (arr1Measure.isEmpty())
        return

    val fl = File(Dir11ForTargetingSaveNewExperiment,"${NAME_OF_NEW_EXPERIMENT}_${generateTimestampLastUpdate()}"+"_chart.txt")
    CoroutineScope(Dispatchers.Default).launch {
        //logInfo("createMeasureExperiment ${arr8Measure.joinToString()}")


        fl.createNewFile()
        val bw = fl.bufferedWriter()
        try {
            // read lines in txt by Bufferreader
            bw.write("#standard#${chartFileStandard.value?.name}\n")
            bw.write(
                "#visibility#${pressures[0].isVisible.toBin()}#${pressures[1].isVisible.toBin()}#${pressures[2].isVisible.toBin()}#${pressures[3].isVisible.toBin()}"+
                    "#${pressures[4].isVisible.toBin()}#${pressures[5].isVisible.toBin()}#${pressures[6].isVisible.toBin()}#${pressures[7].isVisible.toBin()}\n"
            )
            bw.write("#\n")
            repeat(arr1Measure.size-1) {
                val newStroke =
                    "${arr1Measure[it].x};${arr1Measure[it].y}|"+
                    "${arr2Measure[it].x};${arr2Measure[it].y}|"+
                    "${arr3Measure[it].x};${arr3Measure[it].y}|"+
                    "${arr4Measure[it].x};${arr4Measure[it].y}|"+
                    "${arr5Measure[it].x};${arr5Measure[it].y}|"+
                    "${arr6Measure[it].x};${arr6Measure[it].y}|"+
                    "${arr7Measure[it].x};${arr7Measure[it].y}|"+
                    "${arr8Measure[it].x};${arr8Measure[it].y}"
                logInfo("newStroke= ${newStroke}")
                bw.write("${newStroke}\n")
            }
            bw.close()

            arr1Measure.clear()
            arr2Measure.clear()
            arr3Measure.clear()
            arr4Measure.clear()
            arr5Measure.clear()
            arr6Measure.clear()
            arr7Measure.clear()
            arr8Measure.clear()

        }catch (e: Exception){
            showMeSnackBar("Error! ${e.message}")
        }
        delay(1200)
        chartFileAfterExperiment.value = fl
        doOpen_First_ChartWindow.value = true
    }
}

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
