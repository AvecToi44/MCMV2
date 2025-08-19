package ru.atrsx.mcmcomposer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf


var scenarios = mutableStateListOf<ScenarioStep>(
//    ScenarioStep(stepTimeMs = 1000, channelValues = MutableList(12) { 1 }, text = "Step 1", gradientTimeMs = 0),
//    ScenarioStep(stepTimeMs = 2000, channelValues = MutableList(12) { 2 }, text = "Step 2", gradientTimeMs = 0),
//    ScenarioStep(stepTimeMs = 3000, channelValues = MutableList(12) { 3 }, text = "Step 3", gradientTimeMs = 0)
)

var pressures = mutableStateListOf<PressureChannel>(
    PressureChannel(index = 0 ,  displayName = "Канал 0 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true,),
    PressureChannel(index = 1 ,  displayName = "Канал 1 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 2 ,  displayName = "Канал 2 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 3 ,  displayName = "Канал 3 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 4 ,  displayName = "Канал 4 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 5 ,  displayName = "Канал 5 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 6 ,  displayName = "Канал 6 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 7 ,  displayName = "Канал 7 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 8 ,  displayName = "Канал 8 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 9 ,  displayName = "Канал 9 ", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 10, displayName =  "Канал 10", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
    PressureChannel(index = 11, displayName =  "Канал 11", minValue = 0, maxValue = 25, tolerance = 0, unit = "Бар", comment = "Комментарий не определен", preferredColorHex = "#FF008001", isVisible = true, ),
)

var solenoids = mutableStateListOf<SolenoidChannel>(
    SolenoidChannel(displayName = "Соленоид #0", index = 0, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #1", index = 1, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #2", index = 2, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #3", index = 3, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #4", index = 4, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #5", index = 5, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #6", index = 6, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #7", index = 7, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #8", index = 8, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #9", index = 9, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #10", index = 10, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
    SolenoidChannel(displayName = "Соленоид #11", index = 11, maxPwm0_255 = 0, valueOfDivision = 0, DitherAmplitude = 0, DitherFrequency = 0, minValue = 0, maxValue = 0, isVisible = true),
)

var MAIN_CONFIG = mutableStateOf<MainExperimentConfig>(
    MainExperimentConfig(
        pressures = PressuresBlockDto(channels = pressures),
        solenoids = SolenoidsBlock(channels = solenoids),
        scenario  = ScenarioBlockDto(steps = scenarios.toDtoList()),
        standardPath = "",//"C:\\Users\\...\\0b5_combi_18_08_2025 11_31_32_arstest5.txt",
        sheetName = ""//"scenario_with_${pressures.count { it.isVisible }}_pressures_${generateTimestampLastUpdate()}"
    )
)