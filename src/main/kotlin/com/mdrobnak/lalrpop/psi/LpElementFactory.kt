package com.mdrobnak.lalrpop.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.mdrobnak.lalrpop.LpLanguage
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.descendantOfTypeStrict
import org.rust.lang.core.psi.ext.elementType

class LpElementFactory(val project: Project) {
    fun createPsiFile(text: CharSequence): PsiFile =
        PsiFileFactory
            .getInstance(project)
            .createFileFromText("dummy.lalrpop", LpLanguage, text)

    private inline fun <reified T : PsiElement> createFromText(code: CharSequence): T? =
        createPsiFile(code).descendantOfTypeStrict()

    fun createIdentifier(name: String): PsiElement =
        createFromText<LpNonterminalName>("grammar;\n$name = \" \";")?.nameIdentifier
            ?: error("Failed to create identifier: `$name`")

    fun createNonterminalParamsFromSingle(name: String): LpNonterminalParams =
        createFromText("grammar;\ndummy<$name> = {};")
            ?: error("Failed to create nonterminal params from single param with name = `$name`")

    fun createComma(): PsiElement =
        createFromText<LpNonterminalParams>("grammar;\ndummy<T,U> = {};")?.childrenWithLeaves?.first { it.elementType == LpElementTypes.COMMA }
            ?: error("Failed to create psi element for comma (`,`)")

    fun createNonterminalParam(name: String): LpNonterminalParam =
        createFromText("grammar;\ndummy<$name> = {};")
            ?: error("Failed to create nonterminal param from name = `$name`")

    fun createNonterminal(name: String, params: List<String>?): LpNonterminal {
        val paramsString = params?.joinToString(prefix = "<", separator = ", ", postfix = ">") ?: ""
        return createFromText("grammar;\n$name$paramsString = ();")
            ?: error("Failed to create nonterminal with name = `$name` and params = `$params`")
    }

    fun createWhitespace(s: String): PsiWhiteSpace = createFromText("${s}grammar;")
        ?: error("Failed to create whitespace '$s'")
}