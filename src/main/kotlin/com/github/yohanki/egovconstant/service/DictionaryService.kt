package com.github.yohanki.egovconstant.service

import com.github.yohanki.egovconstant.data.CanonicalStore
import com.github.yohanki.egovconstant.data.ExcelDictionaryLoader
import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.io.File

@Service(Service.Level.PROJECT)
class DictionaryService(private val project: Project) {

    private val log = Logger.getInstance(DictionaryService::class.java)

    @Volatile private var index: DictionaryIndex? = null
    private val store: CanonicalStore get() = project.getService(CanonicalStore::class.java)

    fun getIndex(): DictionaryIndex? {
        if (index == null) {
            ensureLoaded()
        }
        return index
    }

    fun getStatus(): String {
        val effective = store.getEffectiveEntries()
        val count = effective.size
        val summary = store.state.lastImportSummary
        return if (summary != null) {
            "Loaded $count entries. Last import: ${java.time.Instant.ofEpochMilli(summary.importedAt)}. Added: ${summary.addedCount}, Merged: ${summary.mergedCount}"
        } else {
            "Loaded $count entries (default)."
        }
    }

    fun loadFromXlsx(xlsx: File, onDone: ((CanonicalStore.ImportSummary?) -> Unit)? = null) {
        val task = object : Task.Backgroundable(project, "Importing eGov Dictionary") {
            var summary: CanonicalStore.ImportSummary? = null
            override fun run(indicator: ProgressIndicator) {
                val result = ExcelDictionaryLoader.load(xlsx)
                if (result.error != null) {
                    log.warn(result.error)
                } else {
                    summary = store.importEntries(result.entries)
                    index = DictionaryIndex(store.getEffectiveEntries())
                }
            }

            override fun onFinished() {
                onDone?.invoke(summary)
            }
        }
        ProgressManager.getInstance().run(task)
    }

    fun resetToDefault() {
        store.reset()
        index = DictionaryIndex(store.getEffectiveEntries())
    }

    fun ensureLoaded(): Boolean {
        if (index == null) {
            index = DictionaryIndex(store.getEffectiveEntries())
        }
        return true
    }
}