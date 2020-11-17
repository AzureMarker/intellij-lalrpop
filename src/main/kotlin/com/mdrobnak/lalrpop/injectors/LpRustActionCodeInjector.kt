package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
import com.mdrobnak.lalrpop.psi.impl.LpSymbolImpl
import com.mdrobnak.lalrpop.psi.util.name
import com.mdrobnak.lalrpop.psi.util.selected
import org.rust.lang.RsLanguage

/**
 * Injects the Rust language into user action code
 */
class LpRustActionCodeInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpActionImpl) {
            return
        }

        val codeNode = context.code ?: return
        val imports = PsiTreeUtil.findChildrenOfType(context.containingFile, LpUseStmt::class.java)
            .joinToString("\n") { it.text }
        val nonterminal = context.parentOfType<LpNonterminal>()!!
        val alternative = context.parentOfType<LpAlternative>()!!

        val inputs = alternative.selected.mapNotNull { (it as LpSymbolImpl).getSelectedType() }
        val returnType = nonterminal.resolveType(listOf())

        val grammarDecl = PsiTreeUtil.findChildOfType(context.containingFile, LpGrammarDecl::class.java)

        val grammarParams = grammarDecl?.grammarParams
        val grammarParametersString =
            grammarParams?.grammarParamList?.joinToString(separator = "") { "${it.name}: ${it.typeRef.text}," }
                ?: ""

        val grammarTypeParams = grammarDecl?.grammarTypeParams
        val genericParameters = nonterminal.nonterminalName.nonterminalParams?.nonterminalParamList

        fun addNullableIterators(a: List<String>?, b: List<String>?): List<String> = (a ?: listOf()) + (b ?: listOf())
        fun List<String>.join(): String =
            if (this.isEmpty()) "" else this.joinToString(prefix = "<", postfix = ">", separator = ", ") { it }

        val grammarTypeParamsString = addNullableIterators(
            grammarTypeParams?.typeParamList?.map { it.text },
            genericParameters?.map { it.text }
        ).join()

        val arguments = inputs.mapIndexed { index, it ->
            when (it) {
                is LpSelectedType.WithName -> (if (it.isMutable) "mut " else "") + it.name + ": " + it.type
                is LpSelectedType.WithoutName -> "__intellij_lalrpop_noname_$index: " + it.type
            }
        }.joinToString(", ")

        val grammarWhereClauses = grammarDecl?.grammarWhereClauses
        val grammarWhereClausesString =
            grammarWhereClauses?.grammarWhereClauseList?.joinToString(prefix = "where ", separator = ", ") { it.text }
                ?: ""

        val prefix = "mod __intellij_lalrpop {\n" +
                "$imports\n" +
                "fn __intellij_lalrpop $grammarTypeParamsString ($grammarParametersString $arguments) -> $returnType" +
                " $grammarWhereClausesString {\n"

        val suffix = "\n}\n}"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, codeNode.textRangeInParent)
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpActionImpl::class.java)
}
