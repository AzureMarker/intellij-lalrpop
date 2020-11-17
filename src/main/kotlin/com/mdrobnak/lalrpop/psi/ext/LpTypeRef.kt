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
                (path.pathId?.resolveType(arguments) ?: path.text) +
                        when (val genericArguments = this.typeGenericArguments) {
                            null -> ""
                            else -> (genericArguments.lifetimeRuleList.map { it.text } +
                                    genericArguments.typeRefList.map { it.resolveType(arguments) })
                                .joinToString(prefix = "<", postfix = ">") { it }
                        }
            }

            LpElementTypes.DYN -> {
                // trait or function-like type?

                when (val secondChild = this.childrenWithLeaves.elementAt(1)) {
                    is LpPath -> {
                        // trait
                        val params = this.childrenWithLeaves.elementAtOrNull(2)
                        if (params == null)
                            "dyn " + secondChild.text
                        else {
                            val typeGenericArguments = params as LpTypeGenericArguments
                            "dyn " + secondChild.text + (
                                    typeGenericArguments.lifetimeRuleList.map { it.text } +
                                            typeGenericArguments.typeRefList.map { it.resolveType(arguments) }
                                    ).joinToString(prefix = "<", postfix = ">", separator = ", ") { it }
                        }
                    }
                    is LpForall -> {
                        // function-like?
                        // FIXME: have no idea what this means

                        "()"
                    }

                    else -> "()"
                }
            }

            else -> "()"
        }
    }
}