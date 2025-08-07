package ru.atrs.mcm.utils

import com.fazecast.jSerialComm.SerialPort
import kotlin.experimental.and

fun getComPorts_JustString() : String{
    var comports = SerialPort.getCommPorts()
    var output_comport = ""

    for (i in comports) {
        output_comport+= " ${i.systemPortName},"

    }
//    if (comports.isNotEmpty()) {
//        COM_PORT = comports[0].systemPortName
//    }
    return output_comport
}

fun getComPorts_Array(): Array<out SerialPort>? = SerialPort.getCommPorts()

fun onesAndTensFloat(onesRAW : UInt, tensRAW : UInt) : Float {
    val ones = onesRAW//.toInt()
    val tens = tensRAW//.toInt()

    //println("${ones.toInt()} ${tens}")

    return if (tens == 0u) {
        //println("ones.toInt()")
        ones.toFloat()
    } else {
        //println("( ones + tens * 256u ).toInt()")
        ( ones + tens * 256u ).toFloat()
    }
}

fun onesAndTens(onesRAW : UInt, tensRAW : UInt) : Int {
    val ones = onesRAW//.toInt()
    val tens = tensRAW//.toInt()

    //println("${ones.toInt()} ${tens}")

    return if (tens == 0u) {
        //println("ones.toInt()")
        ones.toInt()
    } else {
        //println("( ones + tens * 256u ).toInt()")
        ( ones + tens * 256u ).toInt()
    }
}


fun main() {

    var bas = byteArrayOf(0xFF.toByte(), 0xFD.toByte())
    //var a = BigInteger(bas[0])

    println("00: $ ${bas[1].toInt()}")
    println("0${bas[0].toInt()} ${bas[1].toInt()}")
    println("1${bas[0].toUInt()} ${bas[1].toUInt()}")
    println("2<><> ${byteToInt(bas[0])} ${byteToInt(bas[1])}")


    println("3${0xFF} ${0xFD.toUInt()}")

    println("4> ${onesAndTens((0xFF.toUInt()),0u)}")

    println("5> ${onesAndTens(0xFFu,0x0Fu)}")
    println("6> ${onesAndTens(0xFD.toUInt(),0x0F.toUInt())}")
    println("7>>>> ${onesAndTens(0xFE.toUInt(), 0x0F.toUInt())}")

    println("8> ${onesAndTens(0x00u,0x0Fu)}")
    println("9> ${onesAndTens(0xFFu,0x01u)}")
    println("10> ${onesAndTens(0x00u,0x01u)}")
}

fun bytearray2intarray(barray: ByteArray): IntArray? {
    val iarray = IntArray(barray.size)
    var i = 0
    for (b in barray) iarray[i++] = (b and 0xff.toByte()).toInt()
    return iarray
}

/**
 * converts a byte (treated as an unsigned 8 bits) to an integer, If the
 * integer is greater than 255, then the higher order bits will be chopped
 * off
 */
fun byteToInt(b: Byte): Int {
    return if (b >= 0) b.toInt() else b + 256
}