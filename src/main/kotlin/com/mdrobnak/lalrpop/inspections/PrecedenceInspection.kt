package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpAlternatives
import com.mdrobnak.lalrpop.psi.LpAnnotation
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.findAnnotationByName

object PrecedenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : LpVisitor() {
        override fun visitAlternative(alternative: LpAlternative) {
            val precedenceAnnotationNullable = alternative.findAnnotationByName("precedence")
            val assocAnnotation = alternative.findAnnotationByName("assoc")

            precedenceAnnotationNullable?.let { precedenceAnnotation ->
                val arg = precedenceAnnotation.annotationArg

                if (arg == null || arg.annotationArgName.text != "level") {
                    holder.registerProblem(precedenceAnnotation, "Missing `level` arg on #[precedence]")
                } else {
                    val levelText = arg.annotationArgValue.text

                    val level = levelText.substring(1, levelText.length - 1) // remove the "
                        .toIntOrNull()

                    if (level == null) {
                        holder.registerProblem(arg.annotationArgValue, "Level value is not an int")
                    }

                    if (level == 1 && assocAnnotation != null) {
                        holder.registerProblem(
                            assocAnnotation,
                            """#[assoc] used on alternative with #[precedence(level="1")]"""
                        )
                    }
                }
            }

            if (assocAnnotation != null) {
                assocAnnotation.annotationArg.takeIf { it == null || it.annotationArgName.text != "side" }
                    ?.let {
                        holder.registerProblem(assocAnnotation, """Missing side="..." on #[assoc] annotation""")
                    }

                if (precedenceAnnotationNullable == null) {
                    holder.registerProblem(
                        assocAnnotation,
                        """#[assoc] annotation without #[precedence] annotation""",
                        DeleteAnnotationQuickFix("assoc")
                    )
                }
            }
        }

        override fun visitAlternatives(alternatives: LpAlternatives) {
            val (withPrecedence, withoutPrecedence) = alternatives.alternativeList.partition { it.findAnnotationByName("precedence") != null }

            if (withPrecedence.isNotEmpty()) {
                withoutPrecedence.forEach { alternative ->
                    holder.registerProblem(
                        alternative,
                        "Alternative without #[precedence] in a nonterminal that has a #[precedence] alternative"
                    )
                }
            }
        }
    }
}

class DeleteAnnotationQuickFix(private val annotationName: String) : LocalQuickFix {
    override fun getFamilyName(): String = "Delete #[$annotationName] annotation"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        (descriptor.psiElement as LpAnnotation).delete()
    }
}
