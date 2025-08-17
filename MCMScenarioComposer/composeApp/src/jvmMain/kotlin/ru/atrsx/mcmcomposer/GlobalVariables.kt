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

var pressures = mutableStateListOf(
    PressureChannel(index = 0 ,  displayName = "имя не задано 0 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 1 ,  displayName = "имя не задано 1 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 2 ,  displayName = "имя не задано 2 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 3 ,  displayName = "имя не задано 3 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 4 ,  displayName = "имя не задано 4 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 5 ,  displayName = "имя не задано 5 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 6 ,  displayName = "имя не задано 6 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 7 ,  displayName = "имя не задано 7 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 8 ,  displayName = "имя не задано 8 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 9 ,  displayName = "имя не задано 9 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 10, displayName =  "имя не задано 10", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
    PressureChannel(index = 11, displayName =  "имя не задано 11", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, isSelected = false),
)

var scenarioBlock = mutableStateOf<ScenarioBlock>(ScenarioBlock())
var pressuresBlock = mutableStateOf<PressuresBlockDto>(PressuresBlockDto())
var solenoidsBlock = mutableStateOf<SolenoidsBlock>(SolenoidsBlock())
