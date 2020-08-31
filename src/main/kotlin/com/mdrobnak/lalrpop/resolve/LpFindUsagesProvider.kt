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

class LpFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
            LpLexerAdaptor(),
            TokenSet.create(LpElementTypes.ID),
            TokenSet.create(LpElementTypes.COMMENT),
            TokenSet.EMPTY,
            TokenSet.create(LpElementTypes.CODE)
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is LpNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return if (element is LpNonterminalName) {
            "Nonterminal"
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return if (element is LpNonterminalName) {
            element.name!!
        } else {
            ""
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}