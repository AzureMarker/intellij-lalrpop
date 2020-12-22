package com.mdrobnak.lalrpop.psi

import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext

data class NonterminalGenericArgument(val rustType: String, var name: String)

data class LpTypeResolutionContext(
    val locationType: String = "usize",
    val errorType: String = "()",
    val tokenType: String = "&str"
) {
    val errorRecovery = "::lalrpop_util::ErrorRecovery<$locationType, $tokenType, $errorType>"
    val parseError = "::lalrpop_util::ParseError<$locationType, $tokenType, $errorType>"
}

interface LpResolveType : PsiElement {
    /**
     * Returns a string of the rust type the node that implements this would resolve to.
     *
     * @param arguments the list of arguments, only used in the case of a nonterminal ref; for example in
     * Nonterminal<Rule1, Rule2>: SomeType<Rule1, Rule2> = {
     *      Rule1 Rule2 => SomeType::new(<>),
     * }
     *
     * And referenced with Nonterminal<A, B> in another symbol, the list of arguments should be the resolved types of
     * "A" and "B", in this order.
     */
    fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String

//    fun resolveType(arguments: List<NonterminalGenericArgument>): String =
//        this.resolveType(this.containingFile.lalrpopTypeResolutionContext(), arguments)
}

fun LpResolveType.getContextAndResolveType(arguments: List<NonterminalGenericArgument>): String =
    this.resolveType(this.containingFile.lalrpopTypeResolutionContext(), arguments)
