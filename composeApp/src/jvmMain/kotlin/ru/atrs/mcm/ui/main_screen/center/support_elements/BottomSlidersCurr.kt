package ru.atrs.mcm.ui.main_screen.center.support_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.atrs.mcm.serial_port.writeToSerialPort
import ru.atrs.mcm.ui.styles.fontDigital
import ru.atrs.mcm.utils.DELAY_FOR_GET_DATA
import ru.atrs.mcm.utils.dataChunkCurrents
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.pwm1SeekBar
import ru.atrs.mcm.utils.pwm2SeekBar
import ru.atrs.mcm.utils.pwm3SeekBar
import ru.atrs.mcm.utils.pwm4SeekBar
import ru.atrs.mcm.utils.pwm5SeekBar
import ru.atrs.mcm.utils.pwm6SeekBar
import ru.atrs.mcm.utils.pwm7SeekBar
import ru.atrs.mcm.utils.pwm8SeekBar
import ru.atrs.mcm.utils.solenoids
import androidx.compose.material3.*


@Composable
fun solenoidsPanel(
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
            }
        }
    }
    val aspc = 1f
    Row(modifier = Modifier.fillMaxSize().background(Color.DarkGray), horizontalArrangement = Arrangement.End) {
        if (solenoids[0].isVisible) {
            SolenoidControl(
                index = 1,
                solenoids[1].displayName,
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
    }
//        Row(
//            modifier = Modifier.fillMaxSize().weight(1f) //.padding(10.dp)
////            .width(sizeRow.width.dp)
////            .height(IntrinsicSize.Min)
//                .background(Color.Black)
//        ) {
//
//            justBar(index = 1,solenoids[0].displayName, current = map(x=current1,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[0].currentMaxValue), maxPWM = solenoids[0].maxPWM, step = solenoids[0].step, duration = duration)
//            justBar(index = 2,solenoids[1].displayName, current = map(x=current2,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[1].currentMaxValue), maxPWM = solenoids[1].maxPWM, step = solenoids[1].step, duration = duration)
//            justBar(index = 3,solenoids[2].displayName, current = map(x=current3,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[2].currentMaxValue), maxPWM = solenoids[2].maxPWM, step = solenoids[2].step, duration = duration)
//            justBar(index = 4,solenoids[3].displayName, current = map(x=current4,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[3].currentMaxValue), maxPWM = solenoids[3].maxPWM, step = solenoids[3].step, duration = duration)
//        }
//        Row(
//            modifier = androidx.compose.ui.Modifier.weight(1f) //.padding(10.dp)
////                .width(sizeRow.width.dp)
////                .height(IntrinsicSize.Min)
//                .background(Color.Black)
//        ) {
//            justBar(index = 5, solenoids[4].displayName, current = map(x=current5,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[0].currentMaxValue), maxPWM = solenoids[4].maxPWM, step = solenoids[4].step, duration = duration)
//            justBar(index = 6, solenoids[5].displayName, current = map(x=current6,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[0].currentMaxValue), maxPWM = solenoids[5].maxPWM, step = solenoids[5].step, duration = duration)
//            justBar(index = 7, solenoids[6].displayName, current = map(x=current7,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[0].currentMaxValue), maxPWM = solenoids[6].maxPWM, step = solenoids[6].step, duration = duration)
//            justBar(index = 8, solenoids[7].displayName, current = map(x=current8,in_min=0, in_max = 4095, out_min=0, out_max = solenoids[0].currentMaxValue), maxPWM = solenoids[7].maxPWM, step = solenoids[7].step, duration = duration)
//        }
    }

var ch1 = 0x00.toByte()
var ch2 = 0x00.toByte()
var ch3 = 0x00.toByte()
var ch4 = 0x00.toByte()

var ch5 = 0x00.toByte()
var ch6 = 0x00.toByte()
var ch7 = 0x00.toByte()
var ch8 = 0x00.toByte()

@Composable
fun justBar(
    index: Int,
    channelName: String,
    current: Int,
    maxPWM: Int,
    step: Int,
    duration: MutableStateFlow<Long>,
    //PWM: Int
    ) {

    // PWM [from 0 to 255]
    val PWMremember = remember {
        when(index) {
            1 -> pwm1SeekBar
            2 -> pwm2SeekBar
            3 -> pwm3SeekBar
            4 -> pwm4SeekBar

            5 -> pwm5SeekBar
            6 -> pwm6SeekBar
            7 -> pwm7SeekBar
            8 -> pwm8SeekBar
            else -> pwm1SeekBar
        }
    }





    Column(
        modifier = Modifier.padding(0.dp, 1.dp).fillMaxSize()
            //.width(200.dp)
            //.height(90.dp)
            .background(Color.Black)
            .padding(5.dp)
        //.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxSize().weight(2f)//.height(60.dp)
                .background(Color.Black),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.DarkGray).weight(1f).clickable {
                    PWMremember.value = 0
                    //pos.value = 1.0f
                    CoroutineScope(Dispatchers.IO).launch {
                        selectorForChannels(index, PWMremember.value.toByte())
                        //selectorForChannels(index, PWMremember.value.to2ByteArray()[0])
                        if (isChangedFirstFourth) {
                            writeToSerialPort(byteArrayOf(0x71,ch1, 0x00,ch2, 0x00,ch3, 0x00,ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 100L)

                        }else {
                            writeToSerialPort(byteArrayOf(0x51,ch5, 0x00,ch6, 0x00,ch7, 0x00,ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)

                        }
                        delay(100)
                    }
                }
            ) {
                Text(
                    modifier = Modifier.fillMaxSize().padding(vertical = 20.dp).align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    text = "<<",
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray).weight(2f).clickable {
                    PWMremember.value = PWMremember.value - step
                    if (PWMremember.value < 0) {
                        PWMremember.value = 0
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        selectorForChannels(index, PWMremember.value.toByte())
                        //selectorForChannels(index, PWMremember.value.to2ByteArray()[0])
                        if (isChangedFirstFourth) {
                            writeToSerialPort(byteArrayOf(0x71,ch1, 0x00,ch2, 0x00,ch3, 0x00,ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 100L)
                        }else {
                            writeToSerialPort(byteArrayOf(0x51,ch5, 0x00,ch6, 0x00,ch7, 0x00,ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)
                        }
                    }
                    //pos.value-= 0.1f
                }
            ) {
                Text(
                    modifier = Modifier.fillMaxSize().padding(vertical = 20.dp).align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    text = "-",
                    color = Color.White
                )
            }


            Spacer(modifier = Modifier.width(10.dp).fillMaxHeight())
            Column(Modifier.fillMaxSize().weight(5f)) {
                Text(
                    "${channelName}",
                    modifier = Modifier.fillMaxSize().weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 8.sp,
                    color = Color.White
                )
                Text(
                    "${current}",
                    modifier = Modifier.fillMaxSize().weight(1f),
                    fontFamily = fontDigital,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Column(Modifier.fillMaxSize().weight(5f)) {
                Text(
                    "PWM (%)",
                    modifier = Modifier.fillMaxSize().weight(1f),
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    "${map(PWMremember.value, 0, 255, 0, 100)}",
                    modifier = Modifier.fillMaxSize().weight(1f),
                    fontFamily = fontDigital,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp).fillMaxHeight())


            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray).weight(2f).clickable {
                    PWMremember.value = PWMremember.value + step
                    if (PWMremember.value > maxPWM) {
                        PWMremember.value = maxPWM
                    }
                    if (PWMremember.value > 255) {
                        PWMremember.value = 255
                    }
                    println("WELL ${(PWMremember.value.toFloat())}")

                    CoroutineScope(Dispatchers.IO).launch {
                        selectorForChannels(index, PWMremember.value.toByte())
                        if (isChangedFirstFourth) {
                            writeToSerialPort(byteArrayOf(0x71,ch1, 0x00,ch2, 0x00,ch3, 0x00,ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 100L)

                        }else {
                            writeToSerialPort(byteArrayOf(0x51,ch5, 0x00,ch6, 0x00,ch7, 0x00,ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)

                        }
                        delay(100)
                    }
                    //pos.value += 0.1f
                }
            ) {
                Text(
                    modifier = Modifier.fillMaxSize().padding(vertical = 20.dp).align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    text = "+",
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.DarkGray).weight(1f).clickable {
                    PWMremember.value = 255
                    if (PWMremember.value > maxPWM) {
                        PWMremember.value = maxPWM
                    }
                    //pos.value = 1.0f
                    CoroutineScope(Dispatchers.IO).launch {
                        selectorForChannels(index, PWMremember.value.toByte())
                        if (isChangedFirstFourth) {
                            writeToSerialPort(byteArrayOf(0x71,ch1, 0x00,ch2, 0x00,ch3, 0x00,ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 100L)

                        }else {
                            writeToSerialPort(byteArrayOf(0x51,ch5, 0x00,ch6, 0x00,ch7, 0x00,ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)

                        }
                        delay(100)
                    }
                }
            ) {
                Text(
                    modifier = Modifier.fillMaxSize().padding(vertical = 20.dp).align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    text = ">>",
                    color = Color.White
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f)//.height(20.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                progress = (map(PWMremember.value, 0, 255, 0, 100) /100f).toFloat() //((PWMremember.value.toFloat() * 100f) / 255f)/100f //rndTo2deci(pos.value)
            )
        }
    }

}
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
    }
}

@Composable
fun SimpleProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float = 0.7f,
    progressBarColor: Color = Color.Red,
    cornerRadius: Dp = 0.dp,
    trackColor: Color = Color(0XFFFBE8E8),
    thumbRadius: Dp = 0.dp,
    thumbColor: Color = Color.White,
    thumbOffset: Dp = thumbRadius
) {}


