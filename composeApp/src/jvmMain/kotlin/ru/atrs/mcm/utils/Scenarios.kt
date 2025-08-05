package ru.atrs.mcm.utils

fun checkIntervalScenarios(newIndex: Int): Int {
    logGarbage("checkIntervalScenarios newIndex: ${newIndex}")
    if (newIndex in 0 until scenario.size) {
        return newIndex
    }else if (newIndex > scenario.size-1) {
        indexOfScenario.value = scenario.size - 1
        return scenario.size - 1
    } else if (newIndex < 0){
        indexOfScenario.value = 0
        return 0
    } else {
        return indexOfScenario.value
    }
}
