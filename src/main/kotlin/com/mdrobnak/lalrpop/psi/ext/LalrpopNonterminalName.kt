package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.LalrpopElementFactory
import com.mdrobnak.lalrpop.psi.LalrpopNonterminalName
import com.mdrobnak.lalrpop.psi.LalrpopTypes

abstract class LalrpopNonterminalNameMixin(node: ASTNode) : ASTWrapperPsiElement(node), LalrpopNonterminalName {
    override fun getNameIdentifier(): PsiElement {
        return node.findChildByType(LalrpopTypes.ID)!!.psi
    }

    override fun getName(): String {
        return nameIdentifier.text
    }

    override fun setName(name: String): PsiElement {
        val newNode = LalrpopElementFactory(project).createIdentifier(name)
        nameIdentifier.replace(newNode)
        return this
    }
}
