package com.mdrobnak.lalrpop

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object LalrpopFileType: LanguageFileType(LalrpopLanguage) {
    override fun getName(): String = language.displayName

    override fun getDescription(): String = language.displayName

    override fun getDefaultExtension(): String = "lalrpop"

    override fun getIcon(): Icon? = AllIcons.FileTypes.Custom
}