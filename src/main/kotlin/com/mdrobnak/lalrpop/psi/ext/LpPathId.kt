package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpPathId
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.resolve.LpPathIdReference

abstract class LpPathIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpPathId {
    override fun getReference(): PsiReference? = LpPathIdReference(this)

    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        id.text.let { id -> arguments.find { it.name == id }?.rustType ?: id }
}