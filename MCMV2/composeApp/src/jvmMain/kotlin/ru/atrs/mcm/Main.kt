package ru.atrs.mcm// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import MCMV2.composeApp.BuildConfig
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.atrs.mcm.enums.ExplorerMode
import kotlinx.coroutines.*
import ru.atrs.mcm.ui.App
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.serial_port.comparatorToSolenoid
import ru.atrs.mcm.serial_port.pauseSerialComm
import ru.atrs.mcm.storage.initialize
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
        title = "MCM [${generateTimestampLastUpdate()}] ${BuildConfig.APP_VERSION}",
        state = if (SHOW_FULLSCREEN) windowStateFullscreen else windowFloating,
//        icon = painterResource("drawable/ava.png"),
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
            val doOpenSettingsWindowInternal2 = remember { mutableStateOf(false) }



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
            App()

            if (EXPLORER_MODE.value == ExplorerMode.AUTO) {
                if (doOpenNewWindowInternal.value && isAlreadyReceivedBytesForChart.value) {
                    AppChartV3().WindowChartsV3(analysisAfterExperiment = true)
                }
//                if (doOpenNewWindowInternal.value && isAlreadyReceivedBytesForChart.value) {
//                    ChartWindowDeprecated(withStandard = true).chartWindow()
//                }
            }

            if (doOpenNewWindowInternal2.value) {
                AppChartV3().WindowChartsV3(analysisAfterExperiment = false)
            }
//            if (doOpenSettingsWindowInternal2.value) {
//                Window(
//                    title = "Settings",
//                    state = WindowState(size = DpSize(1000.dp, 800.dp)),
//                    onCloseRequest = {  }
//                ) {
//                    Column {
//
//                    }
//                }
//            }
        }
    }
}

fun startTimer() {
    fixedRateTimer("timer_2", daemon = true, 0L,1000L) {

        //timeOfMeasure.value += 1
    }
}