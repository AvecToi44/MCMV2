package ru.atrs.mcm.utils

import ru.atrs.mcm.storage.models.ParameterCommon

fun initialize(params: List<ParameterCommon>) {
    logAct("initialize params")

    Dir1Configs.mkdirs()
    Dir2Reports.mkdirs()
    Dir3Scenarios.mkdirs()
    logGarbage("Parsing variables ${params.joinToString()}")
    params.forEachIndexed { index, parameterCommon ->
        when(parameterCommon.name) {
            "comport" -> COM_PORT = parameterCommon.value
            "baudrate" -> BAUD_RATE = parameterCommon.value.toIntOrNull() ?: 115200
            //"is_demo" ->
            "last_operator_id" -> OPERATOR_ID = parameterCommon.value
            "sound_enabled" -> SOUND_ENABLED = parameterCommon.value.toIntOrNull() ?: 1
            "delay_before_chart" -> DELAY_BEFORE_CHART
            "isFullscreenEnabled" -> SHOW_FULLSCREEN = parameterCommon.value == "true"
        }
    }
}
