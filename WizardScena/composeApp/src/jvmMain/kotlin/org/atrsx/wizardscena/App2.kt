package org.atrsx.wizardscena

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.Globe
import compose.icons.feathericons.Save
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Star
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.atrsx.wizardscena.ui.MainScenarioScreen
import org.atrsx.wizardscena.ui.PressuresScreen
import org.atrsx.wizardscena.ui.SolenoidsScreen
import org.atrsx.wizardscena.ui.FrequencyParamsScreen
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

private enum class RootScreen {
    START,
    EDITOR
}

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(RootScreen.START) }
    var openError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun openScenario(file: File) {
        scope.launch(Dispatchers.IO + CoroutineName("openScenarioFromStart")) {
            val ok = runCatching {
                ExcelImporter.importFromFile(
                    file = file,
                    pressuresState = pressures,
                    solenoidsState = solenoids,
                    scenariosState = scenarios,
                    mainConfigState = MAIN_CONFIG
                )
            }.getOrElse { false }

            withContext(Dispatchers.Main) {
                if (ok) {
                    RecentScenariosStore.add(file.absolutePath)
                    openError = ""
                    screen = RootScreen.EDITOR
                } else {
                    openError = AppI18n.text("start_open_failed")
                }
            }
        }
    }

    when (screen) {
        RootScreen.START -> StartScreen(
            onOpenClick = {
                val file = ExcelImporter.chooseExcelScenarioFile(null) ?: return@StartScreen
                openScenario(file)
            },
            onCreateNew = {
                openError = ""
                screen = RootScreen.EDITOR
            },
            onOpenRecent = { path ->
                val file = File(path)
                if (!file.exists()) {
                    RecentScenariosStore.remove(path)
                    openError = AppI18n.text("start_open_failed")
                    return@StartScreen
                }
                openScenario(file)
            },
            openError = openError
        )

        RootScreen.EDITOR -> EditorScreen(
            onOpen = {
                val file = ExcelImporter.chooseExcelScenarioFile(null) ?: return@EditorScreen
                openScenario(file)
            },
            onBackToStart = {
                openError = ""
                screen = RootScreen.START
            }
        )
    }
}

@Composable
private fun StartScreen(
    onOpenClick: () -> Unit,
    onCreateNew: () -> Unit,
    onOpenRecent: (String) -> Unit,
    openError: String
) {
    val recents = RecentScenariosStore.recentPaths.toList().take(5)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                LanguageSettingsButton()
            }

            ImageTitleHeader()

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onOpenClick) {
                    Icon(imageVector = FeatherIcons.File, contentDescription = tr("start_open"))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("start_open"))
                }

                Button(onClick = onCreateNew) {
                    Icon(imageVector = FeatherIcons.Save, contentDescription = tr("start_create"))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("start_create"))
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(tr("start_recent"), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            if (recents.isEmpty()) {
                Text(
                    tr("start_recent_empty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recents.forEach { path ->
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenRecent(path) }
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(File(path).name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    path,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (openError.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(openError, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ImageTitleHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource("iconapp.png"),
            contentDescription = tr("app_title"),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(tr("app_title"), style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun LanguageSettingsButton() {
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { showLanguageMenu = true }) {
            Icon(imageVector = FeatherIcons.Settings, contentDescription = tr("btn_settings"))
            Spacer(Modifier.width(6.dp))
            Text(tr("btn_settings"))
        }

        DropdownMenu(
            expanded = showLanguageMenu,
            onDismissRequest = { showLanguageMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = FeatherIcons.Globe, contentDescription = tr("language_ru"))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("language_ru"))
                    }
                },
                onClick = {
                    AppI18n.setLanguage(AppLanguage.RU)
                    showLanguageMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = FeatherIcons.Globe, contentDescription = tr("language_en"))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("language_en"))
                    }
                },
                onClick = {
                    AppI18n.setLanguage(AppLanguage.EN)
                    showLanguageMenu = false
                }
            )
        }
    }
}

@Composable
private fun EditorScreen(
    onOpen: () -> Unit,
    onBackToStart: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    var mainConfig by remember { mutableStateOf(MAIN_CONFIG) }
    var lastChangesSaved by remember { LAST_CHANGES_SAVED }

    var standardName by remember { mutableStateOf(MAIN_CONFIG.value.standardPath) }

    val tabs = listOf(
        tr("tab_main"),
        tr("tab_pressures"),
        tr("tab_currents"),
        tr("tab_freq_params")
    )
    Column(Modifier.fillMaxSize()) {
        if (lastChangesSaved.isNotEmpty()) {
            Row {
                Text(
                    AppI18n.textf("last_save", LAST_FILE_SAVED, lastChangesSaved),
                    color = Color.LightGray,
                    fontSize = 8.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            LaunchedEffect(mainConfig.value) {
                standardName = mainConfig.value.standardPath
            }

            Text(
                text = standardName.ifBlank { tr("standard_not_selected") },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            )

            OutlinedButton(
                onClick = {
                    CoroutineScope(Dispatchers.IO + CoroutineName("selectStandardFile")).launch {
                        delay(10)
                        val dialog = FileDialog(null as Frame?, AppI18n.text("choose_standard_dialog"), FileDialog.LOAD)
                        dialog.filenameFilter = FilenameFilter { _, name -> name.lowercase().endsWith(".txt") }
                        dialog.isVisible = true
                        val file = dialog.file ?: return@launch
                        standardName = File(dialog.directory, file).absolutePath
                        MAIN_CONFIG.value.standardPath = standardName
                    }
                }
            ) {
                Icon(imageVector = FeatherIcons.Star, contentDescription = tr("btn_standard"))
                Spacer(Modifier.width(6.dp))
                Text(tr("btn_standard"))
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    val targetName = MAIN_CONFIG.value.sheetName.trim().ifBlank { AppI18n.text("default_scenario_name") }
                    MAIN_CONFIG.value.sheetName = targetName

                    CoroutineScope(Dispatchers.IO + CoroutineName("saveExcel")).launch {
                        delay(10)
                        ExcelExporter.saveWithDialog(suggestedName = "$targetName.xls")?.let {
                            ExcelExporter.export(
                                MAIN_CONFIG.value.copy(
                                    pressures = PressuresBlockDto(pressures),
                                    solenoids = SolenoidsBlock(
                                        mainFrequencyHz = MAIN_CONFIG.value.solenoids.mainFrequencyHz,
                                        frequencyParams0x68 = MAIN_CONFIG.value.solenoids.frequencyParams0x68.toMutableList(),
                                        channels = solenoids
                                    ),
                                    scenario = ScenarioBlockDto(steps = scenarios.toDtoList())
                                ),
                                it
                            )
                        }
                    }
                }
            ) {
                Icon(imageVector = FeatherIcons.Save, contentDescription = tr("btn_save_excel"))
                Spacer(Modifier.width(6.dp))
                Text(tr("btn_save_excel"))
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onOpen) {
                Icon(imageVector = FeatherIcons.File, contentDescription = tr("btn_open_excel"))
                Spacer(Modifier.width(6.dp))
                Text(tr("btn_open_excel"))
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onBackToStart) {
                Text(tr("btn_back_to_start"))
            }

            Spacer(Modifier.width(8.dp))
            LanguageSettingsButton()
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
            3 -> FrequencyParamsScreen()
        }
    }
}
