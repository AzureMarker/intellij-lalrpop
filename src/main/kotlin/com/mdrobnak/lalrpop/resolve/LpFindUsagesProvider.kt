package com.mdrobnak.lalrpop.resolve

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.LpLexerAdaptor
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpNamedElement
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam

class LpFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        LpLexerAdaptor(),
        TokenSet.create(LpElementTypes.ID),
        TokenSet.create(LpElementTypes.COMMENT),
        TokenSet.EMPTY,
        TokenSet.create(LpElementTypes.CODE)
    )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean = psiElement is LpNamedElement

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = when (element) {
        is LpNonterminalName -> "Nonterminal"
        is LpNonterminalParam -> "Macro parameter"
        else -> ""
    }

    override fun getDescriptiveName(element: PsiElement): String = when (element) {
        is LpNonterminalName -> element.name!!
        is LpNonterminalParam -> element.name!!
        else -> ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = getDescriptiveName(element)
}