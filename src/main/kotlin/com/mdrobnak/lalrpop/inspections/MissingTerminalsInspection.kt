package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.mdrobnak.lalrpop.psi.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

val unresolvedKey = Key.create<ConcurrentLinkedQueue<PsiElement>>("LALRPOP.missingTerminals.unresolved")
val terminalDefsKey = Key.create<ConcurrentLinkedQueue<PsiElement>>("LALRPOP.missingTerminals.terminalDefs")

// true if it found an enum / match token
val checkKey = Key.create<AtomicBoolean>("LALRPOP.missingTerminals.check")

// true if the match has a _
val matchHasWildcardKey = Key.create<AtomicBoolean>("LALRPOP.missingTerminals.matchHasWildcard")

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
                    )
            }
        }
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : LpVisitor() {
        override fun visitMatchToken(o: LpMatchToken) = session.dataNotNull(checkKey).set(true)

        override fun visitEnumToken(o: LpEnumToken) = session.dataNotNull(checkKey).set(true)

        override fun visitMatchItem(element: LpMatchItem) {
            val terminal = element.matchSymbol?.quotedLiteral
            if (terminal != null) {
                session.dataNotNull(terminalDefsKey).add(terminal)
            } else {
                // is _ => there cannot be any unresolved terminals, so don't check.
                session.dataNotNull(matchHasWildcardKey).set(true)
            }
        }

        override fun visitTerminal(element: LpTerminal) {
            if (element.parent !is LpConversion) return
            element.quotedTerminal?.let { session.dataNotNull(terminalDefsKey).add(it) }
        }

        override fun visitQuotedTerminal(element: LpQuotedTerminal) {
            if (element.parent is LpTerminal) return
            session.dataNotNull(unresolvedKey).add(element)
        }
    }
}