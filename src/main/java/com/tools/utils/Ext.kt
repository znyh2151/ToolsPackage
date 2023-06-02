package com.tools.utils


import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun todayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val date = LocalDate.now()
    return formatter.format(date)
}

fun getVersionDiff(version1: String, version2: String): Int {
    val parts1 = version1.split(".").map { it.toInt() }
    val parts2 = version2.split(".").map { it.toInt() }

    if (parts1.size != parts2.size) {
        return -10
    }
    var result = 0
    for (i in parts1.indices) {
        result += parts1[i] - parts2[i]
    }
    return result
}

fun main() {
//    var text = """
//    productFlavors {
//        play1 {
//            dimension "product"
//            applicationId "com.play1"
//            versionCode 1000
//            versionName "1.0.0"
//        }
//        play2 {
//            dimension "product"
//            applicationId "com.play2"
//            versionCode 1002
//            versionName "1.0.2"
//        }
//    }"""
//    val pattern2 = """(?<=play2\s{0,10}\{[\s\S]{0,1000}?versionCode\s{0,10})\d+(?=\s*)""".toRegex()
//    val pattern = """(?<=play1\s{0,10}\{[\s\S]{0,1000}?versionName\s{0,10}").*?(?=")""".toRegex()
//    val newVersionName = "1.0.3" // 替换为你想要的新版本名
//    var index = 0
//    val newText = pattern.replaceFirst(text, newVersionName)
//    println(newText)

    val list = listOf("1.0.1", "2.0.2", "1.3.20")
    val sortedList = sortList(list)
    println(sortedList)
}

fun sortList(list: List<String>): List<String> {
    return list.sortedWith(compareBy(
        { it.split(".").first().toInt() },
        { it.split(".")[1].toInt() },
        { it.split(".").last().toInt() }
    )).reversed()
}