package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class EgovSettingsPanel(project: Project) : JPanel(BorderLayout()) {
    private val service = project.service<DictionaryService>()
    private val jsonArea = JTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
    }
    private val importBtn = JButton("Import JSON")
    private val resetBtn = JButton("Reset to Default")
    private val statusLabel = JBLabel("Status: Ready")

    init {
        val top = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(JBLabel("Paste JSON Dictionary:"), BorderLayout.NORTH)
        }
        
        val scroll = JScrollPane(jsonArea).apply {
            border = JBUI.Borders.empty(5)
        }
        
        val bottom = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = JBUI.Borders.empty(5)
            add(importBtn)
            add(resetBtn)
            add(statusLabel)
        }

        add(top, BorderLayout.NORTH)
        add(scroll, BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)

        importBtn.addActionListener {
            val json = jsonArea.text
            if (json.isBlank()) {
                statusLabel.text = "Status: JSON is empty"
                return@addActionListener
            }
            service.loadFromJson(json) {
                statusLabel.text = "Status: Imported successfully"
                jsonArea.text = ""
            }
        }

        resetBtn.addActionListener {
            service.resetToDefault()
            statusLabel.text = "Status: Reset to default"
        }
    }
}
