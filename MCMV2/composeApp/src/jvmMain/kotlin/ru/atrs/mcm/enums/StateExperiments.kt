package ru.atrs.mcm.enums

enum class StateExperiments(var msg: String = "") {
    NONE,

    SENDING_SCENARIO,

    START, ENDING_OF_EXPERIMENT,

    PREPARE_CHART
}