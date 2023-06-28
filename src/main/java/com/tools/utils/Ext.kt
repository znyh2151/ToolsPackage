package com.tools.utils


import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.swing.*


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
    var text = """
    flavorInfos="mars44,1.0.0,10000" 
    reinforcement="true"
    slackNotification="true"
    noAdMode="false"
    """
    val pattern = """(?<=flavorInfos=").*?(?=")""".toRegex()
    val newVersionName = "1.0.3" // 替换为你想要的新版本名
    val newText = pattern.replaceFirst(text, newVersionName)
    println(newText)
}

fun createAndShowGUI(items: List<String>) {
    val frame = JFrame("Dynamic Item List")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(600, 400)
    frame.layout = BorderLayout()

    val itemList = JPanel()
    itemList.layout = GridLayout(0, 6)
    val scrollPane = JScrollPane(itemList)
    scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
    scrollPane.preferredSize = Dimension(300, 300)

    for (item in items) {
        val checkBox = JCheckBox(item)
        itemList.add(checkBox)
    }

    frame.add(scrollPane, BorderLayout.CENTER)
    frame.isVisible = true
}

fun loadEnv() {
// 创建一个ProcessBuilder对象，并将UNIX shell作为命令参数传递
    val builder = ProcessBuilder("/bin/sh", "-c", "source /etc/profile && env")
// 将环境变量加载到当前环境中
    builder.environment().putAll(System.getenv())

// 启动进程并等待它完成
    val process = builder.start()
    process.waitFor()

// 您可以遍历输出并将其分解为键/值对
    process.inputStream.bufferedReader().useLines { lines ->
        lines.forEach { line ->
            val (key, value) = line.split("=", limit = 2)
            System.setProperty(key, value)
            println("key:$key, value:$value")
        }
    }
    val environmentVariables = System.getenv()
    for ((key, value) in environmentVariables) {
        println("环境变量：$key=$value")
    }
}

fun sortList(list: List<String>): List<String> {
    return list.sortedWith(compareBy(
        { it.split(".").first().toInt() },
        { it.split(".")[1].toInt() },
        { it.split(".").last().toInt() }
    )).reversed()
}