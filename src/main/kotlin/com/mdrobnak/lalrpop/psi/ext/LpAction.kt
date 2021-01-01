package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.injectors.findModuleDefinition
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import org.rust.ide.presentation.renderInsertionSafe
import org.rust.lang.RsLanguage
import org.rust.lang.core.macros.RsExpandedElement
import org.rust.lang.core.macros.setContext
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.ext.RsElement
import org.rust.lang.core.psi.ext.block
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.resolve.ImplLookup
import org.rust.lang.core.types.infer.substitute

val LpAction.alternativeParent: LpAlternative
    get() = this.parentOfType()!!

/**
 * The action code function header / definition (<code>fn __intellij_lalrpop <type_params>(params) where where_clauses</code>),
 * without the opening brace.
 *
 * @param withReturnType add the return type? You usually want this to be true, but during type resolution of a nonterminal
 * like <code>A = B => do_something(<>);</code>, to use the return type we should know it first, but to know it we must
 * infer it with the rust plugin, but to infer it we need the header, and this would lead to infinite indirect recursion.
 */
fun LpAction.actionCodeFunctionHeader(withReturnType: Boolean = true): String {
    val alternative = parentOfType<LpAlternative>()!!
    val nonterminal = parentOfType<LpNonterminal>()!!

    val typeResolutionContext = containingFile.lalrpopTypeResolutionContext()

    val inputs = alternative.selectedTypesInContext(typeResolutionContext)

    val arrowReturnType =
        if (withReturnType)
            " -> " + actionType.returnType(
                nonterminal.resolveType(
                    typeResolutionContext,
                    LpMacroArguments.identity(nonterminal.nonterminalName.nonterminalParams)
                ),
                typeResolutionContext
            )
        else ""

    val grammarDecl = this.containingFile.lalrpopFindGrammarDecl()

    val grammarParams = grammarDecl.grammarParams
    val grammarParametersString =
        grammarParams?.grammarParamList?.joinToString(separator = "") { "${it.name}: ${it.typeRef.text}," }
            ?: ""

    val grammarTypeParams = grammarDecl.grammarTypeParams
    val genericParameters = nonterminal.nonterminalName.nonterminalParams?.nonterminalParamList

    val genericParamsString =
        (grammarTypeParams?.typeParamList?.map { it.text }.orEmpty() + genericParameters?.map { it.text }.orEmpty())
            .let { if (it.isEmpty()) "" else it.joinToString(prefix = "<", postfix = ">", separator = ", ") }

    val arguments = inputs.mapIndexed { index, it ->
        when (it) {
            is LpSelectedType.WithName -> (if (it.isMutable) "mut " else "") + it.name + ": " + it.type
            is LpSelectedType.WithoutName -> "__intellij_lalrpop_noname_$index: " + it.type
        }
    }.joinToString(", ")

    val grammarWhereClauses = grammarDecl.grammarWhereClauses
    val grammarWhereClausesString =
        grammarWhereClauses?.grammarWhereClauseList?.joinToString(prefix = "where ", separator = ", ") { it.text }
            ?: ""

    return "fn __intellij_lalrpop $genericParamsString ($grammarParametersString $arguments) $arrowReturnType\n" +
            "$grammarWhereClausesString\n"
}

abstract class LpActionMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAction {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.lastChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        val context = this.containingFile.lalrpopTypeResolutionContext()
        return LpActionLiteralTextEscaper(
            this,
            this.alternativeParent.selectedTypesInContext(
                context,
                resolveTypes = false // no need to know the types of the selected symbols for expanding `<>`s
            )
        )
    }

    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        val importCode = this.containingFile.importCode

        val code = actionCodeEscape(code.text, this.alternativeParent.selectedTypesInContext(context))

        val fnCode = actionCodeFunctionHeader(false) +
                "{\n" +
                "   $code\n" +
                "}\n"
        val genericUnitStructs = this.alternativeParent.nonterminalParent.rustGenericUnitStructs()

        val fileText = "mod __intellij_lalrpop {\n$importCode\n $genericUnitStructs\n $fnCode \n}"
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText(RsLanguage, fileText)

        val fn = PsiTreeUtil.findChildOfType(file, RsFunction::class.java) ?: return "()"
        val block = fn.block ?: return "()"
        val expr = block.expr ?: return "()"

        val moduleDefinition = findModuleDefinition(project, this.containingFile) ?: return "()"

        for (child in file.childrenOfType<RsExpandedElement>()) {
            child.setContext(moduleDefinition)
        }

        val ctx = ImplLookup.relativeTo(fn).ctx

        val inferenceResult = ctx.infer(fn)
        val inferredGenericType = inferenceResult.getExprType(expr)

        val maybeConcreteType = inferredGenericType.substitute(
            arguments.getSubstitution(
                fn.typeParameterList, project, ctx,
                fn.parent as RsElement
            )
        )

        return actionType.nonterminalTypeFromReturn(maybeConcreteType)
            .renderInsertionSafe(fn, includeTypeArguments = true, includeLifetimeArguments = true)
    }
}
