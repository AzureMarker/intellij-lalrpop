package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.mdrobnak.lalrpop.psi.LpQuotedLiteral
import org.intellij.lang.regexp.DefaultRegExpPropertiesProvider
import org.intellij.lang.regexp.RegExpLanguageHost
import org.intellij.lang.regexp.psi.RegExpChar
import org.intellij.lang.regexp.psi.RegExpGroup
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef

fun LpQuotedLiteral.isRegex(): Boolean = regexLiteral != null

abstract class LpQuotedLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpQuotedLiteral, RegExpLanguageHost {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        LiteralTextEscaper.createSimple(this)

    // The below RegExp info is copied from
    // https://github.com/intellij-rust/intellij-rust/blob/c24be22c891b95a7e2c467507b7ae10b57e10aec/src/main/kotlin/org/rust/lang/core/psi/ext/RsLitExpr.kt#L70-L91
    override fun characterNeedsEscaping(c: Char): Boolean = false
    override fun supportsPerl5EmbeddedComments(): Boolean = false
    override fun supportsPossessiveQuantifiers(): Boolean = true
    override fun supportsPythonConditionalRefs(): Boolean = false
    override fun supportsNamedGroupSyntax(group: RegExpGroup): Boolean = true
    override fun supportsNamedGroupRefSyntax(ref: RegExpNamedGroupRef): Boolean =
        ref.isNamedGroupRef
    override fun supportsExtendedHexCharacter(regExpChar: RegExpChar): Boolean = true
    override fun isValidCategory(category: String): Boolean =
        DefaultRegExpPropertiesProvider.getInstance().isValidCategory(category)
    override fun getAllKnownProperties(): Array<Array<String>> =
        DefaultRegExpPropertiesProvider.getInstance().allKnownProperties
    override fun getPropertyDescription(name: String?): String? =
        DefaultRegExpPropertiesProvider.getInstance().getPropertyDescription(name)
    override fun getKnownCharacterClasses(): Array<Array<String>> =
        DefaultRegExpPropertiesProvider.getInstance().knownCharacterClasses
}
