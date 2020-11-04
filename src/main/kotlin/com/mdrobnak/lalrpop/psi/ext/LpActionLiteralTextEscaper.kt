package com.mdrobnak.lalrpop.psi.ext

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.mdrobnak.lalrpop.psi.LpAction
import com.mdrobnak.lalrpop.psi.LpSelectedType

private data class Mapping(val sourceRange: TextRange, val targetRange: TextRange, val context: Context)

class LpActionLiteralTextEscaper(action: LpAction, private val evalOfAngleBracketsExpression: List<LpSelectedType>) :
    LiteralTextEscaper<LpAction>(action) {

    // pairs = maps from host to decoded buffer
    private lateinit var angleBracketsExpressions: List<Mapping>

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        // get the text from the host
        val txt = this.myHost.text.substring(rangeInsideHost.startOffset, rangeInsideHost.endOffset)
//        println("Input: $txt")
        // get all the text ranges with <> from rust
        angleBracketsExpressions = txt.findAllRanges("<>", evalOfAngleBracketsExpression)

        // replace them all with their replacements, in reverse order
        val result = angleBracketsExpressions.foldRight(txt) { mapping, acc ->
            mapping.sourceRange.replace(acc, evalOfAngleBracketsExpression.replacement(mapping.context))
        }

//        println("Output: $result")

        // add the result string to the builder
        outChars.append(result)

        // can never fail so just return true
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        // last mapping before offsetInDecoded in the decoded(second of the pair)
        val mapping = angleBracketsExpressions.findLast { it.targetRange.startOffset < offsetInDecoded }
            ?: return offsetInDecoded + rangeInsideHost.startOffset

        val result = if (offsetInDecoded < mapping.targetRange.endOffset)
        // inside mapping, return index of `<` in host range
            mapping.sourceRange.startOffset + rangeInsideHost.startOffset
        else
        // outside mapping
            offsetInDecoded - mapping.targetRange.endOffset + mapping.sourceRange.endOffset + rangeInsideHost.startOffset

//        println(
//            "range in host: $rangeInsideHost, ${
//                this.myHost.text.substring(
//                    rangeInsideHost.startOffset,
//                    rangeInsideHost.endOffset
//                )
//            }, offset in decoded: $offsetInDecoded -> $result"
//        )

        return result
    }

    /**
     * @return `true` if the host cannot accept multiline content, `false` otherwise
     */
    override fun isOneLine(): Boolean = false
}


private fun String.findAllRanges(text: String, replacementLength: List<LpSelectedType>): List<Mapping> {
    var prevIndex = -text.length
    var index = this.indexOf(text)
    val ranges = mutableListOf<Mapping>()
    var decodedOffset = 0
    while (index != -1) {
        val decodedStart = decodedOffset + index - (prevIndex + text.length)
        ranges.add(
            Mapping(
                TextRange(index, index + text.length), TextRange(
                    decodedStart,
                    decodedStart + replacementLength.lengthFor(Context.Parentheses)
                ),
                Context.Parentheses
            )
        )
        decodedOffset = decodedStart + replacementLength.lengthFor(Context.Parentheses)
        prevIndex = index
        index = this.indexOf(text, index + text.length)
    }

//    println("Text: $this, ranges: $ranges")

    return ranges
}

private enum class Context {
    Parentheses, Braces
}

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

private fun List<LpSelectedType>.lengthFor(context: Context) = this.replacement(context).length