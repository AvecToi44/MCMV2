package org.atrsx.wizardscena

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val appName = "WizardScena ${BuildConfig.APP_VERSION}"

    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        icon = painterResource("iconapp.png"),
    ) {
        AppRoot()
    }
}
