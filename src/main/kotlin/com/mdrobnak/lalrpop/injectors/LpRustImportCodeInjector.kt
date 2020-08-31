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

        registrar.startInjecting(RsLanguage)
            .addPlace("use ", ";", context, context.importCode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpUseStmtImpl::class.java)
}