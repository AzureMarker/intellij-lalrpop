package com.mdrobnak.lalrpop.psi.ext

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpSelectedType
import java.util.*

/**
 * A simple structure to hold data about the ranges of `<>` and the ranges they expanded to in the decoded buffer.
 * Also should know about the context (parentheses / brackets or braces, because the rust code they expand to is
 * different based on where they are). See <a href="http://lalrpop.github.io/lalrpop/tutorial/003_type_inference.html">
 *     the chapter on type inference in the lalrpop manual
 *     </a> for more
 */
private data class Mapping(val sourceRange: TextRange, val targetRange: TextRange, val context: Context)

private fun actionCodeEscapeWithMappings(actionCode: String, evalOfAngleBracketsExpression: List<LpSelectedType>): Pair<List<Mapping>, String> {
    // get all the text ranges with <> from rust
    val mappings = actionCode.findAllMappings("<>", evalOfAngleBracketsExpression)

    // replace them all with their replacements, in reverse order
    val result = mappings.foldRight(actionCode) { mapping, acc ->
        mapping.sourceRange.replace(acc, evalOfAngleBracketsExpression.replacement(mapping.context))
    }

    return mappings to result
}

fun actionCodeEscape(actionCode: String, evalOfAngleBracketsExpression: List<LpSelectedType>): String =
    actionCodeEscapeWithMappings(actionCode, evalOfAngleBracketsExpression).second

class LpActionLiteralTextEscaper(action: LpAction, private val evalOfAngleBracketsExpression: List<LpSelectedType>) :
    LiteralTextEscaper<LpAction>(action) {

    private lateinit var mappings: List<Mapping>

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        // get the text from the host
        val txt = this.myHost.text.substring(rangeInsideHost.startOffset, rangeInsideHost.endOffset)

        val mappingsAndResult = actionCodeEscapeWithMappings(txt, evalOfAngleBracketsExpression)
        mappings = mappingsAndResult.first

        // add the result string to the builder
        outChars.append(mappingsAndResult.second)

        // can never fail so just return true
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        // last mapping before offsetInDecoded in the decoded(second of the pair)
        val mapping = mappings.findLast { it.targetRange.startOffset < offsetInDecoded }
            ?: return offsetInDecoded + rangeInsideHost.startOffset

        return if (offsetInDecoded < mapping.targetRange.endOffset)
        // inside mapping, return index of `<` in host range
            mapping.sourceRange.startOffset + rangeInsideHost.startOffset
        else
        // outside mapping
            offsetInDecoded - mapping.targetRange.endOffset + mapping.sourceRange.endOffset + rangeInsideHost.startOffset
    }

    override fun isOneLine(): Boolean = false
}

/**
 * Returns a list of the mappings within `this` for `text` (= "<>") where the `<>` should be
 * replaced by `replacements`, via the "replacement" function declared below on the `replacements` list.
 */
private fun String.findAllMappings(text: String, replacements: List<LpSelectedType>): List<Mapping> {
    var prevIndex = -text.length
    var index = this.indexOf(text)
    val mappings = mutableListOf<Mapping>()
    var decodedOffset = 0

    val parens = Stack<Context>()

    while (index != -1) {
        val toStartFrom = if (prevIndex >= 0) prevIndex else 0
        for (i in toStartFrom..index) {
            when (this[i]) {
                '(', '[' -> parens.push(Context.Parentheses)
                '{' -> parens.push(Context.Braces)
                ')', ']', '}' -> if (!parens.empty()) parens.pop()
            }
        }

        val decodedStart = decodedOffset + index - (prevIndex + text.length)
        val context = if (!parens.empty()) parens.peek() else Context.Parentheses
        mappings.add(
            Mapping(
                TextRange(index, index + text.length), TextRange(
                    decodedStart,
                    decodedStart + replacements.lengthFor(context)
                ),
                context
            )
        )
        decodedOffset = decodedStart + replacements.lengthFor(context)
        prevIndex = index
        index = this.indexOf(text, index + text.length)
    }


    return mappings
}

/**
 * The context where a <> expression appeared
 */
private enum class Context {
    Parentheses, Braces
}

/**
 * Given a list of selected types and the context where <> appears, find what the <> should be replaced by.
 */
private fun List<LpSelectedType>.replacement(context: Context): String =
    this.mapIndexed { index, it ->
        when (it) {
            is LpSelectedType.WithName -> when (context) {
                Context.Parentheses -> it.name
                Context.Braces -> "${it.name}: ${it.name}"
            }
            is LpSelectedType.WithoutName -> {
                "__intellij_lalrpop_noname_$index"
            }
        }
    }.joinToString(separator = ", ")

/**
 * Find the length of the replacement of <>, in a given list of selected symbols and the context where the <> appears.
 */
private fun List<LpSelectedType>.lengthFor(context: Context) = this.replacement(context).length