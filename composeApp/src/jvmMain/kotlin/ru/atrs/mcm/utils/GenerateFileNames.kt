package ru.atrs.mcm.utils

import java.text.SimpleDateFormat
import java.util.*

fun generateChartShapshoot() = generateTimestampLastUpdate()+" ${OPERATOR_ID}"

fun generateTimestampLastUpdate() : String{
    //return SimpleDateFormat("HHmmss dd_MM_yyyy").format(Date())
    val dateFormatGmt = SimpleDateFormat("dd_MM_yyyy HH_mm_ss")
    dateFormatGmt.timeZone = TimeZone.getDefault()
    return dateFormatGmt.format(Date())
}