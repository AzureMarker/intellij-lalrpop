package com.mdrobnak.lalrpop.psi

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.LalrpopLanguage

class LalrpopTokenType(debugName: String) : IElementType(debugName, LalrpopLanguage) {
    override fun toString(): String {
        return "LALRPOP." + super.toString()
    }
}

val LALRPOP_KEYWORDS = TokenSet.create(
    LalrpopTypes.GRAMMAR,
    LalrpopTypes.USE,
    LalrpopTypes.PUB,
    LalrpopTypes.IF,
    LalrpopTypes.MUT,
    LalrpopTypes.DYN,
    LalrpopTypes.EXTERN,
    LalrpopTypes.MATCH,
    LalrpopTypes.ELSE,
    LalrpopTypes.ENUM,
    LalrpopTypes.TYPE
)

val LALRPOP_OPERATORS = TokenSet.create(
    LalrpopTypes.EQUALS_EQUALS,
    LalrpopTypes.NOT_EQUALS,
    LalrpopTypes.MATCH_OP,
    LalrpopTypes.NOT_MATCH_OP,
    LalrpopTypes.PLUS,
    LalrpopTypes.MULTIPLY,
    LalrpopTypes.QUESTION
)
