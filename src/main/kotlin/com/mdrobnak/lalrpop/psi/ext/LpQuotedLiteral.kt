package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpQuotedLiteral

fun LpQuotedLiteral.isRegex(): Boolean = regexLiteral != null

abstract class LpQuotedLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpQuotedLiteral {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost = apply {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        LiteralTextEscaper.createSimple(this)
}
