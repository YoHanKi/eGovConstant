package com.github.yohanki.egovconstant.data

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

/**
 * Loads the eGov XLSX dictionary from a given file path.
 * It expects exactly 3 sheets (by index: 0 terms, 1 words, 2 domains).
 */
object ExcelDictionaryLoader {

    data class ValidationResult(
        val sheetName: String,
        val missingRequiredColumns: List<String>,
        val detectedColumns: Map<String, Int>,
        val isValid: Boolean = missingRequiredColumns.isEmpty()
    )

    data class LoadResult(
        val entries: List<StdEntry>,
        val validations: List<ValidationResult> = emptyList(),
        val error: String? = null,
    )

    private val termRequired = listOf("공통표준용어명")
    private val wordRequired = listOf("공통표준단어명", "공통표준단어영문약어명")
    private val domainRequired = listOf("공통표준도메인명")

    fun load(file: File): LoadResult {
        if (!file.exists() || !file.isFile) {
            return LoadResult(emptyList(), error = "XLSX file not found: ${file.absolutePath}")
        }
        return try {
            FileInputStream(file).use { fis ->
                val workbook = XSSFWorkbook(fis)
                if (workbook.numberOfSheets < 3) {
                    return LoadResult(emptyList(), error = "Invalid XLSX: expected 3 sheets, found ${workbook.numberOfSheets}")
                }

                val termValidation = validateSheet(workbook.getSheetAt(0), termRequired)
                val wordValidation = validateSheet(workbook.getSheetAt(1), wordRequired)
                val domainValidation = validateSheet(workbook.getSheetAt(2), domainRequired)

                val validations = listOf(termValidation, wordValidation, domainValidation)
                if (validations.any { !it.isValid }) {
                    return LoadResult(emptyList(), validations = validations, error = "Validation failed")
                }

                val terms = parseTermsSheet(workbook.getSheetAt(0), termValidation.detectedColumns)
                val words = parseWordsSheet(workbook.getSheetAt(1), wordValidation.detectedColumns)
                val domains = parseDomainsSheet(workbook.getSheetAt(2), domainValidation.detectedColumns)
                LoadResult(terms + words + domains, validations, null)
            }
        } catch (t: Throwable) {
            LoadResult(emptyList(), error = friendlyMessage(t))
        }
    }

    private fun validateSheet(sheet: org.apache.poi.ss.usermodel.Sheet, required: List<String>): ValidationResult {
        val rows = sheet.iterator()
        if (!rows.hasNext()) return ValidationResult(sheet.sheetName, required, emptyMap())
        val detected = mapHeaders(rows.next())
        val missing = required.filter { req ->
            detected.keys.none { det -> det.replace(" ", "").contains(req.replace(" ", ""), ignoreCase = true) }
        }
        return ValidationResult(sheet.sheetName, missing, detected)
    }

    private fun friendlyMessage(t: Throwable): String = buildString {
        append("Failed to load dictionary: ")
        append(t.message ?: t::class.java.simpleName)
    }

    private fun parseTermsSheet(sheet: org.apache.poi.ss.usermodel.Sheet, header: Map<String, Int>): List<StdEntry> {
        val result = mutableListOf<StdEntry>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val ko = get(row, header, "공통표준용어명") ?: continue
            val desc = get(row, header, "공통표준용어설명")
            val abbr = get(row, header, "공통표준용어영문약어명")
            val domainName = get(row, header, "공통표준도메인명")
            val allowed = get(row, header, "허용값")
            val storage = get(row, header, "저장 형식")
            val display = get(row, header, "표현 형식")
            val synList = splitList(get(row, header, "용어 이음동의어 목록"))
            val entry = StdEntry(
                type = EntryType.TERM,
                koName = ko,
                enAbbr = abbr?.takeIf { it.isNotBlank() },
                enName = null,
                description = desc,
                domainName = domainName,
                allowedValues = allowed,
                storageFormat = storage,
                displayFormat = display,
                synonyms = synList,
                source = EntrySource.XLSX
            )
            result += entry
        }
        return result
    }

    private fun parseWordsSheet(sheet: org.apache.poi.ss.usermodel.Sheet, header: Map<String, Int>): List<StdEntry> {
        val result = mutableListOf<StdEntry>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val ko = get(row, header, "공통표준단어명") ?: continue
            val abbr = get(row, header, "공통표준단어영문약어명")
            val enName = get(row, header, "공통표준단어 영문명")
            val desc = get(row, header, "공통표준단어 설명")
            val syn = splitList(get(row, header, "이음동의어 목록"))
            val forbid = splitList(get(row, header, "금칙어 목록"))
            result += StdEntry(
                type = EntryType.WORD,
                koName = ko,
                enAbbr = abbr?.takeIf { it.isNotBlank() },
                enName = enName?.takeIf { it.isNotBlank() },
                description = desc,
                synonyms = syn,
                forbiddenWords = forbid,
                source = EntrySource.XLSX
            )
        }
        return result
    }

    private fun parseDomainsSheet(sheet: org.apache.poi.ss.usermodel.Sheet, header: Map<String, Int>): List<StdEntry> {
        val result = mutableListOf<StdEntry>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val group = get(row, header, "공통표준도메인그룹명")
            val category = get(row, header, "공통표준도메인분류명")
            val name = get(row, header, "공통표준도메인명") ?: continue
            val desc = get(row, header, "공통표준도메인설명")
            val dataType = get(row, header, "데이터타입")
            val dataLength = get(row, header, "데이터길이")
            val dataScale = get(row, header, "데이터소수점길이")
            val storage = get(row, header, "저장 형식")
            val display = get(row, header, "표현 형식")
            val unit = get(row, header, "단위")
            val allowed = get(row, header, "허용값")
            result += StdEntry(
                type = EntryType.DOMAIN,
                koName = name,
                enAbbr = null,
                enName = null,
                description = desc,
                domainGroup = group,
                domainCategory = category,
                domainName = name,
                dataType = dataType,
                dataLength = dataLength,
                dataScale = dataScale,
                storageFormat = storage,
                displayFormat = display,
                unit = unit,
                allowedValues = allowed,
                source = EntrySource.XLSX
            )
        }
        return result
    }

    private fun mapHeaders(row: Row): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for (cell in row) {
            val value = cellString(cell)
            if (value.isNotBlank()) map[value.trim()] = cell.columnIndex
        }
        return map
    }

    private fun get(row: Row, header: Map<String, Int>, key: String): String? {
        val normalizedKey = key.replace(" ", "")
        val idx = header.entries.find { it.key.replace(" ", "").contains(normalizedKey, ignoreCase = true) }?.value ?: return null
        val cell = row.getCell(idx) ?: return null
        val s = cellString(cell).trim()
        return if (s.isBlank()) null else s
    }

    private fun splitList(value: String?): Set<String> {
        if (value.isNullOrBlank()) return emptySet()
        return value
            .replace('\n', ',')
            .split(',', '，', ';')
            .map { it.trim().trim('"','\'') }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun cellString(cell: Cell): String = when (cell.cellType) {
        org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
        org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
        org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
        org.apache.poi.ss.usermodel.CellType.BLANK -> ""
        else -> cell.toString()
    }
}