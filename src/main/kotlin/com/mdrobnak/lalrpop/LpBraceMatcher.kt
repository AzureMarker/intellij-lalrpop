package com.mdrobnak.lalrpop

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.mdrobnak.lalrpop.psi.*
import org.rust.lang.core.psi.ext.startOffset

object LpBraceMatcher : PairedBraceMatcher {
    private val pairsArray = arrayOf(
        BracePair(LpElementTypes.LBRACE, LpElementTypes.RBRACE, true),
        BracePair(LpElementTypes.LBRACKET, LpElementTypes.RBRACKET, false),
        BracePair(LpElementTypes.LPAREN, LpElementTypes.RPAREN, false),
        BracePair(LpElementTypes.LESSTHAN, LpElementTypes.GREATERTHAN, false),
    )

    override fun getPairs(): Array<BracePair> = pairsArray

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        // walk up the tree and look for a LpGrammarItem or an LpEnumToken
        // which cover the only constructs(aside action code) in a lalrpop file that may have braces.
        var element = file.findElementAt(openingBraceOffset)
        while (element != null) {
            when (element) {
                is LpMatchToken, is LpNonterminal, is LpExternToken, is LpEnumToken -> return element.startOffset
            }

            element = element.parent
        }
        return openingBraceOffset
    }
}