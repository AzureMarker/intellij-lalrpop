package com.mdrobnak.lalrpop.psi

sealed class LpSelectedType {
    data class WithName(val isMutable: Boolean, val name: String, val type: String): LpSelectedType()
    data class WithoutName(val type: String): LpSelectedType()
}