package com.mdrobnak.lalrpop.codeInsight

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.mdrobnak.lalrpop.injectors.findModuleDefinition
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.ext.importCode
import com.mdrobnak.lalrpop.psi.getContextAndResolveType
import com.mdrobnak.lalrpop.psi.lalrpopRustType
import org.rust.ide.hints.type.RsTypeHintsPresentationFactory
import org.rust.lang.core.psi.ext.endOffset

@Suppress("UnstableApiUsage")
class LpExpressionTypeInlayHintProvider : InlayHintsProvider<NoSettings> {
    /**
     * Used for persistance of settings
     */
    override val key: SettingsKey<NoSettings>
        get() = KEY

    /**
     * Name of this kind of hints. It will be used in settings and in context menu.
     * Please, do not use word "hints" to avoid duplication
     */
    override val name: String
        get() = "Type hints"

    /**
     * Text, that will be used in the settings as a preview
     */
    override val previewText: String
        get() = TODO("Not yet implemented")

    /**
     * Creates configurable, that immediately applies changes from UI to [settings]
     */
    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        TODO("Not yet implemented")
    }

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector = object : FactoryInlayHintsCollector(editor) {
        val typeHintsPresentationFactory = RsTypeHintsPresentationFactory(factory, false)

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if (file.project.service<DumbService>().isDumb) return true

            if (element is LpNonterminal) presentTypeForNonterminal(element)

            return true
        }

        fun presentTypeForNonterminal(element: LpNonterminal) {
            if (element.typeRef != null) return
            val type =
                element.getContextAndResolveType(LpMacroArguments.identity(element.nonterminalName.nonterminalParams))

            val modDecl = findModuleDefinition(element.project, element.containingFile) ?: return

            val rustType = type.lalrpopRustType(
                element.project,
                element.containingFile.importCode,
                element,
                modDecl
            ) ?: return

            val inlayPresentation = typeHintsPresentationFactory.typeHint(rustType)

            @Suppress("Deprecation")
            sink.addInlineElement(element.nonterminalName.endOffset, false, inlayPresentation)
        }
    }

    companion object {
        private val KEY: SettingsKey<NoSettings> = SettingsKey("lalrpop.type.hints")
    }
}
