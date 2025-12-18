package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.services.GovVarService
import org.jetbrains.plugins.template.services.GovVariable
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Factory class for creating the GovVar tool window.
 * This tool window allows users to search for government/public-standard variable names.
 */
class GovVarToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().info("GovVar Tool Window Factory initialized")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val govVarToolWindow = GovVarToolWindow(project)
        val content = ContentFactory.getInstance().createContent(govVarToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    /**
     * Inner class representing the GovVar tool window UI.
     */
    class GovVarToolWindow(private val project: Project) {

        private val service = project.service<GovVarService>()
        private val searchField = JBTextField()
        private val resultTableModel = createTableModel()
        private val resultTable = JBTable(resultTableModel)

        /**
         * Creates and returns the main content panel for the tool window.
         */
        fun getContent(): JComponent {
            val mainPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                minimumSize = Dimension(300, 200)
            }

            // Create search panel at the top
            val searchPanel = createSearchPanel()
            mainPanel.add(searchPanel, BorderLayout.NORTH)

            // Create results panel in the center
            val resultsPanel = createResultsPanel()
            mainPanel.add(resultsPanel, BorderLayout.CENTER)

            // Create info panel at the bottom
            val infoPanel = createInfoPanel()
            mainPanel.add(infoPanel, BorderLayout.SOUTH)

            return mainPanel
        }

        /**
         * Creates the search panel with input field and button.
         */
        private fun createSearchPanel(): JPanel {
            val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }

            // Label
            val label = JBLabel(MyBundle.message("govvar.search.label"))
            panel.add(label, BorderLayout.WEST)

            // Search field
            searchField.apply {
                toolTipText = MyBundle.message("govvar.search.tooltip")
                // Add enter key listener for search
                addActionListener { performSearch() }
            }
            
            val fieldPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
                add(searchField, BorderLayout.CENTER)
            }
            panel.add(fieldPanel, BorderLayout.CENTER)

            // Search button
            val searchButton = JButton(MyBundle.message("govvar.search.button")).apply {
                addActionListener { performSearch() }
            }
            panel.add(searchButton, BorderLayout.EAST)

            return panel
        }

        /**
         * Creates the results panel with a table to display search results.
         */
        private fun createResultsPanel(): JComponent {
            // Configure table
            resultTable.apply {
                fillsViewportHeight = true
                setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
                
                // Set column widths
                columnModel.getColumn(0).preferredWidth = 150  // Name
                columnModel.getColumn(1).preferredWidth = 250  // Description
                columnModel.getColumn(2).preferredWidth = 80   // Type
                columnModel.getColumn(3).preferredWidth = 120  // Standard
            }

            val scrollPane = JBScrollPane(resultTable).apply {
                border = BorderFactory.createEmptyBorder(0, 10, 10, 10)
            }

            return scrollPane
        }

        /**
         * Creates the info panel with helpful information.
         */
        private fun createInfoPanel(): JPanel {
            val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                border = BorderFactory.createEmptyBorder(5, 10, 10, 10)
            }

            val infoLabel = JBLabel(MyBundle.message("govvar.info.text")).apply {
                font = font.deriveFont(font.size2D * 0.9f)
            }
            panel.add(infoLabel, BorderLayout.CENTER)

            return panel
        }

        /**
         * Creates the table model for displaying search results.
         */
        private fun createTableModel(): DefaultTableModel {
            return object : DefaultTableModel(
                arrayOf(
                    MyBundle.message("govvar.table.name"),
                    MyBundle.message("govvar.table.description"),
                    MyBundle.message("govvar.table.type"),
                    MyBundle.message("govvar.table.standard")
                ), 0
            ) {
                override fun isCellEditable(row: Int, column: Int) = false
            }
        }

        /**
         * Performs the search operation and updates the results table.
         */
        private fun performSearch() {
            val keyword = searchField.text.trim()
            
            // Clear existing results
            resultTableModel.rowCount = 0

            if (keyword.isEmpty()) {
                thisLogger().info("Search performed with empty keyword")
                return
            }

            thisLogger().info("Searching for keyword: $keyword")

            // Get search results from service
            val results = service.searchGovVariables(keyword)

            // Populate table with results
            results.forEach { variable ->
                resultTableModel.addRow(
                    arrayOf(
                        variable.name,
                        variable.description,
                        variable.type,
                        variable.standard
                    )
                )
            }

            thisLogger().info("Search completed. Found ${results.size} results")
        }
    }
}
