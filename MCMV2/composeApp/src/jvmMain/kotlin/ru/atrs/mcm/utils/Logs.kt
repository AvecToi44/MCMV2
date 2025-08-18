package ru.atrs.mcm.utils

import ru.atrs.mcm.serial_port.RouterCommunication
import ru.atrs.mcm.storage.writeToFile
import java.time.LocalDateTime

fun getCurrentDateTime() = "${LocalDateTime.now().hour}:${LocalDateTime.now().minute}:${LocalDateTime.now().second} ${System.currentTimeMillis()} ms"


fun logAct(msg: String)     = logAgregator("a~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)
fun logInfo(msg: String)    = logAgregator("i~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)
fun logError(msg: String)   = logAgregator("e~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.ERRORS)
fun logGarbage(msg: String) = logAgregator("g~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)


fun logAgregator(msg: String, logLevel: LogLevel) {
    println(msg)
    if (LOG_LEVEL == logLevel) {
        Dir4MainConfig_LogFolder.mkdirs()
        writeToFile(msg, MainConfig_LogFile)
    }
}

fun healthCheck(specialCall: Boolean = false) {
    val lastLogState = LOG_LEVEL
    if (specialCall && lastLogState == LogLevel.ERRORS) {
        LOG_LEVEL = LogLevel.DEBUG
    }
    logInfo("####################################")
    logInfo("HEALTHCHECK. COM_PORT:${COM_PORT}, ${RouterCommunication.getCOMPortInfo()} ${getCurrentDateTime()}")
    logInfo("HEALTHCHECK. Scenario size: ${scenario.size ?: 0} Pressures size: ${pressures.size ?: 0} Solenoid ${solenoids.size ?: 0}")
    logInfo("HEALTHCHECK. PROTOCOL_TYPE: ${PROTOCOL_TYPE.name}, TWELVE_CHANNELS_MODE:${TWELVE_CHANNELS_MODE}")
    logInfo("HEALTHCHECK. Experiment ${if (isExperimentStarts) "started" else "NO started"} ")
    logInfo("####################################")
    if (specialCall && lastLogState == LogLevel.ERRORS) {
        LOG_LEVEL = LogLevel.ERRORS
    }
}
