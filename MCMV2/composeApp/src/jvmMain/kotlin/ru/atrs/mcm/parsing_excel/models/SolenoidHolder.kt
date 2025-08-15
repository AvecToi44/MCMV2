package ru.atrs.mcm.parsing_excel.models

data class SolenoidHolder(
    val displayName : String,
    val index : Int,
    val maxPWM : Int,
    val step : Int,

    val ditherFrequency : Int,
    val ditherAmplitude : String,
    val currentMinValue : Int,
    val currentMaxValue : Int,
    var isVisible : Boolean
)
