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
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.swing.Swing
import ru.atrs.mcm.launchPlay
import ru.atrs.mcm.serial_port.comparatorToSolenoid
import ru.atrs.mcm.serial_port.incrX
import ru.atrs.mcm.serial_port.pauseSerialComm
import ru.atrs.mcm.serial_port.reInitSolenoids
import ru.atrs.mcm.serial_port.sendScenarioToController
import ru.atrs.mcm.serial_port.startReceiveFullData
import ru.atrs.mcm.storage.createMeasureExperiment
import ru.atrs.mcm.ui.charts.Pointer
import ru.atrs.mcm.ui.custom.GaugeX
import ru.atrs.mcm.ui.main_screen.center.support_elements.solenoidsPanel
import ru.atrs.mcm.ui.navigation.Screens
import ru.atrs.mcm.ui.screenNav
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.GLOBAL_STATE
import ru.atrs.mcm.utils.SHOW_BOTTOM_PANEL
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.arr10Measure
import ru.atrs.mcm.utils.arr11Measure
import ru.atrs.mcm.utils.arr12Measure
import ru.atrs.mcm.utils.arr1Measure
import ru.atrs.mcm.utils.arr2Measure
import ru.atrs.mcm.utils.arr3Measure
import ru.atrs.mcm.utils.arr4Measure
import ru.atrs.mcm.utils.arr5Measure
import ru.atrs.mcm.utils.arr6Measure
import ru.atrs.mcm.utils.arr7Measure
import ru.atrs.mcm.utils.arr8Measure
import ru.atrs.mcm.utils.arr9Measure
import ru.atrs.mcm.utils.dataChunkGauges
import ru.atrs.mcm.utils.incrementTime
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.isExperimentStarts
import ru.atrs.mcm.utils.limitTime
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.mapFloat
import ru.atrs.mcm.utils.pressures
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.test_time
import ru.atrs.mcm.utils.txtOfScenario


@Composable
fun CenterPiece(
) {
    var sizeRow    by remember {mutableStateOf(Size.Zero)}
    var pressure1X by remember { mutableStateOf(-1f) }
    var pressure2X by remember { mutableStateOf(-1f) }
    var pressure3X by remember { mutableStateOf(-1f) }
    var pressure4X by remember { mutableStateOf(-1f) }
    var pressure5X by remember { mutableStateOf(-1f) }
    var pressure6X by remember { mutableStateOf(-1f) }
    var pressure7X by remember { mutableStateOf(-1f) }
    var pressure8X by remember { mutableStateOf(-1f) }

    var pressure9X by remember  { mutableStateOf(-1f) }
    var pressure10X by remember { mutableStateOf(-1f) }
    var pressure11X by remember { mutableStateOf(-1f) }
    var pressure12X by remember { mutableStateOf(-1f) }
    val duration = MutableStateFlow(100L)

    val stateChart = remember { STATE_EXPERIMENT }
    val explMode = remember { EXPLORER_MODE }
    val expandedCom = remember { mutableStateOf(false) }
    val showBottomPanel = remember { mutableStateOf(SHOW_BOTTOM_PANEL) }

    val txt = remember { txtOfScenario }

    val ctxScope =
        CoroutineScope(Dispatchers.Swing) + rememberCoroutineScope().coroutineContext + CoroutineName("MainScreen-CenterPart")

    // Get local density from composable
    val localDensity = LocalDensity.current

    // Create element height in dp state
    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }
    var isShowPlay = remember { mutableStateOf(false) }
    LaunchedEffect(true) {
        ctxScope.launch {
            //EXPLORER_MODE.value = ExplorerMode.MANUAL
            //reInitSolenoids()
            indexOfScenario.value = 0

            //sound_On()
            startReceiveFullData()
            comparatorToSolenoid(indexOfScenario.value)
            sendScenarioToController()
            var count = 0
            dataChunkGauges.collect {
                isShowPlay.value = true
                //delay(DELAY_FOR_GET_DATA)
                logGarbage(">>>> ${it.toString()}")
                //logGarbage("dataChunkGauges> ${it.toString()} ||sizes:${arr1Measure.size} ${dataChunkGauges.replayCache.size} ${solenoids.size} ${pressures.size} ${scenario.size}")


                //println("|<<<<<<<<<<<<<<<<<<<${it.isExperiment} [${it.firstGaugeData}]")
                mapFloat(it.firstGaugeData, 0f, 4095f, (pressures[0].minValue), (pressures[0].maxValue),).let { pressure1X = it }

//                pressure1X = mapFloat(it.firstGaugeData, 0f, 4095f, (pressures[0].minValue), (pressures[0].maxValue),)
                pressure2X = mapFloat(it.secondGaugeData, 0f, 4095f, (pressures[1].minValue), (pressures[1].maxValue))
                pressure3X = mapFloat(it.thirdGaugeData, 0f, 4095f, (pressures[2].minValue), (pressures[2].maxValue))
                pressure4X = mapFloat(it.fourthGaugeData, 0f, 4095f, (pressures[3].minValue), (pressures[3].maxValue))
                pressure5X = mapFloat(it.fifthGaugeData, 0f, 4095f, (pressures[4].minValue), (pressures[4].maxValue))
                pressure6X = mapFloat(it.sixthGaugeData, 0f, 4095f, (pressures[5].minValue), (pressures[5].maxValue))
                pressure7X = mapFloat(it.seventhGaugeData, 0f, 4095f, (pressures[6].minValue), (pressures[6].maxValue))
                pressure8X = mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[7].minValue), (pressures[7].maxValue))

                if (pressures.getOrNull(8) != null) {
                    pressure9X = mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[8].minValue), (pressures[8].maxValue))
                }
                if (pressures.getOrNull(9) != null) {
                    pressure10X = mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[9].minValue), (pressures[9].maxValue))
                }
                if (pressures.getOrNull(10) != null) {
                    pressure11X = mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[10].minValue), (pressures[10].maxValue))
                }
                if (pressures.getOrNull(11) != null) {
                    pressure12X = mapFloat(it.eighthGaugeData, 0f, 4095f, (pressures[11].minValue), (pressures[11].maxValue))
                }
                when (EXPLORER_MODE.value) {
                    ExplorerMode.AUTO -> {
                        //logGarbage("konec ${}")
                        if (
                            //limitTime >= incrementTime &&
                            (it.isExperiment)
                        ) {
                            count++

                            arr1Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure1X)).takeIf { pressure1X > 0f } //it.firstGaugeData, ))
                            arr2Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure2X)).takeIf { pressure2X > 0f } //it.secondGaugeData,))
                            arr3Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure3X)).takeIf { pressure3X > 0f } //it.thirdGaugeData, ))
                            arr4Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure4X)).takeIf { pressure4X > 0f } //it.fourthGaugeData,))
                            arr5Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure5X)).takeIf { pressure5X > 0f } //it.fifthGaugeData, ))
                            arr6Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure6X)).takeIf { pressure6X > 0f } //it.sixthGaugeData, ))
                            arr7Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure7X)).takeIf { pressure7X > 0f } //it.seventhGaugeData))
                            arr8Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure8X)).takeIf { pressure8X > 0f } //it.eighthGaugeData, ))

                            arr9Measure.add(Pointer(x = incrementTime.toFloat(),   y = pressure9X)).takeIf { pressure9X > 0f }  //it.eighthGaugeData, ))
                            arr10Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure10X)).takeIf { pressure10X > 0f } //it.eighthGaugeData, ))
                            arr11Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure11X)).takeIf { pressure11X > 0f } //it.eighthGaugeData, ))
                            arr12Measure.add(Pointer(x = incrementTime.toFloat(), y = pressure12X)).takeIf { pressure12X > 0f } //it.eighthGaugeData, ))


//                            num = scenario[indexScenario].time
//
//                            if (num > 0) {
//                                // 2 - is recieve data every 2ms
//                                num -= 2
//                            } else {
//                                indexScenario++
//                                num = scenario[indexScenario].time
//                                txt.value = scenario[indexScenario].text
//                            }
                            incrementTime += 2
                            //test_time += 2

                        } else if (STATE_EXPERIMENT.value == StateExperiments.PREP_DATA) {
                            logGarbage("Output: |${incrX}|=>|${count}|  | ${arr1Measure.size} ${arr1Measure[arr1Measure.lastIndex]}")

                            STATE_EXPERIMENT.value = StateExperiments.PREPARE_CHART
                            incrementTime = 0
                            if (!isAlreadyReceivedBytesForChart.value) {
                                isAlreadyReceivedBytesForChart.value = true
                                createMeasureExperiment()
                            }


                        }
                    }
                    ExplorerMode.MANUAL -> { /** without recording */ }
                }
            }
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
                modifier = Modifier.fillMaxWidth(),
                //columns = GridCells.Adaptive(150.dp),
                columns = GridCells.Fixed(4),
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
                            Box(Modifier
                                .aspectRatio(1f)
                                .onGloballyPositioned { coordinates ->
                                    // Set column height using the LayoutCoordinates
                                    if (coordinates.size.width != 0) {
                                        columnHeightDp = with(localDensity) { coordinates.size.width.toDp() }
                                    }

                                }
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


                }
            )

        }
        if(showBottomPanel.value) {
            Row(Modifier.fillMaxSize().weight(1.5f), horizontalArrangement = Arrangement.SpaceAround) {

                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
                    if (isExperimentStarts.value) {
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

                    AnimatedVisibility(isShowPlay.value) {
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
                                scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
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
                                scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
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
                }

                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
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

                solenoidsPanel(sizeRow = sizeRow, duration = duration)
            }
        } else {
            Row(Modifier.fillMaxSize().background(Color.DarkGray).weight(0.2f), horizontalArrangement = Arrangement.SpaceAround) {
                if (isExperimentStarts.value) {
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

                AnimatedVisibility(isShowPlay.value) {
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
                            scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
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
                            scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
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
