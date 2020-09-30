package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.mdrobnak.lalrpop.psi.LpMatchToken
import com.mdrobnak.lalrpop.psi.util.braceMatcherFoldDescriptors

abstract class LpMatchTokenMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpMatchToken {
    override fun getFoldRegions(document: Document, quick: Boolean): List<FoldingDescriptor> =
        braceMatcherFoldDescriptors(this)

    override fun getFoldReplacement(): String? = "{ ... }"
    override fun getFoldCollapsedByDefault(): Boolean = false
}