package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import org.rust.lang.core.psi.ext.childrenWithLeaves

val LpNonterminalName.nonterminalParent: LpNonterminal
    get() = this.parentOfType()!!

fun LpNonterminalName.addParam(name: String) {
    val factory = LpElementFactory(project)
    this.nonterminalParams?.let { params ->
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
    } ?: this.add(factory.createNonterminalParamsFromSingle(name))
}

abstract class LpNonterminalNameMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalName {
    override fun getNameIdentifier(): PsiElement = id

    override fun getName(): String = nameIdentifier.text

    override fun setName(name: String): PsiElement = apply {
        val newNode = LpElementFactory(project).createIdentifier(name)
        nameIdentifier.replace(newNode)
    }

    // delete the name = delete the nonterminal
    override fun delete() = nonterminalParent.delete()
}
