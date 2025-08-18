package ru.atrs.mcm.parsing_excel

import androidx.compose.ui.graphics.Color
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import ru.atrs.mcm.parsing_excel.models.PressuresHolder
import ru.atrs.mcm.parsing_excel.models.ScenarioStep
import ru.atrs.mcm.parsing_excel.models.SolenoidHolder
import ru.atrs.mcm.storage.refreshJsonParameters
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir7ReportsStandard
import ru.atrs.mcm.utils.Dir11ForTargetingSaveNewExperiment
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.NAME_OF_NEW_SCENARIO
import ru.atrs.mcm.utils.SOLENOID_MAIN_FREQ
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.limitTime
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.logInfo
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.solenoids
import java.io.File
import java.io.FileInputStream

var wholeSheet = mutableListOf<MutableList<String>>()
suspend fun targetParseScenario(inputScenarioFile: File?) : Boolean {
    logGarbage("targetParseScenario ${inputScenarioFile?.absolutePath}")

    if (inputScenarioFile == null || !inputScenarioFile.exists())
        return false
    try {
        LAST_SCENARIO.value = inputScenarioFile
        logGarbage("LAST_SCENARIO.value ${LAST_SCENARIO.value}")
        refreshJsonParameters()

        var needReWriteStandard = false

        val file = FileInputStream(inputScenarioFile)
        //creating workbook instance that refers to .xls file
        val wb = HSSFWorkbook(file)
        //creating a Sheet object to retrieve the object
        val sheet = wb.getSheetAt(0)
        //evaluating cell type
        val formulaEvaluator: FormulaEvaluator = wb.creationHelper.createFormulaEvaluator()

        //Iterate through each row's one by one
        val rowIterator: Iterator<Row> = sheet.iterator()
        var incr = 0

        var NUMBER_OF_GAUGES = 12

        while (rowIterator.hasNext()) {
            // to bottom V
            val row: Row = rowIterator.next()

            var rowComplete = mutableListOf<String>()
            val cellIterator: Iterator<Cell> = row.cellIterator()

            while (cellIterator.hasNext()) {
                // to right ->
                val cell = cellIterator.next()
                if (cell.toString().isNotBlank() && cell.toString().isNotEmpty() && cell.toString() != "") {
                    rowComplete.add(cell.toString())
                }

//            when (cell.cellType) {
//                CellType.NUMERIC -> print(cell.numericCellValue.toString() + "t")
//                CellType.STRING -> print(cell.stringCellValue + "t")
//            }
            }
            println("${incr}Row: ${rowComplete.joinToString()} ${rowComplete.size}")
            incr++
            if (rowComplete.isNotEmpty()) {
                wholeSheet.add(rowComplete)
            }

        }
        println("Size sheet rows:${wholeSheet[2].size} in rows column:${wholeSheet[2][2].length}")

        // check valid of xls:
        if (wholeSheet.size < 22 || wholeSheet[2].size < 2)
            return false

        // clear all in iteration:
        solenoids.clear()
        pressures.clear()
        scenario.clear()

//    NUMBER_OF_GAUGES = wholeSheet[2].size-1
//    TWELVE_CHANNELS_MODE = (NUMBER_OF_GAUGES>8)

        repeat(NUMBER_OF_GAUGES) {
            pressures.add(
                PressuresHolder(
                    displayName = wholeSheet[2][it + 1],
                    index = wholeSheet[3][it + 1].toFloat().toInt(),
                    minValue = wholeSheet[4][it + 1].toFloat(),
                    maxValue = wholeSheet[5][it + 1].toFloat(),
                    tolerance = wholeSheet[6][it + 1].toFloat().toInt(),
                    unit = wholeSheet[7][it + 1],
                    commentString = wholeSheet[8][it + 1],
                    prefferedColor = wholeSheet[9][it + 1],
                    isVisible = wholeSheet[10].getOrNull(it + 1) == "true"
                )
            )
            logInfo("pressures: ${pressures.joinToString()} ]")

        }

        var maxPWMs = arrayListOf<Int>()

        repeat(NUMBER_OF_GAUGES) {

            SOLENOID_MAIN_FREQ = wholeSheet[13][2].toFloat().toInt()
//        GRADIENT_TIME      = wholeSheet[13][4].toFloat().toInt()

            solenoids.add(
                SolenoidHolder(
                    displayName = wholeSheet[14][it + 1],
                    index = wholeSheet[15][it + 1].toDouble().toInt(),
                    maxPWM = wholeSheet[16][it + 1].toDouble().toInt(),
                    step = wholeSheet[17][it + 1].toDouble().toInt(),
                    ditherAmplitude = wholeSheet[18][it + 1],
                    ditherFrequency = wholeSheet[19][it + 1].toDouble().toInt(),
                    currentMinValue = wholeSheet[20][it + 1].toDouble().toInt(),
                    currentMaxValue = wholeSheet[21][it + 1].toDouble().toInt(),
                    isVisible = wholeSheet[22].getOrNull(it + 1) == "true"
                )
            )
            maxPWMs.add(wholeSheet[16][it + 1].toDouble().toInt())
        }



        limitTime = 0
        // SOLENOIDS SCENARIO
        for (i in (27) until wholeSheet.size) {
            var valueSteps = arrayListOf<Int>()

            repeat(NUMBER_OF_GAUGES) {
                var newPWM = wholeSheet[i][it + 1].toDouble().toInt()

                // check limits maxPWM, for not burn out
                if (newPWM > maxPWMs[it]) {
                    newPWM = maxPWMs[it]
                } else if (newPWM < 0) {
                    newPWM = 0
                }

                valueSteps.add(newPWM)

//            selectorForChannels(it+1,wholeSheet[i][it+1].toDouble().toInt().toByte())
//            when(it) {
//                0 -> pwm1SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                1 -> pwm2SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                2 -> pwm3SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                3 -> pwm4SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                4 -> pwm5SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                5 -> pwm6SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                6 -> pwm7SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//                7 -> pwm8SeekBar.value = wholeSheet[i][it+1].toDouble().toInt()
//
//                else -> pwm1SeekBar
//            }

            }
            println("SOLENOID_MAIN_FREQ: ${SOLENOID_MAIN_FREQ}")
            println("<>>> ${valueSteps.joinToString()}")

            val newTime = wholeSheet[i][0].toDouble().toInt()

            limitTime += newTime

            scenario.add(
                ScenarioStep(
                    time = newTime,
                    channels = valueSteps,
                    analog1 = wholeSheet[i][13].toFloat().toInt(),//.toIntOrNull() ?: 0,
                    analog2 = wholeSheet[i][14].toFloat().toInt(),//.toIntOrNull() ?: 0,
                    gradientTime = wholeSheet[i][15].toFloat().toInt(),//.toIntOrNull() ?: 0,
                    comment = wholeSheet[i].getOrNull(16) ?: "no name step"
                )
            )
            logGarbage("<><>${wholeSheet[i][14]}")
        }

        // Create folder with name of EXCEL, coz name of EXCEL == name of experiment
        File(Dir2Reports, inputScenarioFile.nameWithoutExtension).mkdirs()

        NAME_OF_NEW_SCENARIO = inputScenarioFile.nameWithoutExtension
        Dir11ForTargetingSaveNewExperiment = File(Dir2Reports, NAME_OF_NEW_SCENARIO)

        // Fill file address to Standard chart, Эталон . txt
        val standard = File(wholeSheet[0][0])
        if (!standard.name.endsWith("txt")) {
            needReWriteStandard = true
        } else {
//        chartFileStandard.value = File(Dir11ForTargetingSaveNewExperiment, standard.name)
            chartFileStandard.value = standard
        }

        println("scenario steps: ${scenario.joinToString()}")

        file.close()
        wholeSheet.clear()
//    if (needReWriteStandard) {
//        writeToExcel(0, 0, chartFileStandard.value.name)
//    }
        return true
    } catch (e: Exception) {
        logError("ERROR targetParseScenario: ${e.message}")
        showMeSnackBar("ERROR targetParseScenario: ${e.message}", color = Color.Red)
        return false
    }
}