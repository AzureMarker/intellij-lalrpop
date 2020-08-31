package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.psi.SimpleMultiLineTextEscaper
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpUseStmt

abstract class LpUseStmtMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpUseStmt {
    val importCode: PsiElement
        get() = findChildByType(LpElementTypes.IMPORT_CODE)!!

    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = importCode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return SimpleMultiLineTextEscaper(this)
    }
}
