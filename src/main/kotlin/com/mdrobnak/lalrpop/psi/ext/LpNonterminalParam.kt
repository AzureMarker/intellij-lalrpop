package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpNonterminalParam
import com.mdrobnak.lalrpop.psi.LpNonterminalParams
import org.toml.lang.psi.ext.elementType

abstract class LpNonterminalParamMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalParam {
    override fun getNameIdentifier(): PsiElement = id

    override fun getName(): String = nameIdentifier.text

    override fun setName(name: String): PsiElement = apply {
        val newNode = LpElementFactory(project).createIdentifier(name)
        nameIdentifier.replace(newNode)
    }

    override fun delete() {
        // if it is alone, just delete the parent `<...>`
        parentOfType<LpNonterminalParams>()?.takeIf { it.nonterminalParamList.size == 1 }?.apply { delete(); return }

        // on refactoring(safe-delete), also delete the comma that follows this param
        if (nextSibling?.elementType == LpElementTypes.COMMA) nextSibling.delete()
        // or delete the previous comma
        else {
            if (prevSibling?.elementType == TokenType.WHITE_SPACE) prevSibling.delete()
            if (prevSibling?.elementType == LpElementTypes.COMMA) prevSibling.delete()
        }

        super.delete()
    }
}