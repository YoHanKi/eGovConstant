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
        val prefix = result.prefixMatcher.prefix
        
        if (prefix.isEmpty()) return
        
        if (!service.ensureLoaded()) return
        val idx = service.getIndex() ?: return
        val list = idx.search(DictionaryIndex.Query(text = prefix)).take(20)
        for (r in list) {
            val entry = r.entry
            val base = entry.enAbbr ?: entry.enName ?: entry.koName
            val camel = NameGenerator.fromAbbreviation(base).camel
            
            val lookupElement = LookupElementBuilder.create(camel)
                .let { if (eIcon != null) it.withIcon(eIcon) else it }
                .withPresentableText(camel)
                .withTailText(" (${entry.koName})", true)
                .withTypeText(entry.type.name, true)
            
            result.addElement(lookupElement)
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