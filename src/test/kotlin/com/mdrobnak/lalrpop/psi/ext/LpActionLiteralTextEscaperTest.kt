package com.mdrobnak.lalrpop.psi.ext

import com.mdrobnak.lalrpop.psi.LpSelectedType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LpActionLiteralTextEscaperTest {
    @Test
    fun `no replacements`() {
        assertEquals("", actionCodeEscape("", listOf()))
        assertEquals("X::Y::new()", actionCodeEscape("X::Y::new()", listOf()))
    }

    @Test
    fun `simple inside-parentheses replacements`() {
        assertEquals(
            "X::new(p1, p2, p3)",
            actionCodeEscape(
                "X::new(<>)", listOf(
                    LpSelectedType.WithName(name = "p1", type = ""),
                    LpSelectedType.WithName(name = "p2", type = ""),
                    LpSelectedType.WithName(name = "p3", type = "")
                )
            )
        )
        assertEquals(
            "X::new(a, b, c, p1, p2, p3)",
            actionCodeEscape(
                "X::new(a, b, c, <>)", listOf(
                    LpSelectedType.WithName(name = "p1", type = ""),
                    LpSelectedType.WithName(name = "p2", type = ""),
                    LpSelectedType.WithName(name = "p3", type = "")
                )
            )
        )
    }

    @Test
    fun `simple inside-braces replacements`() {
        assertEquals(
            "X {p1, p2, p3}",
            actionCodeEscape(
                "X {<>}", listOf(
                    LpSelectedType.WithName(name = "p1", type = ""),
                    LpSelectedType.WithName(name = "p2", type = ""),
                    LpSelectedType.WithName(name = "p3", type = "")
                )
            )
        )
        assertEquals(
            "X {a, b, c, p1, p2, p3}",
            actionCodeEscape(
                "X {a, b, c, <>}", listOf(
                    LpSelectedType.WithName(name = "p1", type = ""),
                    LpSelectedType.WithName(name = "p2", type = ""),
                    LpSelectedType.WithName(name = "p3", type = "")
                )
            )
        )
    }
}