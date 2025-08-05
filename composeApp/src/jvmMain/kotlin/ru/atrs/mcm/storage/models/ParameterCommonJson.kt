package ru.atrs.mcm.storage.models

import kotlinx.serialization.Serializable

@Serializable
data class ParamComm(
    val parcom : ArrayList<ParameterCommonJson>
)

@Serializable
data class ParameterCommonJson(
    val name: String,
    val value: String,
    val flag: String? = null,
    val numeric: Int? = null
)


data class ParameterCommon(
    val name: String,
    val value: String
    // bool is 0 or 1
)