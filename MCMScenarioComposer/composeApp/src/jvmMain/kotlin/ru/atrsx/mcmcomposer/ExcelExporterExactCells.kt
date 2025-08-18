// ExcelExporterOb5Exact.kt

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.HorizontalAlignment.*
import org.apache.poi.ss.usermodel.VerticalAlignment.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.atrsx.mcmcomposer.PressureChannel
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.SolenoidChannel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

import org.apache.poi.ss.usermodel.*
import ru.atrsx.mcmcomposer.MainExperimentConfig
import java.awt.FileDialog
import java.awt.Frame
import java.util.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ExcelExporter {

    // Fixed layout from your screenshot
    private const val COL_LABELS = 0                         // A
    private const val COL_FIRST_CHANNEL = 1                  // B  (B..M -> 12 channels)
    private const val COL_ANALOG1 = 13                       // N
    private const val COL_ANALOG2 = 14                       // O
    private const val COL_GRADIENT = 15                      // P (время перехода)
    private const val COL_TEXT = 16                          // Q

    // Row anchors (0-based)
    private const val ROW_PATH = 0                           // A1
    private const val ROW_PRESSURES_HEADER = 1               // A2: "Pressures"
    private const val ROW_P_DisplayName = 2                  // A3
    private const val ROW_P_Index = 3                        // A4
    private const val ROW_P_Min = 4                          // A5
    private const val ROW_P_Max = 5                          // A6
    private const val ROW_P_Tol = 6                          // A7
    private const val ROW_P_Unit = 7                         // A8
    private const val ROW_P_Comment = 8                      // A9
    private const val ROW_P_Color = 9                        // A10
    private const val ROW_P_Visible = 10                     // A11
    private const val ROW_P_Params = 11                      // A12
    private const val ROW_P_Tilda = 12                       // A13

    private const val ROW_S_Header = 14                      // A15: "Solenoids"
    private const val COL_S_MainFreq_Label = 1               // B15: "Main Frequency:"
    private const val COL_S_MainFreq_Value = 2               // C15: value (e.g. 1500)

    private const val ROW_S_DisplayName = 15                 // A16
    private const val ROW_S_Index = 16                       // A17
    private const val ROW_S_MaxPWM = 17                      // A18
    private const val ROW_S_ValueOfDivision = 18             // A19
    private const val ROW_S_DitherAmp = 19                   // A20
    private const val ROW_S_DitherFreq = 20                  // A21
    private const val ROW_S_Min = 21                         // A22
    private const val ROW_S_Max = 22                         // A23
    private const val ROW_S_Visible = 23                     // A24
    private const val ROW_S_Tilda1 = 24                      // A25
    private const val ROW_S_Tilda2 = 25                      // A26

    private const val ROW_SCENARIO_HEADER = 27               // A28: "Scenario:"
    private const val ROW_SCENARIO_COLHEAD = 28              // A29: "step time" + channel headers
    private const val ROW_SCENARIO_FIRST = 29                // A30: first step

    fun export(config: MainExperimentConfig, outputFile: File) {
        val wb = XSSFWorkbook()
        val sh = wb.createSheet(config.sheetName)

        // Styles
        val header = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { bold = true })
        }
        val italic = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { italic = true })
        }

        fun row(r: Int) = sh.getRow(r) ?: sh.createRow(r)
        fun cell(r: Int, c: Int): Cell = (sh.getRow(r) ?: sh.createRow(r)).getCell(c) ?: sh.getRow(r).createCell(c)

        fun writeLabel(r: Int, text: String, style: CellStyle? = header) {
            cell(r, COL_LABELS).apply {
                setCellValue(text)
                style?.let { setCellStyle(it) }
            }
        }

        // A1: path / standard
        if (config.standardPath.isNotBlank()) {
            cell(ROW_PATH, COL_LABELS).setCellValue(config.standardPath)
        }

        // ========== PRESSURES ==========
        writeLabel(ROW_PRESSURES_HEADER, "Pressures", header)

        // Optional channel headers: "Channel Data 1..12" (row 2 / headers row)
        for (ch in 0 until 12) {
            cell(ROW_PRESSURES_HEADER, COL_FIRST_CHANNEL + ch)
                .setCellValue("Channel Data ${ch + 1}")
        }

        writeLabel(ROW_P_DisplayName, "DisplayName")
        writeLabel(ROW_P_Index,       "Index")
        writeLabel(ROW_P_Min,         "MinValue")
        writeLabel(ROW_P_Max,         "MaxValue")
        writeLabel(ROW_P_Tol,         "Tolerance")
        writeLabel(ROW_P_Unit,        "Unit")
        writeLabel(ROW_P_Comment,     "CommentString")
        writeLabel(ROW_P_Color,       "PreferredColor")
        writeLabel(ROW_P_Visible,     "isVisible")
        writeLabel(ROW_P_Params,      "parameters")
        row(ROW_P_Tilda).apply { cell(COL_LABELS, ROW_P_Tilda).apply { setCellValue("~"); setCellStyle(italic) } }

        val p = config.pressures.channels.take(12)
        for ((i, ch) in p.withIndex()) {
            val col = COL_FIRST_CHANNEL + i
            cell(ROW_P_DisplayName, col).setCellValue(ch.displayName)
            cell(ROW_P_Index,       col).setCellValue(ch.index.toDouble())
            cell(ROW_P_Min,         col).setCellValue(ch.minValue.toDouble())
            cell(ROW_P_Max,         col).setCellValue(ch.maxValue.toDouble())
            cell(ROW_P_Tol,         col).setCellValue(ch.tolerance.toDouble())
            cell(ROW_P_Unit,        col).setCellValue(ch.unit)
            cell(ROW_P_Comment,     col).setCellValue(if (ch.comment.isBlank()) "~" else ch.comment)
            cell(ROW_P_Color,       col).setCellValue(ch.preferredColorHex)
            cell(ROW_P_Visible,     col).setCellValue(if (ch.isVisible) "true" else "false")
            // parameters row left blank (as in screenshot)
        }

        // ========== SOLENOIDS ==========
        writeLabel(ROW_S_Header, "Solenoids", header)
        cell(ROW_S_Header, COL_S_MainFreq_Label).apply { setCellValue("Main Frequency:"); setCellStyle(header) }
        cell(ROW_S_Header, COL_S_MainFreq_Value).setCellValue(config.solenoids.mainFrequencyHz.toDouble())

        writeLabel(ROW_S_DisplayName,     "DisplayName")
        writeLabel(ROW_S_Index,           "Index")
        writeLabel(ROW_S_MaxPWM,          "MaxPWM [0 - 255]")
        writeLabel(ROW_S_ValueOfDivision, "Value of division")
        writeLabel(ROW_S_DitherAmp,       "Dither Amplitude")
        writeLabel(ROW_S_DitherFreq,      "Dither Frequency")
        writeLabel(ROW_S_Min,             "Current MinValue")
        writeLabel(ROW_S_Max,             "Current MaxValue")
        writeLabel(ROW_S_Visible,         "isVisible")
        row(ROW_S_Tilda1).apply { cell(ROW_S_Tilda1, COL_LABELS).apply { setCellValue("~"); setCellStyle(italic) } }
        row(ROW_S_Tilda2).apply { cell(ROW_S_Tilda2, COL_LABELS).apply { setCellValue("~"); setCellStyle(italic) } }

        val s = config.solenoids.channels.take(12)
        for ((i, ch) in s.withIndex()) {
            val col = COL_FIRST_CHANNEL + i
            cell(ROW_S_DisplayName,     col).setCellValue(ch.displayName)
            cell(ROW_S_Index,           col).setCellValue(ch.index.toDouble())
            cell(ROW_S_MaxPWM,          col).setCellValue(ch.maxPwm0_255.toDouble())
            cell(ROW_S_ValueOfDivision, col).setCellValue(ch.valueOfDivision.toDouble())
            cell(ROW_S_DitherAmp,       col).setCellValue(ch.DitherAmplitude.toDouble())
            cell(ROW_S_DitherFreq,      col).setCellValue(ch.DitherFrequency.toDouble())
            cell(ROW_S_Min,             col).setCellValue(ch.minValue.toDouble())
            cell(ROW_S_Max,             col).setCellValue(ch.maxValue.toDouble())
            cell(ROW_S_Visible,         col).setCellValue(if (ch.isVisible) "true" else "false")
        }

        // ========== SCENARIO ==========
        writeLabel(ROW_SCENARIO_HEADER, "Scenario:", header)
        // Column headers on A29..Q29
        cell(ROW_SCENARIO_COLHEAD, COL_LABELS).setCellValue("step time")
        for (ch in 0 until 12) {
            cell(ROW_SCENARIO_COLHEAD, COL_FIRST_CHANNEL + ch)
                .setCellValue("приверт ${ch + 1}")     // header text from your sheet (optional)
        }
        cell(ROW_SCENARIO_COLHEAD, COL_ANALOG1).setCellValue("Analog1")
        cell(ROW_SCENARIO_COLHEAD, COL_ANALOG2).setCellValue("Analog2")
        cell(ROW_SCENARIO_COLHEAD, COL_GRADIENT).setCellValue("время перехода")
        cell(ROW_SCENARIO_COLHEAD, COL_TEXT).setCellValue("text")

        config.scenario.steps.forEachIndexed { idx, step ->
            val r = ROW_SCENARIO_FIRST + idx
            // A: step time
            cell(r, COL_LABELS).setCellValue(step.stepTimeMs.toDouble())
            // B..M: 12 channels
            (0 until 12).forEach { ch ->
                val v = step.channelValues.getOrNull(ch) ?: 0
                cell(r, COL_FIRST_CHANNEL + ch).setCellValue(v.toDouble())
            }
            // N, O: analogs (if you don’t use them—stay 0)
            cell(r, COL_ANALOG1).setCellValue(step.analog1.toDouble())
            cell(r, COL_ANALOG2).setCellValue(step.analog2.toDouble())
            // P: gradient time
            cell(r, COL_GRADIENT).setCellValue(step.gradientTimeMs.toDouble())
            // Q: text
            cell(r, COL_TEXT).setCellValue(step.text)
        }

        // Cosmetic: autosize main columns
        (0..COL_TEXT).forEach { c ->
            try { sh.autoSizeColumn(c) } catch (_: Exception) {}
        }

        // Write to disk
        val target = ensureXlsx(outputFile)
        FileOutputStream(target).use { wb.write(it) }
        wb.close()
    }

    // Simple “Save As…” for Compose Desktop
    fun saveWithDialog(parent: Frame? = null, suggestedName: String = "0b5_combi.xlsx"): File? {
        val dialog = FileDialog(parent, "Save Excel", FileDialog.SAVE).apply {
            file = suggestedName
            isVisible = true
        }
        if (dialog.file == null) return null
        return ensureXlsx(File(dialog.directory, dialog.file))
    }

    private fun ensureXlsx(file: File): File =
        if (file.extension.lowercase(Locale.ROOT) != "xlsx")
            File(file.parentFile, file.nameWithoutExtension + ".xlsx")
        else file
}


@Composable
fun ExportExcelButton(
    configProvider: () -> MainExperimentConfig,   // build config from your current arrays
) {
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Button(
            enabled = !exporting,
            onClick = {
                scope.launch(Dispatchers.IO) {
                    exporting = true
                    status = null
                    try {
                        val cfg = configProvider()
                        val ts = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                        val suggestedName = "${cfg.sheetName.ifBlank { "0b5_combi" }}_$ts.xlsx"

                        val out = ExcelExporter.saveWithDialog(
                            parent = null,
                            suggestedName = suggestedName
                        )

                        if (out != null) {
                            ExcelExporter.export(cfg, out)
                            status = "Saved: ${out.absolutePath}"
                        } else {
                            status = "Canceled"
                        }
                    } catch (t: Throwable) {
                        status = "Error: ${t.message ?: t::class.simpleName}"
                    } finally {
                        exporting = false
                    }
                }
            }
        ) {
            Text(if (exporting) "Exporting…" else "Export to Excel (.xlsx)")
        }

        status?.let { Text(it, Modifier.padding(top = 8.dp)) }
    }
}