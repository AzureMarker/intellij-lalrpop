package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.LpSelectedType
import com.mdrobnak.lalrpop.psi.LpSymbol
import com.mdrobnak.lalrpop.psi.impl.LpNonterminalNameImpl
import org.rust.lang.core.psi.ext.childrenWithLeaves

abstract class LpSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol {
    fun getSelectedType(): LpSelectedType? {
        // Ignore unselected symbols
        if (childrenWithLeaves.first().elementType != LpElementTypes.LESSTHAN) return null

        val mut = childrenWithLeaves.any { it.elementType == LpElementTypes.MUT }
        val name = childrenWithLeaves.find { it.elementType == LpElementTypes.ID }?.text
        val nonterminalRef = PsiTreeUtil.findChildOfType(symbol0, LpNonterminalRef::class.java)
        val nonterminal = nonterminalRef?.reference?.resolve() as? LpNonterminalNameImpl?
        val type = nonterminal?.nonterminal?.typeRef?.text ?: "&str"

        return if (name != null) {
            LpSelectedType.WithName(mut, name, type)
        } else {
            LpSelectedType.WithoutName(type)
        }
    }
}
