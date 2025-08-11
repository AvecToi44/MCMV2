package ru.atrs.mcm.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import ru.atrs.mcm.ui.chartsv3.App
import ru.atrs.mcm.utils.doOpen_First_ChartWindow

@Composable
fun WindowSettings() {
    Window(
        title = "Settings",
        state = WindowState(size = DpSize(1000.dp, 800.dp)),
        onCloseRequest = {  }
    ) {
    }
}