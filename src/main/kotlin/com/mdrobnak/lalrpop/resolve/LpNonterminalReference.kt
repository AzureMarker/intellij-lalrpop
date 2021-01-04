package com.mdrobnak.lalrpop.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalRef

class LpNonterminalReference(element: LpNonterminalRef) :
    PsiReferenceBase<LpNonterminalRef>(element, TextRange.allOf(element.text)) {
    override fun resolve(): PsiElement? =
        LpResolveUtil.findNonterminal(element.containingFile, element.text).firstOrNull()
            ?: element.parentOfType<LpNonterminal>()
                ?.let { LpResolveUtil.findNonterminalParameter(it, element.text).firstOrNull() }

    override fun handleElementRename(newElementName: String): PsiElement = element.apply {
        val newNode = LpElementFactory(project).createIdentifier(newElementName)
        firstChild.replace(newNode)
    }

    override fun getVariants(): Array<Any> =
        (LpResolveUtil.findNonterminals(element.containingFile) +
                element.parentOfType<LpNonterminal>()
                    ?.let { LpResolveUtil.findNonterminalParams(it) }.orEmpty()).toTypedArray()
}