package com.mdrobnak.lalrpop.psi.ext

import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpActionType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import org.rust.lang.core.psi.ext.childrenWithLeaves

fun LpActionType.returnType(nonterminalType: String, context: LpTypeResolutionContext): String {
    return when (this.childrenWithLeaves.first().elementType) {
        LpElementTypes.USER_ACTION, LpElementTypes.LOOKAHEAD_ACTION, LpElementTypes.LOOKBEHIND_ACTION -> nonterminalType
        LpElementTypes.FALLIBLE_ACTION -> "::std::result::Result<$nonterminalType, ${context.parseError()}>"
        else -> throw IllegalStateException("Child other than =>, =>@L, =>@R, or =>? in an action_type rule")
    }
}