package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpRustReference
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

val LpRustReference.lifetime: String?
    get() = this.lifetimeRule?.text

val LpRustReference.lifetimeOrInfer: String
    get() = this.lifetime ?: "'_"

val LpRustReference.isRefMut: Boolean
    get() = this.childrenWithLeaves.find { it.elementType == LpElementTypes.MUT } != null

abstract class LpRustReferenceMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustReference {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return "&${this.lifetimeOrInfer} ${if (this.isRefMut) "mut" else ""} ${this.typeRef.resolveType(arguments)}"
    }
}