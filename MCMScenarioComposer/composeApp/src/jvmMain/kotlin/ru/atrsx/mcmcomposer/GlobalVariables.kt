package ru.atrsx.mcmcomposer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow

var MAIN_CONFIG = mutableStateOf<MainExperimentConfig>(MainExperimentConfig())

val waterFall = MutableSharedFlow<ScenarioBlock>()

var scenarios = mutableStateListOf(
    ScenarioStep(stepTimeMs = 1000, channelValues = MutableList(12) { 1 }, text = "Step 1"),
    ScenarioStep(stepTimeMs = 2000, channelValues = MutableList(12) { 2 }, text = "Step 2"),
    ScenarioStep(stepTimeMs = 3000, channelValues = MutableList(12) { 3 }, text = "Step 3")
)

var scenarioBlock = mutableStateOf<ScenarioBlock>(ScenarioBlock())
var pressuresBlock = mutableStateOf<PressuresBlockDto>(PressuresBlockDto())
var solenoidsBlock = mutableStateOf<SolenoidsBlock>(SolenoidsBlock())
