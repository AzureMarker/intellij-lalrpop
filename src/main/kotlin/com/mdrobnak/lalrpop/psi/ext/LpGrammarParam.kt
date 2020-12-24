package com.mdrobnak.lalrpop.psi.ext

import com.mdrobnak.lalrpop.psi.LpGrammarParam

val LpGrammarParam.name: String
    get() = this.firstChild.text