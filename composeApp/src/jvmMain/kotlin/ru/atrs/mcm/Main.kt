package ru.atrs.mcm// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.atrs.mcm.enums.ExplorerMode
import kotlinx.coroutines.*
import ru.atrs.mcm.ui.App
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.serial_port.comparatorToSolenoid
import ru.atrs.mcm.serial_port.pauseSerialComm
import ru.atrs.mcm.storage.initialize
import ru.atrs.mcm.storage.readParameters
import ru.atrs.mcm.storage.readParametersJson
import ru.atrs.mcm.ui.charts.ChartWindowDeprecated
import ru.atrs.mcm.ui.chartsv3.AppChartV3
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.Dir1Configs
import ru.atrs.mcm.utils.Dir4MainConfig_Txt
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow
import ru.atrs.mcm.utils.generateTimestampLastUpdate
import ru.atrs.mcm.utils.getComPorts_Array
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.txtOfScenario
import kotlin.concurrent.fixedRateTimer


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    initialize(readParametersJson())

    val windowStateFullscreen = rememberWindowState(
        placement = if (SHOW_FULLSCREEN) WindowPlacement.Maximized else WindowPlacement.Floating
    )
    val windowFloating = rememberWindowState(width = 1000.dp, height = 800.dp)
    println("window: ${SHOW_FULLSCREEN}")
    Window(
        title = "MCM [${generateTimestampLastUpdate()}]",
        state = if (SHOW_FULLSCREEN) windowStateFullscreen else windowFloating,
//        icon = painterResource("drawable/ava.png"),
        onKeyEvent = {
             if ( it.key == Key.DirectionRight && it.type == KeyEventType.KeyUp) {

                //shiftIsPressed = true

                 CoroutineScope(Dispatchers.IO).launch {
                     indexOfScenario.value++

                     comparatorToSolenoid(indexOfScenario.value)

                     //txtOfScenario.value = scenario.getOrElse(indexOfScenario.value) { 0 }
                     scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
                 }
                 true
            } else if (it.key == Key.DirectionLeft &&  it.type == KeyEventType.KeyUp) {
                //shiftIsPressed = false
                 indexOfScenario.value--
                 CoroutineScope(Dispatchers.IO).launch {

                     comparatorToSolenoid(indexOfScenario.value)
                 }
                 scenario.getOrNull(indexOfScenario.value)?.let { txtOfScenario.value = it.text }
                true
            }else if (it.key == Key.CtrlLeft && it.key == Key.Spacebar &&  it.type == KeyEventType.KeyDown) {
                 //shiftIsPressed = false
                 launchPlay()
                 true
            }else if (it.key == Key.N &&  it.type == KeyEventType.KeyDown && it.key == Key.CtrlLeft) {
                 //shiftIsPressed = false
                 openNewScenario()
                 true
            }else if (it.key == Key.L &&  it.type == KeyEventType.KeyDown && it.key == Key.CtrlLeft) {
                 //shiftIsPressed = false
                 openLastScenario()
                 true
            }else if (it.key == Key.V &&  it.type == KeyEventType.KeyDown && it.key == Key.CtrlLeft) {
                 //shiftIsPressed = false
                 openChartViewer()
                 true
            }

             else {
                // let other handlers receive this event
                false
            }
        },
        onCloseRequest = {
            CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                pauseSerialComm()
                delay(500)
                exitApplication()
            }

        },
    ) {
        // Use ApplicationScope as receiver
        with(this@Window) {
            val doOpenNewWindowInternal = remember { doOpen_First_ChartWindow }
            val doOpenNewWindowInternal2 = remember { doOpen_Second_ChartWindow }



            println("Dir1 ${Dir1Configs.absolutePath}")



            var isHaveConn = false
            getComPorts_Array()?.forEach {
                if (it.systemPortName == COM_PORT) {
                    isHaveConn = true
                }
            }
            if (!isHaveConn) {
                showMeSnackBar("NO Connect to ${COM_PORT} !!", Color.Red)
            }
//            AppChartV3().WindowChartsV3()
            App()

            if (EXPLORER_MODE.value == ExplorerMode.AUTO) {
                if (doOpenNewWindowInternal.value && isAlreadyReceivedBytesForChart.value) {
                    AppChartV3().WindowChartsV3()
                }
//                if (doOpenNewWindowInternal.value && isAlreadyReceivedBytesForChart.value) {
//                    ChartWindowDeprecated(withStandard = true).chartWindow()
//                }
            }

            if (doOpenNewWindowInternal2.value) {
                ChartWindowDeprecated(withStandard = true, isViewerOnly = true).chartWindow()
            }
        }
    }
}

fun startTimer() {
    fixedRateTimer("timer_2", daemon = true, 0L,1000L) {

        //timeOfMeasure.value += 1
    }
}