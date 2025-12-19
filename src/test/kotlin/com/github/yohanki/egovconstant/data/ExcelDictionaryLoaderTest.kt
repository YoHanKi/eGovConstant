package com.github.yohanki.egovconstant.data

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ExcelDictionaryLoaderTest {
    @Test
    fun loadFromTempFile() {
        val tempDir = File("temp")
        assertTrue("temp directory should exist", tempDir.exists())
        val xlsx = tempDir.listFiles { f -> f.isFile && f.name.endsWith(".xlsx", true) }?.firstOrNull()
        assertNotNull("XLSX file not found in temp directory", xlsx)
        val res = ExcelDictionaryLoader.load(xlsx!!)
        assertNull("Loader error: ${res.error}\nValidations: ${res.validations}", res.error)
        assertTrue("Expected some entries parsed", res.entries.isNotEmpty())
    }

    @Test
    fun testValidationFailure() {
        // Create an empty file to trigger validation failure
        val emptyFile = File.createTempFile("empty", ".xlsx")
        try {
            val res = ExcelDictionaryLoader.load(emptyFile)
            assertNotNull(res.error)
        } finally {
            emptyFile.delete()
        }
    }
}