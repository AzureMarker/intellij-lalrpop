package com.mdrobnak.lalrpop.injectors

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.LpGrammarDecl
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpTypeRef
import com.mdrobnak.lalrpop.psi.ext.importCode
import com.mdrobnak.lalrpop.psi.ext.isTopLevel
import org.rust.lang.RsLanguage

/**
 * Inject the Rust language into type references, such as the explicit type
 * annotations on nonterminals.
 */
class LpRustTypeRefInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!context.isValid || context !is LpTypeRef || !context.isTopLevel) {
            return
        }

        val imports = context.containingFile.importCode
        val nonterminal = context.parentOfType<LpNonterminal>()
        val nonterminalTypeParamsString =
            nonterminal?.nonterminalName?.nonterminalParams?.nonterminalParamList?.joinToString(
                separator = ",",
                postfix = ","
            ) { it.text } ?: ""

        val grammarDecl = PsiTreeUtil.findChildOfType(context.containingFile, LpGrammarDecl::class.java)
        val grammarTypeParamsString =
            grammarDecl?.grammarTypeParams?.typeParamList?.joinToString(separator = ",", postfix = ",") { it.text }
                ?: ""

        val prefix = """
            mod __intellij_lalrpop {
                $imports
                type __intellij_lalrpop<$grammarTypeParamsString $nonterminalTypeParamsString> = """.trimIndent()
        val suffix = ";\n}"

        registrar
            .startInjecting(RsLanguage)
            .addPlace(prefix, suffix, context, TextRange.allOf(context.text))
            .doneInjecting()

        attachInjectedCodeToCrate(context)
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(LpTypeRef::class.java)
}