package ru.atrs.mcm.parsing_excel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.*
import ru.atrs.mcm.utils.Dir9Scenario
import ru.atrs.mcm.utils.logGarbage
import java.io.FileInputStream
import java.io.FileOutputStream


fun writeToExcel(row: Int, column: Int, txt: String) {

    CoroutineScope(Dispatchers.Default).launch {
        logGarbage("writeToExcel file: ${Dir9Scenario.name}")
        val file = FileInputStream(Dir9Scenario)

        //val workbook: Workbook = HSSFWorkbook(file)
        // Load existing
        val workbook = WorkbookFactory.create(file);

        val sheet = workbook.getSheetAt(0)
        //val sheet: Sheet = workbook.createSheet("Persons")
        //sheet.setColumnWidth(0, 6000)
        //sheet.setColumnWidth(1, 4000)

        var headerStyle = workbook.createCellStyle()
        //headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.getIndex()
        headerStyle.fillBackgroundColor = IndexedColors.YELLOW.index

        val row: Row = sheet.getRow(row)
        val cell: Cell = row.getCell(column)

        cell.setCellValue(txt)
        cell.setCellStyle(headerStyle)

        //logGarbage("ahahaah ${cell.dateCellValue.toString()}")
        val outputStream = FileOutputStream(Dir9Scenario)
        workbook.write(outputStream)
        workbook.close()
    }

}
