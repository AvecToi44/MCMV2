package ru.atrs.mcm.parsing_excel.models

data class PressuresHolder(
    val displayName : String,
    val index : Int,
    var minValue : Float,
    var maxValue : Float,
    val tolerance : Int,
    val unit : String,
    val commentString : String,
    val prefferedColor : String,
    var isVisible : Boolean
)
