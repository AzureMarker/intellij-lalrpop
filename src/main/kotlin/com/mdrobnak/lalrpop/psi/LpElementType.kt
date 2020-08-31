package com.mdrobnak.lalrpop.psi

import com.intellij.psi.tree.IElementType
import com.mdrobnak.lalrpop.LpLanguage

class LpElementType(debugName: String) : IElementType(debugName, LpLanguage)