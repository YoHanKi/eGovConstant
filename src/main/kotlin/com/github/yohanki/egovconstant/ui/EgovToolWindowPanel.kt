package com.github.yohanki.egovconstant.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import java.awt.BorderLayout

class EgovToolWindowPanel(private val project: Project) : JBPanel<EgovToolWindowPanel>(BorderLayout()) {
    private val tabs = JBTabbedPane()

    init {
        tabs.addTab("Search", EgovSearchPanel(project))
        tabs.addTab("Settings", EgovSettingsPanel(project))
        add(tabs, BorderLayout.CENTER)
    }
}
