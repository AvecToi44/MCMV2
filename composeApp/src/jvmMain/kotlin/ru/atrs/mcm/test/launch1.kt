package ru.atrs.mcm.test

import ru.atrs.mcm.utils.to2ByteArray
import ru.atrs.mcm.utils.toHexString

fun main() {
    test1 {

    }
}

fun test1(x: ()->Unit) {
//    CoroutineScope(Dispatchers.IO).launch {
//        coreParse(byteArrayOf(0xFF.toByte(),0x00,0x00,0x00,0x00))
//    }
    println(255.to2ByteArray().toHexString())
    println(255.to2ByteArray().toHexString())
    println(255.toByte())
    x.invoke()
}