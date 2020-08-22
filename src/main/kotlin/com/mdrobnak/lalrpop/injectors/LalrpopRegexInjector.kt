package com.mdrobnak.lalrpop.injectors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.mdrobnak.lalrpop.psi.impl.LalrpopQuotedLiteralImpl
import org.intellij.lang.regexp.RegExpLanguage

class LalrpopRegexInjector : LanguageInjector {
    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        places: InjectedLanguagePlaces
    ) {
        if (!host.isValidHost || host !is LalrpopQuotedLiteralImpl || !host.isRegex()) {
            return
        }

        val range = TextRange(2, host.textLength - 1)
        places.addPlace(RegExpLanguage.INSTANCE, range, null, null)
    }
}