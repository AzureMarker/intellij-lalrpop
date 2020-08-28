package com.mdrobnak.lalrpop.resolve

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LalrpopNonterminalName

object LalrpopResolveUtil {
    fun findNonterminal(file: PsiFile, name: String): List<LalrpopNonterminalName> =
            findNonterminals(file).filter { it.name == name }

    fun findNonterminals(file: PsiFile): Collection<LalrpopNonterminalName> =
            PsiTreeUtil.findChildrenOfType(file, LalrpopNonterminalName::class.java)
}