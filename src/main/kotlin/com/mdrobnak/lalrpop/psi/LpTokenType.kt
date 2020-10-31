package com.mdrobnak.lalrpop.psi

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.LpLanguage

class LpTokenType(debugName: String) : IElementType(debugName, LpLanguage) {
    override fun toString(): String {
        return "LALRPOP." + super.toString()
    }
}

val LP_KEYWORDS = TokenSet.create(
    LpElementTypes.GRAMMAR,
    LpElementTypes.USE,
    LpElementTypes.PUB,
    LpElementTypes.IF,
    LpElementTypes.MUT,
    LpElementTypes.DYN,
    LpElementTypes.EXTERN,
    LpElementTypes.MATCH,
    LpElementTypes.ELSE,
    LpElementTypes.ENUM,
    LpElementTypes.TYPE,
    LpElementTypes.WHERE,
)

val LP_OPERATORS = TokenSet.create(
    LpElementTypes.COLON,
    LpElementTypes.SEMICOLON,
    LpElementTypes.COMMA,
    LpElementTypes.EQUALS_EQUALS,
    LpElementTypes.NOT_EQUALS,
    LpElementTypes.EQUALS,
    LpElementTypes.MATCH_OP,
    LpElementTypes.NOT_MATCH_OP,
    LpElementTypes.LESSTHAN,
    LpElementTypes.GREATERTHAN,
    LpElementTypes.RSINGLEARROW,
    LpElementTypes.LOOKAHEAD_ACTION,
    LpElementTypes.LOOKBEHIND_ACTION,
    LpElementTypes.FALLIBLE_ACTION,
    LpElementTypes.USER_ACTION,
    LpElementTypes.PLUS,
    LpElementTypes.MULTIPLY,
    LpElementTypes.QUESTION,
    LpElementTypes.NOT,
    LpElementTypes.AND,
)

val LP_PREDEFINED_SYMBOLS = TokenSet.create(
    LpElementTypes.LOOKAHEAD,
    LpElementTypes.LOOKBEHIND,
)
