package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpGrammarDecl
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpSelectedType
import com.mdrobnak.lalrpop.psi.ext.importCode
import com.mdrobnak.lalrpop.psi.ext.name
import com.mdrobnak.lalrpop.psi.ext.returnType
import com.mdrobnak.lalrpop.psi.ext.selected
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
import com.mdrobnak.lalrpop.psi.impl.LpSymbolImpl
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
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
        val imports = context.containingFile.importCode
        val nonterminal = context.parentOfType<LpNonterminal>()!!
        val alternative = context.parentOfType<LpAlternative>()!!

        val typeResolutionContext = context.containingFile.lalrpopTypeResolutionContext()

        val inputs = alternative.selected.map { (it as LpSymbolImpl).getSelectedType(typeResolutionContext) }
        val returnType = context.actionType.returnType(nonterminal.resolveType(typeResolutionContext, listOf()), typeResolutionContext)

        val grammarDecl = PsiTreeUtil.findChildOfType(context.containingFile, LpGrammarDecl::class.java)

        val grammarParams = grammarDecl?.grammarParamList
        val grammarParametersString =
            grammarParams?.joinToString(separator = "") { "${it.name}: ${it.typeRef.text}," }
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
