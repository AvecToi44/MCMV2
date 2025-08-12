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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import ru.atrs.mcm.serial_port.RouterCommunication.solenoidControl
import ru.atrs.mcm.serial_port.RouterCommunication.writeToSerialPort
import ru.atrs.mcm.ui.styles.colorDecrease
import ru.atrs.mcm.ui.styles.colorIncrease
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.pwm10SeekBar
import ru.atrs.mcm.utils.pwm11SeekBar
import ru.atrs.mcm.utils.pwm12SeekBar
import ru.atrs.mcm.utils.pwm1SeekBar
import ru.atrs.mcm.utils.pwm2SeekBar
import ru.atrs.mcm.utils.pwm3SeekBar
import ru.atrs.mcm.utils.pwm4SeekBar
import ru.atrs.mcm.utils.pwm5SeekBar
import ru.atrs.mcm.utils.pwm6SeekBar
import ru.atrs.mcm.utils.pwm7SeekBar
import ru.atrs.mcm.utils.pwm8SeekBar
import ru.atrs.mcm.utils.pwm9SeekBar
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

            9 -> pwm9SeekBar
            10 -> pwm10SeekBar
            11 -> pwm11SeekBar
            12 -> pwm12SeekBar
            else -> pwm1SeekBar
        }
    }
    Column(modifier = Modifier.width(80.dp).background(Color.Gray).border(width = 2.dp, color = Color.LightGray, shape = RoundedCornerShape(8.dp)), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxSize().weight(0.5f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
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
                    "${map(PWMremember.value, 0, 255, 0, 100)}\n%",
                    fontSize = 16.sp
                )
            }
        }
        Row(Modifier.fillMaxSize().weight(1f)) {
            Column(Modifier.fillMaxSize().weight(1f).background(colorIncrease).border(width = 2.dp, color = Color.Black).clickable {
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
                    solenoidControl(isChangedFirstFourth)
                    delay(100)
                }
                //pos.value += 0.1f
            }.padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "+", // +
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
                    solenoidControl(isChangedFirstFourth)
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
            Column(Modifier.fillMaxSize().background(colorDecrease).weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // MINUS
                PWMremember.value = PWMremember.value - step
                if (PWMremember.value < 0) {
                    PWMremember.value = 0
                }

                CoroutineScope(Dispatchers.IO).launch {
                    selectorForChannels(index, PWMremember.value.toByte())
                    solenoidControl(isChangedFirstFourth)
                }
                //pos.value-= 0.1f
            }.padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.material3.Text(
                    text = "-", // minus
                    fontSize = 15.sp
                )
            }
            Column(Modifier.fillMaxSize().weight(1f).border(width = 2.dp, color = Color.Black).clickable {
                // Back to 0
                PWMremember.value = 0
                //pos.value = 1.0f
                CoroutineScope(Dispatchers.IO).launch {
                    selectorForChannels(index, PWMremember.value.toByte())
                    solenoidControl(isChangedFirstFourth)
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