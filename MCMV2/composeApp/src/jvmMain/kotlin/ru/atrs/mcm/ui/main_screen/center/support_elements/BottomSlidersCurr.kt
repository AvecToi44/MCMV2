package ru.atrs.mcm.ui.main_screen.center.support_elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.atrs.mcm.serial_port.RouterCommunication.writeToSerialPort
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.DELAY_FOR_GET_DATA
import ru.atrs.mcm.utils.GAUGES_IN_THE_ROW
import ru.atrs.mcm.utils.dataChunkCurrents
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.solenoids


@Composable
fun SolenoidsPanel(
    modifier: Modifier = Modifier,
    sizeRow: Size,
    duration: MutableStateFlow<Long>,
    showAnalogPanel: Boolean = false
) {
    val crctx = rememberCoroutineScope().coroutineContext
    var analogInput1 by remember { mutableStateOf((analog1.toInt() and 0xFF).toString()) }
    var analogInput2 by remember { mutableStateOf((analog2.toInt() and 0xFF).toString()) }

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

    var widthOfSolenoidControl by remember {mutableStateOf(0.dp)}


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

                it.ninthCurrentData?.let { current9 = it  }
                it.tenthCurrentData?.let { current10 = it  }
                it.eleventhCurrentData?.let { current11 = it  }
                it.twelfthCurrentData?.let { current12 = it  }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
        // Set column height using the LayoutCoordinates
        if (coordinates.size.width != 0) {
            val denominator = if (showAnalogPanel) 13 else 12
            widthOfSolenoidControl = ((coordinates.size.width ) / denominator).dp
            println("<<<<< ${widthOfSolenoidControl}")
        }
    }, horizontalArrangement = Arrangement.End) {
        AnimatedVisibility(showAnalogPanel) {
            val panelWidth = if (widthOfSolenoidControl < 170.dp) 170.dp else widthOfSolenoidControl
            Column(
                modifier = Modifier
                    .width(panelWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF132238), RoundedCornerShape(10.dp))
                    .border(2.dp, Color(0xFF5FA8FF), RoundedCornerShape(10.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = analogInput1,
                        onValueChange = { analogInput1 = it.filter { ch -> ch.isDigit() } },
                        label = { Text("CH1", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .padding(horizontal = 2.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEAF2FF),
                            unfocusedContainerColor = Color(0xFFF4F8FF),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF0B3D91),
                            unfocusedLabelColor = Color(0xFF2E4A76)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = analogInput2,
                        onValueChange = { analogInput2 = it.filter { ch -> ch.isDigit() } },
                        label = { Text("CH2", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .padding(horizontal = 2.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEAF2FF),
                            unfocusedContainerColor = Color(0xFFF4F8FF),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF0B3D91),
                            unfocusedLabelColor = Color(0xFF2E4A76)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            contentColor = Color.White
                        ),
                        onClick = {
                        val ch1Input = analogInput1.trim().toIntOrNull()
                        val ch2Input = analogInput2.trim().toIntOrNull()

                        if (ch1Input == null || ch2Input == null) {
                            showMeSnackBar("CH1 и CH2 должны быть числами 0..255", Color.Red)
                            return@Button
                        }
                        if (ch1Input !in 0..255 || ch2Input !in 0..255) {
                            showMeSnackBar("CH1 и CH2 должны быть в диапазоне 0..255", Color.Red)
                            return@Button
                        }

                        analog1 = ch1Input.toByte()
                        analog2 = ch2Input.toByte()

                        CoroutineScope(Dispatchers.IO + crctx).launch {
                            writeToSerialPort(
                                byteArrayOf(
                                    0x51,
                                    analog1,
                                    analog2,
                                    0x00, 0x00,
                                    0x00, 0x00,
                                    0x00, 0x00,
                                    0x00, 0x00,
                                    0x00, 0x00,
                                    0x00,
                                ),
                                withFlush = false,
                                delay = 0L
                            )
                        }
                        }
                    ) {
                        Text("OK", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (solenoids[0].isVisible) {
            SolenoidControl(
                index = 1,
                solenoids[0].displayName,
                current = map(
                    x = current1,
                    in_min = 0,
                    in_max = 4095,
                    out_min = solenoids[0].currentMinValue,
                    out_max = solenoids[0].currentMaxValue
                ),
                maxPWM = solenoids[0].maxPWM,
                step =   solenoids[0].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[1].currentMinValue,
                    out_max = solenoids[1].currentMaxValue
                ),
                maxPWM = solenoids[1].maxPWM,
                step = solenoids[1].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[2].currentMinValue,
                    out_max = solenoids[2].currentMaxValue
                ),
                maxPWM = solenoids[2].maxPWM,
                step = solenoids[2].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[3].currentMinValue,
                    out_max = solenoids[3].currentMaxValue
                ),
                maxPWM = solenoids[3].maxPWM,
                step = solenoids[3].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[4].currentMinValue,
                    out_max = solenoids[4].currentMaxValue
                ),
                maxPWM = solenoids[4].maxPWM,
                step = solenoids[4].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[5].currentMinValue,
                    out_max = solenoids[5].currentMaxValue
                ),
                maxPWM = solenoids[5].maxPWM,
                step = solenoids[5].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[6].currentMinValue,
                    out_max = solenoids[6].currentMaxValue
                ),
                maxPWM = solenoids[6].maxPWM,
                step = solenoids[6].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[7].currentMinValue,
                    out_max = solenoids[7].currentMaxValue
                ),
                maxPWM = solenoids[7].maxPWM,
                step = solenoids[7].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[8].currentMinValue,
                    out_max = solenoids[8].currentMaxValue
                ),
                maxPWM = solenoids[8].maxPWM,
                step = solenoids[8].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[9].currentMinValue,
                    out_max = solenoids[9].currentMaxValue
                ),
                maxPWM = solenoids[9].maxPWM,
                step = solenoids[9].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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
                    out_min = solenoids[10].currentMinValue,
                    out_max = solenoids[10].currentMaxValue
                ),
                maxPWM = solenoids[10].maxPWM,
                step =   solenoids[10].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
                duration = duration
            )
        }
        if (solenoids[11].isVisible) {
            SolenoidControl(
                index = 12,
                solenoids[11].displayName,
                current = map(
                    x = current12,
                    in_min = 0,
                    in_max = 4095,
                    out_min = solenoids[11].currentMinValue,
                    out_max = solenoids[11].currentMaxValue
                ),
                maxPWM = solenoids[11].maxPWM,
                step =   solenoids[11].step,
                widthOfSolenoidControl= widthOfSolenoidControl,
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

var analog1 = 0x00.toByte()
var analog2 = 0x00.toByte()

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



