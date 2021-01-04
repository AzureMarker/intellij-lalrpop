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
    val braces: Stack<Int> = Stack()
    val descriptors = mutableListOf<FoldingDescriptor>()
    element.childrenWithLeaves.iterator().forEach {
        when (it.elementType) {
            LpElementTypes.LBRACE -> {
                braces.push(it.startOffset)
            }
            LpElementTypes.RBRACE -> {
                if (braces.isNotEmpty()) {
                    descriptors.add(FoldingDescriptor(element, TextRange(braces.pop(), element.endOffset)))
                }
                //else {
                // There are more closing braces than opening braces.
                // We will just ignore them (IntelliJ will do the right thing).
                //}
            }
        }
    }

    return descriptors
}