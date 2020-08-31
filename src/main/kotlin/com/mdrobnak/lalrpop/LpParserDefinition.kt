package com.mdrobnak.lalrpop

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.parser.LalrpopParser
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpFile

class LpParserDefinition : ParserDefinition {
    companion object {
        private val COMMENTS = TokenSet.create(LpElementTypes.COMMENT)
        private val STR_LITERALS = TokenSet.create(LpElementTypes.STR_LITERAL)
        private val FILE = IFileElementType(LpLanguage)
    }

    override fun createLexer(project: Project?): Lexer = LpLexerAdaptor()

    override fun createParser(project: Project?): PsiParser = LalrpopParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STR_LITERALS

    override fun createElement(node: ASTNode?): PsiElement = LpElementTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LpFile(viewProvider)
}