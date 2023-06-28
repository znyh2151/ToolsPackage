package com.tools.bean


const val CONFIG_NAME = "PackageToolsConfig.json"
const val CONFIG_LOG = "PackageTools.log"
const val VERSION_CODE_REGEX = "(?<=flavor[\\s\\S]{0,10}\\{[\\s\\S]{0,1000}?versionCode[\\s=]{0,10})\\d+(?=\\s*)"
const val VERSION_NAME_REGEX = "(?<=flavor[\\s\\S]{0,10}\\{[\\s\\S]{0,1000}?versionName[\\s=]{0,10}\").*?(?=\")"
const val APPLICATION_ID_REGEX = "(?<=flavor[\\s\\S]{0,10}\\{[\\s\\S]{0,1000}?applicationId[\\s=]{0,10}\").*?(?=\")"
const val FLAVORINFOS_REGEX = "(?<=flavorInfos=\").*?(?=\")"
const val PLIST_URL_REGEX = "\\d+\\.\\d+\\.\\d+-\\d+(?=.la)"

data class PackageConfig(
    val packet: HashMap<String, Packet> = hashMapOf(),
    val tip: String = "",
    val regex: RegexConfig = RegexConfig(),
    val path: FilePath = FilePath()
)

data class Packet(
    val appId: String = "",
    val name: String = "",
    val cmd: String = "",
    var version: ArrayList<String> = ArrayList(),
    val versionCode: ArrayList<Int> = ArrayList(),
    val regex: RegexConfig = RegexConfig()
)

data class FilePath(
    val buildConfig: String = "",
    val plist: String = ""
)

data class RegexConfig(
    val versionCode: String = "",
    val versionName: String = "",
    val applicationId: String = "",
    val plistUrl: String = "",
)

data class UIData(
    var applicationId: String = "",
    var versionCode: String = "",
    var versionName: String = "",
    var cmd: String = "",
    var gt: Boolean = false,
    var storeCode: Boolean = false,
    var auditPackage: Boolean = false,
    var storeName: Boolean = false,
    var storeIcon: Boolean = false,
    var tag: String = "",
    var flavorInfos: String = "",
)