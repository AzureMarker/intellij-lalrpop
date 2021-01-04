package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpDynFn
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

abstract class LpDynFnMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpDynFn {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        val forAllString = forall.typeParamList.takeUnless { it.isEmpty() }
            ?.joinToString(prefix = "for<", separator = ", ", postfix = ">") { it.text } ?: ""

        val path = this.path.text

        val argumentTypes =
            this.typeRefList.joinToString(separator = ", ") { it.resolveType(context, arguments) }

        val returnType = this.returnType?.let { " -> " + it.typeRef.resolveType(context, arguments) } ?: ""

        return "$forAllString dyn $path ($argumentTypes) $returnType"
    }
}