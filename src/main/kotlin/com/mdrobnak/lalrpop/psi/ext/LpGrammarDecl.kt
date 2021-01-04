package com.mdrobnak.lalrpop.psi.ext

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.findDescendantOfType
import com.mdrobnak.lalrpop.psi.LpGrammarDecl

fun PsiFile.lalrpopFindGrammarDecl(): LpGrammarDecl = findDescendantOfType()!!

fun LpGrammarDecl.typeParamsRustUnitStructs(): String =
    grammarTypeParams?.typeParamList?.filter { it.id != null }
        ?.joinToString(separator = "\n", postfix = "\n") { "struct ${it.id!!.text}();" } ?: ""