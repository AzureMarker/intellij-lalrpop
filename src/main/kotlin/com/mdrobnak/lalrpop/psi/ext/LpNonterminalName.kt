package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import org.rust.lang.core.psi.ext.childrenWithLeaves

val LpNonterminalName.nonterminalParent: LpNonterminal
    get() = this.parent as LpNonterminal

fun LpNonterminalName.addParam(name: String) {
    val factory = LpElementFactory(project)
    val params = this.nonterminalParams
    if (params == null) {
        this.add(factory.createNonterminalParamsFromSingle(name))
    } else {
        val comma = factory.createComma()
        val newParam = factory.createNonterminalParam(name)
        val lastParam = params.nonterminalParamList.lastOrNull()
        if (lastParam != null) {
            val insertedComma = params.addAfter(comma, lastParam)
            params.addAfter(newParam, insertedComma)
        } else {
            // add after the `<`
            params.addAfter(newParam, params.childrenWithLeaves.first())
        }
    }
}

abstract class LpNonterminalNameMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalName {
    override fun getNameIdentifier(): PsiElement {
        return node.findChildByType(LpElementTypes.ID)!!.psi
    }

    override fun getName(): String {
        return nameIdentifier.text
    }

    override fun setName(name: String): PsiElement {
        val newNode = LpElementFactory(project).createIdentifier(name)
        nameIdentifier.replace(newNode)
        return this
    }
}
