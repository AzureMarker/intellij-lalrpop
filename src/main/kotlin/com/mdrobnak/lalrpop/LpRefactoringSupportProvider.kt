package com.mdrobnak.lalrpop

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalParam

class LpRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return when (element) {
            is LpNonterminal -> !ReferencesSearch.search(element.nonterminalName).any()
            is LpNonterminalParam -> !ReferencesSearch.search(element).any()
            else -> false
        }
    }
}