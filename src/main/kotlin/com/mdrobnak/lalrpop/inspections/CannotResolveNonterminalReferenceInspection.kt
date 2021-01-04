package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.addParam
import com.mdrobnak.lalrpop.psi.ext.arguments
import com.mdrobnak.lalrpop.psi.ext.createNonterminal

object CannotResolveNonterminalReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor = object : LpVisitor() {
        override fun visitNonterminalRef(nonterminalRef: LpNonterminalRef) {
            val ref = nonterminalRef.reference ?: return
            if (ref.resolve() != null) return
            if (nonterminalRef.arguments == null)
                holder.registerProblem(
                    nonterminalRef,
                    "Cannot resolve ${ref.canonicalText}",
                    AddToMacroParamsQuickFix,
                    CreateNonterminalQuickFix
                )
            else
                holder.registerProblem(
                    nonterminalRef,
                    "Cannot resolve ${ref.canonicalText}",
                    CreateNonterminalQuickFix
                )
        }
    }
}

object AddToMacroParamsQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Add to macro parameters"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) =
        descriptor.psiElement.parentOfType<LpNonterminal>()?.run {
            nonterminalName.addParam(descriptor.psiElement.text)
        } ?: Unit
}

object CreateNonterminalQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Create nonterminal"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) =
        (descriptor.psiElement as LpNonterminalRef).createNonterminal()
}
