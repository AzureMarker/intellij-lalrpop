package com.mdrobnak.lalrpop.psi.ext

import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpRepeatOp
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

fun <T> LpRepeatOp.switch(question: T, plus: T, multiply: T): T =
    when (childrenWithLeaves.first().elementType) {
        LpElementTypes.QUESTION -> question
        LpElementTypes.PLUS -> plus
        LpElementTypes.MULTIPLY -> multiply
        else -> throw IllegalStateException("Something other than ?, +, and * in a repeat_op rule")
    }