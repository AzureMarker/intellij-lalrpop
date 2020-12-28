package com.mdrobnak.lalrpop.psi.util

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpExternToken
import com.mdrobnak.lalrpop.psi.LpSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.ext.isExplicitlySelected
import com.mdrobnak.lalrpop.psi.ext.resolveErrorType
import com.mdrobnak.lalrpop.psi.ext.resolveLocationType
import com.mdrobnak.lalrpop.psi.ext.resolveTokenType

val List<LpSymbol>.selected: List<LpSymbol>
    get() = if (this.any { it.isExplicitlySelected }) {
        this.filter { it.isExplicitlySelected }
    } else {
        this
    }

fun List<LpSymbol>.computeType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
    val sel = selected
    return sel.joinToString(
        prefix = if (sel.size != 1) "(" else "",
        postfix = if (sel.size != 1) ")" else ""
    ) {
        it.resolveType(context, arguments)
    }
}

fun PsiFile.lalrpopTypeResolutionContext(): LpTypeResolutionContext = CachedValuesManager.getCachedValue(this) {
    val externTokens = PsiTreeUtil.findChildrenOfType(this, LpExternToken::class.java)

    val locationType = externTokens.mapNotNull { it.resolveLocationType() }.firstOrNull() ?: "usize"
    val errorType = externTokens.mapNotNull { it.resolveErrorType() }.firstOrNull() ?: "()"
    val tokenType = externTokens.mapNotNull { it.resolveTokenType() }.firstOrNull() ?: "&str"

    return@getCachedValue CachedValueProvider.Result<LpTypeResolutionContext>(
        LpTypeResolutionContext(locationType, errorType, tokenType),
        PsiModificationTracker.MODIFICATION_COUNT
    )
}
