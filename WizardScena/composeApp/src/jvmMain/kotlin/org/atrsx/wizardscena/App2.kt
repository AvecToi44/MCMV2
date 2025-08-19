package org.atrsx.wizardscena

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.Save
import compose.icons.feathericons.Star
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.atrsx.wizardscena.ui.MainScenarioScreen
import org.atrsx.wizardscena.ui.PressuresScreen
import org.atrsx.wizardscena.ui.SolenoidsScreen
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


// ---------- App ----------
//fun main() = application {
//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "Scenarios Wizard MCM [1.0.0]",
//        state = WindowState(size = DpSize(1200.dp, 800.dp))
//    ) {
//        MaterialTheme {
//            Surface(Modifier.fillMaxSize()) {
//                AppRoot()
//            }
//        }
//    }
//}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(0) }
    var mainConfig by remember { mutableStateOf(MAIN_CONFIG) }
    var lastChangesSaved by remember { LAST_CHANGES_SAVED }

    var scenarioFileName by remember { mutableStateOf(MAIN_CONFIG.value.sheetName) }
    var standardName by remember { mutableStateOf(MAIN_CONFIG.value.standardPath) }

    val tabs = listOf("Main Scenario", "Pressures", "Currents")
    Column(Modifier.fillMaxSize()) {
        if (lastChangesSaved.isNotEmpty()) {
            Row {
                Text("Сохранение (${LAST_FILE_SAVED}) было крайний раз в ${lastChangesSaved}", color = Color.LightGray, fontSize = 8.sp)
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {


            LaunchedEffect(mainConfig.value) {
                println("!!!!!!!")
                scenarioFileName = mainConfig.value.sheetName
                standardName = mainConfig.value.standardPath
            }

            TextField(
                value = scenarioFileName,
                onValueChange = { newText ->
                    scenarioFileName = newText
                    MAIN_CONFIG.value.sheetName = newText
                },
                label = { Text("Future scenario name") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .border(2.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                        .clickable {
                            CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                                delay(10)
                                val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
                                dialog.isVisible = true
                                val file = dialog.file

                                standardName = File(dialog.directory, file).absolutePath
                                MAIN_CONFIG.value.standardPath = standardName
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = FeatherIcons.Star,
                        contentDescription = "Star",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("${standardName}")
            }
            if (scenarioFileName.isNotEmpty()) {
                Box(
                    Modifier
                        .size(40.dp)
                        .border(2.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                        .clickable {
                            CoroutineScope(Dispatchers.IO+CoroutineName("onCloseRequest")).launch {
                                delay(10)

                                ExcelExporter.saveWithDialog()?.let { ExcelExporter.export(
                                    MAIN_CONFIG.value.copy(
                                        pressures = PressuresBlockDto(pressures),
                                        solenoids = SolenoidsBlock(channels = solenoids),
                                        scenario = ScenarioBlockDto(steps = scenarios.toDtoList())
                                    ), it) }
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
