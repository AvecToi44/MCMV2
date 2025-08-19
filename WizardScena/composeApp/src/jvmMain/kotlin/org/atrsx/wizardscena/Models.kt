package org.atrsx.wizardscena

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

// ---------- Domain model (matches your updated Excel) ----------

data class MainExperimentConfig(
    val pressures: PressuresBlockDto = PressuresBlockDto(),
    val solenoids: SolenoidsBlock = SolenoidsBlock(),
    val scenario: ScenarioBlockDto = ScenarioBlockDto(),
    var standardPath: String,
    var sheetName: String = "test"
)

// --- Pressures (orange) ---

data class PressuresBlockDto(
    val channels: SnapshotStateList<PressureChannel> = mutableStateListOf<PressureChannel>().apply {
        repeat(12) { i ->
            add(
                PressureChannel(
                    displayName = if (i == 0) "Первый показатель" else "Channel Data ${i + 1}",
                    index = i,
                    minValue = 0,
                    maxValue = 0,
                    tolerance = 10,
                    unit = "Bar",
                    comment = "~",
                    // ARGB like in your files (e.g. #FF008001)
                    preferredColorHex = String.format("#FF0080%02X", i),
                    isVisible = true
                )
            )
        }
    }
)

data class PressureChannel(
    var displayName: String,
    var index: Int,
    var minValue: Int,
    var maxValue: Int,
    var tolerance: Int,
    var unit: String,
    var comment: String,
    var preferredColorHex: String,
    var isVisible: Boolean,
    var isSelected: Boolean = false
)

// --- Solenoids (blue) ---

data class SolenoidsBlock(
    var mainFrequencyHz: Int = 1500,      // "Main Frequency:"
    var testVariable: Int? = 0,           // optional
    val channels: MutableList<SolenoidChannel> = MutableList(12) { i ->
        SolenoidChannel(
            displayName = "Без имени ${i + 1}",
            index = i,
            maxPwm0_255 = 0,
            valueOfDivision = 0,
            DitherAmplitude = 0,
            DitherFrequency = 0,
            minValue = 0,
            maxValue = 0,
            isVisible = true
        )
    }
)

data class SolenoidChannel(
    var displayName: String,
    var index: Int,
    var maxPwm0_255: Int,
    var valueOfDivision: Int,
    var DitherAmplitude: Int,
    var DitherFrequency: Int,
    var minValue: Int,
    var maxValue: Int,
    var isVisible: Boolean
)

// --- Scenario (green) ---
data class ScenarioStep(
    val id: String = UUID.randomUUID().toString(),
    var stepTimeMs: Int,
    var channelValues: MutableList<Int>,
    var analog1: Int? = null,
    var analog2: Int? = null,
    var gradientTimeMs: Int? = null,
    var text: String? = null,
    var isSelected: Boolean = false
)

/** One horizontal scenario row in Excel. */
data class ScenarioStepDto(
    var stepTimeMs: Int,
    var channelValues: MutableList<Int>,   // size == number of channels
    var analog1: Int = 0,
    var analog2: Int = 0,
    var gradientTimeMs: Int = 0,           // "время перехода"
    var text: String? = null
)

data class ScenarioBlockDto(
    var mainFrequency: Int = 700,
    var steps: MutableList<ScenarioStepDto> = mutableListOf(
        //ScenarioStepDto(1000, MutableList(12) { 0 }, analog1 = 0, analog2 = 0, gradientTimeMs = 0, text = "")
    )
)

// ---------- UI helper model and mapping ----------

data class UiScenarioRow(
    var number: Int,
    var name: String = "",
    var passThrough: Boolean = true,
    var durationMs: String = "1000",
    var messageText: String = "",
    var interpolationFlags: MutableList<Boolean> = MutableList(16) { false },
    var pressureHighlightFlags: MutableList<Boolean> = MutableList(16) { false },
    var analogSetEnabled: Boolean = false,
    var channels: MutableList<Int> = MutableList(12) { 0 },
    var analog1: Int = 0,
    var analog2: Int = 0,
    var gradientTimeMs: Int = 0
)

fun UiScenarioRow.toScenarioStep(nChannels: Int) =
    ScenarioStepDto(
        stepTimeMs = durationMs.toIntOrNull() ?: 0,
        channelValues = channels.take(nChannels).toMutableList(),
        analog1 = analog1,
        analog2 = analog2,
        gradientTimeMs = gradientTimeMs,
        text = messageText
    )

fun ScenarioStepDto.toUiScenarioRow(number: Int) =
    UiScenarioRow(
        number = number,
        durationMs = stepTimeMs.toString(),
        messageText = text.orEmpty(),
        channels = channelValues.toMutableList(),
        analog1 = analog1,
        analog2 = analog2,
        gradientTimeMs = gradientTimeMs
    )

private const val DEFAULT_CHANNELS = 12

// Single item
fun ScenarioStep.toDto(channelCount: Int = DEFAULT_CHANNELS): ScenarioStepDto {
    // Pad/truncate channel values to exactly channelCount
    val channels = MutableList(channelCount) { idx -> channelValues.getOrNull(idx) ?: 0 }

    return ScenarioStepDto(
        stepTimeMs = stepTimeMs.coerceAtLeast(0),
        channelValues = channels,
        analog1 = (analog1 ?: 0).coerceAtLeast(0),
        analog2 = (analog2 ?: 0).coerceAtLeast(0),
        gradientTimeMs = (gradientTimeMs ?: 0).coerceAtLeast(0),
        text = text?.takeIf { it.isNotBlank() }   // keep null if blank
    )
}

// List / SnapshotStateList -> MutableList<Dto>
fun List<ScenarioStep>.toDtoList(channelCount: Int = DEFAULT_CHANNELS): MutableList<ScenarioStepDto> =
    this.map { it.toDto(channelCount) }.toMutableList()

// Explicit overload (optional; SnapshotStateList is a List, but this helps discoverability)
fun SnapshotStateList<ScenarioStep>.toDtoList(channelCount: Int = DEFAULT_CHANNELS): MutableList<ScenarioStepDto> =
    this.map { it.toDto(channelCount) }.toMutableList()


// ---------- Excel Exporter (hard-mapped to your screenshot layout) ----------

object ExcelExporter {

    // Column anchors (0-based)
    const val C_LABEL = 0         // A
    const val C_CH_FIRST = 1      // B..M (12 channels)
    const val C_ANALOG1 = 13      // N
    const val C_ANALOG2 = 14      // O
    const val C_GRADIENT = 15     // P
    const val C_TEXT = 16         // Q

    // Row anchors (0-based)
    const val R_PATH = 0
    const val R_PRESS_HDR = 1      // "Pressures"
    const val R_P_Display = 2
    const val R_P_Index = 3
    const val R_P_Min = 4
    const val R_P_Max = 5
    const val R_P_Tol = 6
    const val R_P_Unit = 7
    const val R_P_Comment = 8
    const val R_P_Color = 9
    const val R_P_Visible = 10
    const val R_P_Params = 11
    const val R_P_Tilda = 12

    const val R_S_HDR = 13         // "Solenoids"
    const val C_S_MainFreq_L = 1   // B
    const val C_S_MainFreq_V = 2   // C
    const val R_S_Display = 14
    const val R_S_Index = 15
    const val R_S_MaxPWM = 16
    const val R_S_ValueDiv = 17
    const val R_S_DitherAmp = 18
    const val R_S_DitherFreq = 19
    const val R_S_Min = 20
    const val R_S_Max = 21
    const val R_S_Visible = 22
    const val R_S_Tilda1 = 23
    const val R_S_Tilda2 = 24

    const val R_SCEN_HDR = 25      // "Scenario:"
    const val R_SCEN_COLS = 26     // headings
    const val R_SCEN_FIRST = 27   // first step row

    fun export(config: MainExperimentConfig, outFile: File) {
        val wb = HSSFWorkbook()
        val sh = wb.createSheet(config.sheetName)

        // helper creators
        val bold = wb.createCellStyle().apply { setFont(wb.createFont().apply { bold = true }) }
        val italic = wb.createCellStyle().apply { setFont(wb.createFont().apply { italic = true }) }

        fun cell(r: Int, c: Int): Cell {
            val row = sh.getRow(r) ?: sh.createRow(r)
            return row.getCell(c) ?: row.createCell(c)
        }
        fun label(r: Int, text: String, style: CellStyle? = bold) =
            cell(r, C_LABEL).apply { setCellValue(text); style?.let { setCellStyle(it) } }

        // A1: path
        if (config.standardPath.isNotBlank()) cell(R_PATH, C_LABEL).setCellValue(config.standardPath)

        // --------- PRESSURES ----------
        label(R_PRESS_HDR, "Pressures")
        for (i in 0 until 12) cell(R_PRESS_HDR, C_CH_FIRST + i).setCellValue("Channel Data ${i + 1}")

        label(R_P_Display, "DisplayName"); label(R_P_Index, "Index"); label(R_P_Min, "MinValue")
        label(R_P_Max, "MaxValue"); label(R_P_Tol, "Tolerance"); label(R_P_Unit, "Unit")
        label(R_P_Comment, "CommentString"); label(R_P_Color, "PreferredColor"); label(R_P_Visible, "isVisible")
        label(R_P_Params, "parameters")
        cell(R_P_Tilda, C_LABEL).apply { setCellValue("~"); setCellStyle(italic) }

        val pchs = config.pressures.channels.take(12)
        for ((i, ch) in pchs.withIndex()) {
            val c = C_CH_FIRST + i
            cell(R_P_Display, c).setCellValue(ch.displayName)
            cell(R_P_Index, c).setCellValue(ch.index.toDouble())
            cell(R_P_Min, c).setCellValue(ch.minValue.toDouble())
            cell(R_P_Max, c).setCellValue(ch.maxValue.toDouble())
            cell(R_P_Tol, c).setCellValue(ch.tolerance.toDouble())
            cell(R_P_Unit, c).setCellValue(ch.unit)
            cell(R_P_Comment, c).setCellValue(ch.comment.ifBlank { "~" })
            cell(R_P_Color, c).setCellValue(ch.preferredColorHex)
            cell(R_P_Visible, c).setCellValue(if (ch.isVisible) "true" else "false")
        }

        // --------- SOLENOIDS ----------
        label(R_S_HDR, "Solenoids")
        cell(R_S_HDR, C_S_MainFreq_L).apply { setCellValue("Main Frequency:"); setCellStyle(bold) }
        cell(R_S_HDR, C_S_MainFreq_V).setCellValue(config.solenoids.mainFrequencyHz.toDouble())

        label(R_S_Display, "DisplayName"); label(R_S_Index, "Index"); label(R_S_MaxPWM, "MaxPWM [0 - 255]")
        label(R_S_ValueDiv, "Value of division"); label(R_S_DitherAmp, "Dither Amplitude")
        label(R_S_DitherFreq, "Dither Frequency"); label(R_S_Min, "Current MinValue")
        label(R_S_Max, "Current MaxValue"); label(R_S_Visible, "isVisible")
        cell(R_S_Tilda1, C_LABEL).apply { setCellValue("~"); setCellStyle(italic) }
        cell(R_S_Tilda2, C_LABEL).apply { setCellValue("~"); setCellStyle(italic) }

        val soms = config.solenoids.channels.take(12)
        for ((i, ch) in soms.withIndex()) {
            val c = C_CH_FIRST + i
            cell(R_S_Display, c).setCellValue(ch.displayName)
            cell(R_S_Index, c).setCellValue(ch.index.toDouble())
            cell(R_S_MaxPWM, c).setCellValue(ch.maxPwm0_255.toDouble())
            cell(R_S_ValueDiv, c).setCellValue(ch.valueOfDivision.toDouble())
            cell(R_S_DitherAmp, c).setCellValue(ch.DitherAmplitude.toDouble())
            cell(R_S_DitherFreq, c).setCellValue(ch.DitherFrequency.toDouble())
            cell(R_S_Min, c).setCellValue(ch.minValue.toDouble())
            cell(R_S_Max, c).setCellValue(ch.maxValue.toDouble())
            cell(R_S_Visible, c).setCellValue(if (ch.isVisible) "true" else "false")
        }

        // --------- SCENARIO ----------
        label(R_SCEN_HDR, "Scenario:")
        cell(R_SCEN_COLS, C_LABEL).setCellValue("step time")
//        for (i in 0 until 12) cell(R_SCEN_COLS, C_CH_FIRST + i).setCellValue("приверт ${i + 1}")
        cell(R_SCEN_COLS, C_ANALOG1).setCellValue("Analog1")
        cell(R_SCEN_COLS, C_ANALOG2).setCellValue("Analog2")
        cell(R_SCEN_COLS, C_GRADIENT).setCellValue("время перехода")
        cell(R_SCEN_COLS, C_TEXT).setCellValue("text")

        config.scenario.steps.forEachIndexed { idx, step ->
            println("scenario ${idx}>>> ${step}")
            val r = R_SCEN_FIRST + idx
            cell(r, C_LABEL).setCellValue(step.stepTimeMs.toDouble())
            for (i in 0 until 12) {
                val v = step.channelValues.getOrNull(i) ?: 0
                cell(r, C_CH_FIRST + i).setCellValue(v.toDouble())
            }
            cell(r, C_ANALOG1).setCellValue(step.analog1.toDouble())
            cell(r, C_ANALOG2).setCellValue(step.analog2.toDouble())
            cell(r, C_GRADIENT).setCellValue(step.gradientTimeMs.toDouble())
            cell(r, C_TEXT).setCellValue(step.text.orEmpty())
        }

        // Autosize (safe try)
        for (c in 0..C_TEXT) runCatching { sh.autoSizeColumn(c) }

        FileOutputStream(ensureXls(outFile)).use { wb.write(it) }
        wb.close()
        _root_ide_package_.org.atrsx.wizardscena.LAST_CHANGES_SAVED.value = generateTimestampLastUpdate()
    }

    // Native "Save as…" (Compose Desktop/JVM)
    fun saveWithDialog(parent: Frame? = null, suggestedName: String = "${_root_ide_package_.org.atrsx.wizardscena.MAIN_CONFIG.value.sheetName}.xls"): File? {
        _root_ide_package_.org.atrsx.wizardscena.LAST_FILE_SAVED = "${_root_ide_package_.org.atrsx.wizardscena.MAIN_CONFIG.value.sheetName}.xls"
        val dlg = FileDialog(parent, "Save Excel", FileDialog.SAVE).apply {
            file = suggestedName
            isVisible = true
        }
        val chosen = dlg.file ?: return null

        return ensureXls(File(dlg.directory, chosen))
    }

    private fun ensureXls(file: File): File =
        if (file.extension.lowercase(Locale.ROOT) != "xlsx")
            File(file.parentFile, file.nameWithoutExtension + ".xls") else file
}

// ---------- Usage (example in a click handler / coroutine) ----------
//
// val cfg = MainExperimentConfig(
//     pressures = yourPressuresBlockDto,
//     solenoids = yourSolenoidsBlock,
//     scenario  = yourScenarioBlockDto,
//     standardPath = "C:\\Users\\...\\0b5_combi_18_08_2025 11_31_32_arstest5.txt",
//     sheetName = "0b5_combi"
// )
// ExcelExporter.saveWithDialog()?.let { ExcelExporter.export(cfg, it) }
