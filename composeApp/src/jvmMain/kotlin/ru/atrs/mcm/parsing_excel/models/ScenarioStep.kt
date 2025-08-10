package ru.atrs.mcm.parsing_excel.models

data class ScenarioStep(
    val time: Int,
    val channels: ArrayList<Int>,
    val comment: String = "",
    val transitionTime: Int
)
