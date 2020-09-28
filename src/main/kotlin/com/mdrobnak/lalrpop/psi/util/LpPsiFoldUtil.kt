package com.mdrobnak.lalrpop.psi.util

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.endOffset
import org.rust.lang.core.psi.ext.startOffset

fun depthOneBraceMatcherFoldDescriptors(element: PsiElement, document: Document, quick: Boolean): List<FoldingDescriptor> {
    val iter = element.childrenWithLeaves.iterator()
    var lastLBrace = 0
    val descriptors = mutableListOf<FoldingDescriptor>()
    while (iter.hasNext()) {
        val it = iter.next()
        if (it.elementType == LpElementTypes.LBRACE) {
            lastLBrace = it.startOffset
        } else if (it.elementType == LpElementTypes.RBRACE) {
            descriptors += listOf(FoldingDescriptor(element, TextRange(lastLBrace, element.endOffset)))
        }
    }

    return descriptors
}