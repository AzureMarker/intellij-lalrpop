package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.isSelected
import org.rust.lang.core.psi.ext.childrenWithLeaves

abstract class LpSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol {
    fun getSelectedType(): LpSelectedType? {
        // Ignore unselected symbols
        if (!this.isSelected) return null

        val isMutable = childrenWithLeaves.any { it.elementType == LpElementTypes.MUT }
        val name = childrenWithLeaves.find { it.elementType == LpElementTypes.ID }?.text
        val type = this.resolveType()

        return if (name != null) {
            LpSelectedType.WithName(isMutable, name, type)
        } else {
            LpSelectedType.WithoutName(type)
        }
    }

    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String =
        symbol0.resolveType(arguments)

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>) =
        (parent as LpResolveType).completeParameterNames(arguments)

    override val needsParameterNames: Boolean = true
}
