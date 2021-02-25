package com.mdrobnak.lalrpop.psi.ext

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpSelectedType

/**
 * A simple structure to hold data about the ranges of `<>` and the ranges they expanded to in the decoded buffer.
 */
private data class Mapping(val sourceRange: TextRange, val targetRange: TextRange)

private fun actionCodeEscapeWithMappings(actionCode: String, evalOfAngleBracketsExpression: List<LpSelectedType>): Pair<List<Mapping>, String> {
    // get all the text ranges with <> from rust
    val mappings = actionCode.findAllMappings("<>", evalOfAngleBracketsExpression)

    // replace them all with their replacements, in reverse order
    val result = mappings.foldRight(actionCode) { mapping, acc ->
        mapping.sourceRange.replace(acc, evalOfAngleBracketsExpression.replacement())
    }

    return mappings to result
}

/**
 * Get the action code, roughly like what the rust plugin will see after lalrpop does it's magic;
 * mostly resolves <code><></code> and replaces them with something based on evalOfAngleBracketsExpressions and
 * where they are in the action code (parentheses / brackets / braces).
 *
 * @param actionCode The action code in the source lalrpop file
 * @param evalOfAngleBracketsExpression The list of names for selected types
 *
 * @return The final rust form the action code will have.
 */
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

    override fun getRelevantTextRange(): TextRange {
        return myHost.code.textRangeInParent
    }
}

/**
 * Returns a list of the mappings within `this` for `text` (= "<>") where the `<>` should be
 * replaced by `replacements`, via the "replacement" function declared below on the `replacements` list.
 *
 * @param text "<>"
 * @param replacements The list of replacements; used to compute the final length and set up the ranges
 *
 * @return The list of mappings
 *
 * @see Mapping
 * @see lengthFor
 */
private fun String.findAllMappings(text: String, replacements: List<LpSelectedType>): List<Mapping> {
    var prevIndex = -text.length
    var index = indexOf(text)
    val mappings = mutableListOf<Mapping>()
    var decodedOffset = 0

    while (index != -1) {
        val decodedStart = decodedOffset + index - (prevIndex + text.length)
        mappings.add(
            Mapping(
                TextRange(index, index + text.length), TextRange(
                    decodedStart,
                    decodedStart + replacements.lengthFor()
                ),
            )
        )
        decodedOffset = decodedStart + replacements.lengthFor()
        prevIndex = index
        index = indexOf(text, index + text.length)
    }


    return mappings
}

/**
 * Given a list of selected types, find what the <> should be replaced by.
 */
private fun List<LpSelectedType>.replacement(): String =
    mapIndexed { index, it ->
        when (it) {
            is LpSelectedType.WithName -> it.name
            is LpSelectedType.WithoutName -> index.lalrpopNoNameParameterByIndex
        }
    }.joinToString(separator = ", ")

/**
 * Find the length of the replacement of <>, in a given list of selected symbols.
 */
private fun List<LpSelectedType>.lengthFor() = replacement().length
