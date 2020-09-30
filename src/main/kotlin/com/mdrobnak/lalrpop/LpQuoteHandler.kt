package com.mdrobnak.lalrpop

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.mdrobnak.lalrpop.psi.LpElementTypes

class LpQuoteHandler : SimpleTokenSetQuoteHandler(LpElementTypes.STR_LITERAL)