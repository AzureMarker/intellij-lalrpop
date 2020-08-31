package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpUseStmt
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
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

class LpRustInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpActionImpl) {
            return
        }

        val imports = PsiTreeUtil.findChildrenOfType(context.containingFile, LpUseStmt::class.java)
            .joinToString("\n") { it.text }
        val nonterminal = context.parentOfType<LpNonterminal>()!!
        val returnType = nonterminal.typeRef?.text ?: nonterminal.nonterminalName

        val prefix = "mod __intellij_lalrpop { $imports\nfn __intellij_lalrpop() -> $returnType { "
        val suffix = " } }"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, context.code.textRangeInParent)
            .doneInjecting()

        // Hack to attach this code to the current crate. See
        // https://github.com/intellij-rust/intellij-rust/issues/6026
        val parserMod = findModuleDefinition(context.project, context.containingFile) ?: return
        val psi = InjectedLanguageUtil.getCachedInjectedFileWithLanguage(context, RsLanguage) ?: return
        for (child in psi.childrenOfType<RsExpandedElement>()) {
            child.setContext(parserMod)
        }
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpActionImpl::class.java)

    companion object {
        /**
         * Find the Rust file where the generated LALRPOP parser module is defined.
         * The current search algorithm is a little coarse, but should work in most cases.
         */
        private fun findModuleDefinition(project: Project, lalrpopFile: PsiFile): RsModItem? {
            val virtualFiles = FileTypeIndex.getFiles(RsFileType, GlobalSearchScope.projectScope(project))
            val psiManager = PsiManager.getInstance(project)
            val moduleName = lalrpopFile.name.removeSuffix(".lalrpop")
            val cargoPackage = lalrpopFile.findCargoPackage()

            return virtualFiles
                // Find Rust files
                .mapNotNull { psiManager.findFile(it) as RsFile? }
                // Only look at files in the same package
                .filter { it.containingCargoPackage == cargoPackage }
                // Look at module declarations, including those created via macros (ex. lalrpop_mod macro)
                .flatMap { it.expandedItemsExceptImplsAndUses }
                .filterIsInstance<RsModItem>()
                // Find the parser's module declaration
                .find { it.name == moduleName }
        }
    }
}