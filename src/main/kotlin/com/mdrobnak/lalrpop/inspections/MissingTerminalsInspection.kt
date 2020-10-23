package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.*
import java.util.*

object MissingTerminalsInspection : LocalInspectionTool() {
    // null if there is an _ in the match block
    var unresolved: MutableList<PsiElement>? = null
    lateinit var terminalDefs: MutableList<PsiElement>

    // true if it found an enum / match token
    var hasTerminalDefsBlock = false

    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        super.inspectionStarted(session, isOnTheFly)
        unresolved = Collections.synchronizedList(mutableListOf())
        terminalDefs = Collections.synchronizedList(mutableListOf())
        hasTerminalDefsBlock = false
    }

    override fun inspectionFinished(session: LocalInspectionToolSession, problemsHolder: ProblemsHolder) {
        if (hasTerminalDefsBlock)
            unresolved?.forEach {
                problemsHolder.registerProblem(it, "Missing declaration of terminal", ProblemHighlightType.ERROR)
            }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is LpMatchToken || element is LpEnumToken) hasTerminalDefsBlock = true

                if (element is LpMatchItem) {
                    val terminal = element.matchSymbol?.quotedLiteral
                    if (terminal != null) {
                        terminalDefs.add(terminal)
                        unresolved?.removeIf { (it as? LpQuotedTerminal)?.text == terminal.text }
                    } else {
                        // is _ => there cannot be any unresolved terminals, so drop the unresolved list
                        unresolved = null
                    }
                } else if (element is LpTerminal && element.parent is LpConversion) {
                    val terminal = element.quotedTerminal
                    if (terminal != null) {
                        terminalDefs.add(terminal)
                        unresolved?.removeIf { (it as? LpQuotedTerminal)?.text == terminal.text }
                    }
                } else if (element is LpQuotedTerminal && element.parent !is LpTerminal) {
                    if (!terminalDefs.any {
                            when (it) {
                                is LpQuotedTerminal -> it.quotedLiteral.text == element.quotedLiteral.text
                                is LpQuotedLiteral -> it.text == element.quotedLiteral.text
                                else -> false
                            }
                        }
                    ) {
                        unresolved?.add(element)
                    }
                }
            }
        }
    }
}