package com.tools.utils

import com.intellij.ide.util.PropertiesComponent
import com.tools.bean.APPLICATION_ID_REGEX
import com.tools.bean.CONFIG_LOG
import com.tools.bean.VERSION_CODE_REGEX
import com.tools.bean.VersionNameCode
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.StringBuilder

const val APP_NAME_REGEX = "<string\\s+name=\"app_name\">([^<]*)</string>"
const val APP_NAME_UNITY_REGEX = "<string\\s+name=\"pig_farm_name\">([^<]*)</string>"

object Utils {
    private val plistPathKey get() = rootPath + currentBranch + "jtPlistPath"
    private val buildPathKey get() = rootPath + currentBranch + "jtBuildPath"
    private val pluginTypeKey get() = rootPath + "pluginTypeKey"

    var currentBranch = ""
    var rootPath = ""
    var unityRootPath = ""
    var systemPath = ""
    var flavorPrefixPath
        get() = if (rootPath.isEmpty() || currentBranch.isEmpty()) "" else PropertiesComponent.getInstance().getValue(plistPathKey, "")
        set(value) {
            PropertiesComponent.getInstance().setValue(plistPathKey, value)
        }
    var buildGradlePath
        get() = if (rootPath.isEmpty() || currentBranch.isEmpty()) "" else PropertiesComponent.getInstance().getValue(buildPathKey, "")
        set(value) {
            PropertiesComponent.getInstance().setValue(buildPathKey, value)
        }

    val pluginTypeAll = listOf("Android", "Unity")
    var pluginType
        get() = if (rootPath.isEmpty()) "" else PropertiesComponent.getInstance().getValue(pluginTypeKey, "Android")
        set(value) {
            PropertiesComponent.getInstance().setValue(pluginTypeKey, value)
        }
    val isUnity get() = pluginType == "Unity"

    var versions = emptyList<VersionNameCode>()
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
        currentBranch = ""
        this.rootPath = rootPath
        this.unityRootPath = "${rootPath}/.."
        versions = emptyList()
        flavors = emptyList()
        currentBranch = cmdExec("git", "branch", "--show-current")
        println(rootPath)
        println(unityRootPath)
        println("currentBranch:$currentBranch")
        writeLogToFile("rootPath:$rootPath")
        writeLogToFile("rootPath:$unityRootPath")
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
        if (!isUnity) versionCode = getAppVersionCode(flavor)
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
        val buildGradlePath = cmdExec("find", rootPath, "-name", "build.gradle", "-maxdepth", "2").split("\n")
            .filter { !it.contains("libs") }.toMutableList()
        buildGradlePath += cmdExec("find", rootPath, "-name", "build.gradle.kts", "-maxdepth", "2").split("\n")
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
        cmdExec("git", "fetch", "origin", "--tags", "-f", dir = if (isUnity) unityRootPath else rootPath)
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
            val matchResult =
                if (isUnity) APP_NAME_UNITY_REGEX.toRegex().find(content) else APP_NAME_REGEX.toRegex()
                    .find(content)
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
        val regex = APPLICATION_ID_REGEX.replace("flavor", flavor).toRegex()
        val appId = regex.find(content)?.groupValues?.get(0) ?: ""
        writeLogToFile("getAppId: $appId")
        return appId
    }

    private fun getAppVersionCode(flavor: String): String {
        // 读取 build.gradle 文件内容到一个字符串中
        val buildFile = File(buildGradlePath)
        val content = buildFile.readText()

        // 正则表达式，匹配 flavor 下的 applicationId
        val regex = VERSION_CODE_REGEX.replace("flavor", flavor).toRegex()
        val versionCode = regex.find(content)?.groupValues?.get(0) ?: ""
        writeLogToFile("getAppVersionCode: $versionCode")
        return versionCode
    }

    private fun getAppLibVersion(flavor: String) {
        libAds = ""
        libSecurity = ""
        libAppFramework = ""
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

    private fun getVersion(flavor: String): List<VersionNameCode> {
        val allTags = cmdExec("git", "tag", "--sort=-creatordate", "--merged", isLog = false, dir = if (isUnity) unityRootPath else rootPath)
        val tags = allTags.split("\n").filter { it.startsWith("${flavor.capitalize()}-", ignoreCase = true) }
        val tagList = mutableListOf<VersionNameCode>()
        var index = 0
        for (tag in tags) {
            index += 1
            val regex = Regex("""\d+\.\d+\.\d+""")  // 定义匹配规则
            val matchResult = regex.find(tag)  // 查找匹配结果
            if (matchResult != null) {  // 如果找到了匹配结果
                val matchedTag = matchResult.value  // 获取匹配到的字符串
                val versionCode = (Regex("(?<=\\.\\d{0,10}-)[0-9]+(?=-)").find(tag)?.groupValues?.get(0) ?: "")
                tagList.add(
                    VersionNameCode(
                        index.toString(),
                        matchedTag,
                        versionCode,
                        "",
                        tag
                    )
                )  // 添加到列表中
            }
            println("debug ${tag}, version Code=$versionCode")
        }
        val versions = tagList.sortedWith(compareBy(
            { it.name.split(".").first().toInt() },
            { it.name.split(".")[1].toInt() },
            { it.name.split(".").last().toInt() }
        )).reversed()
        if (isUnity) {
            versionCode = if (versions.isNotEmpty()) versions[0].code else ""
        }
        writeLogToFile("getVersion: $versions")
        return versions
    }

    fun cmdExec(vararg command: String, dir: String = rootPath, isLog: Boolean = true): String {
        val bulder = ProcessBuilder(*command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .directory(File(dir))
        if (systemPath.isNotEmpty()) {
            bulder.environment()["PATH"] = systemPath
        }
        val process = bulder.start()
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
        if (isLog) {
            writeLogToFile(result)
        }
        return result
    }

    fun writeLogToFile(message: String, init: Boolean = false) {
        val realPath = "$rootPath/$CONFIG_LOG"
        val file = File(realPath)
        if (!file.exists()) {
            file.createNewFile()
            var gitignore = cmdExec("find", rootPath, "-name", ".gitignore", "-maxdepth", "1").split("\n")
            if (gitignore.isEmpty()) {
                gitignore = cmdExec("find", rootPath, "-name", ".gitignore", "-maxdepth", "2").split("\n")
            }
            if (gitignore.isNotEmpty()) {
                val gitignoreFile = File(gitignore[0])
                if (gitignoreFile.exists()) {
                    val readText = gitignoreFile.readText()
                    val insertContext =
                        gitignore[0].substring(rootPath.length, gitignore[0].length - ".gitignore".length) + CONFIG_LOG
                    if (!readText.contains(insertContext)) {
                        val writer = BufferedWriter(FileWriter(gitignore[0]))
                        writer.write("${readText}\n${insertContext}")
                        writer.close()
                    }
                }
            }
        }
        val size = file.length() / (1024 * 1024)
        val readText = file.readText()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val time = formatter.format(Date())
        val writer = BufferedWriter(FileWriter(realPath))
        writer.write(if (init && size >= 5) "" else readText + "\n${time}--> " + message)
        writer.close()
    }

    fun checkLogFileInGitignore() {
        val realPath = "$rootPath/$CONFIG_LOG"
        val file = File(realPath)
        if (file.exists()) {
            var gitignore = cmdExec("find", rootPath, "-name", ".gitignore", "-maxdepth", "1").split("\n")
            if (gitignore.isEmpty()) {
                gitignore = cmdExec("find", rootPath, "-name", ".gitignore", "-maxdepth", "2").split("\n")
            }
            if (gitignore.isNotEmpty()) {
                val gitignoreFile = File(gitignore[0])
                if (gitignoreFile.exists()) {
                    val readText = gitignoreFile.readText()
                    val insertContext =
                        gitignore[0].substring(rootPath.length, gitignore[0].length - ".gitignore".length) + CONFIG_LOG
                    if (!readText.contains(insertContext)) {
                        val writer = BufferedWriter(FileWriter(gitignore[0]))
                        writer.write("${readText}\n${insertContext}")
                        writer.close()
                    }
                }
            }
        }
    }

    fun initSystemPath() {
        if (systemPath.isNotEmpty()) return
        val home = System.getenv("HOME")
        val path1 = cmdExec("/bin/sh", "-c", "source /etc/profile && env", isLog = false).split("\n")
            .find { it.startsWith("PATH") }
        val path2 = cmdExec("/bin/sh", "-c", "source ${home}/.bash_profile && env", isLog = false).split("\n")
            .find { it.startsWith("PATH") }
        val path3 = cmdExec("/bin/sh", "-c", "source ${home}/.zprofile && env", isLog = false).split("\n")
            .find { it.startsWith("PATH") }
        val path4 = cmdExec("/bin/sh", "-c", "source ${home}/.profile && env", isLog = false).split("\n")
            .find { it.startsWith("PATH") }
        val sb = StringBuilder()
        path1?.let {
            sb.append(it.substring(5) + ":")
        }
        path2?.let {
            sb.append(it.substring(5) + ":")
        }
        path3?.let {
            sb.append(it.substring(5) + ":")
        }
        path4?.let {
            sb.append(it.substring(5) + ":")
        }
        systemPath = sb.deleteAt(sb.length - 1).toString()
        writeLogToFile("path : $systemPath")
    }
}