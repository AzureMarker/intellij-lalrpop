package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

fun LpNonterminal.setType(type: String) {
    val typePsi = LpElementFactory(project).createNonterminalType(type)
    typeRef?.apply { replace(typePsi.second) }
        ?: addRangeAfter(typePsi.first, typePsi.second, nonterminalName)
}

fun LpNonterminal.rustGenericUnitStructs(): String =
    containingFile.lalrpopFindGrammarDecl().typeParamsRustUnitStructs() +
            (nonterminalName.nonterminalParams?.nonterminalParamList?.joinToString(
                separator = "\n",
                postfix = "\n"
            ) { "struct ${it.id.text};" } ?: "")

val LpNonterminal.genericParams: String
    get() = (containingFile.lalrpopFindGrammarDecl().grammarTypeParams?.typeParamList?.map { it.text }
        .orEmpty() + nonterminalName.nonterminalParams?.nonterminalParamList?.map { it.text }.orEmpty())
        .takeUnless { it.isEmpty() }?.joinToString(prefix = "<", postfix = ">", separator = ", ") ?: ""


abstract class LpNonterminalMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminal {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        if (nonterminalName.nonterminalParams != null)
            internallyResolveType(context, arguments)
        else CachedValuesManager.getCachedValue(this) {
            // Isn't a lalrpop macro and therefore can be cached
            return@getCachedValue CachedValueProvider.Result<String>(
                internallyResolveType(context, LpMacroArguments(listOf(), listOf())),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }

    private fun internallyResolveType(
        context: LpTypeResolutionContext,
        arguments: LpMacroArguments
    ): String =
        // get directly from the type_ref if available
        typeRef?.resolveType(context, arguments) ?:
        // or try to infer from the first alternative that doesn't have action code
        alternatives.alternativeList.firstOrNull { it.action == null }?.resolveType(context, arguments) ?:
        // or as a last resort try to infer from the action code with intellij-rust
        alternatives.alternativeList.firstOrNull()?.resolveType(context, arguments) ?:
        // or if we get here, it means the nonterminal looks like `A = {};`, e.g. there are no alternatives and no type_ref
        "()"
}