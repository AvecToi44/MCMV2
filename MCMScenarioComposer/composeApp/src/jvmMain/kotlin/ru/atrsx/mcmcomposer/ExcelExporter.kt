import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import ru.atrsx.mcmcomposer.PressureChannel
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.SolenoidChannel
import java.io.File
import java.io.FileOutputStream
import java.util.*

// ===== Public API =============================================================

/**
 * Exports a workbook with 3 stacked blocks on a single sheet:
 *   1) Pressures (orange)
 *   2) Solenoids (blue) + preamble row (Main Freq / TestVariable)
 *   3) Scenario (green) growing down
 *
 * Layout is transposed: attributes in rows, channels as columns — exactly like the screenshot.
 *
 * @param titleText    optional title in A1 (e.g., source file name)
 * @param sheetName    worksheet name
 * @param mainFreq     value shown in solenoids preamble row after "Main Freq:"
 * @param testVar      value shown after "TestVariable:"
 */
fun exportExperimentToExcelExact(
    outPath: String,
    titleText: String? = null,
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

    // --- sheet geometry (tuned for screenshot proportions) ---
    val pLabelColWidth = 18      // Pressures first column (labels)
    val sLabelColWidth = 20      // Solenoids first column (labels)
    val scLabelColWidth = 14     // Scenario first column ("step time")
    val chanColWidth    = 7      // width for each channel column
    val miscColWidth    = 12     // width for analog/gradient/text
    val headerH         = 20f
    val bodyH           = 18f

    var row = 0

    // A1: source file name/title (no fill)
    if (!titleText.isNullOrBlank()) {
        val r = sheet.createRow(row++)
        r.heightInPoints = headerH
        r.createCell(0).apply {
            setCellValue(titleText)
            cellStyle = st.title
        }
    }

    // ================= Pressures (orange) ====================================
    // Section title
    sheet.createRow(row).apply {
        heightInPoints = headerH
        createCell(0).apply { setCellValue("Pressures"); cellStyle = st.sectionTitle }
    }
    row++

    // Column widths (label + N channels)
    val pCols = 1 + pressures.size
    sheet.setColumnWidth(0, pLabelColWidth * 256)
    for (c in 1 until pCols) sheet.setColumnWidth(c, chanColWidth * 256)

    // Header row (top labels for each channel column)
    run {
        val r = sheet.createRow(row++)
        r.heightInPoints = headerH
        // corner cell: empty (orange fill)
        r.createCell(0).apply { setCellValue(""); cellStyle = st.headerOrange }
        pressures.forEachIndexed { i, ch ->
            r.createCell(i + 1).apply {
                // like "Channel Data 1/2..." or use displayName if you prefer
                setCellValue("Channel Data ${ch.index + 1}")
                cellStyle = st.headerOrange
            }
        }
    }

    // Rows (attributes down; channels across)
    fun pRow(label: String, valueOf: (PressureChannel) -> String) {
        val r = sheet.createRow(row++)
        r.heightInPoints = bodyH
        r.createCell(0).apply { setCellValue(label); cellStyle = st.orangeLeft }
        pressures.forEachIndexed { i, ch ->
            r.createCell(i + 1).apply {
                setCellValue(valueOf(ch))
                cellStyle = st.orangeCenter
            }
        }
    }

    pRow("DisplayName")   { it.displayName }
    pRow("Index")         { it.index.toString() }
    pRow("MinValue")      { it.minValue.toString() }
    pRow("MaxValue")      { it.maxValue.toString() }
    pRow("Tolerance")     { it.tolerance.toString() }
    pRow("Unit")          { it.unit }
    pRow("CommentString") { it.comment }
    pRow("PreferredColor"){ it.preferredColorHex.uppercase(Locale.US) }
    pRow("isVisible")     { it.isVisible.toString() }
    pRow("parameters")    { "" }

    // "~" separator row (single cell like on screenshot)
    sheet.createRow(row++).apply {
        heightInPoints = bodyH
        createCell(0).apply { setCellValue("~"); cellStyle = st.orangeLeft }
    }

    // Blank spacer row (as in screenshot visual rhythm)
    row++

    // ================= Solenoids (blue) ======================================
    // Section title
    sheet.createRow(row).apply {
        heightInPoints = headerH
        createCell(0).apply { setCellValue("Solenoids"); cellStyle = st.sectionTitle }
    }
    row++

    val sCols = 1 + solenoids.size
    sheet.setColumnWidth(0, sLabelColWidth * 256)
    for (c in 1 until sCols) sheet.setColumnWidth(c, chanColWidth * 256)

    // Preamble row: blue fill with "Main Freq:"  value   "TestVariable:"  value
    run {
        val r = sheet.createRow(row++)
        r.heightInPoints = headerH
        // Left-most blue filler
        r.createCell(0).apply { setCellValue(""); cellStyle = st.headerBlue }
        // Main Freq pair
        r.createCell(1).apply { setCellValue("Main Freq:"); cellStyle = st.headerBlue }
        r.createCell(2).apply { setCellValue(mainFreq.toDouble()); cellStyle = st.headerBlueCenter }
        // TestVariable pair (placed a few cells to the right, matches screenshot vibe)
        r.createCell(3).apply { setCellValue("TestVariable:"); cellStyle = st.headerBlue }
        r.createCell(4).apply { setCellValue(testVar.toDouble()); cellStyle = st.headerBlueCenter }
    }

    // Row writer (blue)
    fun sRow(label: String, valueOf: (SolenoidChannel) -> String) {
        val r = sheet.createRow(row++)
        r.heightInPoints = bodyH
        r.createCell(0).apply { setCellValue(label); cellStyle = st.blueLeft }
        solenoids.forEachIndexed { i, ch ->
            r.createCell(i + 1).apply {
                setCellValue(valueOf(ch))
                cellStyle = st.blueCenter
            }
        }
    }

    sRow("DisplayName")          { it.displayName }
    sRow("Index")                { it.index.toString() }
    sRow("MaxPWM [0 - 255]")     { it.maxPwm0_255.toString() }
    sRow("Value of division")    { it.valueOfDivision.toString() }
    sRow("tenth amplitude")      { it.tenthAmplitude.toString() }
    sRow("tenth frequency")      { it.tenthFrequency.toString() }
    sRow("MinValue")             { it.minValue.toString() }
    sRow("MaxValue")             { it.maxValue.toString() }
    sRow("isVisible")            { it.isVisible.toString() }
    // "~" separator
    sheet.createRow(row++).apply {
        heightInPoints = bodyH
        createCell(0).apply { setCellValue("~"); cellStyle = st.blueLeft }
    }

    // Blank spacer row
    row++

    // ================= Scenario (green) ======================================
    // Title
    sheet.createRow(row).apply {
        heightInPoints = headerH
        createCell(0).apply { setCellValue("Scenario:"); cellStyle = st.sectionTitle }
    }
    row++

    // Column widths: first (label), 12 channels, then 4 misc columns
    val scStartCol = 0
    sheet.setColumnWidth(scStartCol, scLabelColWidth * 256)
    for (c in 1..12) sheet.setColumnWidth(scStartCol + c, chanColWidth * 256)
    val miscNames = listOf("Analog1", "Analog2", "Gradient Time", "text")
    for (i in miscNames.indices) sheet.setColumnWidth(scStartCol + 13 + i, miscColWidth * 256)

    // Header row (like screenshot: "step time" on first column, channel headers empty),
    // but we do include right-most labels.
    run {
        val r = sheet.createRow(row++)
        r.heightInPoints = headerH
        r.createCell(scStartCol + 0).apply { setCellValue("step time"); cellStyle = st.headerGreenLeft }
        for (c in 1..12) {
            r.createCell(scStartCol + c).apply { setCellValue(""); cellStyle = st.headerGreenCenter }
        }
        miscNames.forEachIndexed { idx, name ->
            r.createCell(scStartCol + 13 + idx).apply { setCellValue(name); cellStyle = st.headerGreenCenter }
        }
    }

    // Scenario rows
    scenarios.forEach { step ->
        val r = sheet.createRow(row++)
        r.heightInPoints = bodyH

        var col = scStartCol
        r.createCell(col++).apply { setCellValue(step.stepTimeMs.toDouble()); cellStyle = st.greenCenter }

        // 12 channel values (pad/truncate)
        val vals = if (step.channelValues.size >= 12) step.channelValues.take(12)
        else step.channelValues + List(12 - step.channelValues.size) { 0 }
        vals.forEach { v ->
            r.createCell(col++).apply { setCellValue(v.toDouble()); cellStyle = st.greenCenter }
        }

        // Analog1/2 (not provided -> 0), Gradient time, text
        r.createCell(col++).apply { setCellValue(0.0); cellStyle = st.greenCenter } // Analog1
        r.createCell(col++).apply { setCellValue(0.0); cellStyle = st.greenCenter } // Analog2
        r.createCell(col++).apply { setCellValue((step.gradientTimeMs ?: 0).toDouble()); cellStyle = st.greenCenter }
        r.createCell(col).apply    { setCellValue(step.text ?: ""); cellStyle = st.greenLeft }
    }

    // --- Save ---
    File(outPath).parentFile?.mkdirs()
    FileOutputStream(File(outPath)).use { wb.write(it) }
    wb.close()
}

// ===== Styles ================================================================

private class Styles(private val wb: XSSFWorkbook) {
    // Colors (close to screenshot)
    private val orange = XSSFColor(byteArrayOf(0xF9.toByte(), 0x9B.toByte(), 0x6B.toByte()), null) // soft orange
    private val blue   = XSSFColor(byteArrayOf(0xB3.toByte(), 0xD1.toByte(), 0xF0.toByte()), null) // light blue
    private val green  = XSSFColor(byteArrayOf(0xC7.toByte(), 0xE5.toByte(), 0xB4.toByte()), null) // light green

    val title: XSSFCellStyle = wb.createCellStyle().apply {
        setBordersNone()
        val f = wb.createFont().apply { bold = true }
        setFont(f)
    }

    val sectionTitle: XSSFCellStyle = wb.createCellStyle().apply {
        setBordersNone()
        val f = wb.createFont().apply { bold = true }
        setFont(f)
        alignment = HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
    }

    // Headers
    val headerOrange: XSSFCellStyle = wb.createCellStyle().apply {
        baseHeader()
        setFillForegroundColor(orange)
    }
    val headerBlue: XSSFCellStyle = wb.createCellStyle().apply {
        baseHeader()
        setFillForegroundColor(blue)
    }
    val headerBlueCenter: XSSFCellStyle = wb.createCellStyle().apply {
        baseHeader(center = true)
        setFillForegroundColor(blue)
    }
    val headerGreenLeft: XSSFCellStyle = wb.createCellStyle().apply {
        baseHeader()
        setFillForegroundColor(green)
    }
    val headerGreenCenter: XSSFCellStyle = wb.createCellStyle().apply {
        baseHeader(center = true)
        setFillForegroundColor(green)
    }

    // Body fills
    val orangeLeft: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(left = true)
        setFillForegroundColor(orange)
    }
    val orangeCenter: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(center = true)
        setFillForegroundColor(orange)
    }

    val blueLeft: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(left = true)
        setFillForegroundColor(blue)
    }
    val blueCenter: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(center = true)
        setFillForegroundColor(blue)
    }

    val greenLeft: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(left = true)
        setFillForegroundColor(green)
    }
    val greenCenter: XSSFCellStyle = wb.createCellStyle().apply {
        baseBody(center = true)
        setFillForegroundColor(green)
    }

    // --- helpers ---
    private fun XSSFCellStyle.baseHeader(center: Boolean = false) {
        setBordersThin()
        alignment = if (center) HorizontalAlignment.CENTER else HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
        fillPattern = FillPatternType.SOLID_FOREGROUND
        val f = wb.createFont().apply { bold = true }
        setFont(f)
        dataFormat = wb.creationHelper.createDataFormat().getFormat("@")
    }

    private fun XSSFCellStyle.baseBody(left: Boolean = false, center: Boolean = false) {
        setBordersThin()
        alignment = when {
            center -> HorizontalAlignment.CENTER
            left   -> HorizontalAlignment.LEFT
            else   -> HorizontalAlignment.GENERAL
        }
        verticalAlignment = VerticalAlignment.CENTER
        fillPattern = FillPatternType.SOLID_FOREGROUND
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

    // overload: set XSSFColor as foreground
    private fun XSSFCellStyle.setFillForegroundColor(color: XSSFColor) {
        (this as XSSFCellStyle).setFillForegroundColor(color)
    }
}
