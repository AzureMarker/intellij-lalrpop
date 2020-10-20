package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.*

object MissingTerminalsInspection : LocalInspectionTool() {
    var unresolved = mutableListOf<PsiElement>()
    var terminalDefs = mutableListOf<PsiElement>()

    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        super.inspectionStarted(session, isOnTheFly)
        unresolved = mutableListOf()
        terminalDefs = mutableListOf()
    }

    override fun inspectionFinished(session: LocalInspectionToolSession, problemsHolder: ProblemsHolder) {
        unresolved.forEach {
            problemsHolder.registerProblem(it, "Missing declaration of terminal", ProblemHighlightType.ERROR)
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                if (element is LpMatchSymbol) {
                    val terminal = element.quotedLiteral
                    terminalDefs.add(terminal)
                    unresolved.removeIf { (it as? LpQuotedTerminal)?.text == terminal.text }
                } else if (element is LpTerminal && element.parent is LpConversion) {
                    val terminal = element.quotedTerminal
                    if (terminal != null) {
                        terminalDefs.add(terminal)
                        unresolved.removeIf { (it as? LpQuotedTerminal)?.text == terminal.text }
                    }
                } else if (element is LpQuotedTerminal && element.parent !is LpTerminal) {
                    if (terminalDefs.find {
                            when (it) {
                                is LpQuotedTerminal -> it.quotedLiteral.text == element.quotedLiteral.text
                                is LpQuotedLiteral -> it.text == element.quotedLiteral.text
                                else -> false
                            }
                        } == null
                    ) {
                        unresolved.add(element)
                    }
                }
            }
        }
    }
}