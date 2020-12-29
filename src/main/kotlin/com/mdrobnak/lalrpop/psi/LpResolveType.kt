package com.mdrobnak.lalrpop.psi

import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import org.rust.lang.core.psi.RsPsiFactory
import org.rust.lang.core.psi.RsTypeParameterList
import org.rust.lang.core.resolve.ImplLookup
import org.rust.lang.core.types.Substitution
import org.rust.lang.core.types.infer.RsInferenceContext
import org.rust.lang.core.types.toTypeSubst
import org.rust.lang.core.types.ty.TyTypeParameter
import org.rust.lang.core.types.ty.TyUnit
import org.rust.lang.core.types.type

data class LpMacroArgument(val rustType: String, val name: String)
data class LpMacroArguments(val arguments: List<LpMacroArgument> = listOf()): List<LpMacroArgument> by arguments {
    fun getSubstitution(params: RsTypeParameterList?, inferenceContext: RsInferenceContext): Substitution =
        params?.typeParameterList.orEmpty().map { param ->
            TyTypeParameter.named(param) to (arguments.find { arg -> arg.name == param.name }?.rustType?.let {
                println("Type parameter: ${param.name!!}, rust type in substitution: $it")
                inferenceContext.fullyResolve(RsPsiFactory(param.project).createType(it).type)
            } ?: TyUnit)
        }.toMap().toTypeSubst()

    companion object {
        fun identity(params: LpNonterminalParams?): LpMacroArguments =
            LpMacroArguments(params?.nonterminalParamList?.map {
                val name = it.name!!
                LpMacroArgument(name, name)
            }.orEmpty())
    }
}

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
    fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String

}

fun LpResolveType.getContextAndResolveType(arguments: LpMacroArguments): String =
    this.resolveType(this.containingFile.lalrpopTypeResolutionContext(), arguments)
