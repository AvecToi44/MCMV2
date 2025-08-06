package ru.atrs.mcm.utils

import ru.atrs.mcm.storage.writeToFile
import java.time.LocalDateTime

val currentDateTime = "${LocalDateTime.now().hour}:${LocalDateTime.now().minute}:${LocalDateTime.now().second} ${System.currentTimeMillis()}"


fun logAct(msg: String)     = logAgregator("a~>"+msg + " ${currentDateTime}", logLevel = LogLevel.DEBUG)
fun logInfo(msg: String)    = logAgregator("i~>"+msg + " ${currentDateTime}", logLevel = LogLevel.DEBUG)
fun logError(msg: String)   = logAgregator("e~>"+msg + " ${currentDateTime}", logLevel = LogLevel.ERRORS)
fun logGarbage(msg: String) = logAgregator("g~>"+msg + " ${currentDateTime}", logLevel = LogLevel.DEBUG)


fun logAgregator(msg: String, logLevel: LogLevel) {
    println(msg)

    if (LOG_LEVEL == logLevel) {
        writeToFile(msg, Dir4MainConfig_Log)
    }
}
