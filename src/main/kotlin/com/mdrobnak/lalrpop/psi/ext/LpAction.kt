package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.psi.SimpleMultiLineTextEscaper
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpElementTypes

abstract class LpActionMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAction {
    val code: PsiElement
        get() {
            assert(lastChild.elementType == LpElementTypes.CODE)
            return lastChild
        }

    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return SimpleMultiLineTextEscaper(this)
    }
}
