package com.mdrobnak.lalrpop

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpFoldable


object LpFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        return PsiTreeUtil.findChildrenOfType(root, LpFoldable::class.java).stream()
            .map { it.getFoldRegions(document, quick) }
            .flatMap { (List<FoldingDescriptor>::stream)(it) }.toArray { arrayOfNulls<FoldingDescriptor>(it) }
    }

    override fun getPlaceholderText(node: ASTNode): String? = when (val nodePsi = node.psi) {
        is LpFoldable -> nodePsi.getFoldReplacement()
        else -> null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = when (val nodePsi = node.psi) {
        is LpFoldable -> nodePsi.getFoldCollapsedByDefault()
        else -> false
    }
}
