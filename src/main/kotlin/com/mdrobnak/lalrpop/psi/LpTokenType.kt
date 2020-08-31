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
    LpElementTypes.TYPE
)

val LP_OPERATORS = TokenSet.create(
    LpElementTypes.EQUALS_EQUALS,
    LpElementTypes.NOT_EQUALS,
    LpElementTypes.MATCH_OP,
    LpElementTypes.NOT_MATCH_OP,
    LpElementTypes.PLUS,
    LpElementTypes.MULTIPLY,
    LpElementTypes.QUESTION
)
