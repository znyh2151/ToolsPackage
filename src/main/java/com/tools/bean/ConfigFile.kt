package com.tools.bean

data class ConfigFile(
    val tip: String = "",
    val libReplace: List<ReplaceLib> = listOf(),
    var netWork: Boolean = false
)

data class ReplaceLib(
    val regex: String = "",
    val newValue: String = "",
    var oldValue: String = "",
    var enable: Boolean = true
) {
    fun isEnable() = oldValue.isNotEmpty() && newValue.isNotEmpty() && enable && oldValue != newValue
}

