package ru.atrs.mcm.parsing_excel

import androidx.compose.ui.graphics.Color
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import ru.atrs.mcm.parsing_excel.models.PressuresHolder
import ru.atrs.mcm.parsing_excel.models.ScenarioStep
import ru.atrs.mcm.parsing_excel.models.SolenoidHolder
import ru.atrs.mcm.storage.refreshJsonParameters
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir11ForTargetingSaveNewExperiment
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.NAME_OF_NEW_SCENARIO
import ru.atrs.mcm.utils.SOLENOID_FREQ_PARAMS_0x68
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

private const val NUMBER_OF_GAUGES = 12

private const val PRESSURE_DISPLAY_ROW = 2
private const val PRESSURE_INDEX_ROW = 3
private const val PRESSURE_MIN_ROW = 4
private const val PRESSURE_MAX_ROW = 5
private const val PRESSURE_TOLERANCE_ROW = 6
private const val PRESSURE_UNIT_ROW = 7
private const val PRESSURE_COMMENT_ROW = 8
private const val PRESSURE_COLOR_ROW = 9
private const val PRESSURE_VISIBLE_ROW = 10

private const val MAIN_FREQ_ROW = 13
private const val MAIN_FREQ_COL = 2 // C
private const val MAIN_FREQ_EXTRA_START_COL = 3 // D

private const val SOLENOID_DISPLAY_ROW = 14
private const val SOLENOID_INDEX_ROW = 15
private const val SOLENOID_MAX_PWM_ROW = 16
private const val SOLENOID_STEP_ROW = 17
private const val SOLENOID_DITHER_AMP_ROW = 18
private const val SOLENOID_DITHER_FREQ_ROW = 19
private const val SOLENOID_CURRENT_MIN_ROW = 20
private const val SOLENOID_CURRENT_MAX_ROW = 21
private const val SOLENOID_VISIBLE_ROW = 22

private const val SCENARIO_START_ROW = 27
private const val SCENARIO_SECTION_ROW = 25
private const val SCENARIO_HEADER_ROW = 26
private const val SCENARIO_TIME_COL = 0
private const val SCENARIO_CHANNELS_START_COL = 1
private const val SCENARIO_ANALOG1_COL = 13
private const val SCENARIO_ANALOG2_COL = 14
private const val SCENARIO_GRADIENT_COL = 15
private const val SCENARIO_COMMENT_COL = 16

private const val STRICT_TEMPLATE_VALIDATION = true

private fun cellAddress(rowIndex: Int, colIndex: Int): String {
    var col = colIndex
    var name = ""
    do {
        val rem = col % 26
        name = ('A' + rem) + name
        col = col / 26 - 1
    } while (col >= 0)
    return "$name${rowIndex + 1}"
}

private fun normalizeTemplateToken(value: String?): String {
    return value
        ?.lowercase()
        ?.replace(" ", "")
        ?.replace("_", "")
        ?.replace(":", "")
        ?.replace("(", "")
        ?.replace(")", "")
        ?.replace("-", "")
        ?: ""
}

private fun getCell(row: Row?, colIndex: Int): Cell? {
    return row?.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
}

private fun cellAsString(row: Row?, colIndex: Int, evaluator: FormulaEvaluator): String? {
    val cell = getCell(row, colIndex) ?: return null
    return try {
        when (cell.cellType) {
            CellType.NUMERIC -> {
                val number = cell.numericCellValue
                if (number % 1.0 == 0.0) number.toInt().toString() else number.toString()
            }
            CellType.STRING -> cell.stringCellValue.trim().takeIf { it.isNotEmpty() }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                val eval = evaluator.evaluate(cell)
                when (eval.cellType) {
                    CellType.NUMERIC -> {
                        val number = eval.numberValue
                        if (number % 1.0 == 0.0) number.toInt().toString() else number.toString()
                    }
                    CellType.STRING -> eval.stringValue.trim().takeIf { it.isNotEmpty() }
                    CellType.BOOLEAN -> eval.booleanValue.toString()
                    else -> null
                }
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun cellAsDouble(row: Row?, colIndex: Int, evaluator: FormulaEvaluator): Double? {
    val cell = getCell(row, colIndex) ?: return null
    return try {
        when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> cell.stringCellValue
                .replace(',', '.')
                .trim()
                .toDoubleOrNull()
            CellType.BOOLEAN -> if (cell.booleanCellValue) 1.0 else 0.0
            CellType.FORMULA -> {
                val eval = evaluator.evaluate(cell)
                when (eval.cellType) {
                    CellType.NUMERIC -> eval.numberValue
                    CellType.STRING -> eval.stringValue
                        .replace(',', '.')
                        .trim()
                        .toDoubleOrNull()
                    CellType.BOOLEAN -> if (eval.booleanValue) 1.0 else 0.0
                    else -> null
                }
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun requiredInt(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, field: String): Int {
    val row = sheet.getRow(rowIndex)
    val parsed = cellAsDouble(row, colIndex, evaluator)?.toInt()
    if (parsed != null) {
        return parsed
    }
    val raw = cellAsString(row, colIndex, evaluator) ?: "<blank>"
    throw IllegalArgumentException("Parse error: field '$field' expects Int at ${cellAddress(rowIndex, colIndex)}. Raw='$raw'")
}

private fun optionalInt(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, defaultValue: Int): Int {
    val row = sheet.getRow(rowIndex)
    return cellAsDouble(row, colIndex, evaluator)?.toInt() ?: defaultValue
}

private fun requiredFloat(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, field: String): Float {
    val row = sheet.getRow(rowIndex)
    val parsed = cellAsDouble(row, colIndex, evaluator)?.toFloat()
    if (parsed != null) {
        return parsed
    }
    val raw = cellAsString(row, colIndex, evaluator) ?: "<blank>"
    throw IllegalArgumentException("Parse error: field '$field' expects Float at ${cellAddress(rowIndex, colIndex)}. Raw='$raw'")
}

private fun requiredString(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, field: String): String {
    val row = sheet.getRow(rowIndex)
    val parsed = cellAsString(row, colIndex, evaluator)
    if (!parsed.isNullOrBlank()) {
        return parsed
    }
    throw IllegalArgumentException("Parse error: field '$field' expects String at ${cellAddress(rowIndex, colIndex)}. Raw='<blank>'")
}

private fun optionalString(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, defaultValue: String): String {
    val row = sheet.getRow(rowIndex)
    return cellAsString(row, colIndex, evaluator) ?: defaultValue
}

private fun optionalBoolean(sheet: Sheet, rowIndex: Int, colIndex: Int, evaluator: FormulaEvaluator, defaultValue: Boolean): Boolean {
    val text = optionalString(sheet, rowIndex, colIndex, evaluator, if (defaultValue) "true" else "false")
    return when (text.trim().lowercase()) {
        "true", "1", "yes", "y" -> true
        "false", "0", "no", "n" -> false
        else -> defaultValue
    }
}

private fun validateTemplateStrict(sheet: Sheet, evaluator: FormulaEvaluator): List<String> {
    val errors = mutableListOf<String>()

    fun checkLabel(row: Int, col: Int, field: String, expectedTokens: Set<String>) {
        val raw = cellAsString(sheet.getRow(row), col, evaluator) ?: "<blank>"
        val normalized = normalizeTemplateToken(raw)
        if (expectedTokens.none { token -> normalized.contains(token) }) {
            errors += "Template mismatch: $field at ${cellAddress(row, col)}. Expected one of ${expectedTokens.joinToString()} but got '$raw'"
        }
    }

    fun checkNumeric(row: Int, col: Int, field: String, min: Int, max: Int) {
        val value = cellAsDouble(sheet.getRow(row), col, evaluator)?.toInt()
        if (value == null) {
            val raw = cellAsString(sheet.getRow(row), col, evaluator) ?: "<blank>"
            errors += "Template mismatch: $field at ${cellAddress(row, col)} must be numeric. Raw='$raw'"
            return
        }
        if (value !in min..max) {
            errors += "Template mismatch: $field at ${cellAddress(row, col)} out of range [$min..$max]. Value=$value"
        }
    }

    checkLabel(1, 0, "Pressures section", setOf("pressures"))
    checkLabel(PRESSURE_DISPLAY_ROW, 0, "Pressure display row", setOf("displayname"))
    checkLabel(PRESSURE_INDEX_ROW, 0, "Pressure index row", setOf("index"))
    checkLabel(PRESSURE_MIN_ROW, 0, "Pressure min row", setOf("minvalue"))
    checkLabel(PRESSURE_MAX_ROW, 0, "Pressure max row", setOf("maxvalue"))
    checkLabel(PRESSURE_TOLERANCE_ROW, 0, "Pressure tolerance row", setOf("tolerance"))
    checkLabel(PRESSURE_UNIT_ROW, 0, "Pressure unit row", setOf("unit"))
    checkLabel(PRESSURE_COMMENT_ROW, 0, "Pressure comment row", setOf("commentstring"))
    checkLabel(PRESSURE_COLOR_ROW, 0, "Pressure color row", setOf("preferredcolor"))
    checkLabel(PRESSURE_VISIBLE_ROW, 0, "Pressure visible row", setOf("isvisible"))

    checkLabel(MAIN_FREQ_ROW, 1, "Main frequency label", setOf("mainfrequency", "0x68"))
    checkNumeric(MAIN_FREQ_ROW, MAIN_FREQ_COL, "Main frequency (0x68)", 0, 65535)
    repeat(10) { idx ->
        checkNumeric(
            MAIN_FREQ_ROW,
            MAIN_FREQ_EXTRA_START_COL + idx,
            "0x68 byte ${idx + 1}",
            0,
            255
        )
    }

    checkLabel(13, 0, "Solenoids section", setOf("solenoids"))
    checkLabel(SOLENOID_DISPLAY_ROW, 0, "Solenoid display row", setOf("displayname"))
    checkLabel(SOLENOID_INDEX_ROW, 0, "Solenoid index row", setOf("index"))
    checkLabel(SOLENOID_MAX_PWM_ROW, 0, "Solenoid maxPWM row", setOf("maxpwm"))
    checkLabel(SOLENOID_STEP_ROW, 0, "Solenoid step row", setOf("valueofdivision", "step"))
    checkLabel(SOLENOID_DITHER_AMP_ROW, 0, "Solenoid dither amplitude row", setOf("ditheramplitude"))
    checkLabel(SOLENOID_DITHER_FREQ_ROW, 0, "Solenoid dither frequency row", setOf("ditherfrequency"))
    checkLabel(SOLENOID_CURRENT_MIN_ROW, 0, "Solenoid current min row", setOf("currentminvalue"))
    checkLabel(SOLENOID_CURRENT_MAX_ROW, 0, "Solenoid current max row", setOf("currentmaxvalue"))
    checkLabel(SOLENOID_VISIBLE_ROW, 0, "Solenoid visible row", setOf("isvisible"))

    checkLabel(SCENARIO_SECTION_ROW, 0, "Scenario section", setOf("scenario"))
    checkLabel(SCENARIO_HEADER_ROW, 0, "Scenario time header", setOf("steptime", "sleeptime"))

    return errors
}

suspend fun targetParseScenario(inputScenarioFile: File?) : Boolean {
    logGarbage("targetParseScenario ${inputScenarioFile?.absolutePath}")

    if (inputScenarioFile == null || !inputScenarioFile.exists()) {
        return false
    }

    try {
        LAST_SCENARIO.value = inputScenarioFile
        refreshJsonParameters()

        val file = FileInputStream(inputScenarioFile)
        val wb = HSSFWorkbook(file)
        val sheet = wb.getSheetAt(0)
        val evaluator = wb.creationHelper.createFormulaEvaluator()

        if (sheet.lastRowNum < SOLENOID_VISIBLE_ROW) {
            throw IllegalArgumentException("Scenario sheet is too short: lastRow=${sheet.lastRowNum + 1}")
        }

        if (STRICT_TEMPLATE_VALIDATION) {
            val templateErrors = validateTemplateStrict(sheet, evaluator)
            if (templateErrors.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Template validation failed (${templateErrors.size} issues):\n${templateErrors.joinToString("\n")}" 
                )
            }
        }

        solenoids.clear()
        pressures.clear()
        scenario.clear()

        SOLENOID_MAIN_FREQ = if (STRICT_TEMPLATE_VALIDATION) {
            requiredInt(sheet, MAIN_FREQ_ROW, MAIN_FREQ_COL, evaluator, "Main frequency (0x68)")
        } else {
            optionalInt(sheet, MAIN_FREQ_ROW, MAIN_FREQ_COL, evaluator, 0)
        }
        repeat(10) { index ->
            SOLENOID_FREQ_PARAMS_0x68[index] = (
                if (STRICT_TEMPLATE_VALIDATION) {
                    requiredInt(
                        sheet,
                        MAIN_FREQ_ROW,
                        MAIN_FREQ_EXTRA_START_COL + index,
                        evaluator,
                        "0x68 byte ${index + 1}"
                    )
                } else {
                    optionalInt(
                        sheet,
                        MAIN_FREQ_ROW,
                        MAIN_FREQ_EXTRA_START_COL + index,
                        evaluator,
                        0
                    )
                }
            ).coerceIn(0, 255)
        }

        repeat(NUMBER_OF_GAUGES) { idx ->
            val col = SCENARIO_CHANNELS_START_COL + idx
            pressures.add(
                PressuresHolder(
                    displayName = requiredString(sheet, PRESSURE_DISPLAY_ROW, col, evaluator, "Pressure displayName[$idx]"),
                    index = requiredInt(sheet, PRESSURE_INDEX_ROW, col, evaluator, "Pressure index[$idx]"),
                    minValue = requiredFloat(sheet, PRESSURE_MIN_ROW, col, evaluator, "Pressure min[$idx]"),
                    maxValue = requiredFloat(sheet, PRESSURE_MAX_ROW, col, evaluator, "Pressure max[$idx]"),
                    tolerance = requiredInt(sheet, PRESSURE_TOLERANCE_ROW, col, evaluator, "Pressure tolerance[$idx]"),
                    unit = optionalString(sheet, PRESSURE_UNIT_ROW, col, evaluator, ""),
                    commentString = optionalString(sheet, PRESSURE_COMMENT_ROW, col, evaluator, ""),
                    prefferedColor = optionalString(sheet, PRESSURE_COLOR_ROW, col, evaluator, "#FFFFFFFF"),
                    isVisible = optionalBoolean(sheet, PRESSURE_VISIBLE_ROW, col, evaluator, true)
                )
            )
        }

        val maxPWMs = arrayListOf<Int>()
        repeat(NUMBER_OF_GAUGES) { idx ->
            val col = SCENARIO_CHANNELS_START_COL + idx
            val maxPwm = requiredInt(sheet, SOLENOID_MAX_PWM_ROW, col, evaluator, "Solenoid maxPWM[$idx]")
            solenoids.add(
                SolenoidHolder(
                    displayName = requiredString(sheet, SOLENOID_DISPLAY_ROW, col, evaluator, "Solenoid displayName[$idx]"),
                    index = requiredInt(sheet, SOLENOID_INDEX_ROW, col, evaluator, "Solenoid index[$idx]"),
                    maxPWM = maxPwm,
                    step = requiredInt(sheet, SOLENOID_STEP_ROW, col, evaluator, "Solenoid step[$idx]"),
                    ditherAmplitude = optionalString(sheet, SOLENOID_DITHER_AMP_ROW, col, evaluator, "0"),
                    ditherFrequency = requiredInt(sheet, SOLENOID_DITHER_FREQ_ROW, col, evaluator, "Solenoid ditherFrequency[$idx]"),
                    currentMinValue = requiredInt(sheet, SOLENOID_CURRENT_MIN_ROW, col, evaluator, "Solenoid currentMin[$idx]"),
                    currentMaxValue = requiredInt(sheet, SOLENOID_CURRENT_MAX_ROW, col, evaluator, "Solenoid currentMax[$idx]"),
                    isVisible = optionalBoolean(sheet, SOLENOID_VISIBLE_ROW, col, evaluator, true)
                )
            )
            maxPWMs.add(maxPwm)
        }

        limitTime = 0
        for (rowIndex in SCENARIO_START_ROW..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            val time = cellAsDouble(row, SCENARIO_TIME_COL, evaluator)?.toInt() ?: continue

            val channels = ArrayList<Int>(NUMBER_OF_GAUGES)
            repeat(NUMBER_OF_GAUGES) { idx ->
                val raw = cellAsDouble(row, SCENARIO_CHANNELS_START_COL + idx, evaluator)?.toInt() ?: 0
                val clamped = raw.coerceIn(0, maxPWMs[idx])
                channels.add(clamped)
            }

            val analog1 = cellAsDouble(row, SCENARIO_ANALOG1_COL, evaluator)?.toInt() ?: 0
            val analog2 = cellAsDouble(row, SCENARIO_ANALOG2_COL, evaluator)?.toInt() ?: 0
            val gradientTime = cellAsDouble(row, SCENARIO_GRADIENT_COL, evaluator)?.toInt() ?: 0
            val comment = cellAsString(row, SCENARIO_COMMENT_COL, evaluator) ?: "no name step"

            scenario.add(
                ScenarioStep(
                    time = time,
                    channels = channels,
                    analog1 = analog1,
                    analog2 = analog2,
                    gradientTime = gradientTime,
                    comment = comment
                )
            )

            limitTime += time
        }

        File(Dir2Reports, inputScenarioFile.nameWithoutExtension).mkdirs()
        NAME_OF_NEW_SCENARIO = inputScenarioFile.nameWithoutExtension
        Dir11ForTargetingSaveNewExperiment = File(Dir2Reports, NAME_OF_NEW_SCENARIO)

        val standardPath = cellAsString(sheet.getRow(0), 0, evaluator)
        if (!standardPath.isNullOrBlank() && standardPath.lowercase().endsWith("txt")) {
            chartFileStandard.value = File(standardPath)
        }

        logInfo("SOLENOID_MAIN_FREQ=$SOLENOID_MAIN_FREQ")
        logInfo("SOLENOID_FREQ_PARAMS_0x68=${SOLENOID_FREQ_PARAMS_0x68.joinToString()}")
        logInfo("scenario steps loaded=${scenario.size}")

        file.close()
        wb.close()
        return true
    } catch (e: Exception) {
        val details = "ERROR targetParseScenario: ${e.message}\n${e.stackTraceToString()}"
        println(details)
        logError(details)
        val userMessage = e.message?.lineSequence()?.firstOrNull() ?: "ERROR targetParseScenario"
        showMeSnackBar(userMessage, color = Color.Red)
        return false
    }
}
