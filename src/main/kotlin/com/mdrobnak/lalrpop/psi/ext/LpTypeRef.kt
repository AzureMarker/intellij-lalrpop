package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.psi.SimpleMultiLineTextEscaper
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.LpTypeRef
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

/**
 * If this type ref is not a child of another type ref.
 */
val LpTypeRef.isTopLevel: Boolean
    get() = parentOfType<LpTypeRef>(false) == null

abstract class LpTypeRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpTypeRef {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.lastChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        SimpleMultiLineTextEscaper(this)

    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        when (val child = firstChild) {
            // all children of a TypeRef in the AST:
            // LpTuple, LpArray, LpTypeOfSymbol, LpRustReference, LpRustType, LpDynTrait, LpDynFn
            // given all of them implement LpResolveType, shortened it to
            is LpResolveType -> child.resolveType(context, arguments)
            else -> "()"
        }
}