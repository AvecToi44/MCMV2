package ru.atrs.mcm.utils

fun Float.toTwoDecimals(): String = "%.2f".format(this)

fun Float.to5Decimals(): String = "%.5f".format(this).replace(",", ".")