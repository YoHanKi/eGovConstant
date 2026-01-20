package com.github.yohanki.egovconstant.data

import com.github.yohanki.egovconstant.data.StdEntry
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.nio.file.Path
import java.time.Instant

@State(
    name = "com.github.yohanki.egovconstant.data.CanonicalStore",
    storages = [Storage("eGovConstant.xml")],
    category = com.intellij.openapi.components.SettingsCategory.TOOLS
)
class CanonicalStore : PersistentStateComponent<CanonicalStore.State> {

    private val log = Logger.getInstance(CanonicalStore::class.java)

    data class State(
        var userEntries: MutableList<StdEntryState> = mutableListOf(),
        var lastImportSummary: ImportSummary? = null,
        var useCustomOnly: Boolean = false
    ) {
        val entryMap: Map<String, List<StdEntryState>>
            get() = mapOf(
                "DEFAULT" to emptyList(), // This is just for structure as defaults are loaded separately
                "CUSTOM" to userEntries
            )
    }

    data class ImportSummary(
        var importedAt: Long = 0,
        var addedCount: Int = 0,
        var mergedCount: Int = 0,
        var skippedCount: Int = 0,
        var conflictCount: Int = 0
    )

    private var myState = State()
    private var defaultEntries: List<StdEntry> = emptyList()

    init {
        loadDefaultEntries()
    }

    private fun loadDefaultEntries() {
        val gson = Gson()
        val type = object : TypeToken<List<StdEntryState>>() {}.type
        val terms = loadJson(gson, type, "/egovconstant/default/terms.json")
        val words = loadJson(gson, type, "/egovconstant/default/words.json")
        val domains = loadJson(gson, type, "/egovconstant/default/domains.json")
        defaultEntries = (terms + words + domains).map { it.toEntry().copy(source = EntrySource.DEFAULT) }
    }

    private fun loadJson(gson: Gson, type: java.lang.reflect.Type, path: String): List<StdEntryState> {
        return try {
            val stream = javaClass.getResourceAsStream(path) ?: return emptyList()
            InputStreamReader(stream, Charsets.UTF_8).use { 
                gson.fromJson<List<StdEntryState>>(it, type) ?: emptyList()
            }
        } catch (e: Exception) {
            log.error("Failed to load default entries from $path", e)
            emptyList()
        }
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun getEffectiveEntries(): List<StdEntry> {
        val userEntriesMapped = myState.userEntries.map { it.toEntry() }
        
        if (myState.useCustomOnly) {
            return userEntriesMapped
        }

        val mergedMap = mutableMapOf<String, StdEntry>()

        // Load defaults first
        for (entry in defaultEntries) {
            mergedMap[entry.stableKey] = entry
        }

        // Overlay user entries
        for (userEntry in userEntriesMapped) {
            val existing = mergedMap[userEntry.stableKey]
            if (existing == null) {
                mergedMap[userEntry.stableKey] = userEntry
            } else {
                mergedMap[userEntry.stableKey] = merge(existing, userEntry)
            }
        }

        return mergedMap.values.toList()
    }

    fun importEntries(incoming: List<StdEntry>): ImportSummary {
        val summary = ImportSummary(importedAt = Instant.now().toEpochMilli())
        val userEntriesMap = myState.userEntries.associateBy { it.toEntry().stableKey }.toMutableMap()
        val defaultEntriesMap = defaultEntries.associateBy { it.stableKey }

        for (newEntry in incoming) {
            val key = newEntry.stableKey
            val inDefault = defaultEntriesMap[key]
            val inUser = userEntriesMap[key]

            if (inUser != null) {
                // Merge into existing user entry
                val existingEntry = inUser.toEntry()
                val merged = merge(existingEntry, newEntry)
                if (merged != existingEntry) {
                    userEntriesMap[key] = merged.copy(source = EntrySource.XLSX, version = existingEntry.version + 1).toState()
                    summary.mergedCount++
                } else {
                    summary.skippedCount++
                }
            } else if (inDefault != null) {
                // Check if it adds anything to default
                val merged = merge(inDefault, newEntry)
                if (merged != inDefault) {
                    // Create a user entry that overrides/augments default
                    userEntriesMap[key] = merged.copy(source = EntrySource.XLSX, version = 2).toState()
                    summary.mergedCount++
                } else {
                    summary.skippedCount++
                }
            } else {
                // New entry
                userEntriesMap[key] = newEntry.copy(source = EntrySource.XLSX, importedAt = summary.importedAt).toState()
                summary.addedCount++
            }
        }

        myState.userEntries = userEntriesMap.values.toMutableList()
        myState.lastImportSummary = summary
        return summary
    }

    fun reset() {
        myState.userEntries.clear()
        myState.lastImportSummary = null
    }

    private fun merge(existing: StdEntry, incoming: StdEntry): StdEntry {
        // Merge policy:
        // never overwrite existing non-empty fields silently
        // if incoming field is non-empty and existing is empty -> fill it
        // list fields: union (Set)

        return existing.copy(
            enAbbr = existing.enAbbr.orIfEmpty(incoming.enAbbr),
            enName = existing.enName.orIfEmpty(incoming.enName),
            description = existing.description.orIfEmpty(incoming.description),
            domainGroup = existing.domainGroup.orIfEmpty(incoming.domainGroup),
            domainCategory = existing.domainCategory.orIfEmpty(incoming.domainCategory),
            domainName = existing.domainName.orIfEmpty(incoming.domainName),
            dataType = existing.dataType.orIfEmpty(incoming.dataType),
            dataLength = existing.dataLength.orIfEmpty(incoming.dataLength),
            dataScale = existing.dataScale.orIfEmpty(incoming.dataScale),
            storageFormat = existing.storageFormat.orIfEmpty(incoming.storageFormat),
            displayFormat = existing.displayFormat.orIfEmpty(incoming.displayFormat),
            unit = existing.unit.orIfEmpty(incoming.unit),
            allowedValues = existing.allowedValues.orIfEmpty(incoming.allowedValues),
            synonyms = (existing.synonyms + incoming.synonyms).filter { it.isNotBlank() }.toSet(),
            forbiddenWords = (existing.forbiddenWords + incoming.forbiddenWords).filter { it.isNotBlank() }.toSet()
        )
    }

    private fun String?.orIfEmpty(other: String?): String? {
        return if (this.isNullOrBlank()) other else this
    }
}
