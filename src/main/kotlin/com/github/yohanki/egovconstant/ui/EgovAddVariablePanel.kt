package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class EgovAddVariablePanel(project: Project) : JPanel(BorderLayout()) {
    private val service = project.service<DictionaryService>()
    private val importBtn = JButton("JSON 추가").apply { margin = JBUI.insets(2) }
    private val statusLabel = JLabel("상태: 준비됨")

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
        foreground = JBColor.GRAY
    }

    init {
        jsonArea.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyTyped(e: java.awt.event.KeyEvent) {
                if (e.keyChar == '{') {
                    val currentText = jsonArea.text
                    val isPlaceholder = currentText == exampleJson
                    
                    val template = """  {
    "type": "",
    "koName": "",
    "enAbbr": "",
    "enName": "",
    "description": ""
  }"""
                    
                    if (isPlaceholder) {
                        jsonArea.text = ""
                        jsonArea.foreground = JBUI.CurrentTheme.Label.foreground()
                    }
                    
                    val caret = jsonArea.caretPosition
                    val doc = jsonArea.document
                    try {
                        e.consume()
                        doc.insertString(caret, template, null)
                        jsonArea.caretPosition = caret + template.indexOf("\"type\": \"") + 9
                    } catch (ex: Exception) {
                    }
                }
            }
        })

        jsonArea.addFocusListener(object : java.awt.event.FocusAdapter() {
            override fun focusGained(e: java.awt.event.FocusEvent?) {
                if (jsonArea.text == exampleJson) {
                    jsonArea.text = ""
                    jsonArea.foreground = JBUI.CurrentTheme.Label.foreground()
                }
            }

            override fun focusLost(e: java.awt.event.FocusEvent?) {
                if (jsonArea.text.isEmpty() || jsonArea.text.isBlank()) {
                    jsonArea.text = exampleJson
                    jsonArea.foreground = JBColor.GRAY
                }
            }
        })

        val top = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(JBLabel("JSON 사전을 붙여넣으세요 (아래 예시 참고):"), BorderLayout.NORTH)
        }
        
        val scroll = JScrollPane(jsonArea).apply {
            border = JBUI.Borders.empty(5)
        }
        
        val bottom = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(5)
            
            val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 2)).apply {
                add(importBtn)
                add(statusLabel)
            }
            add(buttonPanel)
        }

        add(top, BorderLayout.NORTH)
        add(scroll, BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)

        importBtn.addActionListener {
            val json = jsonArea.text
            if (json.isBlank() || json == exampleJson) {
                statusLabel.text = "상태: JSON이 비어있음"
                return@addActionListener
            }
            service.loadFromJson(json, { summary ->
                statusLabel.text = "상태: 성공적으로 가져옴"
                jsonArea.text = exampleJson
                jsonArea.foreground = JBColor.GRAY
            }, { error ->
                statusLabel.text = "오류: 잘못된 JSON 형식 - ${error.message}"
            })
        }
    }
}
