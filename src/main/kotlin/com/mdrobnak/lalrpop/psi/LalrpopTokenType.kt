package com.mdrobnak.lalrpop.psi

import com.intellij.psi.tree.IElementType
import com.mdrobnak.lalrpop.LalrpopLanguage

class LalrpopTokenType(debugName: String) : IElementType(debugName, LalrpopLanguage) {
    override fun toString(): String {
        return "LALRPOP." + super.toString()
    }
}