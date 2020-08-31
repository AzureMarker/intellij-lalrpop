package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.resolve.LpNonterminalReference

abstract class LpNonterminalRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalRef {
    override fun getReference(): PsiReference? {
        return LpNonterminalReference(this)
    }
}
