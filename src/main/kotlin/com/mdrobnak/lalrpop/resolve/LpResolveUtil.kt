package com.mdrobnak.lalrpop.resolve

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam

object LpResolveUtil {
    fun findNonterminal(file: PsiFile, name: String): List<LpNonterminalName> =
        findNonterminals(file).filter { it.name == name }

    fun findNonterminalParameter(element: LpNonterminal, name: String): List<LpNonterminalParam> =
        findNonterminalParams(element).filter { it.name == name }

    fun findNonterminals(file: PsiFile): Collection<LpNonterminalName> =
        PsiTreeUtil.findChildrenOfType(file, LpNonterminalName::class.java)

    fun findNonterminalParams(element: LpNonterminal): Collection<LpNonterminalParam> =
        PsiTreeUtil.findChildrenOfType(element, LpNonterminalParam::class.java)
}