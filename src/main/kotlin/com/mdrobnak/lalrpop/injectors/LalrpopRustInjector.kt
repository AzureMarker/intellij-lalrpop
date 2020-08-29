package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LalrpopNonterminal
import com.mdrobnak.lalrpop.psi.LalrpopUseStmt
import com.mdrobnak.lalrpop.psi.impl.LalrpopActionImpl
import org.rust.lang.RsLanguage

class LalrpopRustInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LalrpopActionImpl) {
            return
        }

        val imports = PsiTreeUtil.findChildrenOfType(context.containingFile, LalrpopUseStmt::class.java)
            .joinToString("\n") { it.text }
        val nonterminal = context.parentOfType<LalrpopNonterminal>()!!
        val returnType = nonterminal.typeRef?.text ?: nonterminal.nonterminalName

        val prefix = "$imports\nfn __intellij_lalrpop() -> $returnType { "
        val suffix = " }"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, context.code.textRangeInParent)
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LalrpopActionImpl::class.java)
}