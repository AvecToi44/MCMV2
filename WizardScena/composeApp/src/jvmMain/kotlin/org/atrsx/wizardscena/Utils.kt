package org.atrsx.wizardscena

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

fun generateTimestampLastUpdate() : String{
    //return SimpleDateFormat("HHmmss dd_MM_yyyy").format(Date())
    val dateFormatGmt = SimpleDateFormat("dd_MM_yyyy HH_mm_ss")
    dateFormatGmt.timeZone = TimeZone.getDefault()
    return dateFormatGmt.format(Date())
}