//package ru.atrsx.mcmcomposer
//
//import org.apache.poi.ss.usermodel.Cell
//import org.apache.poi.ss.usermodel.CellType
//import org.apache.poi.ss.usermodel.Row
//import org.apache.poi.xssf.usermodel.XSSFSheet
//import org.apache.poi.xssf.usermodel.XSSFWorkbook
//import java.io.File
//import java.io.FileInputStream
//import java.util.Locale
//import javax.swing.JFileChooser
//import javax.swing.JOptionPane
//import javax.swing.filechooser.FileNameExtensionFilter
//
//// ----- You already have these types / state lists in your app -----
//// import ru.atrsx.mcmcomposer.PressureChannel
//// import ru.atrsx.mcmcomposer.SolenoidChannel
//// import ru.atrsx.mcmcomposer.ScenarioStep
//// import ru.atrsx.mcmcomposer.pressures
//// import ru.atrsx.mcmcomposer.solenoids
//// import ru.atrsx.mcmcomposer.scenarios
//
//// ============ Public API ===============================================
//
//data class ImportResult(
//    val pressures: List<PressureChannel>,
//    val solenoids: List<SolenoidChannel>,
//    val scenarios: List<ScenarioStep>,
//    val titleText: String?,
//    val mainFreq: Int?,
//    val testVar: Int?
//)
//
///** Import from path (no UI) and return all arrays. */
//fun importExperimentFromExcelExactCells(path: String, sheetName: String = "test"): ImportResult {
//    FileInputStream(File(path)).use { fis ->
//        val wb = XSSFWorkbook(fis)
//        val sheet = wb.getSheet(sheetName) ?: wb.getSheetAt(0)
//        val result = parseSheetExactCells(sheet)
//        wb.close()
//        return result
//    }
//}
//
///** Show Open dialog, import, and optionally write results into your global state lists. */
//fun importExperimentExcelWithDialog(
//    sheetName: String = "test",
//    writeIntoGlobals: Boolean = true
//): ImportResult? {
//    val chooser = JFileChooser().apply {
//        fileFilter = FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx")
//        isAcceptAllFileFilterUsed = true
//    }
//    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return null
//    val file = chooser.selectedFile ?: return null
//    val res = importExperimentFromExcelExactCells(file.absolutePath, sheetName)
//
//    if (writeIntoGlobals) {
//        // Replace your state with imported values
//        pressures.clear(); pressures.addAll(res.pressures)
//        solenoids.clear(); solenoids.addAll(res.solenoids)
//        scenarios.clear(); scenarios.addAll(res.scenarios)
//    }
//
//    JOptionPane.showMessageDialog(
//        null,
//        "Импорт завершён:\n${file.absolutePath}",
//        "Готово",
//        JOptionPane.INFORMATION_MESSAGE
//    )
//    return res
//}
//
//// ============ Parser (exact layout) ====================================
//
//private object L {
//    // 0-based columns
//    const val A = 0
//    const val B = 1
//    const val CH_START = B         // B..M
//    const val CH_COUNT = 12
//    const val CH_END = CH_START + CH_COUNT - 1 // M
//
//    // Scenario extra cols (N..Q)
//    const val N = 13 // Analog1 (ignored)
//    const val O = 14 // Analog2 (ignored)
//    const val P = 15 // Gradient Time
//    const val Q = 16 // text
//
//    // Block anchors (0-based rows), mirroring the exporter
//    const val ROW_TITLE     = 0  // A1 title (optional)
//    const val ROW_P_CAPTION = 1  // A2 "Pressures"
//    const val ROW_P_HEADER  = 2  // row 3
//    const val ROW_P_START   = 3  // first attribute row for Pressures
//
//    const val ROW_S_CAPTION  = 14 // row 15 "Solenoids"
//    const val ROW_S_PREAMBLE = 15 // row 16: Main Freq/TestVariable
//    const val ROW_S_START    = 16 // first attribute row for Solenoids
//
//    const val ROW_SC_CAPTION = 26 // row 27 "Scenario:"
//    const val ROW_SC_HEADER  = 27 // row 28 header
//    const val ROW_SC_FIRST   = 28 // row 29 first scenario row
//}
//
//private fun parseSheetExactCells(sheet: XSSFSheet): ImportResult {
//    // --- Title (A1) ---
//    val titleText = getString(sheet, L.ROW_TITLE, L.A)?.takeIf { it.isNotBlank() }
//
//    // ------------- PRESSURES -------------
//    // We map by labels found in column A, scanning from ROW_P_START until we hit "~" (or blank).
//    val pRows = findLabeledRows(
//        sheet = sheet,
//        startRow = L.ROW_P_START,
//        stopAtTilde = true
//    )
//    val pressures = readPressures(sheet, pRows)
//
//    // ------------- SOLENOIDS -------------
//    // Preamble (Main Freq/TestVariable)
//    val mainFreq = getInt(sheet, L.ROW_S_PREAMBLE, L.C)
//    val testVar  = getInt(sheet, L.ROW_S_PREAMBLE, L.E)
//
//    val sRows = findLabeledRows(
//        sheet = sheet,
//        startRow = L.ROW_S_START,
//        stopAtTilde = true
//    )
//    val solenoids = readSolenoids(sheet, sRows)
//
//    // ------------- SCENARIO --------------
//    val scenarios = readScenario(sheet)
//
//    return ImportResult(
//        pressures = pressures,
//        solenoids = solenoids,
//        scenarios = scenarios,
//        titleText = titleText,
//        mainFreq = mainFreq,
//        testVar = testVar
//    )
//}
//
///** Find rows where Column A contains a label; stop when we encounter "~" row or a blank streak. */
//private fun findLabeledRows(
//    sheet: XSSFSheet,
//    startRow: Int,
//    stopAtTilde: Boolean
//): Map<String, Int> {
//    val map = mutableMapOf<String, Int>()
//    var r = startRow
//    var blanks = 0
//    while (true) {
//        val a = getString(sheet, r, L.A)?.trim().orEmpty()
//        if (a == "~" && stopAtTilde) break
//        if (a.isBlank()) {
//            blanks++
//            if (blanks >= 2) break // two blank rows in a row -> end of block
//        } else {
//            blanks = 0
//            map[a] = r
//        }
//        r++
//        if (r > sheet.lastRowNum + 2) break
//    }
//    return map
//}
//
//private fun readPressures(sheet: XSSFSheet, rows: Map<String, Int>): List<PressureChannel> {
//    // Build 12 channels B..M
//    val list = MutableList(L.CH_COUNT) { idx ->
//        PressureChannel(
//            index = idx,
//            displayName = "",
//            minValue = 0,
//            maxValue = 0,
//            tolerance = 0,
//            unit = "",
//            comment = "",
//            preferredColorHex = "#FF008001",
//            isVisible = true
//        )
//    }
//
//    fun fillRow(label: String, apply: (PressureChannel, String) -> PressureChannel) {
//        val r = rows[label] ?: return
//        for (c in L.CH_START..L.CH_END) {
//            val i = c - L.CH_START
//            val v = getString(sheet, r, c).orEmpty()
//            list[i] = apply(list[i], v)
//        }
//    }
//
//    fillRow("DisplayName")   { ch, v -> ch.copy(displayName = v) }
//    fillRow("Index")         { ch, v -> ch.copy(index = v.toIntOrNull() ?: ch.index) }
//    fillRow("MinValue")      { ch, v -> ch.copy(minValue = v.toIntOrNull() ?: ch.minValue) }
//    fillRow("MaxValue")      { ch, v -> ch.copy(maxValue = v.toIntOrNull() ?: ch.maxValue) }
//    fillRow("Tolerance")     { ch, v -> ch.copy(tolerance = v.toIntOrNull() ?: ch.tolerance) }
//    fillRow("Unit")          { ch, v -> ch.copy(unit = v) }
//    fillRow("CommentString") { ch, v -> ch.copy(comment = v) }
//    fillRow("PreferredColor"){ ch, v ->
//        val hex = v.trim()
//        val norm = when {
//            hex.isEmpty() -> ch.preferredColorHex
//            hex.startsWith("#") && (hex.length == 7 || hex.length == 9) -> hex.uppercase(Locale.US)
//            else -> ch.preferredColorHex
//        }
//        ch.copy(preferredColorHex = norm)
//    }
//    fillRow("isVisible")     { ch, v ->
//        ch.copy(isVisible = parseBool(v) ?: ch.isVisible)
//    }
//    // "parameters" row is ignored
//
//    return list
//}
//
//private fun readSolenoids(sheet: XSSFSheet, rows: Map<String, Int>): List<SolenoidChannel> {
//    val list = MutableList(L.CH_COUNT) { idx ->
//        SolenoidChannel(
//            displayName = "",
//            index = idx,
//            maxPwm0_255 = 0,
//            valueOfDivision = 0,
//            tenthAmplitude = 0,
//            tenthFrequency = 0,
//            minValue = 0,
//            maxValue = 0,
//            isVisible = true
//        )
//    }
//
//    fun fillRow(label: String, apply: (SolenoidChannel, String) -> SolenoidChannel) {
//        val r = rows[label] ?: return
//        for (c in L.CH_START..L.CH_END) {
//            val i = c - L.CH_START
//            val v = getString(sheet, r, c).orEmpty()
//            list[i] = apply(list[i], v)
//        }
//    }
//
//    fillRow("DisplayName")         { ch, v -> ch.copy(displayName = v) }
//    fillRow("Index")               { ch, v -> ch.copy(index = v.toIntOrNull() ?: ch.index) }
//    fillRow("MaxPWM [0 - 255]")    { ch, v -> ch.copy(maxPwm0_255 = v.toIntOrNull()?.coerceIn(0,255) ?: ch.maxPwm0_255) }
//    fillRow("Value of division")   { ch, v -> ch.copy(valueOfDivision = v.toIntOrNull() ?: ch.valueOfDivision) }
//    fillRow("tenth amplitude")     { ch, v -> ch.copy(tenthAmplitude = v.toIntOrNull() ?: ch.tenthAmplitude) }
//    fillRow("tenth frequency")     { ch, v -> ch.copy(tenthFrequency = v.toIntOrNull() ?: ch.tenthFrequency) }
//    fillRow("MinValue")            { ch, v -> ch.copy(minValue = v.toIntOrNull() ?: ch.minValue) }
//    fillRow("MaxValue")            { ch, v -> ch.copy(maxValue = v.toIntOrNull() ?: ch.maxValue) }
//    fillRow("isVisible")           { ch, v -> ch.copy(isVisible = parseBool(v) ?: ch.isVisible) }
//
//    return list
//}
//
//private fun readScenario(sheet: XSSFSheet): List<ScenarioStep> {
//    val rows = mutableListOf<ScenarioStep>()
//    var r = L.ROW_SC_FIRST
//    while (r <= sheet.lastRowNum + 1) {
//        val stepTime = getInt(sheet, r, L.A)
//        // Stop when the row is empty (no step time and no values)
//        val empty = stepTime == null &&
//                (L.CH_START..L.CH_END).all { getString(sheet, r, it).isNullOrBlank() } &&
//                getString(sheet, r, L.Q).isNullOrBlank()
//
//        if (empty) break
//
//        // Channel values (pad/truncate to 12)
//        val values = IntArray(L.CH_COUNT) { i ->
//            val c = L.CH_START + i
//            getInt(sheet, r, c) ?: 0
//        }.toMutableList()
//
//        val grad = getInt(sheet, r, L.P) ?: 0
//        val text = getString(sheet, r, L.Q).orEmpty()
//
//        val step = ScenarioStep(
//            stepTimeMs = stepTime ?: 0,
//            channelValues = values,
//            text = text,
//            gradientTimeMs = grad
//        )
//        rows += step
//        r++
//    }
//    return rows
//}
//
//// ============ Cell helpers ==============================================
//
//private fun getString(sheet: XSSFSheet, row: Int, col: Int): String? {
//    val r: Row = sheet.getRow(row) ?: return null
//    val c: Cell = r.getCell(col) ?: return null
//    return when (c.cellType) {
//        CellType.STRING  -> c.stringCellValue
//        CellType.NUMERIC -> {
//            val n = c.numericCellValue
//            if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
//        }
//        CellType.BOOLEAN -> c.booleanCellValue.toString()
//        CellType.FORMULA -> runCatching {
//            when (c.cachedFormulaResultType) {
//                CellType.NUMERIC -> {
//                    val n = c.numericCellValue
//                    if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
//                }
//                CellType.STRING -> c.stringCellValue
//                CellType.BOOLEAN -> c.booleanCellValue.toString()
//                else -> null
//            }
//        }.getOrNull()
//        else -> null
//    }?.trim()
//}
//
//private fun getInt(sheet: XSSFSheet, row: Int, col: Int): Int? {
//    val s = getString(sheet, row, col) ?: return null
//    return s.trim().replace(",", ".").toDoubleOrNull()?.toInt()
//}
//
//private fun parseBool(v: String): Boolean? {
//    val t = v.trim().lowercase(Locale.US)
//    return when {
//        t == "true" || t == "1" || t == "✓" -> true
//        t == "false" || t == "0" || t == "—" || t == "-" -> false
//        else -> null
//    }
//}
