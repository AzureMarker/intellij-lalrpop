package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.isUserAction

class RedundantActionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : LpVisitor() {
            override fun visitAction(action: LpAction) {
                if (!action.actionType.isUserAction) return

                var actionCode = action.code.text.trim()

                // remove ( and )
                while (actionCode.startsWith("(") && actionCode.endsWith(")")) {
                    actionCode = actionCode.substring(1, actionCode.length - 1).trim()
                }

                if (actionCode == "<>") {
                    holder.registerProblem(action, "Redundant action", RemoveActionQuickFix)
                }
            }
        }
}

object RemoveActionQuickFix: LocalQuickFix {
    override fun getFamilyName(): String = "Remove action"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        descriptor.psiElement.delete()
    }
}
