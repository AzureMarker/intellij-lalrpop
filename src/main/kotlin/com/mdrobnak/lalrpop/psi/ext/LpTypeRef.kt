package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.isRefMut
import com.mdrobnak.lalrpop.psi.util.lifetimeOrInfer
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

abstract class LpTypeRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpTypeRef {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return when (val child = firstChild) {
            // all children of a TypeRef in the AST:
            // LpTuple, LpArray, LpTypeOfSymbol, LpRustReference, LpRustType, LpDynTrait, LpDynFn
            // given all of them implement LpResolveType, shortened it to
            is LpResolveType -> child.resolveType(arguments)
            else -> "()"
        }
    }
}