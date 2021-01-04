package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam
import com.mdrobnak.lalrpop.psi.ext.nonterminalParent

class UnusedInspection : LocalInspectionTool() {
    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        file.children.filterIsInstance<LpNonterminal>().filter {
            // ignore nonterminals visible from the outside
            it.visibility == null
        }.forEach {
            if (!ReferencesSearch.search(it.nonterminalName).any()) {
                holder.registerProblem(it.nonterminalName, "Unused nonterminal", DeleteNonterminal)
            }
            it.nonterminalName.nonterminalParams?.nonterminalParamList?.forEach { param ->
                if (!ReferencesSearch.search(param).any()) {
                    holder.registerProblem(param, "Unused macro parameter", DeleteMacroParameter)
                }
            }
        }

        return holder.resultsArray
    }
}

object DeleteNonterminal : LocalQuickFix {
    override fun getFamilyName(): String = "Delete"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) =
        (descriptor.psiElement as LpNonterminalName).nonterminalParent.delete()
}

object DeleteMacroParameter : LocalQuickFix {
    override fun getFamilyName(): String = "Delete macro parameter"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) =
        (descriptor.psiElement as LpNonterminalParam).delete()
}
