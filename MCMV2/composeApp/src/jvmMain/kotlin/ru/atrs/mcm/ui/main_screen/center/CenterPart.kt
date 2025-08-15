package ru.atrs.mcm.ui.main_screen.center

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import ru.atrs.mcm.enums.ExplorerMode
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.enums.StateParseBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.atrs.mcm.launchPlay
import ru.atrs.mcm.serial_port.RouterCommunication
import ru.atrs.mcm.serial_port.RouterCommunication.comparatorToSolenoid
import ru.atrs.mcm.serial_port.RouterCommunication.pauseSerialComm
import ru.atrs.mcm.serial_port.RouterCommunication.reInitSolenoids
import ru.atrs.mcm.serial_port.payloadWriterMachine
import ru.atrs.mcm.serial_port.bytesReceiverMachine
import ru.atrs.mcm.ui.custom.GaugeX
import ru.atrs.mcm.ui.main_screen.center.support_elements.SolenoidsPanel
import ru.atrs.mcm.ui.navigation.Screens
import ru.atrs.mcm.ui.screenNav
import ru.atrs.mcm.ui.styles.colorDarkForDashboardText
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.GAUGES_IN_THE_ROW
import ru.atrs.mcm.utils.GLOBAL_STATE
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.SHOW_BOTTOM_PANEL
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import ru.atrs.mcm.utils.dataGauges
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.isExperimentStarts
import ru.atrs.mcm.utils.limitTime
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.test_time
import ru.atrs.mcm.utils.txtOfScenario


@Composable
fun CenterPiece(
) {
    var sizeRow    by remember {mutableStateOf(Size.Zero)}

//    var pressuresX by remember { mutableStateOf(FloatArray(8) { 0f }) }

    var pressure1X by remember { mutableStateOf(0f) }
    var pressure2X by remember { mutableStateOf(0f) }
    var pressure3X by remember { mutableStateOf(0f) }
    var pressure4X by remember { mutableStateOf(0f) }
    var pressure5X by remember { mutableStateOf(0f) }
    var pressure6X by remember { mutableStateOf(0f) }
    var pressure7X by remember { mutableStateOf(0f) }
    var pressure8X by remember { mutableStateOf(0f) }
    var pressure9X by remember  { mutableStateOf(0f) }
    var pressure10X by remember { mutableStateOf(0f) }
    var pressure11X by remember { mutableStateOf(0f) }
    var pressure12X by remember { mutableStateOf(0f) }
    val duration = MutableStateFlow(100L)

    val stateChart = remember { STATE_EXPERIMENT }
    val explMode = remember { EXPLORER_MODE }
    val expandedCom = remember { mutableStateOf(false) }
    val showBottomPanel = remember { mutableStateOf(SHOW_BOTTOM_PANEL) }

    val txt = remember { txtOfScenario }

    val ctxScope =
        CoroutineScope(Dispatchers.IO) + rememberCoroutineScope().coroutineContext + CoroutineName("MainScreen-CenterPart")


    // Create element height in dp state
    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }
    var isPayloadComing = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        RouterCommunication.sendFrequency()
        bytesReceiverMachine()
    }
    LaunchedEffect(true) {
        payloadWriterMachine()
    }
    LaunchedEffect(true) {
        println("12 mode: ${TWELVE_CHANNELS_MODE.toString()}")
        dataGauges.collect {
            isPayloadComing.value = true
            pressure1X = it.pressure1
            pressure2X = it.pressure2
            pressure3X = it.pressure3
            pressure4X = it.pressure4
            pressure5X = it.pressure5
            pressure6X = it.pressure6
            pressure7X = it.pressure7
            pressure8X = it.pressure8
            pressure9X = it.pressure9
            pressure10X = it.pressure10
            pressure11X = it.pressure11
            pressure12X = it.pressure12
        }
    }

    /**
     * Composer:
     */
    Column(
        modifier = Modifier //.padding(10.dp)
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { coordinates ->
                sizeRow = coordinates.size.toSize()
            }
    ) {
        Row(Modifier.weight(5f)) {

            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
                    // Set column height using the LayoutCoordinates
                    if (coordinates.size.width != 0) {
                        columnHeightDp = (coordinates.size.width / GAUGES_IN_THE_ROW).dp
                    }
                },
                //columns = GridCells.Adaptive(150.dp),
                columns = GridCells.Fixed(GAUGES_IN_THE_ROW),
                verticalArrangement =   Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                // content padding
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = 0.dp,
                    end = 0.dp,
                    bottom = 0.dp
                ),
                content = {
                    if (pressures[0].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)
                            ) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure1X,
                                    (pressures[0].minValue),
                                    (pressures[0].maxValue),
                                    type = "Бар",
                                    displayName = pressures[0].displayName,
                                    comment = pressures[0].commentString
                                )
                            }
                        }
                    }
                    if (pressures[1].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure2X,
                                    (pressures[1].minValue),
                                    (pressures[1].maxValue),
                                    type = "Бар",
                                    displayName = pressures[1].displayName,
                                    comment = pressures[1].commentString
                                )
                            }

                        }
                    }

                    if (pressures[2].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure3X,
                                    (pressures[2].minValue),
                                    (pressures[2].maxValue),
                                    type = "Бар",
                                    displayName = pressures[2].displayName,
                                    comment = pressures[2].commentString
                                )
                            }
                        }
                    }

                    if (pressures[3].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure4X,
                                    (pressures[3].minValue),
                                    (pressures[3].maxValue),
                                    type = "Бар",
                                    displayName = pressures[3].displayName,
                                    comment = pressures[3].commentString
                                )
                            }
                        }
                    }

                    if (pressures[4].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure5X,
                                    (pressures[4].minValue),
                                    (pressures[4].maxValue),
                                    type = "Бар",
                                    displayName = pressures[4].displayName,
                                    comment = pressures[4].commentString
                                )
                            }
                        }
                    }

                    if (pressures[5].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure6X,
                                    (pressures[5].minValue),
                                    (pressures[5].maxValue),
                                    type = "Бар",
                                    displayName = pressures[5].displayName,
                                    comment = pressures[5].commentString
                                )
                            }
                        }
                    }

                    if (pressures[6].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure7X,
                                    (pressures[6].minValue),
                                    (pressures[6].maxValue),
                                    type = "Бар",
                                    displayName = pressures[6].displayName,
                                    comment = pressures[6].commentString
                                )
                            }
                        }
                    }

                    if (pressures[7].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure8X,
                                    (pressures[7].minValue),
                                    (pressures[7].maxValue),
                                    type = "Бар",
                                    displayName = pressures[7].displayName,
                                    comment = pressures[7].commentString
                                )
                            }
                        }
                    }
                    if (pressures[8].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure9X,
                                    (pressures[8].minValue),
                                    (pressures[8].maxValue),
                                    type = "Бар",
                                    displayName = pressures[8].displayName,
                                    comment =     pressures[8].commentString
                                )
                            }
                        }
                    }
                    if (pressures[9].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure10X,
                                    (pressures[9].minValue),
                                    (pressures[9].maxValue),
                                    type = "Бар",
                                    displayName = pressures[9].displayName,
                                    comment =     pressures[9].commentString
                                )
                            }
                        }
                    }
                    if (pressures[10].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure11X,
                                    (pressures[10].minValue),
                                    (pressures[10].maxValue),
                                    type = "Бар",
                                    displayName = pressures[10].displayName,
                                    comment =     pressures[10].commentString
                                )
                            }
                        }
                    }
                    if (pressures[11].isVisible) {
                        item {
                            Box(Modifier.aspectRatio(1f)) {
                                GaugeX(
                                    DpSize(columnHeightDp, columnHeightDp),
                                    pressure12X,
                                    (pressures[11].minValue),
                                    (pressures[11].maxValue),
                                    type = "Бар",
                                    displayName = pressures[11].displayName,
                                    comment = pressures[11].commentString
                                )
                            }
                        }
                    }
                    item {
                        Box(Modifier.aspectRatio(1f)
//                            .onGloballyPositioned { coordinates ->
//                            // Set column height using the LayoutCoordinates
//                            if (coordinates.size.width != 0) {
//                                columnHeightDp = with(localDensity) { coordinates.size.width.toDp() }
//                            }
//                            }
                        ) {
                            Text("${LAST_SCENARIO.absolutePath}", color = colorDarkForDashboardText)
                        }
                    }
                }
            )
        }
        if(showBottomPanel.value) {
            Row(Modifier.fillMaxSize().weight(1.3f), horizontalArrangement = Arrangement.SpaceAround) {

                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {


                    if (isPayloadComing.value) {
                        if (STATE_EXPERIMENT.value != StateExperiments.NONE) {
                            Text(
                                "Rec... ${STATE_EXPERIMENT.value.name}",
                                modifier = Modifier.padding(top = (10).dp, start = 20.dp).clickable {
                                },
                                fontFamily = FontFamily.Default,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        } else {
                            Column {
                                Box(Modifier.clickable {
                                    test_time = 0
                                    // launch
                                    if (explMode.value == ExplorerMode.AUTO) {
                                        launchPlay()
                                    } else if (explMode.value == ExplorerMode.MANUAL) {
                                        indexOfScenario.value--

                                        ctxScope.launch {
                                            comparatorToSolenoid(indexOfScenario.value)
                                        }

                                        scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.comment }
                                        //txtOfScenario.value = scenario.getOrNull(indexOfScenario.value)?.text
                                        //txtOfScenario.value = scenario[indexOfScenario.value].text
                                    }


                                }) {
                                    Text(
                                        if (explMode.value == ExplorerMode.AUTO) "▶" else "⏪",
                                        modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                                        fontFamily = FontFamily.Default,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(Modifier.clickable {
                                    //stop scenario

                                    CoroutineScope(Dispatchers.IO).launch {
                                        if (explMode.value == ExplorerMode.AUTO) {
                                            reInitSolenoids()
                                            GLOBAL_STATE.value = StateParseBytes.WAIT
//                            initSerialCommunication()
//                            startReceiveFullData()
                                        } else if (explMode.value == ExplorerMode.MANUAL) {
                                            indexOfScenario.value++
                                            comparatorToSolenoid(indexOfScenario.value)

                                            //txtOfScenario.value = scenario.getOrElse(indexOfScenario.value) { 0 }
                                            scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.comment }
                                            //txtOfScenario.value = scenario.getOrElse(indexOfScenario.value) { scenario[0] }.text
                                        }
                                    }
                                }) {
                                    Text(
                                        if (explMode.value == ExplorerMode.AUTO) "⏸" else "⏩",
                                        modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                                        fontFamily = FontFamily.Default,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                        }
                    } else {
                        Text(
                            "${STATE_EXPERIMENT.value.name}",
                            modifier = Modifier.padding(top = (10).dp, start = 20.dp).clickable {
                            },
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    }



                    Box(Modifier.clickable {
                        CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                            delay(10)
                            pauseSerialComm()
                            scenario.clear()
                        }
                        screenNav.value = Screens.STARTER
                    }) {
                        Text(
                            "Home↩️",
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
//                    Text("", modifier = Modifier.padding(top = (10).dp,start = 20.dp)
//                        , fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.Light, color = Color.DarkGray
//                    )

                    Text(
                        "${txt.value}",
                        modifier = Modifier.width(90.dp).padding(top = (10).dp, start = 20.dp).clickable {
                            //screenNav.value = Screens.STARTER
                        },
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )

                    Text("${COM_PORT},${BAUD_RATE},${limitTime}ms", modifier = Modifier.padding(top = (10).dp,start = 20.dp)
                        , fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.Light, color = Color.DarkGray
                    )

                    Box(Modifier.clickable {
                        expandedCom.value = !expandedCom.value
                    }) {
                        Text(
                            "Mode: ${explMode.value.name}",
                            modifier = Modifier.padding(top = (10).dp, start = 20.dp),
                            fontFamily = FontFamily.Default, fontSize = 20.sp,
                            fontWeight = FontWeight.Bold, color = Color.White
                        )

                        DropdownMenu(
                            modifier = Modifier.background(Color.White),
                            expanded = expandedCom.value,
                            onDismissRequest = { expandedCom.value = false },
                        ) {
                            Text(
                                "AUTO", fontSize = 18.sp, modifier = Modifier.fillMaxSize().padding(10.dp)
                                    .clickable(onClick = {
                                        EXPLORER_MODE.value = ExplorerMode.AUTO
                                    }), color = Color.Black
                            )
                            Text(
                                "MANUAL", fontSize = 18.sp, modifier = Modifier.fillMaxSize().padding(10.dp)
                                    .clickable(onClick = {
                                        EXPLORER_MODE.value = ExplorerMode.MANUAL
                                    }), color = Color.Black
                            )
                        }
                    }

                    Box(Modifier.clickable {
                        showBottomPanel.value = !showBottomPanel.value
                        SHOW_BOTTOM_PANEL = showBottomPanel.value
                    }) {
                        Text(
                            "Hide Currents⚡️",
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                }
                /**
                 * SOLENOIDS PANEL
                 */
                SolenoidsPanel(sizeRow = sizeRow, duration = duration)
            }
        } else {
            Row(Modifier.fillMaxSize().background(Color.DarkGray).weight(0.2f), horizontalArrangement = Arrangement.SpaceAround) {
                if (isExperimentStarts) {
                    Text(
                        "Rec...",
                        modifier = Modifier.padding(top = (10).dp, start = 20.dp).clickable {
                        },
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                AnimatedVisibility(isPayloadComing.value) {
                    Box(Modifier.clickable {
                        test_time = 0
                        // launch
                        if (explMode.value == ExplorerMode.AUTO) {
                            launchPlay()
                        } else if (explMode.value == ExplorerMode.MANUAL) {
                            indexOfScenario.value--
                            ctxScope.launch {

                                comparatorToSolenoid(indexOfScenario.value)
                            }
                            scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.comment }
                            //txtOfScenario.value = scenario.getOrNull(indexOfScenario.value)?.text
                            //txtOfScenario.value = scenario[indexOfScenario.value].text
                        }


                    }) {
                        Text(
                            if (explMode.value == ExplorerMode.AUTO) "▶" else "⏪",
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(Modifier.clickable {
                    //stop scenario

                    CoroutineScope(Dispatchers.IO).launch {
                        if (explMode.value == ExplorerMode.AUTO) {
                            reInitSolenoids()
                            GLOBAL_STATE.value = StateParseBytes.WAIT
//                            initSerialCommunication()
//                            startReceiveFullData()
                        } else if (explMode.value == ExplorerMode.MANUAL) {
                            indexOfScenario.value++
                            comparatorToSolenoid(indexOfScenario.value)

                            //txtOfScenario.value = scenario.getOrElse(indexOfScenario.value) { 0 }
                            scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.comment }
                            //txtOfScenario.value = scenario.getOrElse(indexOfScenario.value) { scenario[0] }.text
                        }
                    }
                }) {
                    Text(
                        if (explMode.value == ExplorerMode.AUTO) "⏸" else "⏩",
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(Modifier.clickable {
                    CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                        delay(10)
                        pauseSerialComm()
                        scenario.clear()
                    }
                    screenNav.value = Screens.STARTER
                }) {
                    Text(
                        "Home↩️",
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(Modifier.clickable {
                    showBottomPanel.value = !showBottomPanel.value
                    SHOW_BOTTOM_PANEL = showBottomPanel.value
                }) {
                    Text(
                        "Open Currents⚡️",
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = (10).dp, start = 20.dp),
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
