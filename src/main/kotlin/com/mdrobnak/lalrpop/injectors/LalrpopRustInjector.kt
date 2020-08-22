package com.mdrobnak.lalrpop.injectors

import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LalrpopNonterminal
import com.mdrobnak.lalrpop.psi.LalrpopUseStmt
import com.mdrobnak.lalrpop.psi.impl.LalrpopActionImpl
import org.rust.lang.RsLanguage

class LalrpopRustInjector : LanguageInjector {
    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        places: InjectedLanguagePlaces
    ) {
        if (!host.isValidHost || host !is LalrpopActionImpl) {
            return
        }

        val imports = PsiTreeUtil.findChildrenOfType(host.containingFile, LalrpopUseStmt::class.java)
            .joinToString("\n") { it.text }
        val nonterminal = host.parentOfType<LalrpopNonterminal>()!!
        val returnType = nonterminal.typeRef?.text ?: nonterminal.nonterminalName

        val prefix = "$imports\nfn __intellij_lalrpop() -> $returnType { "
        val suffix = " }"

        places.addPlace(RsLanguage, host.code.textRangeInParent, prefix, suffix)
    }
}