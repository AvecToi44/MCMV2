package ru.atrs.mcm.parsing_excel.models

data class ScenarioStep(
    val time: Int,
    val channels: ArrayList<Int>,
    val analog1: Int,
    val analog2: Int,
    val gradientTime: Int,
    val comment: String = "",
)
