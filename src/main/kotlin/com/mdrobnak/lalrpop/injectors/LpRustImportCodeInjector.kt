package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.impl.LpUseStmtImpl
import org.rust.lang.RsLanguage

/**
 * Injects the Rust language into use statements
 */
class LpRustImportCodeInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpUseStmtImpl) {
            return
        }

        val prefix = """
            mod __tmp__ {
                #[allow(unused_imports)]
                use """.trimIndent()

        val suffix = ";\n}"

        registrar.startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, context.importCode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpUseStmtImpl::class.java)
}