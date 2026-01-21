package com.github.yohanki.egovconstant.ui

import com.github.yohanki.egovconstant.data.EntryType
import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.github.yohanki.egovconstant.service.DictionarySettingsListener
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

    private val searchField = JBTextField().apply {
        emptyText.text = "검색어 입력..."
    }
    private val typeCombo = JComboBox(arrayOf("전체 (ALL)", EntryType.TERM.name, EntryType.WORD.name, EntryType.DOMAIN.name))
    private val resultsModel = DefaultListModel<DictionaryIndex.Ranked>()
    private val resultsList = JBList(resultsModel)

    private val detailPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        background = JBUI.CurrentTheme.ToolWindow.background()
    }

    private val closeDetailBtn = JButton("상세 닫기").apply {
        isFocusable = false
    }

    private val prevBtn = JButton("<").apply { isFocusable = false; margin = JBUI.insets(1) }
    private val nextBtn = JButton(">").apply { isFocusable = false; margin = JBUI.insets(1) }
    
    private val pageField = JTextField().apply {
        preferredSize = Dimension(40, 24)
        horizontalAlignment = JTextField.CENTER
    }
    private val totalPageLabel = JBLabel("/ 1")
    
    private var currentPage = 0
    private var pageSize = 100
    private var fullResults: List<DictionaryIndex.Ranked> = emptyList()

    private val pageSizeCombo = JComboBox(arrayOf(50, 100, 200, 500)).apply {
        selectedItem = 100
        isFocusable = false
        addActionListener {
            pageSize = selectedItem as Int
            currentPage = 0
            updatePage()
        }
    }

    private val copyBtn = JButton("복사").apply { margin = JBUI.insets(1) }
    private val insertBtn = JButton("삽입").apply { margin = JBUI.insets(1) }
    private val statusLabel = JLabel("상태: ")

    private var detailScroll: JScrollPane

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

        val paginationPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(5)
            
            val pageRow = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)).apply {
                add(prevBtn)
                add(pageField)
                add(totalPageLabel)
                add(nextBtn)
            }
            
            val sizeRow = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)).apply {
                add(JLabel("표시:"))
                add(pageSizeCombo)
            }
            
            add(pageRow)
            add(sizeRow)
        }

        val center = JPanel(BorderLayout())
        center.add(resultsScroll, BorderLayout.CENTER)
        center.add(paginationPanel, BorderLayout.SOUTH)
        top.add(center, BorderLayout.CENTER)

        // Detail Panel
        val detailContent = JPanel(BorderLayout())
        detailScroll = JScrollPane(detailPane).apply {
            preferredSize = Dimension(-1, 200)
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(JBUI.CurrentTheme.ToolWindow.borderColor(), 1, 0, 0, 0),
                JBUI.Borders.empty(5)
            )
        }
        detailContent.add(detailScroll, BorderLayout.CENTER)
        
        val detailActions = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(closeDetailBtn)
        }
        detailContent.add(detailActions, BorderLayout.NORTH)
        detailContent.isVisible = false

        // Bottom Actions
        val bottom = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
        }
        val actionButtons = JPanel(FlowLayout(FlowLayout.LEFT, 2, 2))
        actionButtons.add(copyBtn)
        actionButtons.add(insertBtn)
        
        val bottomWrapper = JPanel(BorderLayout()).apply {
            add(actionButtons, BorderLayout.WEST)
            add(statusLabel, BorderLayout.CENTER)
        }
        bottom.add(bottomWrapper, BorderLayout.CENTER)

        // Correcting layout
        val content = JPanel(BorderLayout())
        content.add(top, BorderLayout.CENTER)
        content.add(detailContent, BorderLayout.SOUTH)
        
        add(content, BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)

        updateStatus()
        runSearch()

        project.messageBus.connect().subscribe(DictionarySettingsListener.TOPIC, object : DictionarySettingsListener {
            override fun onSettingsChanged() {
                runSearch()
                updateStatus()
            }
        })

        resultsList.addListSelectionListener {
            val r = resultsList.selectedValue
            if (r != null) {
                updateDetail(r.entry)
                detailContent.isVisible = true
                revalidate()
                repaint()
            }
        }

        closeDetailBtn.addActionListener {
            resultsList.clearSelection()
            detailContent.isVisible = false
            revalidate()
            repaint()
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

        pageField.addActionListener {
            val totalPages = maxOf(1, (fullResults.size + pageSize - 1) / pageSize)
            val requestedPage = pageField.text.toIntOrNull()
            if (requestedPage != null && requestedPage in 1..totalPages) {
                currentPage = requestedPage - 1
                updatePage()
            } else {
                pageField.text = (currentPage + 1).toString()
            }
        }

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
        val type = when (typeCombo.selectedIndex) {
            1 -> EntryType.TERM
            2 -> EntryType.WORD
            3 -> EntryType.DOMAIN
            else -> null
        }
        val idx = service.getIndex() ?: return
        ApplicationManager.getApplication().executeOnPooledThread {
            val results = idx.search(DictionaryIndex.Query(text = text, type = type))
            SwingUtilities.invokeLater {
                fullResults = results
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
        val totalPages = maxOf(1, (fullResults.size + pageSize - 1) / pageSize)
        pageField.text = (currentPage + 1).toString()
        totalPageLabel.text = "/ $totalPages"
        
        prevBtn.isEnabled = currentPage > 0
        nextBtn.isEnabled = (currentPage + 1) * pageSize < fullResults.size
    }

    private fun updateDetail(e: com.github.yohanki.egovconstant.data.StdEntry) {
        val html = buildString {
            append("<html><body style='font-family: sans-serif; font-size: 11pt;'>")
            append("<h3 style='margin-bottom: 5px;'>${e.koName} <span style='color: gray; font-weight: normal;'>(${e.enAbbr ?: e.enName ?: "-"})</span></h3>")
            append("<div style='margin-bottom: 10px;'><b>유형:</b> ${e.type} | <b>출처:</b> ${e.source}</div>")

            if (!e.description.isNullOrBlank()) {
                append("<p><b>설명:</b><br/>${e.description}</p>")
            }

            if (e.synonyms.isNotEmpty()) {
                append("<p><b>동의어:</b> ${e.synonyms.joinToString(", ")}</p>")
            }

            if (e.type == EntryType.DOMAIN || !e.domainName.isNullOrBlank()) {
                append("<hr/>")
                append("<p><b>도메인 정보:</b><br/>")
                append("명칭: ${e.domainName ?: "-"}<br/>")
                append("유형: ${e.dataType ?: "-"} (${e.dataLength ?: "-"}${if (!e.dataScale.isNullOrBlank()) ", " + e.dataScale else ""})<br/>")
                if (!e.allowedValues.isNullOrBlank()) {
                    append("허용값: ${e.allowedValues}<br/>")
                }
                append("</p>")
            }
            append("</body></html>")
        }
        detailPane.text = html
        detailPane.caretPosition = 0
    }

    private fun updateStatus() {
        statusLabel.text = service.getStatus()
    }
}
