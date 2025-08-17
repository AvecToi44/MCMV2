// ExcelExporterExactCells.kt
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import ru.atrsx.mcmcomposer.PressureChannel
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.SolenoidChannel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Writes the workbook in the *same cell positions* as your screenshot:
 * A1 title; A2 "Pressures"; A15 "Solenoids"; A27 "Scenario:" etc.
 * Channels span columns B..M (12 channels). Scenario extra columns: N..Q.
 */
fun exportExperimentToExcelExactCells(
    outPath: String,
    titleText: String?,
    sheetName: String = "test",
    pressures: List<PressureChannel>,
    solenoids: List<SolenoidChannel>,
    scenarios: List<ScenarioStep>,
    mainFreq: Int = 0,
    testVar: Int = 0
) {
    val wb = XSSFWorkbook()
    val sheet = wb.createSheet(sheetName)
    val st = Styles(wb)
    val L = Layout

    // -------- Column widths (A label wide, B..M tight, N..Q medium) --------
    sheet.setColumnWidth(L.A, L.widthLabel * 256)
    for (c in L.CH_START..L.CH_END) sheet.setColumnWidth(c, L.widthChannel * 256)
    for ((i, col) in L.SC_MISC_COLS.withIndex()) sheet.setColumnWidth(col, L.widthMisc * 256)

    // -------- Title A1 --------
    if (!titleText.isNullOrBlank()) {
        sheet.createRow(L.ROW_TITLE).apply {
            heightInPoints = L.hHeader
            createCell(L.A).apply { setCellValue(titleText); cellStyle = st.title }
        }
    }

    // ========================= PRESSURES =========================
    // A2 section caption
    sheet.createRow(L.ROW_P_CAPTION).apply {
        heightInPoints = L.hHeader
        createCell(L.A).apply { setCellValue("Pressures"); cellStyle = st.sectionTitle }
    }

    // Row 3: header over channels (B..M)
    sheet.createRow(L.ROW_P_HEADER).apply {
        heightInPoints = L.hHeader
        // corner A3 as orange header filler
        createCell(L.A).apply { setCellValue(""); cellStyle = st.headerOrangeLeft }
        for (c in L.CH_START..L.CH_END) {
            val idx = c - L.CH_START
            val ch = pressures.getOrNull(idx)
            val headerText = ch?.displayName?.takeIf { it.isNotBlank() } ?: "Channel Data ${idx + 1}"
            createCell(c).apply { setCellValue(headerText); cellStyle = st.headerOrangeCenter }
        }
    }

    // Rows 4..12 (labels in A, channel values B..M)
    pRow(sheet, L.ROW_P_FIRST + 0, "DisplayName", pressures, st) { it.displayName }
    pRow(sheet, L.ROW_P_FIRST + 1, "Index",       pressures, st) { it.index.toString() }
    pRow(sheet, L.ROW_P_FIRST + 2, "MinValue",    pressures, st) { it.minValue.toString() }
    pRow(sheet, L.ROW_P_FIRST + 3, "MaxValue",    pressures, st) { it.maxValue.toString() }
    pRow(sheet, L.ROW_P_FIRST + 4, "Tolerance",   pressures, st) { it.tolerance.toString() }
    pRow(sheet, L.ROW_P_FIRST + 5, "Unit",        pressures, st) { it.unit }
    pRow(sheet, L.ROW_P_FIRST + 6, "CommentString", pressures, st) { it.comment }
    pRow(sheet, L.ROW_P_FIRST + 7, "PreferredColor", pressures, st) { it.preferredColorHex.uppercase(Locale.US) }
    pRow(sheet, L.ROW_P_FIRST + 8, "isVisible",   pressures, st) { it.isVisible.toString() }
    pRow(sheet, L.ROW_P_FIRST + 9, "parameters",  pressures, st) { "" }

    // Row 13: "~"
    sheet.createRow(L.ROW_P_TILDE).apply {
        heightInPoints = L.hBody
        createCell(L.A).apply { setCellValue("~"); cellStyle = st.orangeLeft }
    }

    // ========================= SOLENOIDS =========================
    // A15 caption
    sheet.createRow(L.ROW_S_CAPTION).apply {
        heightInPoints = L.hHeader
        createCell(L.A).apply { setCellValue("Solenoids"); cellStyle = st.sectionTitle }
    }

    // Row 16: preamble "Main Freq:" value   "TestVariable:" value
    sheet.createRow(L.ROW_S_PREAMBLE).apply {
        heightInPoints = L.hHeader
        createCell(L.A).apply { setCellValue(""); cellStyle = st.headerBlueLeft }
        createCell(L.B).apply { setCellValue("Main Freq:"); cellStyle = st.headerBlueLeft }
        createCell(L.C).apply { setCellValue(mainFreq.toDouble()); cellStyle = st.headerBlueCenter }
        createCell(L.D).apply { setCellValue("TestVariable:"); cellStyle = st.headerBlueLeft }
        createCell(L.E).apply { setCellValue(testVar.toDouble()); cellStyle = st.headerBlueCenter }
    }

    // Rows 17..23 (attributes exactly like screenshot)
    sRow(sheet, L.ROW_S_FIRST + 0, "DisplayName",       solenoids, st) { it.displayName }
    sRow(sheet, L.ROW_S_FIRST + 1, "Index",             solenoids, st) { it.index.toString() }
    sRow(sheet, L.ROW_S_FIRST + 2, "MaxPWM [0 - 255]",  solenoids, st) { it.maxPwm0_255.toString() }
    sRow(sheet, L.ROW_S_FIRST + 3, "Value of division", solenoids, st) { it.valueOfDivision.toString() }
    sRow(sheet, L.ROW_S_FIRST + 4, "tenth amplitude",   solenoids, st) { it.tenthAmplitude.toString() }
    sRow(sheet, L.ROW_S_FIRST + 5, "tenth frequency",   solenoids, st) { it.tenthFrequency.toString() }
    sRow(sheet, L.ROW_S_FIRST + 6, "MinValue",          solenoids, st) { it.minValue.toString() }
    sRow(sheet, L.ROW_S_FIRST + 7, "MaxValue",          solenoids, st) { it.maxValue.toString() }
    sRow(sheet, L.ROW_S_FIRST + 8, "isVisible",         solenoids, st) { it.isVisible.toString() }

    // Row 24: "~"
    sheet.createRow(L.ROW_S_TILDE).apply {
        heightInPoints = L.hBody
        createCell(L.A).apply { setCellValue("~"); cellStyle = st.blueLeft }
    }

    // ========================= SCENARIO =========================
    // A27 "Scenario:"
    sheet.createRow(L.ROW_SC_CAPTION).apply {
        heightInPoints = L.hHeader
        createCell(L.A).apply { setCellValue("Scenario:"); cellStyle = st.sectionTitle }
    }

    // Row 28: header ("step time", then 12 blank channel headers, then misc names)
    sheet.createRow(L.ROW_SC_HEADER).apply {
        heightInPoints = L.hHeader
        createCell(L.A).apply { setCellValue("step time"); cellStyle = st.headerGreenLeft }
        for (c in L.CH_START..L.CH_END) {
            createCell(c).apply { setCellValue(""); cellStyle = st.headerGreenCenter }
        }
        createCell(L.N).apply { setCellValue("Analog1");        cellStyle = st.headerGreenCenter }
        createCell(L.O).apply { setCellValue("Analog2");        cellStyle = st.headerGreenCenter }
        createCell(L.P).apply { setCellValue("Gradient Time");  cellStyle = st.headerGreenCenter }
        createCell(L.Q).apply { setCellValue("text");           cellStyle = st.headerGreenCenter }
    }

    // Rows 29+ : data
    var r = L.ROW_SC_FIRST
    scenarios.forEach { step ->
        val row = sheet.createRow(r++)
        row.heightInPoints = L.hBody

        var col = L.A
        row.createCell(col++).apply { setCellValue(step.stepTimeMs.toDouble()); cellStyle = st.greenCenter }

        // 12 channels (pad/truncate)
        val vals = if (step.channelValues.size >= 12) step.channelValues.take(12)
        else step.channelValues + List(12 - step.channelValues.size) { 0 }
        for (i in 0 until 12) {
            row.createCell(L.CH_START + i).apply {
                setCellValue(vals[i].toDouble()); cellStyle = st.greenCenter
            }
        }

        // N..Q
        row.createCell(L.N).apply { setCellValue(0.0); cellStyle = st.greenCenter } // Analog1
        row.createCell(L.O).apply { setCellValue(0.0); cellStyle = st.greenCenter } // Analog2
        row.createCell(L.P).apply { setCellValue((step.gradientTimeMs ?: 0).toDouble()); cellStyle = st.greenCenter }
        row.createCell(L.Q).apply { setCellValue(step.text ?: ""); cellStyle = st.greenLeft }
    }

    // Save
    File(outPath).parentFile?.mkdirs()
    FileOutputStream(File(outPath)).use { wb.write(it) }
    wb.close()
}

// ---------- Row/Column map (EXACT cells from screenshot) ----------
private object Layout {
    // 0-based indices for POI
    const val A = 0
    const val B = 1
    const val C = 2
    const val D = 3
    const val E = 4
    const val N = 13
    const val O = 14
    const val P = 15
    const val Q = 16

    const val CH_START = B        // B..M for 12 channels
    const val CH_END   = B + 11

    // Rows (0-based)
    const val ROW_TITLE     = 0   // A1
    const val ROW_P_CAPTION = 1   // A2 "Pressures"
    const val ROW_P_HEADER  = 2   // row 3
    const val ROW_P_FIRST   = 3   // rows 4..12 attributes (10 rows)
    const val ROW_P_TILDE   = 12  // row 13 "~"

    const val ROW_S_CAPTION  = 14 // row 15 "Solenoids"
    const val ROW_S_PREAMBLE = 15 // row 16 preamble
    const val ROW_S_FIRST    = 16 // rows 17..23 attributes (9 rows)
    const val ROW_S_TILDE    = 23 // row 24 "~"

    const val ROW_SC_CAPTION = 26 // row 27 "Scenario:"
    const val ROW_SC_HEADER  = 27 // row 28 header
    const val ROW_SC_FIRST   = 28 // row 29 first data row

    // Column widths (Excel chars)
    const val widthLabel   = 18
    const val widthChannel = 7
    const val widthMisc    = 12

    // Row heights
    const val hHeader = 20f
    const val hBody   = 18f

    val SC_MISC_COLS = intArrayOf(N, O, P, Q)
}

// ---------- Writers for blocks ----------
private fun pRow(
    sheet: XSSFSheet,
    rowIndex: Int,
    label: String,
    data: List<PressureChannel>,
    st: Styles,
    valueOf: (PressureChannel) -> String
) {
    val row = sheet.createRow(rowIndex)
    row.heightInPoints = Layout.hBody
    row.createCell(Layout.A).apply { setCellValue(label); cellStyle = st.orangeLeft }
    for (c in Layout.CH_START..Layout.CH_END) {
        val idx = c - Layout.CH_START
        val txt = data.getOrNull(idx)?.let(valueOf).orEmpty()
        row.createCell(c).apply { setCellValue(txt); cellStyle = st.orangeCenter }
    }
}

private fun sRow(
    sheet: XSSFSheet,
    rowIndex: Int,
    label: String,
    data: List<SolenoidChannel>,
    st: Styles,
    valueOf: (SolenoidChannel) -> String
) {
    val row = sheet.createRow(rowIndex)
    row.heightInPoints = Layout.hBody
    row.createCell(Layout.A).apply { setCellValue(label); cellStyle = st.blueLeft }
    for (c in Layout.CH_START..Layout.CH_END) {
        val idx = c - Layout.CH_START
        val txt = data.getOrNull(idx)?.let(valueOf).orEmpty()
        row.createCell(c).apply { setCellValue(txt); cellStyle = st.blueCenter }
    }
}

// ---------- Styles ----------
private class Styles(private val wb: XSSFWorkbook) {
    private val orange = XSSFColor(byteArrayOf(0xF9.toByte(), 0x9B.toByte(), 0x6B.toByte()), null)
    private val blue   = XSSFColor(byteArrayOf(0xB3.toByte(), 0xD1.toByte(), 0xF0.toByte()), null)
    private val green  = XSSFColor(byteArrayOf(0xC7.toByte(), 0xE5.toByte(), 0xB4.toByte()), null)

    val title: XSSFCellStyle = wb.createCellStyle().apply {
        setBordersNone()
        setFont(wb.createFont().apply { bold = true })
        alignment = HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
    }

    val sectionTitle: XSSFCellStyle = wb.createCellStyle().apply {
        setBordersNone()
        setFont(wb.createFont().apply { bold = true })
        alignment = HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
    }

    val headerOrangeLeft: XSSFCellStyle  = headerBase(orange, HorizontalAlignment.LEFT)
    val headerOrangeCenter: XSSFCellStyle= headerBase(orange, HorizontalAlignment.CENTER)
    val headerBlueLeft: XSSFCellStyle    = headerBase(blue, HorizontalAlignment.LEFT)
    val headerBlueCenter: XSSFCellStyle  = headerBase(blue, HorizontalAlignment.CENTER)
    val headerGreenLeft: XSSFCellStyle   = headerBase(green, HorizontalAlignment.LEFT)
    val headerGreenCenter: XSSFCellStyle = headerBase(green, HorizontalAlignment.CENTER)

    val orangeLeft: XSSFCellStyle = bodyBase(orange, HorizontalAlignment.LEFT)
    val orangeCenter: XSSFCellStyle = bodyBase(orange, HorizontalAlignment.CENTER)
    val blueLeft: XSSFCellStyle = bodyBase(blue, HorizontalAlignment.LEFT)
    val blueCenter: XSSFCellStyle = bodyBase(blue, HorizontalAlignment.CENTER)
    val greenLeft: XSSFCellStyle = bodyBase(green, HorizontalAlignment.LEFT)
    val greenCenter: XSSFCellStyle = bodyBase(green, HorizontalAlignment.CENTER)

    private fun headerBase(fill: XSSFColor, align: HorizontalAlignment): XSSFCellStyle =
        wb.createCellStyle().apply {
            setBordersThin()
            setFont(wb.createFont().apply { bold = true })
            alignment = align
            verticalAlignment = VerticalAlignment.CENTER
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFillForegroundColor(fill)
            dataFormat = wb.creationHelper.createDataFormat().getFormat("@")
        }

    private fun bodyBase(fill: XSSFColor, align: HorizontalAlignment): XSSFCellStyle =
        wb.createCellStyle().apply {
            setBordersThin()
            alignment = align
            verticalAlignment = VerticalAlignment.CENTER
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFillForegroundColor(fill)
            dataFormat = wb.creationHelper.createDataFormat().getFormat("@")
        }

    private fun XSSFCellStyle.setBordersThin() {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }
    private fun XSSFCellStyle.setBordersNone() {
        borderTop = BorderStyle.NONE
        borderBottom = BorderStyle.NONE
        borderLeft = BorderStyle.NONE
        borderRight = BorderStyle.NONE
    }
    private fun XSSFCellStyle.setFillForegroundColor(color: XSSFColor) {
        (this as XSSFCellStyle).setFillForegroundColor(color)
    }
}
