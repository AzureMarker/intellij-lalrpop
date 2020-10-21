package com.mdrobnak.lalrpop.psi.util

import com.mdrobnak.lalrpop.psi.LpGrammarParam

class LpPsiUtil {
    companion object {
        @JvmStatic
        fun getName(grammarParam: LpGrammarParam): String = grammarParam.firstChild.text
    }
}