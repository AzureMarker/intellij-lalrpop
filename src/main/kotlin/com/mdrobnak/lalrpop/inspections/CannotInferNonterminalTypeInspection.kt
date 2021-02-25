package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.setType
import com.mdrobnak.lalrpop.psi.getContextAndResolveType

object CannotInferNonterminalTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : LpVisitor() {
        override fun visitNonterminal(nonterminal: LpNonterminal) {
            if (nonterminal.typeRef == null &&
                nonterminal.alternatives.alternativeList.isNotEmpty() &&
                nonterminal.alternatives.alternativeList.all { it.action != null }
            ) {
                holder.registerProblem(
                    nonterminal,
                    "Cannot infer type of nonterminal",
                    InferFromRustPluginQuickFix,
                )
            }
        }
    }
}

object InferFromRustPluginQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Get from action code"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        (descriptor.psiElement as LpNonterminal).apply {
            setType(getContextAndResolveType(LpMacroArguments.identity(nonterminalName.nonterminalParams)))
        }
    }
}
