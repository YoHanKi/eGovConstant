package org.jetbrains.plugins.template.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

/**
 * Service layer for government/public-standard variable name search.
 * This service provides mock data for demonstration purposes.
 */
@Service(Service.Level.PROJECT)
class GovVarService(project: Project) {

    init {
        thisLogger().info("GovVarService initialized for project: ${project.name}")
    }

    /**
     * Search for government/public-standard variable names based on keyword.
     * 
     * TODO: Replace mock data with actual data source
     * TODO: Integrate with external API or database
     * TODO: Add caching mechanism for frequently searched terms
     * 
     * @param keyword The search keyword
     * @return List of matching variable name suggestions
     */
    fun searchGovVariables(keyword: String): List<GovVariable> {
        if (keyword.isBlank()) {
            return emptyList()
        }

        // Mock data for demonstration
        val mockData = listOf(
            GovVariable("userId", "User identifier", "string", "Gov Standard v1.0"),
            GovVariable("userName", "User's full name", "string", "Gov Standard v1.0"),
            GovVariable("userEmail", "User's email address", "string", "Gov Standard v1.0"),
            GovVariable("userPhone", "User's phone number", "string", "Gov Standard v1.0"),
            GovVariable("userAddress", "User's residential address", "string", "Gov Standard v1.0"),
            GovVariable("organizationId", "Organization identifier", "string", "Gov Standard v1.0"),
            GovVariable("organizationName", "Organization name", "string", "Gov Standard v1.0"),
            GovVariable("departmentId", "Department identifier", "string", "Gov Standard v1.0"),
            GovVariable("departmentName", "Department name", "string", "Gov Standard v1.0"),
            GovVariable("applicationId", "Application identifier", "string", "Gov Standard v1.0"),
            GovVariable("applicationStatus", "Application processing status", "enum", "Gov Standard v1.0"),
            GovVariable("submissionDate", "Date of submission", "date", "Gov Standard v1.0"),
            GovVariable("approvalDate", "Date of approval", "date", "Gov Standard v1.0"),
            GovVariable("documentId", "Document identifier", "string", "Gov Standard v1.0"),
            GovVariable("documentType", "Type of document", "string", "Gov Standard v1.0"),
            GovVariable("citizenId", "Citizen national ID", "string", "Gov Standard v1.0"),
            GovVariable("citizenName", "Citizen full name", "string", "Gov Standard v1.0"),
            GovVariable("taxId", "Tax identification number", "string", "Gov Standard v1.0"),
            GovVariable("fiscalYear", "Fiscal year", "integer", "Gov Standard v1.0"),
            GovVariable("budgetAmount", "Budget amount", "decimal", "Gov Standard v1.0")
        )

        // Filter mock data based on keyword (case-insensitive)
        val lowerKeyword = keyword.lowercase()
        return mockData.filter { 
            it.name.lowercase().contains(lowerKeyword) || 
            it.description.lowercase().contains(lowerKeyword)
        }
    }
}

/**
 * Data class representing a government/public-standard variable.
 * 
 * @property name The variable name
 * @property description Description of the variable
 * @property type Data type of the variable
 * @property standard The standard specification it belongs to
 */
data class GovVariable(
    val name: String,
    val description: String,
    val type: String,
    val standard: String
)
