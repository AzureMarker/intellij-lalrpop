package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

object MissingTerminalsInspection : LocalInspectionTool() {
    lateinit var unresolved: ConcurrentLinkedQueue<PsiElement>
    lateinit var terminalDefs: ConcurrentLinkedQueue<PsiElement>

    // true if it found an enum / match token
    lateinit var check: AtomicBoolean

    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        super.inspectionStarted(session, isOnTheFly)
        unresolved = ConcurrentLinkedQueue()
        terminalDefs = ConcurrentLinkedQueue()
        check = AtomicBoolean(false)
    }

    override fun inspectionFinished(session: LocalInspectionToolSession, problemsHolder: ProblemsHolder) {
        if (check.get())
            unresolved.forEach { unresolvedElement ->
                if (unresolvedElement !is LpQuotedTerminal) return@forEach
                if (!terminalDefs.any {
                        when (it) {
                            is LpQuotedTerminal -> it.quotedLiteral.text == unresolvedElement.quotedLiteral.text
                            is LpQuotedLiteral -> it.text == unresolvedElement.quotedLiteral.text
                            else -> false
                        }
                    })
                    problemsHolder.registerProblem(
                        unresolvedElement,
                        "Missing declaration of terminal",
                        ProblemHighlightType.ERROR
                    )
            }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is LpMatchToken || element is LpEnumToken) check.set(true)

                if (element is LpMatchItem) {
                    val terminal = element.matchSymbol?.quotedLiteral
                    if (terminal != null) {
                        terminalDefs.add(terminal)
                    } else {
                        // is _ => there cannot be any unresolved terminals, so don't check.
                        check.set(false)
                    }
                } else if (element is LpTerminal && element.parent is LpConversion) {
                    val terminal = element.quotedTerminal
                    if (terminal != null) {
                        terminalDefs.add(terminal)
                    }
                } else if (element is LpQuotedTerminal && element.parent !is LpTerminal) {
                    unresolved.add(element)
                }
            }
        }
    }
}