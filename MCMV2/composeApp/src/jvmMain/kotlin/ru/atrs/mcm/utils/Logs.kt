package ru.atrs.mcm.utils

import ru.atrs.mcm.serial_port.RouterCommunication
import ru.atrs.mcm.storage.writeToFile
import java.time.LocalDateTime

private val CONTROL_TX_COMMANDS = setOf(0x22, 0x54, 0x68, 0x74, 0x78)
private val CONTROL_RX_TYPES = setOf(0x10, 0x11, 0x12)

fun getCurrentDateTime() = "${LocalDateTime.now().hour}:${LocalDateTime.now().minute}:${LocalDateTime.now().second} ${System.currentTimeMillis()} ms"


fun logAct(msg: String)     = logAgregator("a~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)
fun logInfo(msg: String)    = logAgregator("i~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)
fun logError(msg: String)   = logAgregator("e~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.ERRORS)
fun logGarbage(msg: String) = logAgregator("g~>"+msg + " |${getCurrentDateTime()}", logLevel = LogLevel.DEBUG)

fun isSerialLogFullMode(): Boolean = LOG_LEVEL == LogLevel.DEBUG

private fun txCommandName(cmd: Int): String = when (cmd) {
    0x74 -> "START_RX"
    0x54 -> "RESET_COMM"
    0x78 -> "EXP_CONTROL"
    0x68 -> "SET_FREQ"
    0x22 -> "RESUME_AFTER_PAUSE"
    else -> "CMD"
}

private fun rxTypeName(type: Int): String = when (type) {
    0x10 -> "EXPERIMENT_START"
    0x11 -> "EXPERIMENT_END"
    0x12 -> "PAUSE"
    0x01 -> "PRESSURE"
    0x02 -> "CURRENT"
    else -> "UNKNOWN"
}

fun logSerialTx(sendBytes: ByteArray, portName: String, isOpen: Boolean) {
    if (sendBytes.isEmpty()) {
        return
    }

    val cmd = sendBytes[0].toInt() and 0xFF

    if (isSerialLogFullMode()) {
        logAct("TX ${txCommandName(cmd)} 0x${String.format("%02X", cmd)} (${sendBytes.size}B) port=$portName open=$isOpen: ${sendBytes.toHexString()}")
        return
    }

    if (cmd in CONTROL_TX_COMMANDS) {
        logInfo("TX CTRL ${txCommandName(cmd)} 0x${String.format("%02X", cmd)} (${sendBytes.size}B) port=$portName open=$isOpen")
    }
}

fun logSerialRxFrame(type: Int, seq: Int, payloadSize: Int) {
    if (isSerialLogFullMode()) {
        logGarbage("RX ${rxTypeName(type)} 0x${String.format("%02X", type)} seq=$seq payload=${payloadSize}B")
        return
    }

    if (type in CONTROL_RX_TYPES) {
        logInfo("RX CTRL ${rxTypeName(type)} 0x${String.format("%02X", type)} seq=$seq")
    }
}


fun logAgregator(msg: String, logLevel: LogLevel) {
    println(msg)
    if (LOG_LEVEL == logLevel) {
        Dir4MainConfig_LogFolder.mkdirs()
        writeToFile(msg, MainConfig_LogFile)
    }
}

fun healthCheck(specialCall: Boolean = false) {
    return
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
