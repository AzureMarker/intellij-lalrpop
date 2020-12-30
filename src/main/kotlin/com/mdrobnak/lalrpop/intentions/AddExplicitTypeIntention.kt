package com.mdrobnak.lalrpop.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.LpLanguage
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.ext.setType
import com.mdrobnak.lalrpop.psi.getContextAndResolveType

class AddExplicitTypeIntention : IntentionAction {
    override fun startInWriteAction(): Boolean = true

    override fun getText(): String = "Add explicit type"

    override fun getFamilyName(): String = "Add explicit type"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || file.language != LpLanguage) return false

        return file.findElementAt(editor.caretModel.primaryCaret.offset)?.parentOfType<LpNonterminal>(withSelf = true) != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || file.language != LpLanguage) return

        file.findElementAt(editor.caretModel.primaryCaret.offset)?.parentOfType<LpNonterminal>(withSelf = true)?.apply {
            setType(getContextAndResolveType(LpMacroArguments.identity(nonterminalName.nonterminalParams)))
        }
    }
}