package com.mdrobnak.lalrpop.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.mdrobnak.lalrpop.psi.LpElementTypes
import java.util.*

class LpBlock(
    node: ASTNode, wrap: Wrap?, alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {
    override fun buildChildren(): List<Block> {
        val blocks: MutableList<Block> = ArrayList()
        var child = myNode.firstChildNode
        while (child != null) {
            val block: Block? = when (child.elementType) {
                LpElementTypes.SHEBANG_ATTRIBUTE, LpElementTypes.USE_STMT, LpElementTypes.GRAMMAR_DECL, LpElementTypes.GRAMMAR_ITEM,
                LpElementTypes.VISIBILITY, LpElementTypes.GRAMMAR_TYPE_PARAMS, LpElementTypes.TYPE_PARAM, LpElementTypes.FORALL,
                LpElementTypes.MATCH_TOKEN, LpElementTypes.EXTERN_TOKEN, LpElementTypes.NONTERMINAL -> {
                    LpBlock(child, null, null, spacingBuilder)
                }
                LpElementTypes.GRAMMAR_WHERE_CLAUSES -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, true),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.GRAMMAR_PARAMS -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.GRAMMAR_PARAM -> {
                    LpBlock(child, Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true), null, spacingBuilder)
                }
                LpElementTypes.TYPE_REF, LpElementTypes.NONTERMINAL_NAME, LpElementTypes.NONTERMINAL_REF, LpElementTypes.PATH,
                LpElementTypes.PATH_REF, LpElementTypes.SYMBOL, LpElementTypes.SYMBOL_0, LpElementTypes.SYMBOL_1, LpElementTypes.EXPR_SYMBOL,
                LpElementTypes.REPEAT_OP -> {
                    LpBlock(child, null, null, spacingBuilder)
                }
                LpElementTypes.TYPE_REF_OR_LIFETIME -> {
                    LpBlock(child.firstChildNode, null, null, spacingBuilder)
                }
                LpElementTypes.GRAMMAR_WHERE_CLAUSE -> {
                    LpBlock(child, Wrap.createWrap(WrapType.NONE, true), null, spacingBuilder)
                }
                LpElementTypes.TYPE_BOUNDS -> {
                    LpBlock(child, Wrap.createWrap(WrapType.NONE, false), null, spacingBuilder)
                }
                LpElementTypes.TYPE_BOUND, LpElementTypes.TYPE_BOUND_PARAM -> {
                    LpBlock(child, Wrap.createWrap(WrapType.NONE, false), null, spacingBuilder)
                }
                LpElementTypes.ENUM_TOKEN -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, true),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.ASSOCIATED_TYPE -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, false),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.MATCH_ITEM, LpElementTypes.CONVERSION, LpElementTypes.TERMINAL -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, true),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.ALTERNATIVES, LpElementTypes.EXTERN_CONTENTS, LpElementTypes.MATCH_CONTENTS -> {
                    LpBlock(child, Wrap.createWrap(WrapType.ALWAYS, false), null, spacingBuilder)
                }
                LpElementTypes.ALTERNATIVE -> {
                    LpBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, true),
                        null,
                        spacingBuilder
                    )
                }
                LpElementTypes.COMMENT -> LpBlock(child, null, null, spacingBuilder)
                /// match all single tokens here
                LpElementTypes.EXTERN, LpElementTypes.MATCH, LpElementTypes.ENUM, LpElementTypes.GRAMMAR,
                LpElementTypes.PUB, LpElementTypes.USE, LpElementTypes.DYN, LpElementTypes.MUT,
                LpElementTypes.IF, LpElementTypes.ELSE, LpElementTypes.FOR, LpElementTypes.WHERE,
                LpElementTypes.LBRACE, LpElementTypes.RBRACE,
                LpElementTypes.LBRACKET, LpElementTypes.RBRACKET,
                LpElementTypes.LPAREN, LpElementTypes.RPAREN,
                LpElementTypes.GREATERTHAN, LpElementTypes.LESSTHAN,
                LpElementTypes.IMPORT_CODE,
                LpElementTypes.QUESTION, LpElementTypes.MULTIPLY, LpElementTypes.PLUS,
                LpElementTypes.AND, LpElementTypes.NOT, LpElementTypes.POUND,
                LpElementTypes.ANNOTATION, LpElementTypes.ACTION,
                LpElementTypes.COLON, LpElementTypes.COMMA, LpElementTypes.SEMICOLON,
                LpElementTypes.CODE, LpElementTypes.TYPE,
                LpElementTypes.ID, LpElementTypes.LIFETIME,
                LpElementTypes.PATH_REF,
                LpElementTypes.LOOKAHEAD, LpElementTypes.LOOKAHEAD_ACTION,
                LpElementTypes.LOOKBEHIND, LpElementTypes.LOOKBEHIND_ACTION,
                LpElementTypes.USER_ACTION, LpElementTypes.FALLIBLE_ACTION,
                LpElementTypes.RSINGLEARROW, LpElementTypes.UNDERSCORE,
                LpElementTypes.EQUALS, LpElementTypes.EQUALS_EQUALS, LpElementTypes.NOT_EQUALS,
                LpElementTypes.MATCH_OP, LpElementTypes.NOT_MATCH_OP, LpElementTypes.MATCH_SYMBOL,
                LpElementTypes.QUOTED_LITERAL, LpElementTypes.QUOTED_TERMINAL, LpElementTypes.STR_LITERAL,
                -> {
                    LpBlock(child, null, null, spacingBuilder)
                }
                else -> null
            }
            if (block != null) blocks.add(block)
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent = when (node.elementType) {
        LpElementTypes.ALTERNATIVE, LpElementTypes.MATCH_ITEM, LpElementTypes.ASSOCIATED_TYPE, LpElementTypes.ENUM_TOKEN,
        LpElementTypes.GRAMMAR_WHERE_CLAUSE, LpElementTypes.CODE,
        LpElementTypes.GRAMMAR_PARAM, LpElementTypes.CONVERSION -> Indent.getNormalIndent()
        LpElementTypes.GRAMMAR -> Indent.getAbsoluteNoneIndent()
        else -> Indent.getNoneIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }
}