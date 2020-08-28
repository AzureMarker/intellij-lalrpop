package com.mdrobnak.lalrpop.resolve

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.LalrpopLexerAdaptor
import com.mdrobnak.lalrpop.psi.LalrpopNamedElement
import com.mdrobnak.lalrpop.psi.LalrpopNonterminalName
import com.mdrobnak.lalrpop.psi.LalrpopTypes

class LalrpopFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(
                LalrpopLexerAdaptor(),
                TokenSet.create(LalrpopTypes.ID),
                TokenSet.create(LalrpopTypes.COMMENT),
                TokenSet.EMPTY,
                TokenSet.create(LalrpopTypes.CODE)
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is LalrpopNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return if(element is LalrpopNonterminalName) {
            "Nonterminal"
        }
        else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return if(element is LalrpopNonterminalName) {
            element.name!!
        }
        else {
            ""
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}