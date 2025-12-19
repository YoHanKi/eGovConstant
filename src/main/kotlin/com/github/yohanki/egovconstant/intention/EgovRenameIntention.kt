package com.github.yohanki.egovconstant.intention

import com.github.yohanki.egovconstant.index.DictionaryIndex
import com.github.yohanki.egovconstant.naming.NameGenerator
import com.github.yohanki.egovconstant.service.DictionaryService
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException

class EgovRenameIntention : IntentionAction {
    override fun getText(): String = "Rename to eGovConstant recommended name"
    override fun getFamilyName(): String = "eGovConstant"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null) return false
        val sel = getCurrentWord(editor) ?: return false
        return sel.length >= 2
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null) return
        val word = getCurrentWord(editor) ?: return
        val service = project.service<DictionaryService>()
        if (!service.ensureLoaded()) return
        val idx = service.getIndex() ?: return
        val candidates = idx.search(DictionaryIndex.Query(text = word)).take(5).map { r ->
            val base = r.entry.enAbbr ?: r.entry.enName ?: r.entry.koName
            NameGenerator.fromAbbreviation(base).camel
        }.distinct()
        if (candidates.isEmpty()) return
        val chosen = if (candidates.size == 1) candidates.first() else Messages.showEditableChooseDialog(
            "Choose recommended name", "eGovConstant", null, candidates.toTypedArray(), candidates.first(), null
        )
        if (chosen.isNullOrBlank()) return
        val range = currentWordRange(editor) ?: return
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(range.startOffset, range.endOffset, chosen)
        }
    }

    override fun startInWriteAction(): Boolean = false

    private fun getCurrentWord(editor: Editor): String? {
        val range = currentWordRange(editor) ?: return null
        return editor.document.getText(range)
    }

    private fun currentWordRange(editor: Editor): TextRange? {
        val caret = editor.caretModel.offset
        val text = editor.document.charsSequence
        var start = caret
        var end = caret
        while (start > 0 && Character.isJavaIdentifierPart(text[start - 1])) start--
        while (end < text.length && Character.isJavaIdentifierPart(text[end])) end++
        if (start >= end) return null
        return TextRange(start, end)
    }
}