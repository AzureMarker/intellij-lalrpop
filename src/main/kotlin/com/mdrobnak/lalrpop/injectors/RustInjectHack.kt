package com.mdrobnak.lalrpop.injectors

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.rust.lang.RsFileType
import org.rust.lang.RsLanguage
import org.rust.lang.core.macros.RsExpandedElement
import org.rust.lang.core.macros.setContext
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsModItem
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.psi.ext.containingCargoPackage
import org.rust.lang.core.psi.ext.expandedItemsExceptImplsAndUses
import org.rust.lang.core.psi.ext.findCargoPackage

/**
 * Attach the injected code (the PSI element) to the Rust crate it is in.
 * The attachment point is found by searching for the parser module declaration.
 */
fun attachInjectedCodeToCrate(element: PsiElement) {
    // Hack to attach the injected code to the current crate. See
    // https://github.com/intellij-rust/intellij-rust/issues/6026
    val parserMod = findModuleDefinition(element.project, element.containingFile) ?: return
    val psi = InjectedLanguageUtil.getCachedInjectedFileWithLanguage(element, RsLanguage) ?: return
    for (child in psi.childrenOfType<RsExpandedElement>()) {
        child.setContext(parserMod)
    }
}

/**
 * Find the Rust file where the generated LALRPOP parser module is defined.
 * The current search algorithm is a little coarse, but should work in most cases.
 */
fun findModuleDefinition(project: Project, lalrpopFile: PsiFile): RsModItem? {
    val virtualFiles = FileTypeIndex.getFiles(RsFileType, GlobalSearchScope.projectScope(project))
    val psiManager = PsiManager.getInstance(project)
    val moduleName = lalrpopFile.name.removeSuffix(".lalrpop")
    val cargoPackage = lalrpopFile.findCargoPackage() ?: return null

    return virtualFiles
        // Find Rust files
        .mapNotNull { psiManager.findFile(it) as RsFile? }
        // Only look at files in the same package
        .filter { it.containingCargoPackage?.id == cargoPackage.id }
        // Look at module declarations, including those created via macros (ex. lalrpop_mod macro)
        .flatMap { it.expandedItemsExceptImplsAndUses }
        .filterIsInstance<RsModItem>()
        // Find the parser's module declaration
        .find { it.name == moduleName }
}
