package com.mdrobnak.lalrpop.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.rd.util.string.printToString
import com.mdrobnak.lalrpop.injectors.findModuleDefinition
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpVisitor
import com.mdrobnak.lalrpop.psi.ext.importCode
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import org.rust.lang.RsLanguage
import org.rust.lang.core.macros.RsExpandedElement
import org.rust.lang.core.macros.setContext
import org.rust.lang.core.psi.RsModDeclItem
import org.rust.lang.core.psi.RsTypeAlias
import org.rust.lang.core.psi.ext.childOfType
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.resolve.ImplLookup

/**
 * Reports 2 kinds of problems on a nonterminal
 * 1. The inferred type is different from the type declared explicitly on the nonterminal
 * 2. There are 2 different alternatives, without action code, where the inferred types are different
 */
object WrongInferredTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return object : LpVisitor() {
            override fun visitNonterminal(nonterminal: LpNonterminal) {
                // TODO: idea to implement this inspection for lalrpop macros:
                // https://github.com/Mcat12/intellij-lalrpop/pull/22#discussion_r542538737
                if (nonterminal.nonterminalName.nonterminalParams != null) return

                val context = nonterminal.containingFile.lalrpopTypeResolutionContext()

                val explicitType = nonterminal.typeRef?.resolveType(context, listOf())

                // LALRPOP will automatically supply action code of (), so we don't need to worry about inferring the wrong type.
                // https://github.com/lalrpop/lalrpop/blob/8a96e9646b3d00c2226349efed832c4c25631c53/lalrpop/src/normalize/lower/mod.rs#L351-L354
                if (explicitType == "()") return

                var seenType = explicitType

                nonterminal.alternatives.alternativeList.filter { it.action == null }.forEach { alternative ->
                    val alternativeType = alternative.resolveType(context, listOf())

                    if (seenType == null) {
                        seenType = alternativeType
                        return
                    }

                    if (!sameTypes(
                            nonterminal.project,
                            nonterminal.containingFile,
                            nonterminal.containingFile.importCode,
                            seenType!!,
                            alternativeType
                        )
                    ) {
                        holder.registerProblem(
                            alternative,
                            "Resolved type of alternative is `$alternativeType`, while expected type is `$seenType`"
                        )
                    }
                }
            }
        }
    }

    fun sameTypes(project: Project, lalrpopFile: PsiFile, importCode: String, type1: String, type2: String): Boolean {
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText(RsLanguage, "mod __intellij_lalrpop {\n$importCode\ntype T1 = $type1;\ntype T2 = $type2;\n}")
        val aliases = PsiTreeUtil.findChildrenOfType(file, RsTypeAlias::class.java)

        if (aliases.size != 2) return false

        val first = aliases.first()
        val second = aliases.last()

        val moduleDefinition = findModuleDefinition(project, lalrpopFile) ?: return false

        for (child in file.childrenOfType<RsExpandedElement>()) {
            child.setContext(moduleDefinition)
        }

        val ctx = ImplLookup.relativeTo(first).ctx
        val ty1 = ctx.fullyResolve(first.declaredType)
        val ty2 = ctx.fullyResolve(second.declaredType)

        return ctx.canCombineTypes(ty1, ty2)
    }
}
