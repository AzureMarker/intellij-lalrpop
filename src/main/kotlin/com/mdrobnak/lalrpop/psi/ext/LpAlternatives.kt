package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.mdrobnak.lalrpop.psi.LpAlternatives
import com.mdrobnak.lalrpop.psi.util.braceMatcherFoldDescriptors

abstract class LpAlternativesMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternatives {
    override fun getFoldRegions(document: Document, quick: Boolean) = braceMatcherFoldDescriptors(this)

    override fun getFoldReplacement(): String? = "{ ... }"
    override fun getFoldCollapsedByDefault(): Boolean = false
}