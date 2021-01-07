package com.mdrobnak.lalrpop.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import org.rust.lang.core.macros.setContext
import org.rust.lang.core.psi.RsPsiFactory
import org.rust.lang.core.psi.RsTypeParameterList
import org.rust.lang.core.psi.ext.RsElement
import org.rust.lang.core.types.Substitution
import org.rust.lang.core.types.infer.RsInferenceContext
import org.rust.lang.core.types.toTypeSubst
import org.rust.lang.core.types.ty.TyTypeParameter
import org.rust.lang.core.types.ty.TyUnit
import org.rust.lang.core.types.type

data class LpMacroArgument(val rustType: String, val name: String)
data class LpMacroArguments(val arguments: List<LpMacroArgument> = listOf()) : List<LpMacroArgument> by arguments {
    fun getSubstitution(
        params: RsTypeParameterList?,
        project: Project,
        inferenceContext: RsInferenceContext,
        expandedElementContext: RsElement
    ): Substitution =
        params?.typeParameterList?.map { param ->
            TyTypeParameter.named(param) to (arguments.find { arg -> arg.name == param.identifier.text }?.rustType?.let {
                RsPsiFactory(project).createType(it).run {
                    setContext(expandedElementContext)

                    inferenceContext.fullyResolve(type)
                }
            } ?: TyUnit)
        }.orEmpty().toMap().toTypeSubst()

    companion object {
        fun identity(params: LpNonterminalParams?): LpMacroArguments =
            LpMacroArguments(params?.nonterminalParamList?.mapNotNull { it.name }?.map { LpMacroArgument(it, it) }
                .orEmpty())
    }
}


data class LpTypeResolutionContext(
    val locationType: String = "usize",
    val errorType: String = "&'static str",
    val tokenType: String = "&str"
) {
    val errorRecovery = "::lalrpop_util::ErrorRecovery<$locationType, $tokenType, $errorType>"
    val parseError = "::lalrpop_util::ParseError<$locationType, $tokenType, $errorType>"
}

interface LpResolveType : PsiElement {
    /**
     * Returns a string of the rust type the node that implements this would resolve to.
     *
     * @param context the type resolution context of this file
     * @param arguments the list of arguments, only used in the case of a nonterminal ref; for example in
     * Nonterminal<Rule1, Rule2>: SomeType<Rule1, Rule2> = {
     *      Rule1 Rule2 => SomeType::new(<>),
     * }
     *
     * And referenced with Nonterminal<A, B> in another symbol, the list of arguments should be the resolved types of
     * "A" and "B", not necessarily in this order.
     *
     * @see LpMacroArguments.identity
     *
     * @see PsiFile.lalrpopTypeResolutionContext
     * @see LpResolveType.getContextAndResolveType
     */
    fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String

}

fun LpResolveType.getContextAndResolveType(arguments: LpMacroArguments): String =
    resolveType(containingFile.lalrpopTypeResolutionContext(), arguments)
