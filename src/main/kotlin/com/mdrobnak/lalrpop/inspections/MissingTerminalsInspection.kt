package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

val unresolvedKey = Key.create<ConcurrentLinkedQueue<PsiElement>>("missingTerminals.unresolved")
val terminalDefsKey = Key.create<ConcurrentLinkedQueue<PsiElement>>("missingTerminals.terminalDefs")
// true if it found an enum / match token
val checkKey = Key.create<AtomicBoolean>("missingTerminals.check")
// true if the match has a _
val matchHasWildcardKey = Key.create<AtomicBoolean>("missingTerminals.matchHasWildcard")

/**
 * Gets the user data from the holder and asserts it is not null.
 */
private fun <T> UserDataHolderBase.dataNotNull(key: Key<T>): T = getUserData(key)!!

object MissingTerminalsInspection : LocalInspectionTool() {
    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        super.inspectionStarted(session, isOnTheFly)

        session.putUserData(unresolvedKey, ConcurrentLinkedQueue())
        session.putUserData(terminalDefsKey, ConcurrentLinkedQueue())
        session.putUserData(checkKey, AtomicBoolean(false))
        session.putUserData(matchHasWildcardKey, AtomicBoolean(false))
    }

    override fun inspectionFinished(session: LocalInspectionToolSession, problemsHolder: ProblemsHolder) {
        if (session.dataNotNull(checkKey).get() && !session.dataNotNull(matchHasWildcardKey).get()) {
            val terminalDefs = session.dataNotNull(terminalDefsKey)
            session.dataNotNull(unresolvedKey).forEach { unresolvedElement ->
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
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is LpMatchToken || element is LpEnumToken) session.dataNotNull(checkKey).set(true)

                if (element is LpMatchItem) {
                    val terminal = element.matchSymbol?.quotedLiteral
                    if (terminal != null) {
                        session.dataNotNull(terminalDefsKey).add(terminal)
                    } else {
                        // is _ => there cannot be any unresolved terminals, so don't check.
                        session.dataNotNull(matchHasWildcardKey).set(true)
                    }
                } else if (element is LpTerminal && element.parent is LpConversion) {
                    val terminal = element.quotedTerminal
                    if (terminal != null) {
                        session.dataNotNull(terminalDefsKey).add(terminal)
                    }
                } else if (element is LpQuotedTerminal && element.parent !is LpTerminal) {
                    session.dataNotNull(unresolvedKey).add(element)
                }
            }
        }
    }
}