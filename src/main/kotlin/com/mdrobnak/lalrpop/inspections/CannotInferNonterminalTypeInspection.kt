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
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor = object : LpVisitor() {
        override fun visitNonterminal(nonterminal: LpNonterminal) {
            if (nonterminal.typeRef == null &&
                nonterminal.alternatives.alternativeList.isNotEmpty() &&
                nonterminal.alternatives.alternativeList.all { it.action != null }
            ) {
                // Try to resolve the type
                val type = nonterminal.getContextAndResolveType(
                    LpMacroArguments.identity(nonterminal.nonterminalName.nonterminalParams)
                )

                if (type == "_") {
                    // IntelliJ-Rust was unable to resolve the type, so don't
                    // suggest the quick fix.
                    return
                }

                holder.registerProblem(
                    nonterminal,
                    "Cannot infer type of nonterminal",
                    InferFromRustPluginQuickFix(type),
                )
            }
        }
    }
}

class InferFromRustPluginQuickFix(val type: String) : LocalQuickFix {
    override fun getFamilyName(): String = "Get from action code"

    override fun getName(): String = "Get from action code ($type)"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        (descriptor.psiElement as LpNonterminal).setType(type)
    }
}
