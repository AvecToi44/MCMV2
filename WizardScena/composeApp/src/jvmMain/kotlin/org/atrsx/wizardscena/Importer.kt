package org.atrsx.wizardscena

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
import org.atrsx.wizardscena.ExcelExporter.C_ANALOG1
import org.atrsx.wizardscena.ExcelExporter.C_ANALOG2
import org.atrsx.wizardscena.ExcelExporter.C_CH_FIRST
import org.atrsx.wizardscena.ExcelExporter.C_GRADIENT
import org.atrsx.wizardscena.ExcelExporter.C_LABEL
import org.atrsx.wizardscena.ExcelExporter.C_S_MainFreq_V
import org.atrsx.wizardscena.ExcelExporter.C_TEXT
import org.atrsx.wizardscena.ExcelExporter.R_PATH
import org.atrsx.wizardscena.ExcelExporter.R_P_Color
import org.atrsx.wizardscena.ExcelExporter.R_P_Comment
import org.atrsx.wizardscena.ExcelExporter.R_P_Display
import org.atrsx.wizardscena.ExcelExporter.R_P_Index
import org.atrsx.wizardscena.ExcelExporter.R_P_Max
import org.atrsx.wizardscena.ExcelExporter.R_P_Min
import org.atrsx.wizardscena.ExcelExporter.R_P_Tol
import org.atrsx.wizardscena.ExcelExporter.R_P_Unit
import org.atrsx.wizardscena.ExcelExporter.R_P_Visible
import org.atrsx.wizardscena.ExcelExporter.R_SCEN_FIRST
import org.atrsx.wizardscena.ExcelExporter.R_S_Display
import org.atrsx.wizardscena.ExcelExporter.R_S_DitherAmp
import org.atrsx.wizardscena.ExcelExporter.R_S_DitherFreq
import org.atrsx.wizardscena.ExcelExporter.R_S_HDR
import org.atrsx.wizardscena.ExcelExporter.R_S_Index
import org.atrsx.wizardscena.ExcelExporter.R_S_Max
import org.atrsx.wizardscena.ExcelExporter.R_S_MaxPWM
import org.atrsx.wizardscena.ExcelExporter.R_S_Min
import org.atrsx.wizardscena.ExcelExporter.R_S_ValueDiv
import org.atrsx.wizardscena.ExcelExporter.R_S_Visible
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

object ExcelImporter {

    // ==== SAME LAYOUT CONSTANTS AS EXPORTER (0-based) ====
//    private const val C_LABEL = 0
//    private const val C_CH_FIRST = 1
//    private const val C_ANALOG1 = 13
//    private const val C_ANALOG2 = 14
//    private const val C_GRADIENT = 15
//    private const val C_TEXT = 16
//
//    private const val R_PATH = 0
//
//    private const val R_PRESS_HDR = 1
//    private const val R_P_Display = 2
//    private const val R_P_Index = 3
//    private const val R_P_Min = 4
//    private const val R_P_Max = 5
//    private const val R_P_Tol = 6
//    private const val R_P_Unit = 7
//    private const val R_P_Comment = 8
//    private const val R_P_Color = 9
//    private const val R_P_Visible = 10
//    // R_P_Params = 11, R_P_Tilda = 12
//
//    private const val R_S_HDR = 14
//    private const val C_S_MainFreq_V = 2
//    private const val R_S_Display = 15
//    private const val R_S_Index = 16
//    private const val R_S_MaxPWM = 17
//    private const val R_S_ValueDiv = 18
//    private const val R_S_DitherAmp = 19
//    private const val R_S_DitherFreq = 20
//    private const val R_S_Min = 21
//    private const val R_S_Max = 22
//    private const val R_S_Visible = 23
//
//    private const val R_SCEN_COLS = 28
//    private const val R_SCEN_FIRST = 29

    // ===== Public entry points =====

    /** Native "Open…" + import into state lists + update MAIN_CONFIG. */
    fun openAndImportIntoState(
        parent: Frame? = null,
        pressuresState: SnapshotStateList<PressureChannel>,
        solenoidsState: SnapshotStateList<SolenoidChannel>,
        scenariosState: SnapshotStateList<ScenarioStep>,
        mainConfigState: MutableState<MainExperimentConfig>
    ): Boolean {
        val dlg = FileDialog(parent, "Open Excel", FileDialog.LOAD).apply { isVisible = true }
        val chosen = dlg.file ?: return false
        val file = File(dlg.directory, chosen)
        return importFromFile(file, pressuresState, solenoidsState, scenariosState, mainConfigState)
    }

    /** Import from a given file path. */
    fun importFromFile(
        file: File,
        pressuresState: SnapshotStateList<PressureChannel>,
        solenoidsState: SnapshotStateList<SolenoidChannel>,
        scenariosState: SnapshotStateList<ScenarioStep>,
        mainConfigState: MutableState<MainExperimentConfig>
    ): Boolean {
        val wb = WorkbookFactory.create(file, null, true) // auto-detect xls/xlsx
        wb.use {
            val sh = wb.getSheetAt(0)

            // Parse blocks
            val importedPath = getString(sh, R_PATH, C_LABEL).orEmpty()

            val pressures = parsePressures(sh)
            val (mainFreq, solenoids) = parseSolenoids(sh)
            val scenarioSteps = parseScenario(sh)

            // Fill state lists (clear + add to keep Compose reactive)
            pressuresState.clear(); pressuresState.addAll(pressures)
            solenoidsState.clear(); solenoidsState.addAll(solenoids)
            scenariosState.clear(); scenariosState.addAll(scenarioSteps)

            // Refresh MAIN_CONFIG
            mainConfigState.value = MainExperimentConfig(
                pressures = PressuresBlockDto(channels = pressuresState),
                solenoids = SolenoidsBlock(mainFrequencyHz = mainFreq, channels = solenoidsState),
                scenario  = ScenarioBlockDto(steps = scenariosState.toDtoList()),
                standardPath = if (importedPath.isNotBlank()) importedPath else file.absolutePath,
                sheetName = file.nameWithoutExtension
            )
            return true
        }
    }

    // ====== Parsers ======

    private fun parsePressures(sh: Sheet): List<PressureChannel> {
        val out = ArrayList<PressureChannel>(12)
        for (i in 0 until 12) {
            val c = C_CH_FIRST + i
            out += PressureChannel(
                displayName = getString(sh, R_P_Display, c).orEmpty(),
                index       = getInt(sh, R_P_Index, c, fallback = i),
                minValue    = getInt(sh, R_P_Min, c),
                maxValue    = getInt(sh, R_P_Max, c),
                tolerance   = getInt(sh, R_P_Tol, c),
                unit        = getString(sh, R_P_Unit, c).orEmpty(),
                comment     = getString(sh, R_P_Comment, c)?.takeIf { it != "~" }.orEmpty(),
                preferredColorHex = getString(sh, R_P_Color, c)?.let { normalizeHex(it) } ?: "#FF008001",
                isVisible   = getBool(sh, R_P_Visible, c, defaultValue = true)
            )
        }
        return out
    }

    /** Returns pair(mainFrequency, channels). */
    private fun parseSolenoids(sh: Sheet): Pair<Int, List<SolenoidChannel>> {
        val mainFreq = getInt(sh, R_S_HDR, C_S_MainFreq_V, fallback = 1500)
        val out = ArrayList<SolenoidChannel>(12)
        for (i in 0 until 12) {
            val c = C_CH_FIRST + i
            out += SolenoidChannel(
                displayName     = getString(sh, R_S_Display, c).orEmpty(),
                index           = getInt(sh, R_S_Index, c, fallback = i),
                maxPwm0_255     = getInt(sh, R_S_MaxPWM, c),
                valueOfDivision = getInt(sh, R_S_ValueDiv, c),
                DitherAmplitude = getInt(sh, R_S_DitherAmp, c),
                DitherFrequency = getInt(sh, R_S_DitherFreq, c),
                minValue        = getInt(sh, R_S_Min, c),
                maxValue        = getInt(sh, R_S_Max, c),
                isVisible       = getBool(sh, R_S_Visible, c, defaultValue = true)
            )
        }
        return mainFreq to out
    }

    private fun parseScenario(sh: Sheet): List<ScenarioStep> {
        val out = mutableListOf<ScenarioStep>()
        val last = maxOf(sh.lastRowNum, R_SCEN_FIRST)

        // Stop when we reach a fully-empty row after header
        var r = R_SCEN_FIRST
        while (r <= last) {
            val time = getIntOrNull(sh, r, C_LABEL)
            val text = getString(sh, r, C_TEXT)
            val channels = (0 until 12).map { i -> getIntOrNull(sh, r, C_CH_FIRST + i) }.toMutableList()

            val allEmpty = time == null &&
                    channels.all { it == null } &&
                    getIntOrNull(sh, r, C_ANALOG1) == null &&
                    getIntOrNull(sh, r, C_ANALOG2) == null &&
                    getIntOrNull(sh, r, C_GRADIENT) == null &&
                    text.isNullOrBlank()

            if (allEmpty) {
                // To be robust: break only after at least one row parsed
                if (out.isNotEmpty()) break
                r++; continue
            }

            out += ScenarioStep(
                stepTimeMs = time ?: 0,
                channelValues = channels.map { it ?: 0 }.toMutableList(),
                analog1 = getIntOrNull(sh, r, C_ANALOG1) ?: 0,
                analog2 = getIntOrNull(sh, r, C_ANALOG2) ?: 0,
                gradientTimeMs = getIntOrNull(sh, r, C_GRADIENT) ?: 0,
                text = text.orEmpty()
            )
            r++
        }
        return out
    }

    // ===== Helpers =====

    private fun Sheet.cellAt(r: Int, c: Int): Cell? =
        getRow(r)?.getCell(c, RETURN_BLANK_AS_NULL)

    private fun getString(sh: Sheet, r: Int, c: Int): String? =
        sh.cellAt(r, c)?.let { cell ->
            when (cell.cellType) {
                CellType.STRING  -> cell.stringCellValue
                CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) cell.dateCellValue.toString()
                else {
                    val v = cell.numericCellValue
                    if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                }
                CellType.BOOLEAN -> if (cell.booleanCellValue) "true" else "false"
                else -> null
            }?.trim()
        }

    private fun getInt(sh: Sheet, r: Int, c: Int, fallback: Int = 0): Int =
        getIntOrNull(sh, r, c) ?: fallback

    private fun getIntOrNull(sh: Sheet, r: Int, c: Int): Int? =
        sh.cellAt(r, c)?.let { cell ->
            when (cell.cellType) {
                CellType.NUMERIC -> cell.numericCellValue.roundToInt()
                CellType.STRING  -> cell.stringCellValue.trim()
                    .lowercase(Locale.ROOT)
                    .takeIf { it.isNotBlank() && it != "~" }
                    ?.toDoubleOrNull()?.roundToInt()
                CellType.BOOLEAN -> if (cell.booleanCellValue) 1 else 0
                else -> null
            }
        }

    private fun getBool(sh: Sheet, r: Int, c: Int, defaultValue: Boolean): Boolean =
        sh.cellAt(r, c)?.let { cell ->
            when (cell.cellType) {
                CellType.BOOLEAN -> cell.booleanCellValue
                CellType.NUMERIC -> cell.numericCellValue != 0.0
                CellType.STRING  -> cell.stringCellValue.trim().equals("true", true)
                else -> defaultValue
            }
        } ?: defaultValue

    private fun normalizeHex(s: String): String {
        val t = s.trim()
        return if (t.startsWith("#")) t else "#$t"
    }
}
