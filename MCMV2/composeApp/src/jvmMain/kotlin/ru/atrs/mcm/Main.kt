package ru.atrs.mcm// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.atrs.mcm.enums.ExplorerMode
import kotlinx.coroutines.*
import ru.atrs.mcm.serial_port.RouterCommunication
import ru.atrs.mcm.serial_port.flowRawComparatorMachine
import ru.atrs.mcm.serial_port.flowWriterMachine
import ru.atrs.mcm.ui.App
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.storage.initialize
import ru.atrs.mcm.storage.readParametersJson
import ru.atrs.mcm.ui.chartsv3.AppChartV3
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.Dir1Configs
import ru.atrs.mcm.utils.EXPLORER_MODE
import ru.atrs.mcm.utils.SHOW_FULLSCREEN
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow
import ru.atrs.mcm.utils.getComPorts_Array
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.logError
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val APP_NAME = "MCM 1.2.24"
    // Attempt to acquire a named mutex or file lock
    if (isAnotherInstanceRunning(APP_NAME)) {
        println("Another instance is already running. Exiting.")
        exitApplication()
        return@application
    }

    initialize(readParametersJson())

    val windowStateFullscreen = rememberWindowState(
        placement = if (SHOW_FULLSCREEN) WindowPlacement.Maximized else WindowPlacement.Floating
    )
    val windowFloating = rememberWindowState(width = 1000.dp, height = 800.dp)
    println("window: ${SHOW_FULLSCREEN}")
    LaunchedEffect(Unit) {
        flowRawComparatorMachine()
    }
    LaunchedEffect(Unit) {
        // FLOW
        flowWriterMachine()
    }

    Window(
        title = APP_NAME,
        state = if (SHOW_FULLSCREEN) windowStateFullscreen else windowFloating,
//        icon = Res.drawable.ava, //painterResource("resources/icon1.png"),
        onCloseRequest = {
            CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                RouterCommunication.stopSerialCommunication()
                delay(2000)
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


//            var isHaveConn = false
//            getComPorts_Array()?.forEach {
//                if (it.systemPortName == COM_PORT) {
//                    isHaveConn = true
//                }
//            }
//            if (!isHaveConn) {
//                showMeSnackBar("NO Connect to ${COM_PORT} !!", Color.Red)
//            }

            App()

            if (EXPLORER_MODE.value == ExplorerMode.AUTO) {
                if (doOpenNewWindowInternal.value && isAlreadyReceivedBytesForChart.value) {
                    AppChartV3().WindowChartsV3(analysisAfterExperiment = true)
                }
            }

            if (doOpenNewWindowInternal2.value) {
                AppChartV3().WindowChartsV3(analysisAfterExperiment = false)
            }
        }
    }
}

// Conceptual function for checking if another instance is running
// In a real application, this would use platform-specific named mutexes or file locking.
private fun isAnotherInstanceRunning(appName: String): Boolean {
    // Example using a simple file lock (less robust than named mutex)
    val lockFile = File(System.getProperty("java.io.tmpdir"), "$appName.lock")
    try {
        val fileChannel = RandomAccessFile(lockFile, "rw").channel
        val lock: FileLock? = fileChannel.tryLock()
        if (lock == null) {
            return true // Another instance has the lock
        } else {
            // Register a shutdown hook to release the lock when the application exits
            Runtime.getRuntime().addShutdownHook(Thread {
                lock.release()
                fileChannel.close()
                lockFile.delete()
            })
            return false // This instance acquired the lock
        }
    } catch (e: Exception) {
        logError("isAnotherInstanceRunning: ${e.message}")
        showMeSnackBar("isAnotherInstanceRunning: ${e.message}", color = Color.Red)

        return true // Assume another instance is running on error
    }
}