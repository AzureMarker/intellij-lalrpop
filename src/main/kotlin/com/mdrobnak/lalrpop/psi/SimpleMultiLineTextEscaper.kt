package com.mdrobnak.lalrpop.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import java.lang.StringBuilder

/**
 * Same as [com.intellij.psi.LiteralTextEscaper], but multi-line.
 * This is a copy of Intellij-Rust's version. They moved the package, so it's
 * copied here for stability across plugin versions.
 */
class SimpleMultiLineTextEscaper<T: PsiLanguageInjectionHost>(host: T): LiteralTextEscaper<T>(host) {
    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(rangeInsideHost.substring(myHost.text))
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        return rangeInsideHost.startOffset + offsetInDecoded
    }

    override fun isOneLine(): Boolean = false
}