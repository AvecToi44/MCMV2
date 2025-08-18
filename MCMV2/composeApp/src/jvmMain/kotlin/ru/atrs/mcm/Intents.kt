package ru.atrs.mcm

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.enums.StateParseBytes
import ru.atrs.mcm.parsing_excel.targetParseScenario
import ru.atrs.mcm.serial_port.RouterCommunication
import ru.atrs.mcm.ui.screenNav
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.storage.PickTarget
import ru.atrs.mcm.storage.generateNewChartLogFile
import ru.atrs.mcm.storage.openPicker
import ru.atrs.mcm.storage.refreshJsonParameters
import ru.atrs.mcm.ui.navigation.Screens
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir3Scenarios
import ru.atrs.mcm.utils.GLOBAL_STATE
import ru.atrs.mcm.utils.LAST_SCENARIO
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.indexScenario
import ru.atrs.mcm.utils.isAlreadyReceivedBytesForChart
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.sound_Error
import ru.atrs.mcm.utils.sound_On
import ru.atrs.mcm.utils.test_time

fun launchPlay() {
    if (STATE_EXPERIMENT.value != StateExperiments.RECORDING) {

        CoroutineScope(Dispatchers.IO).launch {
            RouterCommunication.cleanCOMPort()
            delay(2000)
            RouterCommunication.writeToSerialPort(
                byteArrayOf(
                    0x78,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00
                ), withFlush = false
            )
            generateNewChartLogFile()
        }

        GLOBAL_STATE.value = StateParseBytes.PLAY
        sound_On()
        logGarbage("ONON ${test_time} V")
        test_time = 0

        indexOfScenario.value = 0
        indexScenario = 0
        isAlreadyReceivedBytesForChart.value = false
        logGarbage("Start Play ${test_time} A")

    } else {
        sound_Error()
    }
}


fun openNewScenario() {
    CoroutineScope(Dispatchers.Default).launch {
        isAlreadyReceivedBytesForChart.value = false
        refreshJsonParameters()
        if (!targetParseScenario(openPicker(Dir3Scenarios))) {
            //showMeSnackBar("Ошибка при парсинге xls", Color.Red)
        }else {
            screenNav.value = Screens.MAIN
        }
    }
}

fun openLastScenario() {
    CoroutineScope(Dispatchers.Default).launch {
        if (targetParseScenario(LAST_SCENARIO.value)) {
            screenNav.value = Screens.MAIN
        } else {
            showMeSnackBar("Last scenario NO DEFINED!", color = Color.Red)
        }
    }
}

fun openChartViewer() {
    CoroutineScope(Dispatchers.Default).launch {
        openPicker(Dir2Reports, PickTarget.PICK_CHART_VIEWER,isOnlyViewer = true)?.let { chartFileAfterExperiment.value = it }




    }
}