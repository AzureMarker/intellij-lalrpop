package com.mdrobnak.lalrpop.psi.util

import com.mdrobnak.lalrpop.psi.LpGrammarParam

fun LpGrammarParam.getName(): String = this.firstChild.text