package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.mdrobnak.lalrpop.psi.LpAlternatives
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.util.braceMatcherFoldDescriptors
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType
import org.rust.lang.core.psi.ext.endOffset
import org.rust.lang.core.psi.ext.startOffset

abstract class LpAlternativesMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternatives {
    override fun getFoldRegions(document: Document, quick: Boolean) = braceMatcherFoldDescriptors(this)

    override fun getFoldReplacement(): String? = "{ ... }"
    override fun getFoldCollapsedByDefault(): Boolean = false
}