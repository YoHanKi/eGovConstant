package com.github.yohanki.egovconstant.service

import com.github.yohanki.egovconstant.data.CanonicalStore
import com.github.yohanki.egovconstant.data.StdEntry
import com.github.yohanki.egovconstant.data.toEntry
import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import java.io.File

interface DictionarySettingsListener {
    fun onSettingsChanged()

    companion object {
        val TOPIC = Topic.create("eGovConstant Settings Changed", DictionarySettingsListener::class.java)
    }
}

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
            "총 $count 개의 변수. 추가된 변수: ${java.time.Instant.ofEpochMilli(summary.importedAt)}. 추가: ${summary.addedCount}, 병합: ${summary.mergedCount}"
        } else {
            "총 $count 개의 변수 (기본 값)"
        }
    }

    fun loadFromJson(
        json: String, 
        onDone: ((CanonicalStore.ImportSummary?) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val task = object : Task.Backgroundable(project, "Importing eGov Dictionary") {
            var summary: CanonicalStore.ImportSummary? = null
            var error: Exception? = null
            override fun run(indicator: ProgressIndicator) {
                try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<com.github.yohanki.egovconstant.data.StdEntryState>>() {}.type
                    val states = gson.fromJson<List<com.github.yohanki.egovconstant.data.StdEntryState>>(json, type)
                    if (states == null) throw Exception("Empty or invalid JSON")
                    val entries = states.map { it.toEntry() }
                    summary = store.importEntries(entries)
                    index = DictionaryIndex(store.getEffectiveEntries())
                    project.messageBus.syncPublisher(DictionarySettingsListener.TOPIC).onSettingsChanged()
                } catch (e: Exception) {
                    log.warn("Failed to load JSON", e)
                    error = e
                }
            }

            override fun onFinished() {
                if (error != null) {
                    onError?.invoke(error!!)
                } else {
                    onDone?.invoke(summary)
                }
            }
        }
        ProgressManager.getInstance().run(task)
    }

    fun resetToDefault() {
        store.reset()
        index = DictionaryIndex(store.getEffectiveEntries())
        project.messageBus.syncPublisher(DictionarySettingsListener.TOPIC).onSettingsChanged()
    }

    var useCustomOnly: Boolean
        get() = store.state.useCustomOnly
        set(value) {
            store.state.useCustomOnly = value
            index = DictionaryIndex(store.getEffectiveEntries())
            project.messageBus.syncPublisher(DictionarySettingsListener.TOPIC).onSettingsChanged()
        }

    fun ensureLoaded(): Boolean {
        if (index == null) {
            index = DictionaryIndex(store.getEffectiveEntries())
        }
        return true
    }
}