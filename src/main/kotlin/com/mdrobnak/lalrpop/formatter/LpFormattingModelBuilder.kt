package com.mdrobnak.lalrpop.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.LpLanguage
import com.mdrobnak.lalrpop.psi.LpElementTypes

class LpFormattingModelBuilder : FormattingModelBuilder {
    @Suppress("UnstableApiUsage")
    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                element.containingFile,
                LpBlock(
                    element.node,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    createSpaceBuilder(settings)
                ),
                settings
            )
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange? = null

    companion object {
        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val commonSettings = settings.getCommonSettings(LpLanguage.id)
            return SpacingBuilder(settings, LpLanguage)
                .around(LpElementTypes.EQUALS).spaceIf(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
                .around(TokenSet.create(LpElementTypes.EQUALS_EQUALS, LpElementTypes.NOT_EQUALS)).spaceIf(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS)
                .before(LpElementTypes.GRAMMAR_DECL).blankLines(1)
                .after(LpElementTypes.GRAMMAR_DECL).blankLines(1)
                .before(LpElementTypes.NONTERMINAL).blankLines(1)
                .after(LpElementTypes.NONTERMINAL).blankLines(1)
                .before(LpElementTypes.MATCH_TOKEN).blankLines(1)
                .after(LpElementTypes.MATCH_TOKEN).blankLines(1)
                .before(LpElementTypes.EXTERN_TOKEN).blankLines(1)
                .after(LpElementTypes.EXTERN_TOKEN).blankLines(1)
                .before(LpElementTypes.COLON).none()
                .after(LpElementTypes.COLON).spaces(1)
                .after(LpElementTypes.MATCH).spaces(1)
                .after(LpElementTypes.COMMA).spaces(1)
                .after(LpElementTypes.LPAREN).spaces(0)
                .before(LpElementTypes.RPAREN).spaces(0)
                .around(LpElementTypes.SYMBOL).spaces(1)
        }
    }
}

