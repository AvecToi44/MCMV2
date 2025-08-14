package ru.atrs.mcm.utils

import ru.atrs.mcm.storage.writeToFile
import java.time.LocalDateTime

fun getCurrentDateTime() = "${LocalDateTime.now().hour}:${LocalDateTime.now().minute}:${LocalDateTime.now().second} ${System.currentTimeMillis()}"


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
