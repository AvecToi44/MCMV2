package ru.atrs.mcm.ui.main_screen.center.support_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.atrs.mcm.serial_port.writeToSerialPort
import ru.atrs.mcm.utils.arr1Measure
import ru.atrs.mcm.utils.dataChunkGauges
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.pwm1SeekBar
import ru.atrs.mcm.utils.pwm2SeekBar
import ru.atrs.mcm.utils.pwm3SeekBar
import ru.atrs.mcm.utils.pwm4SeekBar
import ru.atrs.mcm.utils.pwm5SeekBar
import ru.atrs.mcm.utils.pwm6SeekBar
import ru.atrs.mcm.utils.pwm7SeekBar
import ru.atrs.mcm.utils.pwm8SeekBar
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.solenoids

@Composable
fun SolenoidControl(
    index: Int,
    displayName: String,
    current: Int,
    maxPWM: Int,
    step: Int,
    duration: MutableStateFlow<Long>
) {
    logGarbage("FULL>  || ${solenoids.joinToString()} ][ ${pressures.joinToString()} ][ ${scenario.joinToString()} ||")
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
    Column(modifier = Modifier.width(100.dp).background(Color.Gray).border(width = 2.dp, color = Color.LightGray, shape = RoundedCornerShape(8.dp)), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxSize().weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Text("${displayName}")
        }
        Row(Modifier.fillMaxSize().weight(1f).background(Color.DarkGray)) {
            Column(Modifier.fillMaxSize().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    "$current",
                    fontSize = 16.sp
                )
            }
            Column(Modifier.fillMaxSize().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    "${map(PWMremember.value, 0, 255, 0, 100)}%",
                    fontSize = 16.sp
                )
            }
        }
        Row(Modifier.fillMaxSize().weight(1f)) {
            Column(Modifier.fillMaxSize().weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // PLUS +1
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
            }.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "\u002B", // +
                    fontSize = 15.sp
                )
            }

            Column(Modifier.fillMaxSize().weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // TO BIGGEST 255!!!
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
            }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "\u2191", // UP
                    fontSize = 30.sp
                )
            }

        }
        Row(Modifier.fillMaxSize().weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.fillMaxSize().weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // MINUS
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
            }.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "\u2212", // minus
                    fontSize = 15.sp
                )
            }
            Column(Modifier.fillMaxSize().weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // Back to 0
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
            }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "\u2193", // DOWN
                    fontSize = 30.sp
                )
            }
        }
    }
}