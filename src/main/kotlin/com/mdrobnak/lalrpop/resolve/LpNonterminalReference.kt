package com.mdrobnak.lalrpop.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalRef

class LpNonterminalReference(element: LpNonterminalRef) :
    PsiReferenceBase<LpNonterminalRef>(element, TextRange.allOf(element.text)) {
    override fun resolve(): PsiElement? =
        LpResolveUtil.findNonterminal(element.containingFile, element.text).firstOrNull() ?: when (val thisNonTerminal =
            PsiTreeUtil.getParentOfType(element, LpNonterminal::class.java)) {
            null -> null
            else -> LpResolveUtil.findNonterminalParameter(thisNonTerminal, element.text).firstOrNull()
        }

    override fun handleElementRename(newElementName: String): PsiElement {
        val newNode = LpElementFactory(element.project).createIdentifier(newElementName)
        element.firstChild.replace(newNode)
        return element
    }

    override fun getVariants(): Array<Any> {
        return (LpResolveUtil.findNonterminals(element.containingFile) + when (val thisNonTerminal =
            PsiTreeUtil.getParentOfType(element, LpNonterminal::class.java)) {
            null -> listOf()
            else -> LpResolveUtil.findNonterminalParams(thisNonTerminal)
        }).toTypedArray()
    }
}