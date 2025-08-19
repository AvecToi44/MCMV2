package ru.atrsx.mcmcomposer

import PressuresScreen
import SolenoidsScreen
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import compose.icons.FeatherIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.Save
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrsx.mcmcomposer.ui.MainScenarioScreen

// ---------- Models ----------
data class ScenarioRow(
    var number: Int = 0,
    var name: String = "",
    var passThrough: Boolean = true,
    var durationMs: String = "0",
    var messageText: String = "",
    var interpolationFlags: MutableList<Boolean> = MutableList(16) { false },
    var pressureHighlightFlags: MutableList<Boolean> = MutableList(16) { false },
    var analogSetEnabled: Boolean = false
)


data class PWMChannel(
    val index: Int,
    var used: Boolean = true,
    var color: Color,
    var displayName: String = "",
    var maxPwm: String = "",
    var tolerance: String = "",
    var frequency: String = "",
    var isDC: Boolean = false,
    var expectedTestValue: String = ""
)

// ---------- App ----------
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Test Console",
        state = WindowState(size = DpSize(1200.dp, 800.dp))
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(0) }

    val tabs = listOf("Main Scenario", "Pressures", "Currents")
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
            var text by remember { mutableStateOf("") }
            TextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    MAIN_CONFIG.value.sheetName = newText
                },
                label = { Text("Future scenario name") }
            )
            var text2 by remember { mutableStateOf("") }
            TextField(
                value = text2,
                onValueChange = { newText ->
                    text2 = newText
                    MAIN_CONFIG.value.standardPath = newText
                },
                label = { Text("Path to Standard") }
            )
            Box(
                Modifier
                    .size(40.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                    .clickable {
                        CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                            delay(10)

                            ExcelExporter.saveWithDialog()?.let { ExcelExporter.export(MAIN_CONFIG.value, it) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = FeatherIcons.Save,
                    contentDescription = "Save",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                Modifier
                    .size(40.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                    .clickable {
                        CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                            delay(10)
                            val ok = ExcelImporter.openAndImportIntoState(
                                parent = null, // or your Window.frame
                                pressuresState = pressures,
                                solenoidsState = solenoids,
                                scenariosState = scenarios,
                                mainConfigState = MAIN_CONFIG
                            )

                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = FeatherIcons.File,
                    contentDescription = "Open new",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> MainScenarioScreen()
            1 -> PressuresScreen()
            2 -> SolenoidsScreen()
        }
    }
}
