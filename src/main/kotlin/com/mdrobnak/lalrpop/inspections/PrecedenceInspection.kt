package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpAlternatives
import com.mdrobnak.lalrpop.psi.LpVisitor

object PrecedenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : LpVisitor() {
            override fun visitAlternatives(alternatives: LpAlternatives) {
                val alternativesWithoutPrecedenceAnnotation = mutableListOf<LpAlternative>()
                var hasPrecedence = false
                alternatives.alternativeList.forEach {
                    val precedenceAnnotation =
                        it.annotationList.find { annotation -> annotation.annotationName.text == "precedence" }
                    val assocAnnotation =
                        it.annotationList.find { annotation -> annotation.annotationName.text == "assoc" }

                    if (precedenceAnnotation != null) {
                        hasPrecedence = true
                        val arg = precedenceAnnotation.annotationArg

                        if (arg == null || arg.annotationArgName.text != "level") {
                            holder.registerProblem(precedenceAnnotation, "Missing `level` arg on #[precedence]")
                        } else {
                            val levelText = arg.annotationArgValue.text

                            val level = levelText?.substring(1, levelText.length - 1) // remove the "
                                ?.toIntOrNull()

                            if (level == null) {
                                holder.registerProblem(arg.annotationArgValue, "Level value is not an int")
                            }

                            if (level == 1) {
                                if (assocAnnotation != null) {
                                    holder.registerProblem(
                                        assocAnnotation,
                                        "#[assoc] used on alternative with #[precedence(level=\"1\")]"
                                    )
                                }
                            }
                        }
                    }


                    if (assocAnnotation != null) {
                        val assocAnnotationArg = assocAnnotation.annotationArg

                        if (assocAnnotationArg == null || assocAnnotationArg.annotationArgName.text != "side") {
                            holder.registerProblem(assocAnnotation, "Missing side=\"...\" on #[assoc] annotation")
                        }
                    }

                    if (precedenceAnnotation == null) {
                        alternativesWithoutPrecedenceAnnotation.add(it)
                    }
                }

                if (hasPrecedence) {
                    alternativesWithoutPrecedenceAnnotation.forEach { alternative ->
                        holder.registerProblem(
                            alternative,
                            "Alternative without #[precedence] in a nonterminal that has a #[precedence] alternative"
                        )
                    }
                }
            }
        }
    }
}
