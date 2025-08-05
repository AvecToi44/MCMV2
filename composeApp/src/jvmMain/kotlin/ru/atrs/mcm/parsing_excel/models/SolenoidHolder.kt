package ru.atrs.mcm.parsing_excel.models

data class SolenoidHolder(
    val displayName : String,
    val index : Int,
    val maxPWM : Int,
    val step : Int,

    val frequency : Int,
    val preferredColor : String,
    val expectedTestValue : Int,
    val currentMaxValue : Int,
    var isVisible : Boolean
)
