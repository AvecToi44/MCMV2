package ru.atrs.mcm.ui.starter_screen

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.delay
import ru.atrs.mcm.openChartViewer
import ru.atrs.mcm.openLastScenario
import ru.atrs.mcm.openNewScenario
import ru.atrs.mcm.ui.screenNav
import ru.atrs.mcm.storage.refreshParameters
import ru.atrs.mcm.ui.navigation.Screens
import ru.atrs.mcm.ui.styles.fontRoboGirls
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.OPERATOR_ID
import ru.atrs.mcm.utils.LOG_LEVEL
import ru.atrs.mcm.utils.LogLevel
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import ru.atrs.mcm.utils.SOUND_ENABLED
import ru.atrs.mcm.utils.arrayOfComPorts
import ru.atrs.mcm.utils.getComPorts_Array
import ru.atrs.mcm.utils.logAct


@OptIn(ExperimentalTextApi::class)
@Composable
fun StarterScreen() {

    var expandedOperator by remember { mutableStateOf(false) }
    var expandedCom by remember { mutableStateOf(false) }
    var expandedBaud by remember { mutableStateOf(false) }
    var expandedSound by remember { mutableStateOf(false) }
    var expandedLogs by remember { mutableStateOf(false) }
    var visibilitySettings = remember { mutableStateOf(false)}
    var choosenCOM = remember { mutableStateOf(0) }
    var choosenBaud = remember { mutableStateOf(BAUD_RATE) }
    val textState = remember { mutableStateOf(OPERATOR_ID) }
    var listOfOperators = mutableListOf<String>()//loadOperators()

    var crtxscp = rememberCoroutineScope().coroutineContext

    //var remarrayports = remember { arrayOfComPorts }
    LaunchedEffect(true) {

        while (true) {
            arrayOfComPorts = getComPorts_Array() as Array<SerialPort>
            delay(1000)
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(modifier = Modifier.fillMaxSize().weight(2f), horizontalArrangement = Arrangement.Center) {

            //Image("",painter = painterResource("drawable/trs.jpg"))
            Text("MCM - Modulation Control Module",
                modifier = Modifier//.fillMaxSize()
                    .padding(top = 20.dp).clickable {
                    //screenNav.value = Screens.MAIN
                }, fontSize = 50.sp, fontFamily = fontRoboGirls, color = Color.White, textAlign = TextAlign.Center)
        }
        Row(modifier = Modifier.fillMaxSize().weight(3f).background(Color.Black), horizontalArrangement = Arrangement.Center) {
//            Image(
//                painter = painterResource("drawable/trs.jpg"),
//                contentDescription = null
//            )
            Column(Modifier.padding(16.dp)) {

                TextField(colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor =  Color.Transparent, //hide the indicator
                    unfocusedIndicatorColor = Color.Green),
                    modifier = Modifier.fillMaxWidth(),
                    value = textState.value,
                    onValueChange = {
                        textState.value = it
                        OPERATOR_ID = it
                        refreshParameters()
                    },
                    textStyle = TextStyle.Default.copy(fontSize = 35.sp)
                )

                Box {
                    Text("OPERATOR ID ⬆️",
                        modifier = Modifier.fillMaxSize().clickable {
                            expandedOperator = true
                        }, fontSize = 20.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

                    DropdownMenu(
                        modifier = Modifier.background(Color.White),
                        expanded = expandedOperator,
                        onDismissRequest = { expandedOperator = false },
                    ) {
                        repeat(listOfOperators.size) {
                            Text("${listOfOperators[it]}", fontSize=18.sp, modifier = Modifier.fillMaxSize().padding(10.dp).clickable(onClick={}))
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxSize().weight(3f).padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))
                .clickable {
                    openNewScenario()

                }) {
                Text("Open Scenario",
                    modifier = Modifier.padding(4.dp), fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)
            }

            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))
                .clickable {
                    openLastScenario()
                }) {
                Text("Open last scenario",
                    modifier = Modifier.padding(4.dp), fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)
            }

            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))
                .clickable {
                   openChartViewer()

                }) {
                Text("Open Chart",
                    modifier = Modifier.padding(4.dp), fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)
            }

            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {},
                        onDoubleTap = {
                            screenNav.value = Screens.EASTER_EGG
                        },
                        onLongPress = {},
                        onTap = {
                            visibilitySettings.value = !visibilitySettings.value
                        }
                    )
                }
                ) {
                Text("Settings",
                    modifier = Modifier.padding(4.dp)
                    , fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)
            }


//            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))
//                .clickable { visibilitySettings.value = !visibilitySettings.value }) {
//                Text("Settings",
//                    modifier = Modifier.padding(4.dp)
//                    , fontSize = 24.sp, fontFamily = FontFamily.Monospace,
//                    color = Color.White, textAlign = TextAlign.Center
//                )
//
//            }
//            Box(Modifier.width(200.dp).border(BorderStroke(2.dp, Color.Blue))) {
//                Text("Quick",
//                    modifier = Modifier.padding(4.dp).clickable {
//                        screenNav.value = Screens.MAIN
//                    }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)
//
//            }
        }


        if(visibilitySettings.value) {
            Row(modifier = Modifier.fillMaxSize().weight(4f).background(Color.DarkGray),
                horizontalArrangement = Arrangement.SpaceBetween) {
                LazyColumn(Modifier.width(300.dp)) {
                    item {
                        Row {
                            Text("COM Port:",
                                modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

                            Box {
                                Text(
                                    if (arrayOfComPorts.isEmpty()) "‼️NO COM PORTS‼️" else arrayOfComPorts[choosenCOM.value].systemPortName,
                                    modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                        expandedCom = !expandedCom
                                    }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.Blue, textAlign = TextAlign.Center)

                                DropdownMenu(
                                    modifier = Modifier.background(Color.White),
                                    expanded = expandedCom,
                                    onDismissRequest = { expandedCom = false },
                                ) {
                                    repeat(arrayOfComPorts.size-1) {
                                        Text("${arrayOfComPorts[it].descriptivePortName}", fontSize=18.sp, modifier = Modifier.fillMaxSize().padding(10.dp)
                                            .clickable(onClick= {
                                                choosenCOM.value = it
                                                COM_PORT = arrayOfComPorts[it].systemPortName
                                                logAct("DropdownMenu click ${COM_PORT}")
                                                refreshParameters()
                                            }))

                                    }
                                }
                            }

                        }
                    }

                    item {
                        Row {
                            Text("Baud-rate:",
                                modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

                            Box {
                                Text(choosenBaud.value.toString(),
                                    modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                        expandedBaud = !expandedBaud
                                    }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.Blue, textAlign = TextAlign.Center)

                                DropdownMenu(
                                    modifier = Modifier.background(Color.White),
                                    expanded = expandedBaud,
                                    onDismissRequest = { expandedBaud = false },
                                ) {
                                    Text("38400",   fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 38400
                                    BAUD_RATE = choosenBaud.value
                                    })  .fillMaxSize().padding(10.dp))
                                    Text("57600",   fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 57600
                                    BAUD_RATE = choosenBaud.value
                                    })  .fillMaxSize().padding(10.dp))
                                    Text("115200",  fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 115200
                                    BAUD_RATE = choosenBaud.value
                                    }) .fillMaxSize().padding(10.dp))
                                    Text("128000",  fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 128000
                                    BAUD_RATE = choosenBaud.value
                                    }) .fillMaxSize().padding(10.dp))
                                    Text("256000",  fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 256000
                                    BAUD_RATE = choosenBaud.value
                                    }) .fillMaxSize().padding(10.dp))
                                    Text("500000",  fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 500000
                                    BAUD_RATE = choosenBaud.value
                                    }) .fillMaxSize().padding(10.dp))
                                    Text("1000000", fontSize=18.sp, modifier = Modifier.clickable(onClick= { choosenBaud.value = 1000000
                                    BAUD_RATE = choosenBaud.value
                                    }).fillMaxSize().padding(10.dp))
                                    Divider()
                                }
                            }
                        }
                    }

                    item {
                        Row {
                            Text("Sound type",
                                modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

                            Box {
                                Text("${SOUND_ENABLED}",
                                    modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                        expandedSound = !expandedSound
                                    }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.Blue, textAlign = TextAlign.Center)

                                DropdownMenu(
                                    modifier = Modifier.background(Color.White),
                                    expanded = expandedSound,
                                    onDismissRequest = { expandedSound = false },
                                ) {
                                    Text("0",   fontSize=18.sp, modifier = Modifier.clickable(onClick= {
                                        SOUND_ENABLED = 0
                                    })  .fillMaxSize().padding(10.dp))
                                    Text("1",   fontSize=18.sp, modifier = Modifier.clickable(onClick= {
                                        SOUND_ENABLED = 1
                                    })  .fillMaxSize().padding(10.dp))
                                    Text("2",   fontSize=18.sp, modifier = Modifier.clickable(onClick= {
                                        SOUND_ENABLED = 2
                                    })  .fillMaxSize().padding(10.dp))

                                }
                            }
                        }
                    }

                    item {
                        Row {
                            Text("Enable Logger",
                                modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

                            Box {
                                Text("${LOG_LEVEL.name}",
                                    modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                        expandedLogs = !expandedLogs
                                    }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.Blue, textAlign = TextAlign.Center)

                                DropdownMenu(
                                    modifier = Modifier.background(Color.White),
                                    expanded = expandedLogs,
                                    onDismissRequest = { expandedLogs = false },
                                ) {
                                    Text("ERRORS",   fontSize=18.sp, modifier = Modifier.clickable(onClick= {
                                        LOG_LEVEL = LogLevel.ERRORS
                                        refreshParameters()
                                        expandedLogs = false
                                    })  .fillMaxSize().padding(10.dp))
                                    Text("DEBUG",   fontSize=18.sp, modifier = Modifier.clickable(onClick= {
                                        LOG_LEVEL = LogLevel.DEBUG
                                        refreshParameters()
                                        expandedLogs = false
                                    })  .fillMaxSize().padding(10.dp))

                                }
                            }
                        }
                    }

                    item {
                        Row {
                            Text("Enable Fullscreen",
                                modifier = Modifier.width(200.dp).padding(4.dp).clickable {
                                }, fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)


                            val checkedState = remember { mutableStateOf(SHOW_FULLSCREEN) }
                            Checkbox(
                                checked = checkedState.value,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Blue,
                                    uncheckedColor = Color.Gray
                                ),
                                onCheckedChange = {
                                    checkedState.value = it
                                    SHOW_FULLSCREEN = it
                                    refreshParameters()
                                }
                            )
                        }

                    }
                }
                }

                Column(Modifier.width(600.dp).verticalScroll(rememberScrollState())) {
                    Row(Modifier.width(600.dp)) {
                        Text("⌨️\uD83D\uDD25Hot Keys:" +
                                "\n [ctrl + N] - new experiment" +
                                "\n [ctrl + L] - later experiment" +
                                "\n [ctrl + V] - open viewer" +
                                "\n [ctrl + Space] - start experiment" +
                                "\n [<-] - previous scenario" +
                                "\n [->] - next scenario" +
                                "",
                            modifier = Modifier.fillMaxSize().clickable {
                            },
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }


            }
        }
}