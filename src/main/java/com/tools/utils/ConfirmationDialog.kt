package com.tools.utils

import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class ConfirmationDialog(
    owner: JFrame,
    title: String,
    message: String,
    onContinue: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) : JDialog(owner, title, true) {

    private val continueButton = JButton("确定")
    private val cancelButton = JButton("关闭")

    init {
        val contentPane = JPanel()
        contentPane.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val messageLabel = JLabel("<html>$message</html>")
        messageLabel.horizontalAlignment = SwingConstants.CENTER // 将 Label 中的文本居中显示
        contentPane.layout = BorderLayout()
        contentPane.add(messageLabel, BorderLayout.CENTER)

        val buttonPane = JPanel()
        buttonPane.layout = FlowLayout(FlowLayout.CENTER)
        buttonPane.add(continueButton)
        buttonPane.add(cancelButton)
        contentPane.add(buttonPane, BorderLayout.SOUTH)
        continueButton.addActionListener {
            SwingUtilities.invokeLater {
                onContinue?.invoke()
                dispose()
            }
        }
        cancelButton.addActionListener {
            SwingUtilities.invokeLater {
                onCancel?.invoke()
                dispose()
            }
        }
        add(contentPane)
        isResizable = false // 窗口不可调整大小
        setSize(300, 300)
        setLocationRelativeTo(owner)
        isVisible = true
    }
}
