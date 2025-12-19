package com.github.yohanki.egovconstant.completion

import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service

class EgovCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val service = project.service<DictionaryService>()
        val prefix = result.prefixMatcher.prefix
        if (prefix.length < 2) return
        if (!service.ensureLoaded()) return
        val idx = service.getIndex() ?: return
        val list = idx.search(DictionaryIndex.Query(text = prefix)).take(20)
        for (r in list) {
            val entry = r.entry
            val base = entry.enAbbr ?: entry.enName ?: entry.koName
            val camel = NameGenerator.fromAbbreviation(base).camel
            result.addElement(LookupElementBuilder.create(camel).withTypeText(entry.type.name, true))
        }
    }
}