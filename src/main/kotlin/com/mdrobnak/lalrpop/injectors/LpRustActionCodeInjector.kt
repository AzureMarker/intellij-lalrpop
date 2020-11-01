package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.impl.LpActionImpl
import com.mdrobnak.lalrpop.psi.impl.LpSymbolImpl
import com.mdrobnak.lalrpop.psi.util.getName
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
        val inputs = alternative.symbolList
            .filterIsInstance<LpSymbolImpl>()
            .mapNotNull { it.getSelectedType() }
        val returnType = nonterminal.typeRef?.text ?: nonterminal.nonterminalName

        val grammarDecl = PsiTreeUtil.findChildOfType(context.containingFile, LpGrammarDecl::class.java)

        val grammarParams = grammarDecl?.grammarParams
        val grammarParametersString = if (grammarParams != null && grammarParams.grammarParamList.isNotEmpty()) {
            grammarParams.grammarParamList.joinToString(separator = "") { "${it.getName()}: ${it.typeRef.text}," }
        } else {
            ""
        }

        val grammarTypeParams = grammarDecl?.grammarTypeParams
        val grammarTypeParamsString =
            grammarTypeParams?.typeParamList?.joinToString(prefix = "<", separator = ", ", postfix = ">") { it.text }
                ?: ""

        val arguments = inputs.joinToString(", ") {
            when (it) {
                is LpSelectedType.WithName -> (if (it.isMutable) "mut " else "") + it.name + ": " + it.type
                // FIXME: do something with unnamed argument?
                is LpSelectedType.WithoutName -> "intellij_lalrpop_noname: " + it.type
            }
        }

        val grammarWhereClauses = grammarDecl?.grammarWhereClauses
        val grammarWhereClausesString =
            grammarWhereClauses?.grammarWhereClauseList?.joinToString(prefix = "where ", separator = ", ") { it.text }
                ?: ""

        val prefix = "mod __intellij_lalrpop {\n" +
                "$imports\n" +
                "fn __intellij_lalrpop $grammarTypeParamsString ($grammarParametersString $arguments) -> $returnType" +
                "$grammarWhereClausesString {\n"
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