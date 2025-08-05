package ru.atrs.mcm.parsing_excel.models

data class ScenarioStep(
    val time: Int,
    val chs: ArrayList<Int>,
    val text: String,
    val comment: String = ""
)
