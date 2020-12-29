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

    override fun delete() {
        // if it is alone, just delete the parent `<...>`
        this.parentOfType<LpNonterminalParams>()?.let { if (it.nonterminalParamList.size == 1) it.delete() }

        // on refactoring(safe-delete), also delete the comma that follows this param
        if (this.nextSibling?.elementType == LpElementTypes.COMMA) this.nextSibling.delete()
        // or delete the previous comma
        else {
            if (this.prevSibling?.elementType == TokenType.WHITE_SPACE) this.prevSibling.delete()
            if (this.prevSibling?.elementType == LpElementTypes.COMMA) this.prevSibling.delete()
        }

        super.delete()
    }
}