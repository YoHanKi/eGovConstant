package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.*
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.*

class EgovToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = EgovToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}

private class EgovToolWindowPanel(private val project: Project) : JBPanel<EgovToolWindowPanel>(BorderLayout()) {
    private val service = project.service<DictionaryService>()

    private val pathField = JBTextField()
    private val browseBtn = JButton("Browse…")
    private val importBtn = JButton("Import XLSX")
    private val resetBtn = JButton("Reset to Default")
    private val statusLabel = JBLabel("Status: ")

    private val searchField = JBTextField()
    private val typeCombo = JComboBox(arrayOf("ALL", EntryType.TERM.name, EntryType.WORD.name, EntryType.DOMAIN.name))
    private val resultsModel = DefaultListModel<DictionaryIndex.Ranked>()
    private val resultsList = JBList(resultsModel)

    private val detailPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        background = JBUI.CurrentTheme.ToolWindow.background()
    }

    private val prevBtn = JButton("< Prev")
    private val nextBtn = JButton("Next >")
    private val pageLabel = JBLabel("Page 1")
    private var currentPage = 0
    private val pageSize = 100
    private var fullResults: List<DictionaryIndex.Ranked> = emptyList()

    private val copyBtn = JButton("Copy name")
    private val insertBtn = JButton("Insert at caret")

    init {
        minimumSize = Dimension(300, 400)

        // Import Panel
        val top = JPanel(BorderLayout(5, 5)).apply {
            border = JBUI.Borders.empty(5)
        }
        val filePanel = JPanel(BorderLayout(5, 5))
        filePanel.add(pathField, BorderLayout.CENTER)
        filePanel.add(browseBtn, BorderLayout.EAST)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        buttonPanel.add(importBtn)
        buttonPanel.add(resetBtn)

        val importGroup = JPanel(BorderLayout(5, 5))
        importGroup.add(JBLabel("Import XLSX:"), BorderLayout.NORTH)
        importGroup.add(filePanel, BorderLayout.CENTER)
        importGroup.add(buttonPanel, BorderLayout.SOUTH)
        top.add(importGroup, BorderLayout.CENTER)

        // Search & Results Panel
        val mid = JPanel(BorderLayout(5, 5)).apply {
            border = JBUI.Borders.empty(5, 5, 0, 5)
        }
        val searchBar = JPanel(BorderLayout(5, 5))
        searchBar.add(searchField, BorderLayout.CENTER)
        searchBar.add(typeCombo, BorderLayout.EAST)
        mid.add(searchBar, BorderLayout.NORTH)

        resultsList.apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): java.awt.Component {
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    val r = value as? DictionaryIndex.Ranked
                    if (r != null) {
                        val e = r.entry
                        val abbr = e.enAbbr ?: e.enName ?: e.koName
                        val typeColor = when(e.type) {
                            EntryType.TERM -> "#4a86e8"
                            EntryType.WORD -> "#6aa84f"
                            EntryType.DOMAIN -> "#e69138"
                        }
                        comp.text = "<html><body style='width: 100%;'>" +
                                "<span style='color: $typeColor; font-weight: bold;'>[${e.type}]</span> " +
                                "<b>${e.koName}</b> " +
                                "<span style='color: gray;'>($abbr)</span>" +
                                "</body></html>"
                    }
                    return comp
                }
            }
        }

        val resultsScroll = JScrollPane(resultsList).apply {
            preferredSize = Dimension(-1, 200)
        }

        val paginationPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(prevBtn)
            add(pageLabel)
            add(nextBtn)
        }

        val midCenter = JPanel(BorderLayout())
        midCenter.add(resultsScroll, BorderLayout.CENTER)
        midCenter.add(paginationPanel, BorderLayout.SOUTH)
        mid.add(midCenter, BorderLayout.CENTER)

        // Detail Panel
        val detailScroll = JScrollPane(detailPane).apply {
            preferredSize = Dimension(-1, 120)
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(JBUI.CurrentTheme.ToolWindow.borderColor(), 1, 0, 0, 0),
                JBUI.Borders.empty(5)
            )
        }

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(mid, BorderLayout.CENTER)
        contentPanel.add(detailScroll, BorderLayout.SOUTH)

        // Bottom Actions
        val bottom = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
        }
        val actionButtons = JPanel(FlowLayout(FlowLayout.LEFT))
        actionButtons.add(copyBtn)
        actionButtons.add(insertBtn)
        bottom.add(actionButtons, BorderLayout.WEST)
        bottom.add(statusLabel, BorderLayout.CENTER)

        add(top, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)

        updateStatus()
        runSearch()

        browseBtn.addActionListener {
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            val base = VfsUtil.findFile(File(pathField.text.ifBlank { project.basePath ?: "." }).toPath(), true)
            val vf: VirtualFile? = FileChooser.chooseFile(descriptor, project, base)
            if (vf != null) pathField.text = VfsUtil.virtualToIoFile(vf).absolutePath
        }

        importBtn.addActionListener {
            val file = File(pathField.text)
            if (!file.exists()) {
                statusLabel.text = "Status: File not found"
                return@addActionListener
            }
            service.loadFromXlsx(file) {
                updateStatus()
                runSearch()
            }
        }

        resetBtn.addActionListener {
            service.resetToDefault()
            updateStatus()
            runSearch()
        }

        resultsList.addListSelectionListener {
            val r = resultsList.selectedValue ?: return@addListSelectionListener
            updateDetail(r.entry)
        }

        prevBtn.addActionListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        nextBtn.addActionListener {
            if ((currentPage + 1) * pageSize < fullResults.size) {
                currentPage++
                updatePage()
            }
        }

        searchField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) { runSearch() }
        })
        typeCombo.addActionListener { runSearch() }

        copyBtn.addActionListener {
            val r = resultsList.selectedValue ?: return@addActionListener
            val abbr = r.entry.enAbbr ?: r.entry.enName ?: r.entry.koName
            val camel = NameGenerator.fromAbbreviation(abbr).camel
            val clip = Toolkit.getDefaultToolkit().systemClipboard
            val sel = StringSelection(camel)
            clip.setContents(sel, sel)
        }
        insertBtn.addActionListener {
            val r = resultsList.selectedValue ?: return@addActionListener
            val abbr = r.entry.enAbbr ?: r.entry.enName ?: r.entry.koName
            val camel = NameGenerator.fromAbbreviation(abbr).camel
            val editor = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).selectedTextEditor ?: return@addActionListener
            EditorModificationUtil.insertStringAtCaret(editor, camel)
        }
    }

    private fun runSearch() {
        val text = searchField.text
        if (!service.ensureLoaded()) { updateStatus(); return }
        val type = when (typeCombo.selectedItem as String) {
            EntryType.TERM.name -> EntryType.TERM
            EntryType.WORD.name -> EntryType.WORD
            EntryType.DOMAIN.name -> EntryType.DOMAIN
            else -> null
        }
        val idx = service.getIndex() ?: return
        ApplicationManager.getApplication().executeOnPooledThread {
            fullResults = idx.search(DictionaryIndex.Query(text = text, type = type))
            SwingUtilities.invokeLater {
                currentPage = 0
                updatePage()
            }
        }
    }

    private fun updatePage() {
        resultsModel.clear()
        val start = currentPage * pageSize
        val end = minOf(start + pageSize, fullResults.size)
        for (i in start until end) {
            resultsModel.addElement(fullResults[i])
        }
        pageLabel.text = "Page ${currentPage + 1} / ${maxOf(1, (fullResults.size + pageSize - 1) / pageSize)}"
        prevBtn.isEnabled = currentPage > 0
        nextBtn.isEnabled = (currentPage + 1) * pageSize < fullResults.size
    }

    private fun updateDetail(e: com.github.yohanki.egovconstant.data.StdEntry) {
        val html = buildString {
            append("<html><body style='font-family: sans-serif; font-size: 11pt;'>")
            append("<h3 style='margin-bottom: 5px;'>${e.koName} <span style='color: gray; font-weight: normal;'>(${e.enAbbr ?: e.enName ?: "-"})</span></h3>")
            append("<div style='margin-bottom: 10px;'><b>Type:</b> ${e.type} | <b>Source:</b> ${e.source}</div>")

            if (!e.description.isNullOrBlank()) {
                append("<p><b>Description:</b><br/>${e.description}</p>")
            }

            if (e.synonyms.isNotEmpty()) {
                append("<p><b>Synonyms:</b> ${e.synonyms.joinToString(", ")}</p>")
            }

            if (e.type == EntryType.DOMAIN || !e.domainName.isNullOrBlank()) {
                append("<hr/>")
                append("<p><b>Domain Info:</b><br/>")
                append("Name: ${e.domainName ?: "-"}<br/>")
                append("Type: ${e.dataType ?: "-"} (${e.dataLength ?: "-"}${if (!e.dataScale.isNullOrBlank()) ", " + e.dataScale else ""})<br/>")
                if (!e.allowedValues.isNullOrBlank()) {
                    append("Allowed: ${e.allowedValues}<br/>")
                }
                append("</p>")
            }
            append("</body></html>")
        }
        detailPane.text = html
        detailPane.caretPosition = 0
    }

    private fun updateStatus() {
        statusLabel.text = "Status: ${service.getStatus()}"
    }
}