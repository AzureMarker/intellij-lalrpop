package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.LalrpopNonterminalRef
import com.mdrobnak.lalrpop.resolve.LalrpopNonterminalReference

abstract class LalrpopNonterminalRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LalrpopNonterminalRef {
    override fun getReference(): PsiReference? {
        return LalrpopNonterminalReference(this)
    }
}
