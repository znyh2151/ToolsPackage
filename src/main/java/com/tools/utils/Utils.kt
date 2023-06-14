package com.tools.utils

import com.tools.bean.CONFIG_LOG
import java.io.*
import java.lang.StringBuilder

const val APP_NAME_REGEX = "<string\\s+name=\"app_name\">([^<]*)</string>"

object Utils {
    var rootPath = ""
    var flavorPrefixPath = ""
    var buildGradlePath = ""
    var versions = emptyList<String>()
    var flavors = emptyList<String>()
        get() {
            return field.ifEmpty {
                field = getAllFlavors()
                field
            }
        }
    var appId = ""
    var appName = ""
    var versionCode = ""
    var libSecurity = ""
    var libAppFramework = ""
    var libAds = ""

    fun initPath(rootPath: String) {
        this.rootPath = rootPath
        flavorPrefixPath = ""
        buildGradlePath = ""
        versions = emptyList()
        flavors = emptyList()
        println(rootPath)
        writeLogToFile("rootPath:$rootPath")
    }

    fun refreshData(flavor: String) {
        if (flavorPrefixPath.isEmpty()) {
            flavorPrefixPath = findFlavorPrefixPath()[0]
            println(flavorPrefixPath)
        }
        if (buildGradlePath.isEmpty()) {
            buildGradlePath = findBuildGradlePrefixPath()[0]
            println(buildGradlePath)
        }
        appName = getAppName(flavor)
        appId = getAppId(flavor)
        versions = getVersion(flavor)
        versionCode = getAppVersionCode(flavor)
        getAppLibVersion(flavor)
        println("name: $appName, id: $appId,versionCode: $versionCode, versions: $versions")
    }

    fun findFlavorPrefixPath(): List<String> {
        val targetFileName = "main"
        val mainPath = cmdExec("find", rootPath, "-name", targetFileName, "-maxdepth", "4").split("\n")
            .filter { !it.contains("libs") }
        val result = mutableListOf<String>()
        for (s in mainPath) {
            result.add(s.substring(0, s.length - targetFileName.length - 1))
            writeLogToFile("findFlavorPrefixPath: $result")
        }
        if (result.isEmpty()) {
            writeLogToFile("findFlavorPrefixPath: size = 0")
        }
        return result
    }

    fun findBuildGradlePrefixPath(): List<String> {
        val targetFileName = "build.gradle"
        val buildGradlePath = cmdExec("find", rootPath, "-name", targetFileName, "-maxdepth", "2").split("\n")
            .filter { !it.contains("libs") }
        if (buildGradlePath.isEmpty()) {
            throw RuntimeException("获取 build.gradle file path 路径错误")
        }
        if (buildGradlePath.size != 1) {
            val list = buildGradlePath.filter { it.contains("app") }
            if (list.isEmpty()) {
                return buildGradlePath
            }
            writeLogToFile("findBuildGradlePrefixPath $list")
            return list
        }
        writeLogToFile("findBuildGradlePrefixPath $buildGradlePath")
        return buildGradlePath
    }

    private fun getAllFlavors(): List<String> {
        flavorPrefixPath = flavorPrefixPath.ifEmpty { findFlavorPrefixPath()[0] }
        buildGradlePath = buildGradlePath.ifEmpty { findBuildGradlePrefixPath()[0] }
        cmdExec("git", "fetch", "origin", "--tags", "-f")
        val mainSrcPath = File(flavorPrefixPath)
        if (!mainSrcPath.exists() || !mainSrcPath.isDirectory) {
            return emptyList()
        }

        val flavorDirs = mainSrcPath.listFiles { file ->
            println(file.name)
            var result = file.isDirectory && !file.name.startsWith("main") && !file.name.contains("Test")
            println(file.name + ":$result")
            if (result) {
                for (listFile in file.listFiles()!!) {
                    result = listFile.name.contains("AndroidManifest.xml")
                    if (result) {
                        break
                    }
                }
            }
            result
        }
        val flavors = (flavorDirs?.map { it.name } ?: emptyList()).sortedWith { str1, str2 ->
            val num1 = str1.filter { it.isDigit() }.toIntOrNull()
            val num2 = str2.filter { it.isDigit() }.toIntOrNull()
            if (num1 != null && num2 != null) {
                num2.compareTo(num1)
            } else if (num1 != null) {
                -1
            } else if (num2 != null) {
                1
            } else {
                str1.compareTo(str2)
            }
        }
        writeLogToFile("getFlavors: $flavors")
        return flavors
    }

    private fun getAppName(flavor: String): String {
        val path = "$flavorPrefixPath/$flavor/res/values/strings.xml"
        val stringsFile = File(path)
        if (stringsFile.exists()) {
            // 读取文件并将其加载到一个字符串中
            val reader = FileReader(stringsFile)
            val content = reader.readText()
            reader.close()
            // 使用正则表达式匹配 app_name 值
            val matchResult = APP_NAME_REGEX.toRegex().find(content)
            if (matchResult != null) {
                val result = matchResult.groupValues[1]
                writeLogToFile("getAppName $result")
                return result
            }
        }
        writeLogToFile("getAppName null")
        return ""
    }

    private fun getAppId(flavor: String): String {
        // 读取 build.gradle 文件内容到一个字符串中
        val buildFile = File(buildGradlePath)
        val content = buildFile.readText()

        // 正则表达式，匹配 flavor 下的 applicationId
        val regex = "${flavor}[^{]*?\\{[^}]*?applicationId\\s+\"([a-zA-Z.]+)\"".toRegex()
        val appId = regex.find(content)?.groupValues?.get(1) ?: ""
        writeLogToFile("getAppId: $appId")
        return appId
    }

    private fun getAppVersionCode(flavor: String): String {
        // 读取 build.gradle 文件内容到一个字符串中
        val buildFile = File(buildGradlePath)
        val content = buildFile.readText()

        // 正则表达式，匹配 flavor 下的 applicationId
        val regex = "${flavor}[^{]*?\\{[^}]*?versionCode\\s+(\\d+)".toRegex()
        val versionCode = regex.find(content)?.groupValues?.get(1) ?: ""
        writeLogToFile("getAppVersionCode: $versionCode")
        return versionCode
    }

    private fun getAppLibVersion(flavor: String) {
        val buildFile = File(buildGradlePath)
        val content = buildFile.readText()
        val regex = "['\"]cn.*.appframework:libSecurity.*['\"]".toRegex()
        val regex2 = "['\"]cn.appcloudbox.ads:${flavor}_.*['\"]".toRegex()
        val regex3 = "['\"]cn.*.appframework:libAppframework.*['\"]".toRegex()
        regex.find(content)?.let {
            val result = it.groupValues[0]
            writeLogToFile("getAppLibVersion$result")
            libSecurity = result.substring(1, result.length - 1)
        }
        regex2.find(content)?.let {
            val result = it.groupValues[0]
            writeLogToFile("getAppLibVersion$result")
            libAds = result.substring(1, result.length - 1)
        }
        regex3.find(content)?.let {
            val result = it.groupValues[0]
            writeLogToFile("getAppLibVersion$result")
            libAppFramework = result.substring(1, result.length - 1)
        }
    }

    private fun getVersion(flavor: String): List<String> {
        val allTags = cmdExec("git", "tag", "--sort=-creatordate", "--merged")
        val tags = allTags.split("\n").filter { it.startsWith("${flavor.capitalize()}-", ignoreCase = true) }
        val tagList = mutableListOf<String>()
        for (tag in tags) {
            val regex = Regex("""\d+\.\d+\.\d+""")  // 定义匹配规则
            val matchResult = regex.find(tag)  // 查找匹配结果
            if (matchResult != null) {  // 如果找到了匹配结果
                val matchedTag = matchResult.value  // 获取匹配到的字符串
                tagList.add(matchedTag)  // 添加到列表中
            }
        }
        val versions = tagList.sortedWith(compareBy(
            { it.split(".").first().toInt() },
            { it.split(".")[1].toInt() },
            { it.split(".").last().toInt() }
        )).reversed()
        writeLogToFile("getVersion: $versions")
        return versions
    }

    fun cmdExec(vararg command: String, dir: String = rootPath): String {
        val process = ProcessBuilder(*command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .directory(File(dir))
            .start()
        process.waitFor()
        val output = StringBuilder()
        if (process.exitValue() != 0) {
            BufferedReader(InputStreamReader(process.errorStream)).useLines { lines ->
                lines.forEach {
                    output.appendln(it)
                }
            }
            writeLogToFile(
                "Command '${command.joinToString(" ")}' failed with exit code ${process.exitValue()} ${
                    output.toString().trimEnd()
                }"
            )
            return "-1"
        }
        BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
            lines.forEach {
                output.appendln(it)
            }
        }
        val result = output.toString().trimEnd()
        writeLogToFile(result)
        return result
    }

    fun writeLogToFile(message: String) {
        val realPath = "$rootPath/$CONFIG_LOG"
        val file = File(realPath)
        if (file.exists()) {
            val readText = file.readText()
            val writer = BufferedWriter(FileWriter(realPath))
            writer.write(readText + "\n" + message)
            writer.close()
        }
    }
}