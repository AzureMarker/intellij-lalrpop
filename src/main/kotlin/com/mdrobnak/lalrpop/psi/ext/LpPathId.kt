package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.LpPathId
import com.mdrobnak.lalrpop.resolve.LpPathIdReference

open class LpPathIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpPathId {
    override fun getReference(): PsiReference? = LpPathIdReference(this)
}