package ru.atrsx.mcmcomposer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Scenarios Wizard MCM [1.0.0]",
        state = WindowState(size = DpSize(1200.dp, 800.dp))
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                AppRoot()
            }
        }
    }
}