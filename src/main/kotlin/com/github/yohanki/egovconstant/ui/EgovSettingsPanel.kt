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
    private val importBtn = JButton("Import JSON")
    private val resetBtn = JButton("Reset to Default")
    private val statusLabel = JBLabel("Status: Ready")

    private val exampleJson = """
        [
          {
            "type": "TERM",
            "koName": "사용자아이디",
            "enAbbr": "USER_ID",
            "enName": "User ID",
            "description": "Unique identifier for a user"
          },
          {
            "type": "WORD",
            "koName": "사용자",
            "enAbbr": "USER"
          }
        ]
    """.trimIndent()

    private val jsonArea = JTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        text = exampleJson
        foreground = Color.GRAY
    }

    init {
        jsonArea.addFocusListener(object : java.awt.event.FocusAdapter() {
            override fun focusGained(e: java.awt.event.FocusEvent?) {
                if (jsonArea.text == exampleJson) {
                    jsonArea.text = ""
                    jsonArea.foreground = JBUI.CurrentTheme.Label.foreground()
                }
            }

            override fun focusLost(e: java.awt.event.FocusEvent?) {
                if (jsonArea.text.isEmpty()) {
                    jsonArea.text = exampleJson
                    jsonArea.foreground = Color.GRAY
                }
            }
        })

        val top = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(JBLabel("Paste JSON Dictionary (see example below):"), BorderLayout.NORTH)
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
            if (json.isBlank() || json == exampleJson) {
                statusLabel.text = "Status: JSON is empty"
                return@addActionListener
            }
            service.loadFromJson(json, { summary ->
                statusLabel.text = "Status: Imported successfully"
                jsonArea.text = ""
                if (jsonArea.isFocusOwner.not()) {
                    jsonArea.text = exampleJson
                    jsonArea.foreground = Color.GRAY
                }
            }, { error ->
                statusLabel.text = "Error: Invalid JSON format - ${error.message}"
            })
        }

        resetBtn.addActionListener {
            service.resetToDefault()
            statusLabel.text = "Status: Reset to default"
        }
    }
}
