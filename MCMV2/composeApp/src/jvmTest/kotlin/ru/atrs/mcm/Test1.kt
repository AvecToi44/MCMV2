package ru.atrs.mcm

import androidx.compose.ui.window.application
import junit.framework.TestCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test
import ru.atrs.mcm.storage.NewPointerLine
import ru.atrs.mcm.storage.addNewLineForChart
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.incrementTime
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.to2ByteArray
import ru.atrs.mcm.utils.toHexString
import kotlin.test.assertEquals

class Test1 : TestCase() {
    @Test
    fun test1() {
        println("${302.to2ByteArray().toHexString()}")
        GlobalScope.launch {
            repeat(19) {
                addNewLineForChart(
                    newLine = NewPointerLine(
                        incrementTime = incrementTime,
                        ch1 = 1f,
                        ch2 = 1f,
                        ch3 = 1f,
                        ch4 = 1f,
                        ch5 = 1f,
                        ch6 = 1f,
                        ch7 = 1f,
                        ch8 = 1f,
                        ch9 = 1f ,
                        ch10 =1f,
                        ch11 =1f,
                        ch12 =1f
                    ),
                    isRecordingExperiment = true
                )
            }
        }
        assertEquals(expected = byteArrayOf(),actual = 300.to2ByteArray())


    }

    @Test
    fun test2() {
        repeat(10) {
            STATE_EXPERIMENT.value.msg = "${it}"
        }
        println(STATE_EXPERIMENT.value.msg)
        assertEquals(STATE_EXPERIMENT.value.msg,"10")
    }
}


