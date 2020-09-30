package com.mdrobnak.lalrpop.psi.util

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.endOffset
import org.rust.lang.core.psi.ext.startOffset
import java.util.*

/**
 * For a given PSI element, returns a list of folding descriptors for pairs of braces.
 * The ranges also contain the braces themselves.
 */
fun braceMatcherFoldDescriptors(element: PsiElement): List<FoldingDescriptor> {
    val iter = element.childrenWithLeaves.iterator()
    val braces: Stack<Int> = Stack()
    val descriptors = mutableListOf<FoldingDescriptor>()
    while (iter.hasNext()) {
        val it = iter.next()
        if (it.elementType == LpElementTypes.LBRACE) {
            braces.push(it.startOffset)
        } else if (it.elementType == LpElementTypes.RBRACE) {
            if (braces.empty()) {
                // There are more closing braces than opening braces.
                // We will just ignore them (IntelliJ will do the right thing).
            } else {
                descriptors.add(FoldingDescriptor(element, TextRange(braces.pop(), element.endOffset)))
            }
        }
    }

    return descriptors
}