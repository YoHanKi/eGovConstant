package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class EgovSettingsPanel(project: Project) : JPanel(BorderLayout()) {
    private val service = project.service<DictionaryService>()
    private val resetBtn = JButton("초기화").apply { margin = JBUI.insets(2) }
    private val useCustomOnlyCb = JCheckBox("커스텀 데이터만 사용").apply {
        isSelected = service.useCustomOnly
    }
    private val completionEnabledCb = JCheckBox("변수 추천 활성화").apply {
        isSelected = service.completionEnabled
    }
    private val completionCountCombo = JComboBox(arrayOf(5, 10, 20, 40)).apply {
        selectedItem = service.completionCount
    }
    private val statusLabel = JLabel("상태: 준비됨")

    init {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
        }

        mainPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(useCustomOnlyCb)
        })
        
        mainPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(completionEnabledCb)
        })

        mainPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("변수 추천 개수: "))
            add(completionCountCombo)
        })

        mainPanel.add(JSeparator())

        mainPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(resetBtn)
            add(statusLabel)
        })

        add(mainPanel, BorderLayout.NORTH)

        useCustomOnlyCb.addActionListener {
            service.useCustomOnly = useCustomOnlyCb.isSelected
        }

        completionEnabledCb.addActionListener {
            service.completionEnabled = completionEnabledCb.isSelected
        }

        completionCountCombo.addActionListener {
            service.completionCount = completionCountCombo.selectedItem as Int
        }

        resetBtn.addActionListener {
            service.resetToDefault()
            useCustomOnlyCb.isSelected = service.useCustomOnly
            completionEnabledCb.isSelected = service.completionEnabled
            completionCountCombo.selectedItem = service.completionCount
            statusLabel.text = "상태: 초기화됨"
        }
    }
}
