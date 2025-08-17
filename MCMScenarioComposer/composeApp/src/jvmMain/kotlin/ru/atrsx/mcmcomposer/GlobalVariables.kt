package ru.atrsx.mcmcomposer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow

var MAIN_CONFIG = mutableStateOf<MainExperimentConfig>(MainExperimentConfig())

val waterFall = MutableSharedFlow<ScenarioBlock>()

var scenarios = mutableStateListOf<ScenarioStep>(
    ScenarioStep(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
    ScenarioStep(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
)

var scenarioBlock = mutableStateOf<ScenarioBlock>(ScenarioBlock())
var pressuresBlock = mutableStateOf<PressuresBlockDto>(PressuresBlockDto())
var solenoidsBlock = mutableStateOf<SolenoidsBlock>(SolenoidsBlock())
