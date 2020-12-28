package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.ext.*
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
import com.mdrobnak.lalrpop.psi.impl.LpSymbolImpl
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import org.rust.lang.RsLanguage

/**
 * Injects the Rust language into user action code
 */
class LpRustActionCodeInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpAction) {
            return
        }

        val codeNode = context.code ?: return
        val imports = context.containingFile.importCode
        val alternative = context.parentOfType<LpAlternative>()!!

        val rustFunctionHeader = context.actionCodeFunctionHeader(true)

        val prefix = "mod __intellij_lalrpop {\n" +
                "$imports\n" +
                "$rustFunctionHeader {\n"

        val suffix = "\n}\n}"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, codeNode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpAction::class.java)
}
