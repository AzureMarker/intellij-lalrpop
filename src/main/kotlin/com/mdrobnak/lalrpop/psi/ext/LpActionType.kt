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

val LpActionType.isUserAction: Boolean
    get() = userAction != null

/**
 * The type an action of this type returns, given the nonterminal type.
 *
 * @param nonterminalType The type of the nonterminal this action belongs to.
 * @param context The type resolution context of this file.
 *
 * @return String-version of the rust type
 */
fun LpActionType.returnType(nonterminalType: String, context: LpTypeResolutionContext): String =
    when (childrenWithLeaves.first().elementType) {
        LpElementTypes.USER_ACTION, LpElementTypes.LOOKAHEAD_ACTION, LpElementTypes.LOOKBEHIND_ACTION -> nonterminalType
        LpElementTypes.FALLIBLE_ACTION -> "::core::result::Result<$nonterminalType, ${context.parseError}>"
        else -> throw IllegalStateException("Child other than =>, =>@L, =>@R, or =>? in an action_type rule")
    }

/**
 * The inverse of LpActionType.returnType. Given the return type of the action code, return the type the nonterminal has to be.
 *
 * @param ty Inferred type of action code
 *
 * @return The type the nonterminal has to be
 *
 * @see LpActionType.returnType
 */
fun LpActionType.nonterminalTypeFromReturn(ty: Ty): Ty = when (childrenWithLeaves.first().elementType) {
    LpElementTypes.USER_ACTION, LpElementTypes.LOOKAHEAD_ACTION, LpElementTypes.LOOKBEHIND_ACTION -> ty
    LpElementTypes.FALLIBLE_ACTION ->
        ty.takeIf { (it as? TyAdt)?.item?.qualifiedName == "core::result::Result" }
            ?.typeParameterValues?.typeParameterByName("T")
            ?: error("Inferred type from fallible action code(${ty.render()}) is not Result")
    else -> throw IllegalStateException("Child other than =>, =>@L, =>@R, or =>? in an action_type rule")
}