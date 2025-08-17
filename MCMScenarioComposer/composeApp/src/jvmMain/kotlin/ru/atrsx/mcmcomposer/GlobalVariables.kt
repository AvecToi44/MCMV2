package ru.atrsx.mcmcomposer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow

//var MAIN_CONFIG = mutableStateOf<MainExperimentConfig>(
//    MainExperimentConfig(
//        pressures = PressuresBlockDto(channels = pressures),
//        solenoids = TODO(),
//        scenario = TODO(),
//        standardPath = TODO(),
//        sheetName = TODO()
//    )
//)

val waterFall = MutableSharedFlow<ScenarioBlockDto>()

var scenarios = mutableStateListOf(
    ScenarioStep(stepTimeMs = 1000, channelValues = MutableList(12) { 1 }, text = "Step 1", gradientTimeMs = 0),
    ScenarioStep(stepTimeMs = 2000, channelValues = MutableList(12) { 2 }, text = "Step 2", gradientTimeMs = 0),
    ScenarioStep(stepTimeMs = 3000, channelValues = MutableList(12) { 3 }, text = "Step 3", gradientTimeMs = 0)
)

var pressures = mutableStateListOf(
    PressureChannel(index = 0 ,  displayName = "имя не задано 0 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true,),
    PressureChannel(index = 1 ,  displayName = "имя не задано 1 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 2 ,  displayName = "имя не задано 2 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 3 ,  displayName = "имя не задано 3 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 4 ,  displayName = "имя не задано 4 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 5 ,  displayName = "имя не задано 5 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 6 ,  displayName = "имя не задано 6 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 7 ,  displayName = "имя не задано 7 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 8 ,  displayName = "имя не задано 8 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 9 ,  displayName = "имя не задано 9 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 10, displayName =  "имя не задано 10", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 11, displayName =  "имя не задано 11", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
)

var solenoids = mutableStateListOf(
    SolenoidChannel(displayName = "Соленоид без имени", index = 0, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 1, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 2, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 3, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 4, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 5, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 6, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 7, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 8, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 9, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 10, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид без имени", index = 11, maxPwm0_255 = 0, valueOfDivision = 0, tenthAmplitude = 0, tenthFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
)

var scenarioBlock = mutableStateOf<ScenarioBlockDto>(ScenarioBlockDto())
var pressuresBlock = mutableStateOf<PressuresBlockDto>(PressuresBlockDto())
var solenoidsBlock = mutableStateOf<SolenoidsBlock>(SolenoidsBlock())
