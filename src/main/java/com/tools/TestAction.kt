package com.tools

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.JBUI
import com.tools.bean.*
import com.tools.ui.HomePage
import com.tools.utils.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.*
import javax.swing.*

class TestAction : AnAction() {

    private var rootPath = ""
    private var selectedPacketCode = ""
    private lateinit var uiData: UIData
    private lateinit var jProjectList: JComboBox<String>
    private lateinit var jProjectName: JLabel
    private lateinit var jSubmitButton: JButton
    private lateinit var jbInitPath: JButton
    private lateinit var jProjectTip: JTextArea
    private lateinit var jHistoryVersion: JTextArea
    private lateinit var jHistoryScrollPane: JScrollPane
    private lateinit var jVersion: JTextField
    private lateinit var jVersionCode: JTextField
    private lateinit var jBuildProjectCmd: JTextField
    private lateinit var jTag: JTextField
    private lateinit var jAppId: JTextField
    private lateinit var jGT: JCheckBox
    private lateinit var jStoreCode: JCheckBox
    private lateinit var jStoreName: JCheckBox
    private lateinit var jStoreIcon: JCheckBox
    private lateinit var jAuditPackage: JCheckBox
    private lateinit var jRootFrame: JFrame
    private lateinit var jtPlistPath: JComboBox<String>
    private lateinit var jtBuildPath: JComboBox<String>
    private lateinit var uiHome: HomePage
    override fun actionPerformed(e: AnActionEvent) {
        rootPath = e.project?.basePath!!
        Utils.initPath(rootPath)
        Utils.writeLogToFile("当前路径$rootPath")
//        initView()
        initView2()
    }

    private fun initView2() {
        jRootFrame = JFrame("打包界面")
        uiHome = HomePage().apply {
            jRootFrame.contentPane = root
            this@TestAction.jProjectList = jProjectList
            this@TestAction.jProjectName = jProjectName
            this@TestAction.jSubmitButton = jSubmitButton
            this@TestAction.jHistoryVersion = jHistoryVersion
            this@TestAction.jHistoryScrollPane = jHistoryScrollPane
            this@TestAction.jVersion = jVersion
            this@TestAction.jVersionCode = jVersionCode
            this@TestAction.jBuildProjectCmd = jBuildProjectCmd
            this@TestAction.jTag = jTag
            this@TestAction.jAppId = jAppId
            this@TestAction.jGT = jGT
            this@TestAction.jStoreCode = jStoreCode
            this@TestAction.jStoreName = jStoreName
            this@TestAction.jStoreIcon = jStoreIcon
            this@TestAction.jAuditPackage = jAuditPackage
            this@TestAction.jtPlistPath = jtPlistPath
            this@TestAction.jtBuildPath = jtBuildPath
            this@TestAction.jbInitPath = jbInitPath
        }
        uiHome.jTokenInput.text = ConfigUtils.token
        uiHome.jToken.addActionListener {
            PropertiesComponent.getInstance().setValue(ConfigUtils.TOKEN_KEY, uiHome.jTokenInput.text)
            initData()
        }
        jtPlistPath.model = DefaultComboBoxModel(Utils.findFlavorPrefixPath().toTypedArray())
        jtBuildPath.model = DefaultComboBoxModel(Utils.findBuildGradlePrefixPath().toTypedArray())
        jProjectList.addActionListener { refreshUI() }
        jbInitPath.text = "修改路径"
        jbInitPath.addActionListener {
            Utils.flavorPrefixPath = jtPlistPath.selectedItem!!.toString()
            Utils.buildGradlePath = jtBuildPath.selectedItem!!.toString()
            jProjectList.model = DefaultComboBoxModel(Utils.flavors.toTypedArray())
            Utils.refreshData(jProjectList.selectedItem!!.toString())
            refreshUI()
        }
        jSubmitButton.addActionListener { submitBuild() }
        jRootFrame.apply {
            setSize(1100, 600)
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            isVisible = true
        }
        initData()
    }

    private fun initData() {
        Thread {
            ConfigUtils.getConfigFile()
            jProjectList.model = DefaultComboBoxModel(Utils.flavors.toTypedArray())
            Utils.refreshData(jProjectList.selectedItem!!.toString())
            refreshUI()
        }.start()
    }

    private fun initView() {
        // 创建 JFrame 对象并设置标题
        jRootFrame = JFrame("打包界面")

        val panel = JPanel(GridBagLayout())
        jRootFrame.contentPane.add(panel)

        jProjectList = JComboBox(emptyArray())
        jProjectList.addActionListener {
            refreshUI()
        }
        val gbcComboBox = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            fill = GridBagConstraints.HORIZONTAL
        }
        panel.add(jProjectList, gbcComboBox)
        jProjectName = JLabel("项目名称：加载中...")
        val gbcLabel1 = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(5, 0)
        }
        panel.add(jProjectName, gbcLabel1)

        jProjectTip = JTextArea("", 3, 10).apply {
            lineWrap = true
            wrapStyleWord = true
        }
        GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 2
            fill = GridBagConstraints.BOTH
            insets = JBUI.insets(5, 0)
            panel.add(JScrollPane(jProjectTip), this)
        }

        val label2 = JLabel("已打包版本包如下")
        val gbcLabel2 = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 2
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(5, 0)
        }
        panel.add(label2, gbcLabel2)

        jHistoryVersion =
            JTextArea("", 5, 20).apply {
                lineWrap = true
                wrapStyleWord = true
            }
        jHistoryScrollPane = JScrollPane(jHistoryVersion)
        val gbcScrollPane = GridBagConstraints().apply {
            gridx = 0
            gridy = 4
            gridwidth = 2
            fill = GridBagConstraints.BOTH
        }
        panel.add(jHistoryScrollPane, gbcScrollPane)

        val label3 = JLabel("打包选项")
        val gbcLabel3 = GridBagConstraints().apply {
            gridx = 0
            gridy = 5
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(10, 0)
        }
        panel.add(label3, gbcLabel3)

        jAppId = JTextField()
        val gbcAppId = GridBagConstraints().apply {
            gridx = 0
            gridy = 6
            anchor = GridBagConstraints.LINE_START
        }
        panel.add(JLabel("AppId："), gbcAppId)
        panel.add(jAppId, GridBagConstraints().apply {
            gridx = 1
            gridy = 6
            fill = GridBagConstraints.HORIZONTAL
        })

        jVersion = JTextField().apply {
            toolTipText = "1.0.0"
        }
        panel.add(JLabel("版本名称："), GridBagConstraints().apply {
            gridx = 0
            gridy = 7
            anchor = GridBagConstraints.LINE_START
        })
        panel.add(jVersion, GridBagConstraints().apply {
            gridx = 1
            gridy = 7
            fill = GridBagConstraints.HORIZONTAL
        })

        jVersionCode = JTextField().apply {
            toolTipText = "1000"
        }
        panel.add(JLabel("版本号："), GridBagConstraints().apply {
            gridx = 0
            gridy = 8
            anchor = GridBagConstraints.LINE_START
        })
        panel.add(jVersionCode, GridBagConstraints().apply {
            gridx = 1
            gridy = 8
            fill = GridBagConstraints.HORIZONTAL
        })

        jBuildProjectCmd = JTextField().apply {
            toolTipText = "请输入命令"
        }
        panel.add(JLabel("命令："), GridBagConstraints().apply {
            gridx = 0
            gridy = 9
            anchor = GridBagConstraints.LINE_START
        })
        panel.add(jBuildProjectCmd, GridBagConstraints().apply {
            gridx = 1
            gridy = 9
            fill = GridBagConstraints.HORIZONTAL
        })

        jTag = JTextField()
        panel.add(JLabel("Tag："), GridBagConstraints().apply {
            gridx = 0
            gridy = 10
            anchor = GridBagConstraints.LINE_START
        })
        panel.add(jTag, GridBagConstraints().apply {
            gridx = 1
            gridy = 10
            fill = GridBagConstraints.HORIZONTAL
        })

        jGT = JCheckBox("GT")
        jStoreCode = JCheckBox("StoreCode")
        jStoreName = JCheckBox("StoreName")
        jAuditPackage = JCheckBox("AuditPackage")
        jStoreIcon = JCheckBox("StoreIcon")
        panel.add(jGT, GridBagConstraints().apply {
            gridx = 0
            gridy = 11
            anchor = GridBagConstraints.LINE_START
        })
        jGT.isSelected = true
        panel.add(jStoreCode, GridBagConstraints().apply {
            gridx = 1
            gridy = 11
            fill = GridBagConstraints.HORIZONTAL
        })
        panel.add(jStoreName, GridBagConstraints().apply {
            gridx = 0
            gridy = 12
            anchor = GridBagConstraints.LINE_START
        })
        panel.add(jAuditPackage, GridBagConstraints().apply {
            gridx = 1
            gridy = 12
            fill = GridBagConstraints.HORIZONTAL
        })
        panel.add(jStoreIcon, GridBagConstraints().apply {
            gridx = 0
            gridy = 13
            anchor = GridBagConstraints.LINE_START
        })
        jSubmitButton = JButton("提交打包请求").apply {
            addActionListener { submitBuild() }
        }
        panel.add(jSubmitButton, GridBagConstraints().apply {
            gridx = 0
            gridy = 14
            gridwidth = 2
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(10, 0)
        })

        jRootFrame.apply {
            setSize(400, 700)
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            isVisible = true
        }
        Thread {
            jProjectList.model = DefaultComboBoxModel(Utils.flavors.toTypedArray())
            Utils.refreshData(jProjectList.selectedItem!!.toString())
            refreshUI()
        }.start()
    }

    private fun submitBuild() {
        uiData = UIData(
            versionCode = jVersionCode.text.trim(),
            versionName = jVersion.text.trim(),
            cmd = jBuildProjectCmd.text.trim(),
            gt = jGT.isSelected,
            storeCode = jStoreCode.isSelected,
            auditPackage = jAuditPackage.isSelected,
            storeName = jStoreName.isSelected,
            storeIcon = jStoreIcon.isSelected,
            applicationId = jAppId.text.trim(),
            tag = jTag.text
        )
        if (isNeedWarningDialog(uiData)) return
        if (Utils.versions.isNotEmpty()) {
            val diffCode = getVersionDiff(uiData.versionName, Utils.versions[0])
            // 版本号异常
            if (diffCode == 0) {
                showWarningDialog("版本号与上次版本相同，请检测！！！<br><br> 是否确定打包？") {
                    commit(uiData)
                }
                return
            }
            if (diffCode != 1) {
                showWarningDialog("版本号异常，请检测！！！<br><br> 是否确定打包？") {
                    commit(uiData)
                }
                return
            }
            if (Utils.versions.contains(uiData.versionName.trim())) {
                showWarningDialog("版本号已存在，请确认是否继续！！！<br><br> 是否确定打包？") {
                    commit(uiData)
                }
                return
            }
        }
        commit(uiData)
    }

    private fun commit(data: UIData) {
        jSubmitButton.text = "正在操作，请稍后..."
        jSubmitButton.isEnabled = false
        jSubmitButton.repaint()
        Thread {
            data.cmd += "(${data.versionName}-${data.versionCode})"
            if (data.gt) {
                data.cmd += " GT"
            }
            if (data.storeCode) {
                data.cmd += " StoreCode"
            }
            if (data.storeIcon) {
                data.cmd += " StoreIcon"
            }
            if (data.storeName) {
                data.cmd += " StoreName"
            }
            if (data.auditPackage) {
                data.cmd += " AuditPackage"
            }
            if (data.tag.isNotEmpty()) {
                data.cmd += " " + data.tag
            }
            Utils.cmdExec("git", "reset", "--hard", "HEAD", dir = rootPath)
            writeReplaceFile(data)
//            updateConfigFile()
            Utils.cmdExec("git", "add", ".", dir = rootPath)
            if (Utils.cmdExec("git", "status", dir = rootPath).contains("Changes")) {
                Utils.cmdExec("git", "commit", "-m", data.cmd, dir = rootPath)
            } else {
                Utils.cmdExec("git", "commit", "--allow-empty", "-m", data.cmd, dir = rootPath)
            }
            if (data.tag.isNotEmpty()) {
                data.tag =
                    "${selectedPacketCode.capitalize()}-${data.versionName}-${data.versionCode}-${todayDate()}-${data.tag}"
                // 删除本地和远端tag
                Utils.cmdExec("git", "tag", "-d", data.tag)
                Utils.cmdExec("git", "push", "origin", "--delete", "refs/tags/${data.tag}")
                // 添加tag
                Utils.cmdExec("git", "tag", data.tag)
            }
            ConfirmationDialog(jRootFrame, "通知", "提交成功，是否push到远端", {
                Thread {
                    val currentBranchName = Utils.cmdExec("git", "rev-parse", "--abbrev-ref", "HEAD", dir = rootPath)
                    if (currentBranchName.isNotEmpty() && currentBranchName != "-1") {
                        if (if (data.tag.isNotEmpty()) {
                                Utils.cmdExec(
                                    "git",
                                    "push",
                                    "origin",
                                    currentBranchName,
                                    "--tags",
                                    dir = rootPath
                                ) != "-1"
                            } else {
                                Utils.cmdExec(
                                    "git",
                                    "push",
                                    "origin",
                                    currentBranchName,
                                    "--tags",
                                    dir = rootPath
                                ) != "-1"
                            }
                        ) {
                            ConfirmationDialog(
                                jRootFrame,
                                "通知",
                                "push成功！！！",
                                { refreshUI() },
                                { refreshUI() })
                        } else {
                            ConfirmationDialog(
                                jRootFrame,
                                "通知",
                                "push命令执行失败了！！！",
                                { refreshUI() },
                                { refreshUI() })
                        }
                    } else {
                        ConfirmationDialog(
                            jRootFrame,
                            "通知",
                            "rev-parse执行获取分支名失败了！！！，请检测后再尝试",
                            { refreshUI() },
                            { refreshUI() })
                    }
                }.start()
            }, {
                refreshUI()
            })
        }.start()
    }

    /*  private fun updateConfigFile() {
          selectedPacket?.apply {
              if (jUpdateVersion.isSelected) {
                  if (!version.contains(uiData.versionName)) {
                      version.add(0, uiData.versionName)
                      version = ArrayList(version.sortedByDescending {
                          it.split(".").sumOf { it.toInt() }
                      })
                      versionCode.add(version.indexOf(uiData.versionName), uiData.versionCode.toInt())
                  }
              }
          }
          writeConfigFile(rootPath, config)
      }*/

    private fun isNeedWarningDialog(data: UIData): Boolean {
        if (data.applicationId.isEmpty()) {
            showWarningDialog("applicationId is empty")
            return true
        }
        if (data.cmd.isEmpty()) {
            showWarningDialog("cmd is empty")
            return true
        }
        if (data.versionCode.isEmpty()) {
            showWarningDialog("versionCode is empty")
            return true
        }
        if (data.versionName.isEmpty()) {
            showWarningDialog("versionName is empty")
            return true
        }
        if (data.versionName.isEmpty()) {
            showWarningDialog("versionName is empty")
            return true
        }
        return false
    }


    private fun showWarningDialog(message: String, callback: (() -> Unit)? = null) {
        ConfirmationDialog(jRootFrame, "警告", message, callback)
    }

    private fun refreshUI() {
        ConfigUtils.refreshReplaceLib()
        Utils.refreshData(jProjectList.selectedItem!!.toString())
        SwingUtilities.invokeLater {
            jSubmitButton.text = "提交打包请求"
            jSubmitButton.isEnabled = true
            uiHome.jLibAd.text = Utils.libAds
            uiHome.jLibFramework.text = Utils.libAppFramework
            uiHome.jLibSecurity.text = Utils.libSecurity
            selectedPacketCode = jProjectList.selectedItem!!.toString()
            println(selectedPacketCode)
//            jProjectTip.text = config.tip
            jProjectName.text = "项目名称：${Utils.appName}"
            jAppId.text = Utils.appId
            jVersionCode.text = Utils.versionCode
            var historyVersion = Utils.versions.toString()
            historyVersion = historyVersion.substring(1, historyVersion.length - 1)
            jHistoryVersion.text =
                if (historyVersion.contains(",")) historyVersion.replace(", ", "\n") else historyVersion
            SwingUtilities.invokeLater {
                jHistoryScrollPane.verticalScrollBar.value = 0
            }
            if (Utils.versions.isNotEmpty()) jVersion.text = Utils.versions[0]
            jBuildProjectCmd.text = "[Build_${selectedPacketCode.capitalize()}]"
            uiHome.jTip.text = ConfigUtils.getTip()
            val libChangList = ConfigUtils.config.libReplace.filter { it.isEnable() }
            if (libChangList.isNotEmpty()) {
                var jLibChangeText = "原版本：\n"
                for (lib in libChangList) {
                    jLibChangeText += lib.oldValue + "\n"
                }
                jLibChangeText += "新版本：\n"
                for (lib in libChangList) {
                    jLibChangeText += lib.newValue + "\n"
                }
                uiHome.jLibChange.text = jLibChangeText
            } else {
                uiHome.jLibChange.text = "无需变更"
            }

        }
    }

    private fun writeReplaceFile(data: UIData) {
        val buildConfigPath = Utils.buildGradlePath
        val yamlPath = "${Utils.flavorPrefixPath}/${jProjectList.selectedItem!!}/assets/config.yaml"
        Utils.writeLogToFile("Start WriteReplaceFile buildConfigPath:$buildConfigPath, yamlPath:$yamlPath")
        // 修改build文件
        replaceFile(
            yamlPath,
            PLIST_URL_REGEX,
            "${data.versionName}-${data.versionCode}"
        )
        replaceFile(
            buildConfigPath,
            VERSION_CODE_REGEX.replace("flavor", selectedPacketCode),
            data.versionCode
        )
        replaceFile(
            buildConfigPath,
            VERSION_NAME_REGEX.replace("flavor", selectedPacketCode),
            data.versionName
        )
       if (uiHome.jLibChangeSwitch.isSelected) {
           for (lib in ConfigUtils.config.libReplace) {
               if (lib.isEnable()) {
                   replaceFile(buildConfigPath, lib.regex, "\"${lib.newValue}\"")
               }
           }
       }
//        replaceFile(
//            buildConfigPath,
//            APPLICATION_ID_REGEX.replace("flavor", selectedPacketCode),
//            data.applicationId
//        )
        Utils.writeLogToFile("修改完成")
    }

    private fun replaceFile(path: String, regexStr: String, newValue: String) {
        try {
            if (regexStr.isEmpty()) return
            val regex = regexStr.toRegex()
            val gradleFile = File(path)
            var content = gradleFile.readText()
            content = content.replaceFirst(regex, newValue)
            gradleFile.writeText(content)
        } catch (e: Exception) {
            Utils.writeLogToFile(e.message.toString())
        }
    }
}
