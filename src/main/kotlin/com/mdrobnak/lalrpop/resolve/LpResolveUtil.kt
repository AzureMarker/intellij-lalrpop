package com.mdrobnak.lalrpop.resolve

import com.intellij.psi.PsiFile
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam
import org.rust.lang.core.psi.ext.descendantsOfType

object LpResolveUtil {
    fun findNonterminal(file: PsiFile, name: String): List<LpNonterminalName> =
        findNonterminals(file).filter { it.name == name }

    fun findNonterminalParameter(element: LpNonterminal, name: String): List<LpNonterminalParam> =
        findNonterminalParams(element).filter { it.name == name }

    fun findNonterminals(file: PsiFile): Collection<LpNonterminalName> =
        file.descendantsOfType()

    fun findNonterminalParams(element: LpNonterminal): Collection<LpNonterminalParam> =
        element.descendantsOfType()
}