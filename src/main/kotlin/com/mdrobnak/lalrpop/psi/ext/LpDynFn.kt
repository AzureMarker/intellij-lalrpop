package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpDynFn
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpDynFnMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpDynFn {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String {
        val forAllTypeParams = this.forall.typeParamList
        val forAllString = if (forAllTypeParams.isEmpty()) {
            forAllTypeParams.joinToString(prefix = "for<", separator = ", ", postfix = ">") { it.text }
        } else {
            ""
        }

        val path = this.path.text

        val argumentTypes =
            this.typeRefList.joinToString(separator = ", ") { it.resolveType(context, arguments) }

        val returnType = when (val returnType = this.returnType) {
            null -> ""
            else -> " -> " + returnType.typeRef.resolveType(context, arguments)
        }

        return "$forAllString dyn $path ($argumentTypes) $returnType"
    }
}