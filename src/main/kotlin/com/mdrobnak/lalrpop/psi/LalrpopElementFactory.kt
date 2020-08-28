package com.mdrobnak.lalrpop.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.mdrobnak.lalrpop.LalrpopLanguage
import org.rust.lang.core.psi.ext.descendantOfTypeStrict

class LalrpopElementFactory(val project: Project) {
    fun createPsiFile(text: CharSequence): PsiFile =
            PsiFileFactory
                    .getInstance(project)
                    .createFileFromText("dummy.lalrpop", LalrpopLanguage, text)

    private inline fun <reified T : PsiElement> createFromText(code: CharSequence): T? =
            createPsiFile(code).descendantOfTypeStrict()

    fun createIdentifier(name: String): PsiElement =
            createFromText<LalrpopNonterminalName>("grammar;\n$name = \" \";")?.nameIdentifier
                    ?: error("Failed to create identifier: `$name`")
}