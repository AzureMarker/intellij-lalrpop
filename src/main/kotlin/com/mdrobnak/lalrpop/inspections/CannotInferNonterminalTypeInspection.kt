package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpNonterminal

object CannotInferNonterminalTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is LpNonterminal && element.typeRef == null &&
                    element.alternatives.alternativeList.isNotEmpty() &&
                    element.alternatives.alternativeList.all { it.action != null }
                ) {
                    holder.registerProblem(
                        element,
                        "Cannot infer type of nonterminal",
                        ProblemHighlightType.ERROR
                    ) // TODO Quickfix: get from the rust plugin if available.
                }
            }
        }
    }
}