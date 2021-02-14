package com.mdrobnak.lalrpop.psi.ext

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpGrammarDecl

fun PsiFile.lalrpopFindGrammarDecl(): LpGrammarDecl = PsiTreeUtil.findChildOfType(this, LpGrammarDecl::class.java)!!

fun LpGrammarDecl.typeParamsRustUnitStructs(): String =
    grammarTypeParams?.typeParamList?.filter { it.id != null }
        ?.joinToString(separator = "\n", postfix = "\n") { "struct ${it.id!!.text};" } ?: ""

val LpGrammarDecl.grammarParametersString: String
    get() = this.grammarParams?.grammarParamList?.joinToString(separator = "") { "${it.name}: ${it.typeRef.text}," } ?: ""
