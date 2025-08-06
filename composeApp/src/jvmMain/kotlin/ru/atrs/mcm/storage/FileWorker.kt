package ru.atrs.mcm.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import java.io.*

fun checkNeededFolders(): Boolean {
        return false
}

fun createNeededFolders() {
    logAct("createNeededFolders")

  var dirs = arrayOf<File>()
  dirs+= Dir1Configs
  dirs+= Dir2Reports
  dirs+= Dir3Scenarios

  dirs.forEach {
          if (!it.exists()) {
                  it.mkdirs()
              logAct("Folder created: ${it.absoluteFile}")
          }
  }
}

fun createDemoConfigFile() : File {
    logAct("createDemoConfigFile")
   val theFileXls = File(Dir3Scenarios,"scenario_demo.xls")
   if (!theFileXls.exists()) {
           theFileXls.createNewFile()
       logAct("Excel file-config created: ${theFileXls.absoluteFile}")
   }
    return theFileXls
}

fun readParameters(file: File) : List<ParameterCommon> {
    logAct("readParameters")
    if (!file.exists()) {
        refreshParameters()
    }

    //val PCListSerializer: KSerializer<List<ParameterCommonJson>> = ListSerializer(ParameterCommonJson.serializer())
//
    //val obj = Json.decodeFromString(PCListSerializer, file.readText(Charsets.UTF_8))

    //Json.decodeFromString<ArrayList<ParameterCommon>>()
    var listParams = mutableListOf<ParameterCommon>()
    try {
        val br = BufferedReader(FileReader(file))
        var line: String?
        var countOfLine = 0
        while (br.readLine().also { line = it } != null) {
            if(line != ""|| line != " "){
                val items = line?.split("=")?.toTypedArray()
                if (items != null ) {
                    when(items[0]) {
                        "comport" -> {
                            listParams.add(ParameterCommon(name = "comport", value = items[1]))
                        }
                        "baudrate" -> {
                            listParams.add(ParameterCommon(name = "baudrate", value = items[1]))
                        }
                        "last_operator_id" -> {
                            listParams.add(ParameterCommon(name = "last_operator_id", value = items[1]))
                        }
                        "sound_enabled" -> {
                            listParams.add(ParameterCommon(name = "sound_enabled", value = items[1]))
                        }
                        "last_scenario" -> {
                            listParams.add(ParameterCommon(name = "last_scenario", value = items[1]))
                        }
                        "delay_before_chart" -> {
                            listParams.add(ParameterCommon(name = "delay_before_chart", value = items[1]))
                        }
                        "save_log" -> { listParams.add(ParameterCommon(name = "save_log", value = items[1])) }
                        "isFullscreenEnabled" -> { listParams.add(ParameterCommon(name = "isFullscreenEnabled", value = items[1])) }
                    }
                }
            }
            countOfLine++
        }
        br.close()
    } catch (e: Exception) {
        logError("error +${e.message}")
    }


    return listParams
}

fun refreshParameters() {
    logAct("createParameters")
    //"comport" -> COM_P
    //"baudrate" -> BAUD
    ////"is_demo" ->
    //"last_operator_id"
//    var newParameters = arrayListOf<ParameterCommonJson>(
//        ParameterCommonJson("comport","COM10"),
//        ParameterCommonJson("baudrate","COM10"),
//        ParameterCommonJson("last_operator_id","Жималбек Аббас Гамлядиндов Оглы"),
//    )

    var newParameters = arrayListOf(
        ParameterCommon("comport","${COM_PORT}"),
        ParameterCommon("baudrate","${BAUD_RATE}"),
        ParameterCommon("last_operator_id","${OPERATOR_ID}"),
        ParameterCommon("sound_enabled","${SOUND_ENABLED}"),
        ParameterCommon("last_scenario","${LAST_SCENARIO}"),
        ParameterCommon("delay_before_chart","${DELAY_BEFORE_CHART}"),
        ParameterCommon("save_log","${LOG_LEVEL}"),
        ParameterCommon("isFullscreenEnabled","${SHOW_FULLSCREEN}"),
    )

    //IF first launch
    val fl = Dir4MainConfig_Txt
    if (!fl.exists()) {
        fl.createNewFile()
//        newParameters = arrayListOf(
//            ParameterCommon("comport","COM10"),
//            ParameterCommon("baudrate","500000"),
//            ParameterCommon("last_operator_id","Гаджилы Жималбек Али оглы"),
//            ParameterCommon("sound_enabled","1"),
//            ParameterCommon("last_scenario","${Dir9Scenario.absolutePath}"),
//        )
    }

    val bw = fl.bufferedWriter()

    try {
        // read lines in txt by Bufferreader
        repeat(newParameters.size) {
            bw.write("${newParameters[it].name}=${newParameters[it].value}\n")
        }
        bw.close()
    }catch (e: Exception){
        showMeSnackBar("Error! ${e.message}")
    }

//    val PCListSerializer: KSerializer<List<ParameterCommonJson>> = ListSerializer(ParameterCommonJson.serializer())
//
//    // json:
//    val json = Json.encodeToString(PCListSerializer, newParameters)
//
//    var newFileJson = Dir4MainConfig_Json
//    newFileJson.writeText(json)
//
//    if (!newFileJson.exists()) {
//        newFileJson.createNewFile()
//    }

}

fun loadOperators() : MutableList<String> {
    return Dir5Operators.readLines().toMutableList().asReversed()
}

fun createMeasureExperiment() {
    logGarbage("createMeasureExperiment() ${arr1Measure.size}")
    if (arr1Measure.isEmpty())
        return

    val fl = File(Dir2Reports, generateTimestampLastUpdate() +"_${OPERATOR_ID}"+"_chart.txt")
    CoroutineScope(Dispatchers.Default).launch {
        //logInfo("createMeasureExperiment ${arr8Measure.joinToString()}")


        fl.createNewFile()
        val bw = fl.bufferedWriter()
        try {
            // read lines in txt by Bufferreader
            bw.write("#standard#${chartFileStandard.value.name}\n")
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
    }
    chartFileAfterExperiment.value = fl
    doOpen_First_ChartWindow.value = true
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
