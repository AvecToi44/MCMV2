package ru.atrsx.mcmcomposer

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

// ----------------- Export -----------------

fun MainExperimentConfig.toWorkbook(): XSSFWorkbook {
    val wb = XSSFWorkbook()
    val sheet = wb.createSheet(sheetName)
    val st = ExcelStyles(wb)

    var r = 0

    // Pressures
    r = title(sheet, r, "Pressures", st.titleOrange, 1 + pressures.channels.size)
    r = writePressures(sheet, r, pressures, st)

    r += 1

    // Solenoids
    r = title(sheet, r, "Solenoids", st.titleBlue, 1 + solenoids.channels.size)
    r = writeSolenoids(sheet, r, solenoids, st)

    r += 1

    // Scenario
    val nChannels = solenoids.channels.size.takeIf { it > 0 } ?: pressures.channels.size
    val scenarioCols = 1 + nChannels + 3 + 1
    r = title(sheet, r, "Scenario:", st.titleGreen, scenarioCols)
    r = writeScenario(sheet, r, scenario, nChannels, st)

    repeat(80) { sheet.autoSizeColumn(it) }
    return wb
}

private fun title(sheet: Sheet, start: Int, text: String, style: CellStyle, span: Int): Int {
    val row = sheet.createRow(start)
    row.createCell(0).apply {
        setCellValue(text); cellStyle = style
    }
    sheet.addMergedRegion(CellRangeAddress(start, start, 0, span - 1))
    return start + 1
}

private fun writePressures(sheet: Sheet, start: Int, block: PressuresBlockDto, st: ExcelStyles): Int {
    var r = start
    // header
    sheet.createRow(r).apply {
        createCell(0).apply { setCellValue("DisplayName"); cellStyle = st.header }
        block.channels.forEachIndexed { i, ch ->
            createCell(1 + i).apply { setCellValue(ch.displayName); cellStyle = st.header }
        }
    }
    r++

    fun row(label: String, values: List<Any?>) {
        sheet.createRow(r).apply {
            createCell(0).apply { setCellValue(label); cellStyle = st.sideHeader }
            values.forEachIndexed { i, v -> createCell(1 + i).setAny(v) }
        }; r++
    }

    row("Index", block.channels.map { it.index })
    row("MinValue", block.channels.map { it.minValue })
    row("MaxValue", block.channels.map { it.maxValue })
    row("Tolerance", block.channels.map { it.tolerance })
    row("Unit", block.channels.map { it.unit })
    row("CommentString", block.channels.map { it.comment })
    row("PreferredColor", block.channels.map { it.preferredColorHex })
    row("isVisible", block.channels.map { it.isVisible })
//    row("parameters", block.channels.map { if (it.parameters.isEmpty()) "" else it.parameters.entries.joinToString { e -> "${e.key}=${e.value}" } })
    return r
}

private fun writeSolenoids(sheet: Sheet, start: Int, block: SolenoidsBlock, st: ExcelStyles): Int {
    var r = start

    // meta line
    if (block.mainFrequencyHz != null || block.testVariable != null) {
        sheet.createRow(r).apply {
            var c = 0
            if (block.mainFrequencyHz != null) {
                createCell(c++).apply { setCellValue("Main Freq:"); cellStyle = st.sideHeader }
                createCell(c++).setAny(block.mainFrequencyHz)
            }
            if (block.testVariable != null) {
                if (c > 0) c++
                createCell(c++).apply { setCellValue("TestVariable:"); cellStyle = st.sideHeader }
                createCell(c).setAny(block.testVariable)
            }
        }
        r++
    }

    // header
    sheet.createRow(r).apply {
        createCell(0).apply { setCellValue("DisplayName"); cellStyle = st.header }
        block.channels.forEachIndexed { i, ch ->
            createCell(1 + i).apply { setCellValue(ch.displayName); cellStyle = st.header }
        }
    }
    r++

    fun row(label: String, values: List<Any?>) {
        sheet.createRow(r).apply {
            createCell(0).apply { setCellValue(label); cellStyle = st.sideHeader }
            values.forEachIndexed { i, v -> createCell(1 + i).setAny(v) }
        }; r++
    }

    row("Index",             block.channels.map { it.index })
    row("MaxPWM [0 - 255]",  block.channels.map { it.maxPwm0_255 })
    row("Value of division", block.channels.map { it.valueOfDivision })
    row("tenth amplitude",   block.channels.map { it.tenthAmplitude })
    row("tenth frequency",   block.channels.map { it.tenthFrequency })
    row("MinValue",          block.channels.map { it.minValue })
    row("MaxValue",          block.channels.map { it.maxValue })
    row("isVisible",         block.channels.map { it.isVisible })
    return r
}

private fun writeScenario(sheet: Sheet, start: Int, block: ScenarioBlockDto, nChannels: Int, st: ExcelStyles): Int {
    var r = start

    // header
    sheet.createRow(r).apply {
        createCell(0).apply { setCellValue("step time"); cellStyle = st.header }
        repeat(nChannels) { i -> createCell(1 + i).apply { setCellValue("ch ${i+1}"); cellStyle = st.header } }
        val base = 1 + nChannels
        createCell(base + 0).apply { setCellValue("Analog1");       cellStyle = st.header }
        createCell(base + 1).apply { setCellValue("Analog2");       cellStyle = st.header }
        createCell(base + 2).apply { setCellValue("Gradient Time"); cellStyle = st.header }
        createCell(base + 3).apply { setCellValue("text");          cellStyle = st.header }
    }
    r++

    block.steps.forEach { s ->
        sheet.createRow(r).apply {
            createCell(0).setAny(s.stepTimeMs)
            s.channelValues.forEachIndexed { i, v -> createCell(1 + i).setAny(v) }
            val base = 1 + nChannels
            createCell(base + 0).setAny(s.analog1)
            createCell(base + 1).setAny(s.analog2)
            createCell(base + 2).setAny(s.gradientTimeMs)
            createCell(base + 3).setAny(s.text)
        }
        r++
    }
    return r
}

private fun Cell.setAny(v: Any?) {
    when (v) {
        null -> setBlank()
        is Boolean -> setCellValue(if (v) "true" else "false")
        is Int -> setCellValue(v.toDouble())
        is Long -> setCellValue(v.toDouble())
        is Number -> setCellValue(v.toDouble())
        else -> setCellValue(v.toString())
    }
}

// Simple styles
private class ExcelStyles(wb: XSSFWorkbook) {
    val header: CellStyle = wb.createCellStyle().apply {
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
        setBorders(this)
        wb.createFont().apply { bold = true }.also { setFont(it) }
    }
    val sideHeader: CellStyle = wb.createCellStyle().apply {
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFillForegroundColor(IndexedColors.LEMON_CHIFFON.index)
        setBorders(this)
    }
    val titleOrange: CellStyle = wb.createCellStyle().apply {
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFillForegroundColor(IndexedColors.LIGHT_ORANGE.index)
        setBorders(this)
        wb.createFont().apply { bold = true }.also { setFont(it) }
    }
    val titleBlue: CellStyle = wb.createCellStyle().apply {
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFillForegroundColor(IndexedColors.PALE_BLUE.index)
        setBorders(this)
        wb.createFont().apply { bold = true }.also { setFont(it) }
    }
    val titleGreen: CellStyle = wb.createCellStyle().apply {
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFillForegroundColor(IndexedColors.LIGHT_GREEN.index)
        setBorders(this)
        wb.createFont().apply { bold = true }.also { setFont(it) }
    }
    private fun setBorders(cs: CellStyle) {
        cs.borderTop = BorderStyle.THIN; cs.borderBottom = BorderStyle.THIN
        cs.borderLeft = BorderStyle.THIN; cs.borderRight = BorderStyle.THIN
    }
}

// ----------------- Import (expects same structure as exporter) -----------------

fun readSheetModelFromXlsx(file: File): MainExperimentConfig {
    XSSFWorkbook(file).use { wb ->
        val sheet = wb.getSheetAt(0)

        fun findRowIdx(text: String): Int? {
            for (r in 0..sheet.lastRowNum) {
                val c0 = sheet.getRow(r)?.getCell(0)?.stringCellValue?.trim() ?: continue
                if (c0 == text) return r
            }
            return null
        }

        // --- Pressures ---
        val pStart = findRowIdx("Pressures") ?: 0
        val pHeader = sheet.getRow(pStart + 1)
        val pCols = pHeader.physicalNumberOfCells - 1
        val pressures = PressuresBlockDto(
            channels = MutableList(pCols) { i ->
                PressureChannelDto(
                    displayName = pHeader.getCell(1 + i).stringCellValue,
                    index = sheet.getRow(pStart + 2).getCell(1 + i).numericCellValue.toInt(),
                    minValue = sheet.getRow(pStart + 3).getCell(1 + i).numericCellValue.toInt(),
                    maxValue = sheet.getRow(pStart + 4).getCell(1 + i).numericCellValue.toInt(),
                    tolerance = sheet.getRow(pStart + 5).getCell(1 + i).numericCellValue.toInt(),
                    unit = sheet.getRow(pStart + 6).getCell(1 + i).stringCellValue,
                    comment = sheet.getRow(pStart + 7).getCell(1 + i).stringCellValue,
                    preferredColorHex = sheet.getRow(pStart + 8).getCell(1 + i).stringCellValue,
                    isVisible = sheet.getRow(pStart + 9).getCell(1 + i).toString() == "true",
//                    parameters = mutableMapOf()
                )
            }
        )

        // --- Solenoids ---
        val sStart = findRowIdx("Solenoids") ?: (pStart + 20)
        var cursor = sStart + 1
        var mainFreq: Int? = null
        var testVar: Int? = null

        sheet.getRow(cursor)?.let { row ->
            // If the first cell is "Main Freq:" then parse meta row
            if (row.getCell(0)?.stringCellValue == "Main Freq:") {
                mainFreq = row.getCell(1)?.numericCellValue?.toInt()
                val maybeLabel = row.getCell(3)?.stringCellValue
                if (maybeLabel == "TestVariable:") {
                    testVar = row.getCell(4)?.numericCellValue?.toInt()
                }
                cursor++
            }
        }

        val sHeader = sheet.getRow(cursor)
        val sCols = sHeader.physicalNumberOfCells - 1
        val solenoids = SolenoidsBlock(
            mainFrequencyHz = mainFreq, testVariable = testVar,
            channels = MutableList(sCols) { i ->
                SolenoidChannel(
                    displayName = sHeader.getCell(1 + i).stringCellValue,
                    index = sheet.getRow(cursor + 1).getCell(1 + i).numericCellValue.toInt(),
                    maxPwm0_255 = sheet.getRow(cursor + 2).getCell(1 + i).numericCellValue.toInt(),
                    valueOfDivision = sheet.getRow(cursor + 3).getCell(1 + i).numericCellValue.toInt(),
                    tenthAmplitude = sheet.getRow(cursor + 4).getCell(1 + i).numericCellValue.toInt(),
                    tenthFrequency = sheet.getRow(cursor + 5).getCell(1 + i).numericCellValue.toInt(),
                    minValue = sheet.getRow(cursor + 6).getCell(1 + i).numericCellValue.toInt(),
                    maxValue = sheet.getRow(cursor + 7).getCell(1 + i).numericCellValue.toInt(),
                    isVisible = sheet.getRow(cursor + 8).getCell(1 + i).toString() == "true"
                )
            }
        )

        // --- Scenario ---
        val scStart = findRowIdx("Scenario:") ?: (sStart + 20)
        val scHeader = sheet.getRow(scStart + 1)
        val nChannels = (scHeader.physicalNumberOfCells - 1 - 4).coerceAtLeast(0) // step + N + 4 tails
        val scenarioSteps = mutableListOf<ScenarioStepDto>()
        var rr = scStart + 2
        while (true) {
            val row = sheet.getRow(rr) ?: break
            val first = row.getCell(0) ?: break
            if (first.cellType == CellType.BLANK) break
            val step = first.numericCellValue.toInt()
            val ch = MutableList(nChannels) { i -> row.getCell(1 + i)?.numericCellValue?.toInt() ?: 0 }
            val base = 1 + nChannels
            val a1 = row.getCell(base + 0)?.numericCellValue?.toInt()
            val a2 = row.getCell(base + 1)?.numericCellValue?.toInt()
            val grad = row.getCell(base + 2)?.numericCellValue?.toInt()
            val txt = row.getCell(base + 3)?.toString()
            scenarioSteps += ScenarioStepDto(step, ch, a1, a2, grad, txt)
            rr++
        }

        return MainExperimentConfig(
            standardPath = "PPP",
            pressures = pressures,
            solenoids = solenoids,
            scenario = ScenarioBlockDto(mainFrequency = 1500, steps = scenarioSteps)
        )
    }
}
