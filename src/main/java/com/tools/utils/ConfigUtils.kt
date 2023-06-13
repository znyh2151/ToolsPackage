package com.tools.utils

import com.google.gson.Gson
import com.tools.bean.ConfigFile
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

object ConfigUtils {
    var config = ConfigFile()

    fun getConfigFile() {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://git.atcloudbox.com/api/v4/projects/734/repository/files/tools%2Ftools_package_config.json?private_token=${System.getenv("TOOLS_PACKAGE_TOKEN")}&ref=develop")
            .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseData = response.body?.string() ?: ""
            val base64String = JSONObject(responseData).get("content") as String
            println(base64String)
            val json = decodeBase64(base64String)
            println(json)
            config = Gson().fromJson(json, ConfigFile::class.java)
            config.netWork = true
            println(config)
        } else {
            Utils.writeLogToFile("网络获取文件失败")
            println(config)
        }
    }

    fun getTip() = config.tip.ifEmpty { if (!config.netWork) "网络获取文件失败" else "null" }
    fun refreshReplaceLib() {
        val buildFile = File(Utils.buildGradlePath)
        val content = buildFile.readText()
        for (lib in config.libReplace) {
            if (lib.regex.isNotEmpty()) {
                lib.regex.toRegex().find(content)?.let {
                    val result = it.groupValues[0]
                    Utils.writeLogToFile("getAppLibVersion$result")
                    lib.oldValue = result.substring(1, result.length - 1)
                }
            }
        }

        println(config.libReplace)
    }


    private fun decodeBase64(base64String: String): String {
        // 将 Base64 字符串转换为字节数组
        val base64Decoded = Base64.getDecoder().decode(base64String)

        // 将字节数组转换为字符串
        return String(base64Decoded, StandardCharsets.UTF_8)
    }
}