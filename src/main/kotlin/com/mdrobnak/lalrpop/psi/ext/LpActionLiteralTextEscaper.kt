package com.mdrobnak.lalrpop.psi.ext

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.mdrobnak.lalrpop.psi.LpAction

// TODO: change the evalOfAngleBracketsExpression to the list of selected symbols in the alternative
class LpActionLiteralTextEscaper(action: LpAction, private val evalOfAngleBracketsExpression: String) :
    LiteralTextEscaper<LpAction>(action) {

    // pairs = maps from host to decoded buffer
    lateinit var angleBracketsExpressions: List<Pair<TextRange, TextRange>>

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        // get the text from the host
        val txt = this.myHost.text.substring(rangeInsideHost.startOffset, rangeInsideHost.endOffset)
//        println("Input: $txt")
        // get all the text ranges with <> from rust
        angleBracketsExpressions = txt.findAllRanges("<>", evalOfAngleBracketsExpression.length)

        // replace them all with their replacements, in reverse order
        val result = angleBracketsExpressions.foldRight(txt) { pair, acc ->
            pair.first.replace(acc, evalOfAngleBracketsExpression)
        }

//        println("Output: $result")

        // add the result string to the builder
        outChars.append(result)

        // can never fail so just return true
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        // last mapping before offsetInDecoded in the decoded(second of the pair)
        val pair = angleBracketsExpressions.findLast { it.second.startOffset < offsetInDecoded }
            ?: return offsetInDecoded + rangeInsideHost.startOffset

        val result = if (offsetInDecoded < pair.second.endOffset)
        // inside mapping, return index of `<` in host range
            pair.first.startOffset + rangeInsideHost.startOffset
        else
        // outside mapping
            offsetInDecoded - pair.second.endOffset + pair.first.endOffset + rangeInsideHost.startOffset

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


fun String.findAllRanges(text: String, replacementLength: Int): List<Pair<TextRange, TextRange>> {
    var prevIndex = -text.length
    var index = this.indexOf(text)
    val ranges = mutableListOf<Pair<TextRange, TextRange>>()
    var decodedOffset = 0
    while (index != -1) {
        val decodedStart = decodedOffset + index - (prevIndex + text.length)
        ranges.add(
            TextRange(index, index + text.length) to TextRange(
                decodedStart,
                decodedStart + replacementLength
            )
        )
        decodedOffset = decodedStart + replacementLength
        prevIndex = index
        index = this.indexOf(text, index + text.length)
    }

//    println("Text: $this, ranges: $ranges")

    return ranges
}