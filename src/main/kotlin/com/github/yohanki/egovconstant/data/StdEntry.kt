package com.github.yohanki.egovconstant.data

enum class EntryType { TERM, WORD, DOMAIN }
enum class EntrySource { DEFAULT, XLSX, USER }

/**
 * Unified dictionary entry model.
 */
data class StdEntry(
    val type: EntryType,
    val koName: String,
    val enAbbr: String?,
    val enName: String?,
    val description: String?,
    // Domain metadata (for DOMAIN and TERM that references a domain)
    val domainGroup: String? = null,
    val domainCategory: String? = null,
    val domainName: String? = null,
    val dataType: String? = null,
    val dataLength: String? = null,
    val dataScale: String? = null,
    val storageFormat: String? = null,
    val displayFormat: String? = null,
    val unit: String? = null,
    val allowedValues: String? = null,
    val synonyms: Set<String> = emptySet(),
    val forbiddenWords: Set<String> = emptySet(),
    // Metadata
    val source: EntrySource = EntrySource.DEFAULT,
    val importedAt: Long? = null,
    val version: Int = 1
) {
    val stableKey: String by lazy {
        when (type) {
            EntryType.TERM -> {
                if (!enAbbr.isNullOrBlank()) enAbbr.trim()
                else normalize(koName)
            }
            EntryType.WORD -> enAbbr?.trim()?.uppercase() ?: normalize(koName)
            EntryType.DOMAIN -> normalize(domainName ?: koName)
        }
    }

    private fun normalize(s: String): String = s.trim().replace(Regex("\\s+"), " ")

    fun allTokens(): Set<String> {
        val base = mutableSetOf<String>()
        fun addToken(s: String?) {
            if (!s.isNullOrBlank()) base += s.trim()
        }
        addToken(koName)
        addToken(enAbbr)
        addToken(enName)
        addToken(description)
        synonyms.forEach { addToken(it) }
        return base
    }
}

// State classes for persistent caching
data class StdEntryState(
    var type: String = "",
    var koName: String = "",
    var enAbbr: String? = null,
    var enName: String? = null,
    var description: String? = null,
    var domainGroup: String? = null,
    var domainCategory: String? = null,
    var domainName: String? = null,
    var dataType: String? = null,
    var dataLength: String? = null,
    var dataScale: String? = null,
    var storageFormat: String? = null,
    var displayFormat: String? = null,
    var unit: String? = null,
    var allowedValues: String? = null,
    var synonyms: MutableList<String> = mutableListOf(),
    var forbiddenWords: MutableList<String> = mutableListOf(),
    var source: String = "DEFAULT",
    var importedAt: Long? = null,
    var version: Int = 1
)

fun StdEntry.toState(): StdEntryState = StdEntryState(
    type = type.name,
    koName = koName,
    enAbbr = enAbbr,
    enName = enName,
    description = description,
    domainGroup = domainGroup,
    domainCategory = domainCategory,
    domainName = domainName,
    dataType = dataType,
    dataLength = dataLength,
    dataScale = dataScale,
    storageFormat = storageFormat,
    displayFormat = displayFormat,
    unit = unit,
    allowedValues = allowedValues,
    synonyms = synonyms.toMutableList(),
    forbiddenWords = forbiddenWords.toMutableList(),
    source = source.name,
    importedAt = importedAt,
    version = version
)

fun StdEntryState.toEntry(): StdEntry = StdEntry(
    type = EntryType.valueOf(type),
    koName = koName,
    enAbbr = enAbbr,
    enName = enName,
    description = description,
    domainGroup = domainGroup,
    domainCategory = domainCategory,
    domainName = domainName,
    dataType = dataType,
    dataLength = dataLength,
    dataScale = dataScale,
    storageFormat = storageFormat,
    displayFormat = displayFormat,
    unit = unit,
    allowedValues = allowedValues,
    synonyms = synonyms.toSet(),
    forbiddenWords = forbiddenWords.toSet(),
    source = EntrySource.valueOf(source),
    importedAt = importedAt,
    version = version
)