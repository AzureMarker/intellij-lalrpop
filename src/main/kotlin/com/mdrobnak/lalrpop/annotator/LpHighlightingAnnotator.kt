package com.mdrobnak.lalrpop.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.LpColor
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.LpRustType

class LpHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Leave type highlighting to the Rust plugin
        if (element.parentOfType<LpRustType>(withSelf = true) != null) return

        val color = when (element.elementType) {
            LpElementTypes.ANNOTATION -> LpColor.ANNOTATION
            LpElementTypes.ID -> colorForIdentifier(element.parent)
            LpElementTypes.PATH_REF -> LpColor.PATH
            else -> null
        } ?: return
        val severity = HighlightSeverity.INFORMATION
        holder.newSilentAnnotation(severity).textAttributes(color.textAttributesKey).create()
    }

    private fun colorForIdentifier(parent: PsiElement?): LpColor? =
        when (parent?.elementType) {
            LpElementTypes.GRAMMAR_PARAM -> LpColor.PARAMETER
            LpElementTypes.SYMBOL_NAME -> LpColor.PARAMETER
            LpElementTypes.NONTERMINAL_REF ->
                if ((parent as LpNonterminalRef).reference?.resolve()?.elementType == LpElementTypes.NONTERMINAL_PARAM)
                    LpColor.NONTERMINAL_GENERIC_PARAMETER
                else
                    LpColor.NONTERMINAL_REFERENCE
            LpElementTypes.NONTERMINAL_NAME -> LpColor.NONTERMINAL_NAME_IN_DECLARATION
            LpElementTypes.NONTERMINAL_PARAM -> LpColor.NONTERMINAL_GENERIC_PARAMETER
            LpElementTypes.COND -> LpColor.IDENTIFIER
            LpElementTypes.PATH_ID -> LpColor.PATH
            else -> null
        }
}
