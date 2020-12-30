package com.mdrobnak.lalrpop.psi

sealed class LpSelectedType {
    data class WithName(val name: String, val type: String, val isMutable: Boolean = false): LpSelectedType()
    data class WithoutName(val type: String): LpSelectedType()
}