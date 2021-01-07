package com.mdrobnak.lalrpop

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam

class LpRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean =
        element is LpNonterminalName || element is LpNonterminalParam

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean =
        element is LpNonterminalName || element is LpNonterminalParam
}