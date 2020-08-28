package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LalrpopQuotedLiteral
import com.mdrobnak.lalrpop.psi.LalrpopTypes

abstract class LalrpopQuotedLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), LalrpopQuotedLiteral {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }

    fun isRegex(): Boolean = firstChild.elementType == LalrpopTypes.REGEX_LITERAL
}
