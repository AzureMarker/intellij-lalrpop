package com.mdrobnak.lalrpop.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.mdrobnak.lalrpop.LpFileType
import com.mdrobnak.lalrpop.LpLanguage

class LpFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LpLanguage) {
    override fun getFileType(): FileType = LpFileType

    override fun toString(): String = "LALRPOP File"
}