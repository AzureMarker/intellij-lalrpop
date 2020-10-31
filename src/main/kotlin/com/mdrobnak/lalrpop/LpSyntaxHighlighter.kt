package com.mdrobnak.lalrpop

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.mdrobnak.lalrpop.psi.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default

enum class LpColor(default: TextAttributesKey) {
    LINE_COMMENT(Default.LINE_COMMENT),
    ANNOTATION(Default.METADATA),
    BRACES(Default.BRACES),
    BRACKETS(Default.BRACKETS),
    PARENTHESIS(Default.PARENTHESES),
    OPERATION_SIGN(Default.OPERATION_SIGN),
    KEYWORD(Default.KEYWORD),
    SEMICOLON(Default.SEMICOLON),
    IDENTIFIER(Default.IDENTIFIER),
    NONTERMINAL_GENERIC_PARAMETER(Default.PARAMETER),
    NONTERMINAL_NAME_IN_DECLARATION(Default.FUNCTION_DECLARATION),
    NONTERMINAL_REFERENCE(Default.FUNCTION_CALL),
    PREDEFINED_SYMBOLS(Default.PREDEFINED_SYMBOL),
    PARAMETER(Default.PARAMETER),
    PATH(Default.CLASS_REFERENCE),
    RUST_PRIMITIVE_TYPES(Default.KEYWORD),
    STRING(Default.STRING);

    val textAttributesKey = TextAttributesKey.createTextAttributesKey(name, default)
}

class LpSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        fun map(tokenType: IElementType?): LpColor? =
            when (tokenType) {
                LpElementTypes.ID -> LpColor.IDENTIFIER
                LpElementTypes.LIFETIME -> LpColor.IDENTIFIER
                LpElementTypes.COMMENT -> LpColor.LINE_COMMENT
                LpElementTypes.LBRACE, LpElementTypes.RBRACE -> LpColor.BRACES
                LpElementTypes.LBRACKET, LpElementTypes.RBRACKET -> LpColor.BRACKETS
                LpElementTypes.LPAREN, LpElementTypes.RPAREN -> LpColor.PARENTHESIS
                LpElementTypes.SEMICOLON -> LpColor.SEMICOLON
                LpElementTypes.STR_LITERAL, LpElementTypes.REGEX_LITERAL -> LpColor.STRING
                in LP_OPERATORS -> LpColor.OPERATION_SIGN
                in LP_PREDEFINED_SYMBOLS -> LpColor.PREDEFINED_SYMBOLS
                in LP_KEYWORDS -> LpColor.KEYWORD
                in RUST_PRIMITIVE_TYPES -> LpColor.RUST_PRIMITIVE_TYPES
                else -> null
            }
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        pack(map(tokenType)?.textAttributesKey)

    override fun getHighlightingLexer(): Lexer = LpLexerAdaptor()
}
