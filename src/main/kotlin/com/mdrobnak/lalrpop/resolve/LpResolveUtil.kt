package com.mdrobnak.lalrpop.resolve

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpNonterminalName

object LpResolveUtil {
    fun findNonterminal(file: PsiFile, name: String): List<LpNonterminalName> =
        findNonterminals(file).filter { it.name == name }

    fun findNonterminals(file: PsiFile): Collection<LpNonterminalName> =
        PsiTreeUtil.findChildrenOfType(file, LpNonterminalName::class.java)
}