package ru.atrs.mcm

import junit.framework.TestCase
import org.junit.Test
import ru.atrs.mcm.utils.to2ByteArray
import ru.atrs.mcm.utils.toHexString
import kotlin.test.assertEquals

class Test1 : TestCase() {
    @Test
    fun test1() {
        println("${302.to2ByteArray().toHexString()}")
        assertEquals(expected = byteArrayOf(),actual = 300.to2ByteArray())
    }
}