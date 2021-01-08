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
    class Settings(
        var showForNonterminals: Boolean = true,
        var showForSymbols: Boolean = true,
        var ignoreUnselectedSymbols: Boolean = true,
        var alternativeActionSymbols: Boolean = true,
    )

    /**
     * Used for persistance of settings
     */
    override val key: SettingsKey<Settings>
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
        get() = """
            // preview doesn't work because the types
            // need context about the rust project around
            // them, which this preview is not able to provide
            grammar;
            
            Comma<Rule> = (<Rule> ",")+ Rule?;
            
            FunctionArgs = Comma<Expression>;
            
            Expression = r"[0-9]+";
        """.trimIndent()

    /**
     * Creates configurable, that immediately applies changes from UI to [settings]
     */
    override fun createConfigurable(settings: Settings): ImmediateConfigurable = object : ImmediateConfigurable {
        override val cases: List<Case>
            get() = listOf(
                Case("Show for nonterminals", "nonterminals", settings::showForNonterminals),
                Case("Show for symbols", "symbols", settings::showForSymbols),
                Case("Ignore unselected symbols", "unselected_sybmols", settings::ignoreUnselectedSymbols),
                Case(
                    "Only show for symbols where the alternative has an action",
                    "alternative_action_symbols",
                    settings::alternativeActionSymbols
                )
            )

        /**
         * Creates component, which listen to its components and immediately updates state of settings object
         * This is required to make preview in settings works instantly
         * Note, that if you need to express only cases of this provider, you should use [cases] instead
         */
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

            @Suppress("Deprecation")
            sink.addInlineElement(anchor.endOffset, false, inlayPresentation)
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
