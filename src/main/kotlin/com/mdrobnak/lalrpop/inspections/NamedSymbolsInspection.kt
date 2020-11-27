package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.ext.isExplicitlySelected
import com.mdrobnak.lalrpop.psi.ext.isNamed
import com.mdrobnak.lalrpop.psi.ext.removeName

/**
 * Inspection related to where named symbols can appear.
 * Suggests removing the name if there are issues.
 */
object NamedSymbolsInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is LpSymbol && element.isNamed) {
                    val parent = PsiTreeUtil.findFirstParent(element) {
                        it is LpAlternative || it is LpExprSymbol || it is LpNonterminalArguments || it is LpTypeOfSymbol
                    }
                    if (parent is LpAlternative) {
                        // if there is at least one explicitly selected, unnamed symbol
                        if (parent.symbolList.any { it.isExplicitlySelected && !it.isNamed }) {
                            // then it is an error
                            holder.registerProblem(
                                element,
                                "Usage of named symbol in an alternative where an unnamed symbol was also used",
                                ProblemHighlightType.ERROR,
                                RemoveNameQuickFix
                            )
                        }
                    } else {
                        holder.registerProblem(
                            element,
                            "Usage of named symbol in a context that doesn't allow it",
                            ProblemHighlightType.ERROR,
                            RemoveNameQuickFix
                        )
                    }
                }
            }
        }
    }
}

object RemoveNameQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return "Remove name"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        (descriptor.psiElement as LpSymbol).removeName()
    }
}