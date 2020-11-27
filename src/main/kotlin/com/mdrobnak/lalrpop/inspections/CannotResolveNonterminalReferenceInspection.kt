package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.ext.addParam
import com.mdrobnak.lalrpop.psi.ext.arguments
import com.mdrobnak.lalrpop.psi.ext.createNonterminal

object CannotResolveNonterminalReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is LpNonterminalRef) {
                    val ref = element.reference
                    if (ref != null && ref.resolve() == null) {
                        if (element.arguments == null)
                            holder.registerProblem(
                                element,
                                "Cannot resolve ${ref.canonicalText}",
                                AddToMacroParamsQuickFix,
                                CreateNonterminalQuickFix
                            )
                        else
                            holder.registerProblem(
                                element,
                                "Cannot resolve ${ref.canonicalText}",
                                CreateNonterminalQuickFix
                            )
                    }
                }
            }
        }
    }
}

object AddToMacroParamsQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Add to macro parameters"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val nonterminal = descriptor.psiElement.parentOfType<LpNonterminal>() ?: return
        nonterminal.nonterminalName.addParam(descriptor.psiElement.text)
    }
}

object CreateNonterminalQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Create nonterminal"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        (descriptor.psiElement as LpNonterminalRef).createNonterminal()
    }
}
