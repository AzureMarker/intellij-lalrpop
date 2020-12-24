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
    // FIXME: Replace with createModel(FormattingContext) (added in 2020.3)
    //        when we drop support for 2020.2
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
                .around(TokenSet.create(LpElementTypes.EXTERN_TOKEN, LpElementTypes.MATCH_TOKEN, LpElementTypes.NONTERMINAL, LpElementTypes.GRAMMAR_DECL)).blankLines(1)
                .before(LpElementTypes.COLON).none()
                .after(LpElementTypes.COLON).spaces(1)
                .after(LpElementTypes.MATCH).spaces(1)
                .before(LpElementTypes.COMMA).none()
                .after(LpElementTypes.COMMA).spaces(1)
                .after(LpElementTypes.LPAREN).spaces(0)
                .before(LpElementTypes.RPAREN).spaces(0)
                .afterInside(LpElementTypes.LESSTHAN, LpElementTypes.TYPE_REF).none()
                .beforeInside(LpElementTypes.GREATERTHAN, LpElementTypes.TYPE_REF).none()
                .afterInside(LpElementTypes.LESSTHAN, LpElementTypes.NONTERMINAL_ARGUMENTS).none()
                .beforeInside(LpElementTypes.GREATERTHAN, LpElementTypes.NONTERMINAL_ARGUMENTS).none()
                .around(LpElementTypes.SYMBOL).spaces(1)
        }
    }
}

