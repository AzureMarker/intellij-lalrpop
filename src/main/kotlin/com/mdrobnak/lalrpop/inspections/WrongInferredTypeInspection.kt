package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFileFactory
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.importCode
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.RsTypeAlias
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.resolve.ImplLookup
import org.rust.lang.core.resolve.knownItems
import org.rust.lang.core.types.infer.RsInferenceContext

/**
 * Reports 2 kinds of problems on a nonterminal
 * 1. The inferred type is different from the type declared explicitly on the nonterminal
 * 2. There are 2 different alternatives, without action code, where the inferred types are different
 */
object WrongInferredTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : LpVisitor() {
            override fun visitNonterminal(nonterminal: LpNonterminal) {
                if (nonterminal.nonterminalName.nonterminalParams != null) return

                val explicitType = nonterminal.typeRef?.resolveType(listOf())

                if (explicitType == "()") return

                var seenType = explicitType

                nonterminal.alternatives.alternativeList.filter { it.action == null }.forEach { alternative ->
                    val alternativeType = alternative.resolveType(listOf())

                    if (seenType == null) {
                        seenType = alternativeType
                        return
                    }

                    if (!sameTypes(
                            nonterminal.project,
                            nonterminal.containingFile.importCode,
                            seenType!!,
                            alternativeType
                        )
                    ) {
                        holder.registerProblem(
                            alternative,
                            "Resolved type of alternative is `$alternativeType`, while expected type `$seenType`"
                        )
                    }
                }
            }
        }
    }

    fun sameTypes(project: Project, importCode: String, type1: String, type2: String): Boolean {
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText(RsLanguage, "$importCode\ntype T1 = $type1;\ntype T2 = $type2;")
        val aliases = file.childrenOfType<RsTypeAlias>();

        if (aliases.size != 2) return false

        return RsInferenceContext(project, ImplLookup.relativeTo(aliases[0]), aliases[0].knownItems).canCombineTypes(
            aliases[0].declaredType,
            aliases[1].declaredType
        )
    }
}