package com.github.yohanki.egovconstant.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class EgovToolWindowFactory : ToolWindowFactory {

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = EgovToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
