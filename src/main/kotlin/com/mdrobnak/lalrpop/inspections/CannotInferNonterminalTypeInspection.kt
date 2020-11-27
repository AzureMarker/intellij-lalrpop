package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpVisitor

object CannotInferNonterminalTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : LpVisitor() {
            override fun visitNonterminal(nonterminal: LpNonterminal) {
                if (nonterminal.typeRef == null &&
                    nonterminal.alternatives.alternativeList.isNotEmpty() &&
                    nonterminal.alternatives.alternativeList.all { it.action != null }
                ) {
                    holder.registerProblem(
                        nonterminal,
                        "Cannot infer type of nonterminal",
                    ) // TODO Quickfix: get from the rust plugin if available.
                }
            }
        }
    }
}