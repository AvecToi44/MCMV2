package ru.atrs.mcm.utils

import java.math.RoundingMode
import java.text.DecimalFormat

fun rndTo2deci(num : Float) : Float{
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.DOWN

    return (df.format(num)).replace(",",".").toFloat()

}


fun Int.to2ByteArray() : ByteArray = byteArrayOf(toByte(), shr(8).toByte())


fun map(x: Int, in_min: Int, in_max: Int, out_min: Int, out_max: Int): Int {
    //println("fun map ($x - $in_min) * ($out_max - $out_min) / ($in_max - $in_min) + $out_min")
    var stable_X = x

    if (x < in_min) {
        stable_X = in_min
    }
    if (x > in_max) {
        stable_X = in_max
    }

    return (stable_X - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;

}