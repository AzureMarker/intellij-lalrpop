package com.mdrobnak.lalrpop.codeInsight

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.ImmediateConfigurable.Case
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.injectors.findModuleDefinition
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.ext.importCode
import com.mdrobnak.lalrpop.psi.ext.selected
import org.rust.ide.hints.type.RsTypeHintsPresentationFactory
import org.rust.lang.core.psi.ext.endOffset
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class LpExpressionTypeInlayHintProvider : InlayHintsProvider<LpExpressionTypeInlayHintProvider.Settings> {
    data class Settings(
        var showForNonterminals: Boolean = true,
        var showForSymbols: Boolean = false,
        var ignoreUnselectedSymbols: Boolean = true,
        var alternativeActionSymbols: Boolean = true,
    )

    override val key: SettingsKey<Settings>
        get() = KEY

    override val name: String
        get() = "Type hints"

    override val previewText: String? = null

    override fun createConfigurable(settings: Settings): ImmediateConfigurable = object : ImmediateConfigurable {
        override val cases: List<Case>
            get() = listOf(
                Case("Show for nonterminals", "nonterminals", settings::showForNonterminals),
                Case("Show for symbols", "symbols", settings::showForSymbols),
                Case("Ignore unselected symbols", "unselected_symbols", settings::ignoreUnselectedSymbols),
                Case(
                    "Only show for symbols where the alternative has an action",
                    "alternative_action_symbols",
                    settings::alternativeActionSymbols
                )
            )

        override fun createComponent(listener: ChangeListener): JComponent = JPanel()
    }

    override fun createSettings(): Settings = Settings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Settings,
        sink: InlayHintsSink
    ): InlayHintsCollector = object : FactoryInlayHintsCollector(editor) {
        val typeHintsPresentationFactory = RsTypeHintsPresentationFactory(factory, false)

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if (file.project.service<DumbService>().isDumb) return true

            if (element is LpNonterminal) presentTypeForNonterminal(element)
            if (element is LpAlternative) presentTypesForSymbols(element)

            return true
        }

        private fun presentTypesForSymbols(alternative: LpAlternative) {
            if (!settings.showForSymbols) return
            if (settings.alternativeActionSymbols && alternative.action == null) return
            (if (settings.ignoreUnselectedSymbols) alternative.selected else alternative.symbolList).forEach(this::presentTypeForSymbol)
        }

        private fun presentType(nonterminal: LpNonterminal, element: LpResolveType, anchor: PsiElement) {
            val type =
                element.getContextAndResolveType(LpMacroArguments.identity(nonterminal.nonterminalName.nonterminalParams))

            val modDecl = findModuleDefinition(element.project, element.containingFile) ?: return

            val rustType = type.lalrpopRustType(
                element.project,
                element.containingFile.importCode,
                nonterminal,
                modDecl
            ) ?: return

            val inlayPresentation = typeHintsPresentationFactory.typeHint(rustType)

            sink.addInlineElement(anchor.endOffset, false, inlayPresentation, false)
        }

        private fun presentTypeForSymbol(element: LpSymbol) {
            if (!settings.showForSymbols) return
            val nonterminal = element.parentOfType<LpNonterminal>()!!
            presentType(nonterminal, element, element.symbol0)
        }

        private fun presentTypeForNonterminal(element: LpNonterminal) {
            if (element.typeRef != null) return
            if (!settings.showForNonterminals) return

            presentType(element, element, element.nonterminalName)
        }
    }

    companion object {
        private val KEY: SettingsKey<Settings> = SettingsKey("lalrpop.type.hints")
    }
}
