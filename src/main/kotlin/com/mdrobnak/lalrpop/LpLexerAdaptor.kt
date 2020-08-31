package com.mdrobnak.lalrpop

import com.intellij.lexer.FlexAdapter
import com.mdrobnak.lalrpop.lexer.LalrpopLexer

class LpLexerAdaptor : FlexAdapter(LalrpopLexer())