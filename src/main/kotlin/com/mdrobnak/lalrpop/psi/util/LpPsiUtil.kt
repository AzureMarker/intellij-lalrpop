package com.mdrobnak.lalrpop.psi.util

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.mdrobnak.lalrpop.psi.LpExternToken
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.ext.isExplicitlySelected
import com.mdrobnak.lalrpop.psi.ext.resolveErrorType
import com.mdrobnak.lalrpop.psi.ext.resolveLocationType
import com.mdrobnak.lalrpop.psi.ext.resolveTokenType
import org.rust.lang.core.psi.ext.descendantsOfType

val List<LpSymbol>.selected: List<LpSymbol>
    get() = if (this.any { it.isExplicitlySelected }) {
        this.filter { it.isExplicitlySelected }
    } else {
        this
    }

fun List<LpSymbol>.computeType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
    val sel = selected
    val joined = sel.joinToString(separator = ", ") { it.resolveType(context, arguments) }
    return if (sel.size != 1) "($joined)"
    else joined
}

fun PsiFile.lalrpopTypeResolutionContext(): LpTypeResolutionContext = CachedValuesManager.getCachedValue(this) {
    val externTokens = descendantsOfType<LpExternToken>()

    val locationType = externTokens.mapNotNull { it.resolveLocationType() }.firstOrNull() ?: "usize"
    val errorType = externTokens.mapNotNull { it.resolveErrorType() }.firstOrNull() ?: "&'static str"
    val tokenType = externTokens.mapNotNull { it.resolveTokenType() }.firstOrNull() ?: "&str"

    return@getCachedValue CachedValueProvider.Result<LpTypeResolutionContext>(
        LpTypeResolutionContext(locationType, errorType, tokenType),
        PsiModificationTracker.MODIFICATION_COUNT
    )
}
