package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.LpTypeRef
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.isRefMut
import com.mdrobnak.lalrpop.psi.util.lifetimeOrInfer
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

abstract class LpTypeRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpTypeRef {
    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        return when (childrenWithLeaves.first().elementType) {
            // tuple
            LpElementTypes.LPAREN -> this.typeRefList.joinToString(prefix = "(", postfix = ")") {
                it.resolveType(arguments)
            }
            // array
            LpElementTypes.LBRACKET -> "[" + this.typeRefList.first().resolveType(arguments) + "]"
            // FIXME: not sure if this is supposed to be the type of another symbol,
            //  but would assume so given the context requires a type while a symbol is passed.
            LpElementTypes.POUND -> this.symbol?.resolveType(arguments) ?: "()"

            // reference
            LpElementTypes.AND -> "&${this.lifetimeOrInfer} ${if (this.isRefMut) "mut" else ""} ${
                this.typeRefList[0].resolveType(arguments)
            }"
            // type
            LpElementTypes.PATH -> {
                val path = this.path!!
                val tp = (path.rustPrimitive?.text ?: path.pathId?.resolveType(arguments) ?: path.text)
                println("Type for ${path.text} without generic arguments: $tp")
                tp + when (val genericArguments = this.typeGenericArguments) {
                    null -> ""
                    else -> (genericArguments.lifetimeRuleList.map { it.text } +
                            genericArguments.typeRefList.map { it.resolveType(arguments) })
                        .joinToString(prefix = "<", postfix = ">") { it }
                }
            }

            // TODO: work out how to do `dyn Trait`s

            else -> "()"
        }
    }

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        return (parent as LpResolveType).completeParameterNames(arguments)
    }

    override val needsParameterNames: Boolean = true
}