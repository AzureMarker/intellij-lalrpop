package com.mdrobnak.lalrpop.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.mdrobnak.lalrpop.LpLanguage
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.getContextAndResolveType

class AddExplicitTypeIntention : IntentionAction {
    override fun startInWriteAction(): Boolean = true

    override fun getText(): String = "Add explicit type"

    override fun getFamilyName(): String = "Add explicit type"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || file.language != LpLanguage) return false

        var element = file.findElementAt(editor.caretModel.primaryCaret.offset)
        while (element != null) {
            if (element is LpNonterminal) return true
            element = element.parent
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || file.language != LpLanguage) return

        var element = file.findElementAt(editor.caretModel.primaryCaret.offset)

        var nonterminal: LpNonterminal? = null
        var alternativeToUse: LpAlternative? = null

        while (element != null) {
            if (element is LpAlternative) {
                if (element.action == null) // TODO: maybe get from the rust plugin?
                    alternativeToUse = element
            } else if (element is LpNonterminal) {
                nonterminal = element
                break
            }

            element = element.parent
        }

        if (nonterminal == null) return

        val inferredType =
            alternativeToUse?.getContextAndResolveType(listOf()) ?: nonterminal.getContextAndResolveType(listOf())

        val type = LpElementFactory(project).createNonterminalType(inferredType)
        val typeRef = nonterminal.typeRef

        if (typeRef != null)
            typeRef.replace(type.second)
        else
            nonterminal.addRangeAfter(type.first, type.second, nonterminal.nonterminalName)
    }
}