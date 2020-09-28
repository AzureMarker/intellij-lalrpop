package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.mdrobnak.lalrpop.psi.LpEnumToken
import com.mdrobnak.lalrpop.psi.util.depthOneBraceMatcherFoldDescriptors

abstract class LpEnumTokenMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpEnumToken {
    override fun getFoldRegions(document: Document, quick: Boolean): List<FoldingDescriptor> =
        depthOneBraceMatcherFoldDescriptors(this, document, quick)

    override fun getFoldReplacement(): String? = "{ ... }"
    override fun getFoldCollapsedByDefault(): Boolean = false
}