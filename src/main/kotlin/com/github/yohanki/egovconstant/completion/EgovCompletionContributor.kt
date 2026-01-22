package com.github.yohanki.egovconstant.completion

import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.util.IconLoader
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

class EgovCompletionContributor : CompletionContributor() {
    private val eIcon by lazy {
        try {
            IconLoader.getIcon("/icons/egov_e.svg", EgovCompletionContributor::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        val language = position.language.id.lowercase()

        val isVariableLocation = when (language) {
            "java" -> JavaCheck.isVariableLocation(position)
            "kotlin" -> KotlinCheck.isVariableLocation(position)
            else -> false
        }

        if (!isVariableLocation) return

        val project = parameters.editor.project ?: return
        val service = project.service<DictionaryService>()
        if (!service.completionEnabled) return

        val fullPrefix = result.prefixMatcher.prefix
        if (fullPrefix.isEmpty()) return

        val idx = service.getIndex() ?: return

        val maxCount = service.completionCount
        val lastUpperIdx = fullPrefix.indexOfLast { it.isUpperCase() }
        val list = if (lastUpperIdx == -1) {
            idx.search(DictionaryIndex.Query(text = fullPrefix), limit = maxCount)
        } else {
            val maxCountHalf = maxCount / 2
            val searchPrefix = fullPrefix.substring(lastUpperIdx)
            val listHalf = idx.search(DictionaryIndex.Query(text = searchPrefix), limit = maxCountHalf)
            listHalf + idx.search(DictionaryIndex.Query(text = fullPrefix), limit = maxCount - listHalf.size)
        }

        for (r in list) {
            val entry = r.entry
            val base = entry.enAbbr ?: entry.enName ?: entry.koName
            val camel = NameGenerator.fromAbbreviation(base).camel

            val insertString = if (lastUpperIdx == -1) camel else
                fullPrefix.substring(0, lastUpperIdx) + camel.substring(lastUpperIdx)

            val lookupElement = LookupElementBuilder.create(insertString)
                .let { if (eIcon != null) it.withIcon(eIcon) else it }
                .withPresentableText(camel)
                .withTailText(" (${entry.koName})", true)
                .withTypeText(entry.type.name, true)
                .withLookupString(camel)
                .withLookupString(entry.koName)

            result.addElement(PrioritizedLookupElement.withPriority(lookupElement, 1000.0 + r.score.toDouble()))
        }
    }

    private object JavaCheck {
        fun isVariableLocation(position: com.intellij.psi.PsiElement): Boolean {
            return try {
                PsiJavaPatterns.psiElement(PsiIdentifier::class.java)
                    .withParent(PsiJavaPatterns.psiElement(PsiVariable::class.java))
                    .accepts(position) ||
                        PsiJavaPatterns.psiElement(PsiIdentifier::class.java)
                            .withParent(PsiJavaPatterns.psiElement(PsiParameter::class.java))
                            .accepts(position)
            } catch (e: Throwable) {
                false
            }
        }
    }

    private object KotlinCheck {
        fun isVariableLocation(position: com.intellij.psi.PsiElement): Boolean {
            return try {
                val parent = position.parent
                (parent is KtProperty && parent.nameIdentifier == position) ||
                        (parent is KtParameter && parent.nameIdentifier == position)
            } catch (e: Throwable) {
                false
            }
        }
    }
}