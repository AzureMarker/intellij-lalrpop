package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.mdrobnak.lalrpop.psi.LpExternToken
import com.mdrobnak.lalrpop.psi.util.braceMatcherFoldDescriptors

abstract class LpExternTokenMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpExternToken {
    override fun getFoldRegions(document: Document, quick: Boolean): List<FoldingDescriptor> =
        braceMatcherFoldDescriptors(this)

    override fun getFoldReplacement(): String? = "{ ... }"
    override fun getFoldCollapsedByDefault(): Boolean = false
}