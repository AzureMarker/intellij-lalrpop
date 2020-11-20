package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpRustReference
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.isRefMut
import com.mdrobnak.lalrpop.psi.util.lifetimeOrInfer

abstract class LpRustReferenceMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustReference {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return "&${this.lifetimeOrInfer} ${if (this.isRefMut) "mut" else ""} ${this.typeRef.resolveType(arguments)}"
    }
}