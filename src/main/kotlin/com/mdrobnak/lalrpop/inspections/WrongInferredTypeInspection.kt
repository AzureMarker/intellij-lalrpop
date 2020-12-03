package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpVisitor

/**
 * Reports 2 kinds of problems on a nonterminal
 * 1. The inferred type is different from the type declared explicitly on the nonterminal
 * 2. There are 2 different alternatives, without action code, where the inferred types are different
 */
object WrongInferredTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : LpVisitor() {
            override fun visitNonterminal(nonterminal: LpNonterminal) {
                val explicitType = nonterminal.typeRef?.resolveType(listOf())

                var seenType = explicitType

                nonterminal.alternatives.alternativeList.filter { it.action == null }.forEach { alternative ->
                    val alternativeType = alternative.resolveType(listOf())

                    if (seenType != null && seenType != alternativeType) { // TODO: maybe get the rust plugin to do this comparison because it may be very error-prone by just comparing strings
                        holder.registerProblem(alternative, "Resolved type of alternative is `$alternativeType`, while expected type `$seenType`")
                    }

                    if (seenType == null) {
                        seenType = alternativeType
                    }
                }
            }
        }
    }
}