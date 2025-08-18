package ru.atrs.mcm.storage

import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.DELAY_BEFORE_CHART
import ru.atrs.mcm.utils.Dir1Configs
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir3Scenarios
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.LOG_LEVEL
import ru.atrs.mcm.utils.COMMENT_OF_EXPERIMENT
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import ru.atrs.mcm.utils.SOUND_ENABLED
import ru.atrs.mcm.utils.logAct
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import kotlinx.serialization.Serializable
import ru.atrs.mcm.utils.CHART_FILE_NAME_ENDING
import ru.atrs.mcm.utils.ChartFileNameEnding
import ru.atrs.mcm.utils.GAUGES_IN_THE_ROW
import ru.atrs.mcm.utils.LogLevel
import ru.atrs.mcm.utils.PROTOCOL_TYPE
import ru.atrs.mcm.utils.ProtocolType
import ru.atrs.mcm.utils.SHOW_BOTTOM_PANEL
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import java.io.File

@Serializable
data class ParameterCommon(val name: String, val value: String)

private val configFile = File(Dir1Configs, "config.json")

// Refactored method to parse JSON configuration
fun readParametersJson(): List<ParameterCommon> {
    logAct("readParameters")
    if (!configFile.exists()) {
        refreshJsonParameters() // Create default config if file doesn't exist
    }
    try {
        val jsonText = configFile.readText(Charsets.UTF_8)
        return Json.decodeFromString(ListSerializer(ParameterCommon.serializer()), jsonText)
    } catch (e: Exception) {
        logError("Error parsing JSON: ${e.message}")
        return emptyList() // Return empty list on failure
    }
}

// Updated initialization method
fun initialize(params: List<ParameterCommon>) {
    logAct("initialize params")
    Dir1Configs.mkdirs()
    Dir2Reports.mkdirs()
    Dir3Scenarios.mkdirs()
    logGarbage("Parsing variables ${params.joinToString()}")
    params.forEach { param ->
        when (param.name) {
            "comport" -> COM_PORT = param.value
            "baudrate" -> BAUD_RATE = param.value.toIntOrNull() ?: 115200
            "last_operator_id" -> COMMENT_OF_EXPERIMENT = param.value
            "sound_enabled" -> SOUND_ENABLED = param.value.toIntOrNull() ?: 1
            "last_scenario" -> LAST_SCENARIO = File(param.value)
            "delay_before_chart" -> DELAY_BEFORE_CHART = param.value.toIntOrNull() ?: 0
            "LOG_LEVEL" -> LOG_LEVEL = if (param.value.contains("DEBUG", ignoreCase = true)) LogLevel.DEBUG else LogLevel.ERRORS
            "isFullscreenEnabled" -> SHOW_FULLSCREEN = param.value.toBoolean()
            "isBottomPanelShow" -> SHOW_BOTTOM_PANEL = param.value.toBoolean()
            "is12ChannelsMode" -> TWELVE_CHANNELS_MODE = param.value.toBoolean()
            "GAUGES_IN_THE_ROW" -> GAUGES_IN_THE_ROW = param.value.toInt()
            "protocolType" -> PROTOCOL_TYPE = if(param.value.contains("old",ignoreCase = true)) ProtocolType.OLD_AUG_2025 else ProtocolType.NEW
            "CHART_FILE_NAME_ENDING" -> CHART_FILE_NAME_ENDING =
                if (param.value.contains("${ChartFileNameEnding.COMMENT_AND_TIMESTAMP.name}",ignoreCase = true))
                    ChartFileNameEnding.COMMENT_AND_TIMESTAMP
                else if (param.value.contains("${ChartFileNameEnding.TIMESTAMP.name}",ignoreCase = true)) ChartFileNameEnding.TIMESTAMP
                else ChartFileNameEnding.COMMENT
        }
    }
}

// New method to refresh JSON file from local variables
fun refreshJsonParameters() {
    logAct("refreshParameters")
    Dir1Configs.mkdirs() // Ensure the directory exists
    val params = listOf(
        ParameterCommon("comport", COM_PORT),
        ParameterCommon("baudrate", BAUD_RATE.toString()),
        ParameterCommon("last_operator_id", COMMENT_OF_EXPERIMENT),
        ParameterCommon("sound_enabled", SOUND_ENABLED.toString()),
        ParameterCommon("last_scenario", LAST_SCENARIO?.absolutePath ?: ""),
        ParameterCommon("delay_before_chart", DELAY_BEFORE_CHART.toString()),
        ParameterCommon("LOG_LEVEL", LOG_LEVEL.name.toString()),
        ParameterCommon("isFullscreenEnabled", SHOW_FULLSCREEN.toString()),
        ParameterCommon("isBottomPanelShow", SHOW_BOTTOM_PANEL.toString()),
        ParameterCommon("is12ChannelsMode", TWELVE_CHANNELS_MODE.toString()),
        ParameterCommon("protocolType", PROTOCOL_TYPE.name.toString()),
        ParameterCommon("CHART_FILE_NAME_ENDING", CHART_FILE_NAME_ENDING.name.toString()),
        ParameterCommon("GAUGES_IN_THE_ROW", GAUGES_IN_THE_ROW.toString()),
    )
    try {
        val jsonText = Json.encodeToString(ListSerializer(ParameterCommon.serializer()), params)
        configFile.writeText(jsonText, Charsets.UTF_8)
    } catch (e: Exception) {
        logError("Error writing JSON: ${e.message}")
    }
}