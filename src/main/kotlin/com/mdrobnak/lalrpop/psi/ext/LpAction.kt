package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpElementTypes

abstract class LpActionMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAction {
    val code: PsiElement?
        get() = if (lastChild?.elementType == LpElementTypes.CODE) {
            lastChild
        } else {
            null
        }

    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.lastChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        // TODO: Change the evalOfAngleBracketsExpression to a list of the selected symbols in the alternative so it can be evaluated based on context
        return LpActionLiteralTextEscaper(this, "abcdefghij")
    }
}
