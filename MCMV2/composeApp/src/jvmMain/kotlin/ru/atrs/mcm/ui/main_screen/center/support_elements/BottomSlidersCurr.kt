package ru.atrs.mcm.ui.main_screen.center.support_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.atrs.mcm.utils.DELAY_FOR_GET_DATA
import ru.atrs.mcm.utils.dataChunkCurrents
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.solenoids


@Composable
fun SolenoidsPanel(
    modifier: Modifier = Modifier,
    sizeRow: Size,
    duration: MutableStateFlow<Long>
) {
    val crctx = rememberCoroutineScope().coroutineContext

    var current1 by remember { mutableStateOf(-1) }
    var current2 by remember { mutableStateOf(-1) }
    var current3 by remember { mutableStateOf(-1) }
    var current4 by remember { mutableStateOf(-1) }
    var current5 by remember { mutableStateOf(-1) }
    var current6 by remember { mutableStateOf(-1) }
    var current7 by remember { mutableStateOf(-1) }
    var current8 by remember { mutableStateOf(-1) }

    var current9 by  remember { mutableStateOf(-1) }
    var current10 by remember { mutableStateOf(-1) }
    var current11 by remember { mutableStateOf(-1) }
    var current12 by remember { mutableStateOf(-1) }

    //var internalIndexOfScenario = remember { indexOfScenario }
//    var pwm1 by remember { pwm1SeekBar }
//    var pwm2 by remember { pwm2SeekBar }
//    var pwm3 by remember { pwm3SeekBar }
//    var pwm4 by remember { pwm4SeekBar }
//
//    var pwm5 by remember { mutableStateOf(-1) }
//    var pwm6 by remember { mutableStateOf(-1) }
//    var pwm7 by remember { mutableStateOf(-1) }
//    var pwm8 by remember { mutableStateOf(-1) }

//    if (solenoids.size<5){
//        showMeSnackBar("Excel error",Color.Red)
//    }else {
//        showMeSnackBar("Excel config parse success",Color.White)
//    }
    LaunchedEffect(true) {
        CoroutineScope(Dispatchers.IO+crctx).launch {
            dataChunkCurrents.collect {
                delay(DELAY_FOR_GET_DATA)

                current1 = it.firstCurrentData
                current2 = it.secondCurrentData
                current3 = it.thirdCurrentData
                current4 = it.fourthCurrentData

                current5 = it.fifthCurrentData
                current6 = it.sixthCurrentData
                current7 = it.seventhCurrentData
                current8 = it.eighthCurrentData

                it.fifthCurrentData?.let { current9 = it  }
                it.sixthCurrentData?.let { current10 = it  }
                it.seventhCurrentData?.let { current11 = it  }
                it.eighthCurrentData?.let { current12 = it  }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
        if (solenoids[0].isVisible) {
            SolenoidControl(
                index = 1,
                solenoids[0].displayName,
                current = map(
                    x = current1,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[0].maxPWM,
                step =   solenoids[0].step,
                duration = duration
            )
        }
        if (solenoids[1].isVisible) {
            SolenoidControl(
                index = 2,
                solenoids[1].displayName,
                current = map(
                    x = current2,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[1].currentMaxValue
                ),
                maxPWM = solenoids[1].maxPWM,
                step = solenoids[1].step,
                duration = duration
            )
        }
        if (solenoids[2].isVisible) {
            SolenoidControl(
                index = 3,
                solenoids[2].displayName,
                current = map(
                    x = current3,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[2].currentMaxValue
                ),
                maxPWM = solenoids[2].maxPWM,
                step = solenoids[2].step,
                duration = duration
            )
        }
        if (solenoids[3].isVisible) {
            SolenoidControl(
                index = 4,
                solenoids[3].displayName,
                current = map(
                    x = current4,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[3].currentMaxValue
                ),
                maxPWM = solenoids[3].maxPWM,
                step = solenoids[3].step,
                duration = duration
            )
        }
        /////
        if(solenoids[4].isVisible) {
            SolenoidControl(
                index = 5,
                solenoids[4].displayName,
                current = map(
                    x = current5,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[4].maxPWM,
                step = solenoids[4].step,
                duration = duration
            )
        }
        if (solenoids[5].isVisible) {
            SolenoidControl(
                index = 6,
                solenoids[5].displayName,
                current = map(
                    x = current6,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[5].maxPWM,
                step = solenoids[5].step,
                duration = duration
            )

        }
        if (solenoids[6].isVisible) {
            SolenoidControl(
                index = 7,
                solenoids[6].displayName,
                current = map(
                    x = current7,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[6].maxPWM,
                step = solenoids[6].step,
                duration = duration
            )
        }
        if (solenoids[7].isVisible) {
            SolenoidControl(
                index = 8,
                solenoids[7].displayName,
                current = map(
                    x = current8,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[7].maxPWM,
                step = solenoids[7].step,
                duration = duration
            )
        }
        if (solenoids[8].isVisible) {
            SolenoidControl(
                index = 9,
                solenoids[8].displayName,
                current = map(
                    x = current9,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[8].maxPWM,
                step = solenoids[8].step,
                duration = duration
            )
        }
        if (solenoids[9].isVisible) {
            SolenoidControl(
                index = 10,
                solenoids[9].displayName,
                current = map(
                    x = current10,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[9].maxPWM,
                step = solenoids[9].step,
                duration = duration
            )
        }
        if (solenoids[10].isVisible) {
            SolenoidControl(
                index = 11,
                solenoids[10].displayName,
                current = map(
                    x = current11,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[10].maxPWM,
                step =   solenoids[10].step,
                duration = duration
            )
        }
        if (solenoids[11].isVisible) {
            SolenoidControl(
                index = 12,
                solenoids[11].displayName,
                current = map(
                    x = current11,
                    in_min = 0,
                    in_max = 4095,
                    out_min = 0,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[11].maxPWM,
                step =   solenoids[11].step,
                duration = duration
            )
        }

    }
}

var ch1 = 0x00.toByte()
var ch2 = 0x00.toByte()
var ch3 = 0x00.toByte()
var ch4 = 0x00.toByte()

var ch5 = 0x00.toByte()
var ch6 = 0x00.toByte()
var ch7 = 0x00.toByte()
var ch8 = 0x00.toByte()

var ch9  = 0x00.toByte()
var ch10 = 0x00.toByte()
var ch11 = 0x00.toByte()
var ch12 = 0x00.toByte()

var isChangedFirstFourth = true

fun selectorForChannels(chIndex: Int, byte: Byte) {
    isChangedFirstFourth = chIndex in 1..4

    when(chIndex) {
        1 -> ch1 = byte
        2 -> ch2 = byte
        3 -> ch3 = byte
        4 -> ch4 = byte
        5 -> ch5 = byte
        6 -> ch6 = byte
        7 -> ch7 = byte
        8 -> ch8 = byte
        9 -> ch9 = byte
        10 -> ch10 = byte
        11 -> ch11 = byte
        12 -> ch12 = byte
    }
}



