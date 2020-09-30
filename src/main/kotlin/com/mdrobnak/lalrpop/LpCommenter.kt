package com.mdrobnak.lalrpop

import com.intellij.lang.Commenter

object LpCommenter : Commenter {
    override fun getLineCommentPrefix(): String? = "//"

    override fun getBlockCommentPrefix(): String? = "/*"

    override fun getBlockCommentSuffix(): String? = "*/"

    // TODO: not sure what "commented beginning/end of a block comment" mean so might want to change?
    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}