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
    private fun createPsiFile(text: CharSequence): PsiFile =
        PsiFileFactory
            .getInstance(project)
            .createFileFromText(LpLanguage, text)

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

    /**
     * Returns a pair where the first element corresponds to the ':' and the second to the type_ref.
     * Used with `nonterminal.addRangeAfter(pair.first, pair.second, nonterminal.name)` to add the type to a nonterminal
     * where a type doesn't exist already.
     */
    fun createNonterminalType(type: String): Pair<PsiElement, LpTypeRef> {
        val nonterminal = createFromText<LpNonterminal>("grammar;\n Nonterminal: $type = {};")
            ?: error("Failed to create nonterminal, type = $type")

        val typeRef = nonterminal.typeRef!!
        val whitespace = typeRef.prevSibling
        val colon = whitespace.prevSibling

        return colon to typeRef
    }

    fun createTypeRef(type: String): LpTypeRef =
        createFromText("grammar; dummy: $type = {};") ?: error("Failed to create type ref from `$type`")

    fun createAction(text: String): LpAction =
        createFromText("grammar; dummy = \" \" $text;") ?: error("Failed to create action from `$text`")

    fun createUseStmt(text: String): LpUseStmt =
        createFromText("use $text; grammar;") ?: error("Failed to create use statement from `$text`")
}