package org.jetbrains.plugins.template

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.plugins.template.services.GovVarService

/**
 * Test class for GovVarService
 */
class GovVarServiceTest : BasePlatformTestCase() {

    fun testServiceExists() {
        val service = project.service<GovVarService>()
        assertNotNull("GovVarService should be available", service)
    }

    fun testSearchWithValidKeyword() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("user")
        
        assertTrue("Should find results for 'user'", results.isNotEmpty())
        assertTrue("Results should contain user-related variables", 
            results.any { it.name.contains("user", ignoreCase = true) })
    }

    fun testSearchWithEmptyKeyword() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("")
        
        assertTrue("Should return empty list for empty keyword", results.isEmpty())
    }

    fun testSearchWithBlankKeyword() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("   ")
        
        assertTrue("Should return empty list for blank keyword", results.isEmpty())
    }

    fun testSearchCaseInsensitive() {
        val service = project.service<GovVarService>()
        val resultsLower = service.searchGovVariables("user")
        val resultsUpper = service.searchGovVariables("USER")
        
        assertEquals("Search should be case-insensitive", resultsLower.size, resultsUpper.size)
    }

    fun testSearchByDescription() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("identifier")
        
        assertTrue("Should find results by description", results.isNotEmpty())
        assertTrue("Results should contain variables with 'identifier' in description",
            results.any { it.description.contains("identifier", ignoreCase = true) })
    }

    fun testSearchWithNoMatches() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("xyz123nonexistent")
        
        assertTrue("Should return empty list for non-matching keyword", results.isEmpty())
    }

    fun testResultStructure() {
        val service = project.service<GovVarService>()
        val results = service.searchGovVariables("user")
        
        assertTrue("Should have results", results.isNotEmpty())
        
        val firstResult = results.first()
        assertNotNull("Variable name should not be null", firstResult.name)
        assertNotNull("Description should not be null", firstResult.description)
        assertNotNull("Type should not be null", firstResult.type)
        assertNotNull("Standard should not be null", firstResult.standard)
        
        assertTrue("Variable name should not be empty", firstResult.name.isNotEmpty())
        assertTrue("Description should not be empty", firstResult.description.isNotEmpty())
        assertTrue("Type should not be empty", firstResult.type.isNotEmpty())
        assertTrue("Standard should not be empty", firstResult.standard.isNotEmpty())
    }
}
