package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.LpQuotedLiteral
import com.mdrobnak.lalrpop.psi.ext.isRegex
import org.intellij.lang.regexp.RegExpLanguage

class LpRegexInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpQuotedLiteral || !context.isRegex()) {
            return
        }

        val indexOfFirstQuote = context.text.indexOf('"')
        val indexOfLastQuote = context.text.lastIndexOf('"')
        val range = TextRange(indexOfFirstQuote + 1, indexOfLastQuote)
        registrar
            .startInjecting(RegExpLanguage.INSTANCE)
            .addPlace(null, null, context, range)
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpQuotedLiteral::class.java)
}