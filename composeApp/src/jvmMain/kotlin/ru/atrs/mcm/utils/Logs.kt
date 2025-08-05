package ru.atrs.mcm.utils

import ru.atrs.mcm.storage.writeToFile

fun logAct(msg: String)     = logAgregator("a~>"+msg)
fun logInfo(msg: String)    = println("i~>"+msg)
fun logError(msg: String)   = println("e~>"+msg)
fun logGarbage(msg: String) = println("g~>"+msg)


fun logAgregator(msg: String) {
    println(msg)
    if (SAVELOG) {
        writeToFile(msg, Dir4MainConfig_Log)
    }
}
