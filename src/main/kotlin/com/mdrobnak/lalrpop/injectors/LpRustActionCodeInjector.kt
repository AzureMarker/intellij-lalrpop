package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpSelectedType
import com.mdrobnak.lalrpop.psi.LpUseStmt
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
import com.mdrobnak.lalrpop.psi.impl.LpSymbolImpl
import org.rust.lang.RsLanguage

/**
 * Injects the Rust language into user action code
 */
class LpRustActionCodeInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpActionImpl) {
            return
        }

        val codeNode = context.code ?: return
        val imports = PsiTreeUtil.findChildrenOfType(context.containingFile, LpUseStmt::class.java)
            .joinToString("\n") { it.text }
        val nonterminal = context.parentOfType<LpNonterminal>()!!
        val alternative = context.parentOfType<LpAlternative>()!!
        val inputs = alternative.symbolList
            .filterIsInstance<LpSymbolImpl>()
            .mapNotNull { it.getSelectedType() }
        val returnType = nonterminal.typeRef?.text ?: nonterminal.nonterminalName

        val arguments = inputs.joinToString(", ") {
            when (it) {
                is LpSelectedType.WithName -> it.name + ": " + it.type
                // FIXME: do something with unnamed argument?
                is LpSelectedType.WithoutName -> "intellij_lalrpop_noname: " + it.type
            }
        }
        val prefix = "mod __intellij_lalrpop {\n" +
            "$imports\n" +
            "fn __intellij_lalrpop($arguments) -> $returnType {\n"
        val suffix = "\n}\n}"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, codeNode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpActionImpl::class.java)
}