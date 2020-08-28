package com.mdrobnak.lalrpop.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.mdrobnak.lalrpop.psi.LalrpopElementFactory
import com.mdrobnak.lalrpop.psi.LalrpopNonterminalRef

class LalrpopNonterminalReference(element: LalrpopNonterminalRef) : PsiReferenceBase<LalrpopNonterminalRef>(element, TextRange.allOf(element.text)) {
    override fun resolve(): PsiElement? =
            LalrpopResolveUtil.findNonterminal(element.containingFile, element.text).firstOrNull()

    override fun handleElementRename(newElementName: String): PsiElement {
        val newNode = LalrpopElementFactory(element.project).createIdentifier(newElementName)
        element.firstChild.replace(newNode)
        return element
    }
}