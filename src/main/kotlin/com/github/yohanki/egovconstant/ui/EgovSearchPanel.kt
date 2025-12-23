package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class EgovSearchPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val service = project.service<DictionaryService>()

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
    private val statusLabel = JBLabel("Status: ")

    init {
        // Search & Results Panel
        val top = JPanel(BorderLayout(5, 5)).apply {
            border = JBUI.Borders.empty(5)
        }
        val searchBar = JPanel(BorderLayout(5, 5))
        searchBar.add(searchField, BorderLayout.CENTER)
        searchBar.add(typeCombo, BorderLayout.EAST)
        top.add(searchBar, BorderLayout.NORTH)

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

        val center = JPanel(BorderLayout())
        center.add(resultsScroll, BorderLayout.CENTER)
        center.add(paginationPanel, BorderLayout.SOUTH)
        top.add(center, BorderLayout.CENTER)

        // Detail Panel
        val detailScroll = JScrollPane(detailPane).apply {
            preferredSize = Dimension(-1, 120)
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(JBUI.CurrentTheme.ToolWindow.borderColor(), 1, 0, 0, 0),
                JBUI.Borders.empty(5)
            )
        }

        // Bottom Actions
        val bottom = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
        }
        val actionButtons = JPanel(FlowLayout(FlowLayout.LEFT))
        actionButtons.add(copyBtn)
        actionButtons.add(insertBtn)
        bottom.add(actionButtons, BorderLayout.WEST)
        bottom.add(statusLabel, BorderLayout.CENTER)

        add(top, BorderLayout.CENTER)
        add(detailScroll, BorderLayout.SOUTH)
        add(bottom, BorderLayout.SOUTH) // This will overwrite detailScroll in BorderLayout, fixing below
        
        // Correcting layout
        removeAll()
        val content = JPanel(BorderLayout())
        content.add(top, BorderLayout.CENTER)
        content.add(detailScroll, BorderLayout.SOUTH)
        add(content, BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)

        updateStatus()
        runSearch()

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
            
            WriteCommandAction.runWriteCommandAction(project) {
                val offset = editor.caretModel.offset
                editor.document.insertString(offset, camel)
                editor.caretModel.moveToOffset(offset + camel.length)
            }
            // Request focus back to editor
            editor.contentComponent.requestFocusInWindow()
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
