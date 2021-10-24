package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpUseStmt
import com.mdrobnak.lalrpop.psi.SimpleMultiLineTextEscaper
import org.rust.lang.core.psi.ext.childrenOfType

val PsiFile.importCode: String
    get() = childrenOfType<LpUseStmt>().joinToString(separator = "\n") { it.text }

abstract class LpUseStmtMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpUseStmt {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val newNode = LpElementFactory(project).createUseStmt(text);
        replace(newNode)
        return newNode
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        SimpleMultiLineTextEscaper(this)
}
