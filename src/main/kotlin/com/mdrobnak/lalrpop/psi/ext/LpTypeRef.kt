package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.LpTypeRef
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpTypeRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpTypeRef {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String =
        when (val child = firstChild) {
            // all children of a TypeRef in the AST:
            // LpTuple, LpArray, LpTypeOfSymbol, LpRustReference, LpRustType, LpDynTrait, LpDynFn
            // given all of them implement LpResolveType, shortened it to
            is LpResolveType -> child.resolveType(context, arguments)
            else -> "()"
        }
}