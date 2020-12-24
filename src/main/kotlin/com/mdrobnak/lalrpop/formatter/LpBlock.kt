package com.mdrobnak.lalrpop.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.mdrobnak.lalrpop.psi.LpElementTypes
import java.util.*


class LpBlock(
    node: ASTNode, wrap: Wrap?, alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {
    override fun buildChildren(): List<Block> {
        if (isLeaf) return listOf()
        val blocks: MutableList<Block> = ArrayList()
        val wraps = hashMapOf<IElementType, Wrap>()
        val alignments = mapOf(
            TokenSet.create(LpElementTypes.SHEBANG_ATTRIBUTE, LpElementTypes.USE_STMT) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.ALTERNATIVE) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.GRAMMAR_PARAM) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.GRAMMAR_WHERE_CLAUSE) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.GRAMMAR_WHERE_CLAUSES) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.MATCH_TOKEN) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.CONVERSION) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.ASSOCIATED_TYPE, LpElementTypes.ENUM_TOKEN) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.ANNOTATION) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.WHERE) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.NONTERMINAL) to Alignment.createAlignment(),
//            TokenSet.create(LpElementTypes.EQUALS) to Alignment.createAlignment(),
//            TokenSet.create(LpElementTypes.LBRACE) to Alignment.createAlignment(),
//            TokenSet.create(LpElementTypes.LPAREN) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.EXTERN) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.ENUM) to Alignment.createAlignment(),
            TokenSet.create(LpElementTypes.GRAMMAR) to Alignment.createAlignment(),
        )
        var child = myNode.firstChildNode
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                val block: Block = LpBlock(
                    child,
                    wraps.computeIfAbsent(child.elementType) {
                        when (it) {
                            LpElementTypes.GRAMMAR_PARAM, LpElementTypes.GRAMMAR_WHERE_CLAUSE -> Wrap.createWrap(
                                WrapType.CHOP_DOWN_IF_LONG,
                                true
                            )
                            else -> Wrap.createWrap(WrapType.NONE, false)
                        }
                    },
                    alignments.entries.find { it.key.contains(child.elementType) }?.value,
                    spacingBuilder
                )
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent = when (node.elementType) {
        LpElementTypes.MATCH_ITEM, LpElementTypes.ASSOCIATED_TYPE, LpElementTypes.ENUM_TOKEN,
        LpElementTypes.CODE, LpElementTypes.CONVERSION, LpElementTypes.ALTERNATIVE,
        LpElementTypes.GRAMMAR_PARAM, LpElementTypes.GRAMMAR_WHERE_CLAUSE -> Indent.getNormalIndent()
        else -> Indent.getNoneIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }
}