package com.mdrobnak.lalrpop.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.mdrobnak.lalrpop.LalrpopFileType
import com.mdrobnak.lalrpop.LalrpopLanguage

class LalrpopFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LalrpopLanguage) {
    override fun getFileType(): FileType = LalrpopFileType

    override fun toString(): String = "LALRPOP File"
}