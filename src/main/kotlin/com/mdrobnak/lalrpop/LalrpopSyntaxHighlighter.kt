package com.mdrobnak.lalrpop

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.mdrobnak.lalrpop.psi.LALRPOP_KEYWORDS
import com.mdrobnak.lalrpop.psi.LALRPOP_OPERATORS
import com.mdrobnak.lalrpop.psi.LalrpopTypes
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default

enum class LalrpopColor(default: TextAttributesKey) {
    LINE_COMMENT(Default.LINE_COMMENT),
    BRACES(Default.BRACES),
    BRACKETS(Default.BRACKETS),
    PARENTHESIS(Default.PARENTHESES),
    OPERATION_SIGN(Default.OPERATION_SIGN),
    KEYWORD(Default.KEYWORD),
    SEMICOLON(Default.SEMICOLON),
    IDENTIFIER(Default.IDENTIFIER),
    STRING(Default.STRING);

    val textAttributesKey = TextAttributesKey.createTextAttributesKey(name, default)
}

class LalrpopSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        fun map(tokenType: IElementType?): LalrpopColor? =
            when (tokenType) {
                LalrpopTypes.ID -> LalrpopColor.IDENTIFIER
                LalrpopTypes.COMMENT -> LalrpopColor.LINE_COMMENT
                LalrpopTypes.LBRACE, LalrpopTypes.RBRACE -> LalrpopColor.BRACES
                LalrpopTypes.LBRACKET, LalrpopTypes.RBRACKET -> LalrpopColor.BRACKETS
                LalrpopTypes.LPAREN, LalrpopTypes.RPAREN -> LalrpopColor.PARENTHESIS
                LalrpopTypes.SEMICOLON -> LalrpopColor.SEMICOLON
                LalrpopTypes.STR_LITERAL -> LalrpopColor.STRING
                in LALRPOP_OPERATORS -> LalrpopColor.OPERATION_SIGN
                in LALRPOP_KEYWORDS -> LalrpopColor.KEYWORD
                else -> null
            }
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        pack(map(tokenType)?.textAttributesKey)

    override fun getHighlightingLexer(): Lexer = LalrpopLexerAdaptor()
}
