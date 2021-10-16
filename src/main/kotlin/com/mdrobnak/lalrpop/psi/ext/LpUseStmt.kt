package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.mdrobnak.lalrpop.psi.LpUseStmt
import com.mdrobnak.lalrpop.psi.SimpleMultiLineTextEscaper
import org.rust.lang.core.psi.ext.childrenOfType

val PsiFile.importCode: String
    get() = childrenOfType<LpUseStmt>().joinToString(separator = "\n") { it.text }

abstract class LpUseStmtMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpUseStmt {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = importCode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        SimpleMultiLineTextEscaper(this)
}
