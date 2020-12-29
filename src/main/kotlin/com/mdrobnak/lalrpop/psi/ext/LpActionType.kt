package com.mdrobnak.lalrpop.psi.ext

import com.intellij.psi.util.elementType
import com.mdrobnak.lalrpop.psi.LpActionType
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import org.rust.ide.presentation.render
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.qualifiedName
import org.rust.lang.core.types.ty.Ty
import org.rust.lang.core.types.ty.TyAdt

fun LpActionType.returnType(nonterminalType: String, context: LpTypeResolutionContext): String {
    return when (this.childrenWithLeaves.first().elementType) {
        LpElementTypes.USER_ACTION, LpElementTypes.LOOKAHEAD_ACTION, LpElementTypes.LOOKBEHIND_ACTION -> nonterminalType
        LpElementTypes.FALLIBLE_ACTION -> "::std::result::Result<$nonterminalType, ${context.parseError}>"
        else -> throw IllegalStateException("Child other than =>, =>@L, =>@R, or =>? in an action_type rule")
    }
}

fun LpActionType.nonterminalTypeFromReturn(ty: Ty): Ty {
    return when (this.childrenWithLeaves.first().elementType) {
        LpElementTypes.USER_ACTION, LpElementTypes.LOOKAHEAD_ACTION, LpElementTypes.LOOKBEHIND_ACTION -> ty
        LpElementTypes.FALLIBLE_ACTION ->
            ty.typeParameterValues.let {
                if ((ty as? TyAdt)?.item?.qualifiedName != "core::result::Result") {
                    error("Inferred type from fallible action code(${ty.render()}) is not Result")
                }
                it.typeByName("T")
            }
        else -> throw IllegalStateException("Child other than =>, =>@L, =>@R, or =>? in an action_type rule")
    }
}