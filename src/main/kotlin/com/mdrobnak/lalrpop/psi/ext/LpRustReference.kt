package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpRustReference
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

val LpRustReference.lifetimeOrInfer: String
    get() = lifetime?.text ?: "'_"

val LpRustReference.isRefMut: Boolean
    get() = mut != null

abstract class LpRustReferenceMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustReference {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        "&${lifetimeOrInfer} ${if (isRefMut) "mut" else ""} ${typeRef.resolveType(context, arguments)}"
}