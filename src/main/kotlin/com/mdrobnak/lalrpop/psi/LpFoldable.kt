package com.mdrobnak.lalrpop.psi

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement

interface LpFoldable : PsiElement {
    fun getFoldRegions(document: Document, quick: Boolean): List<FoldingDescriptor>
    fun getFoldReplacement(): String?
    fun getFoldCollapsedByDefault(): Boolean
}