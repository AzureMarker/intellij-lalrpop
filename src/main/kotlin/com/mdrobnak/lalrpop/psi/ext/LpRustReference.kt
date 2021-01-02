package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpRustReference
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

val LpRustReference.lifetimeOrInfer: String
    get() = this.lifetime?.text ?: "'_"

val LpRustReference.isRefMut: Boolean
    get() = this.mut != null

abstract class LpRustReferenceMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustReference {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        return "&${this.lifetimeOrInfer} ${if (this.isRefMut) "mut" else ""} ${this.typeRef.resolveType(context, arguments)}"
    }
}