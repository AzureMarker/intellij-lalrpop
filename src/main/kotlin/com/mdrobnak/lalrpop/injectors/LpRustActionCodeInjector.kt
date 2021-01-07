package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.ext.actionCodeFunctionHeader
import com.mdrobnak.lalrpop.psi.ext.importCode
import org.rust.lang.RsLanguage

/**
 * Injects the Rust language into user action code
 */
class LpRustActionCodeInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpAction) {
            return
        }

        val codeNode = context.code
        val imports = context.containingFile.importCode

        val rustFunctionHeader = context.actionCodeFunctionHeader(true)

        val prefix = """
            mod __intellij_lalrpop {
                $imports
                $rustFunctionHeader {
            """.trimIndent()

        val suffix = """
            |    }
            |}
        """.trimMargin()

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, codeNode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpAction::class.java)
}
