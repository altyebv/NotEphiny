package com.zeros.notephiny.ai.tokenizer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import com.zeros.notephiny.ai.embedder.OnnxEmbedder

class BertTokenizer(context: Context) {

    private val vocab: Map<String, Int>
    private val unkToken = "[UNK]"
    private val clsToken = "[CLS]"
    private val sepToken = "[SEP]"
    private val maxLen = 256
    private val unkId: Int
    private val clsId: Int
    private val sepId: Int

    init {
        val reader = BufferedReader(InputStreamReader(context.assets.open("vocab.txt")))
        vocab = reader.lineSequence()
            .withIndex()
            .associate { it.value to it.index }
        reader.close()

        unkId = vocab[unkToken] ?: error("[UNK] token missing in vocab")
        clsId = vocab[clsToken] ?: error("[CLS] token missing in vocab")
        sepId = vocab[sepToken] ?: error("[SEP] token missing in vocab")
    }

    fun tokenize(text: String): IntArray {
        val tokens = mutableListOf<Int>()
        tokens.add(clsId)

        val words = text.lowercase()
            .replace(Regex("[^\\w\\s]"), "")
            .split(" ")
            .filter { it.isNotBlank() }

        for (word in words) {
            val subwords = tokenizeWord(word)
            if (tokens.size + subwords.size + 1 > maxLen) break
            tokens.addAll(subwords)
        }

        tokens.add(sepId)

        // Pad to maxLen
        val padded = IntArray(maxLen) { 0 }
        for (i in tokens.indices) {
            padded[i] = tokens[i]
        }

        return padded
    }

    private fun tokenizeWord(word: String): List<Int> {
        val subTokens = mutableListOf<Int>()
        var start = 0

        while (start < word.length) {
            var end = word.length
            var matched: String? = null

            while (start < end) {
                val substr = if (start == 0) word.substring(start, end)
                else "##" + word.substring(start, end)

                if (vocab.containsKey(substr)) {
                    matched = substr
                    break
                }
                end--
            }

            if (matched != null) {
                subTokens.add(vocab[matched]!!)
                start = if (matched.startsWith("##")) start + matched.length - 2 else start + matched.length
            } else {
                subTokens.add(unkId)
                break
            }
        }

        return subTokens
    }

    fun attentionMask(inputIds: IntArray): IntArray =
        inputIds.map { if (it == 0) 0 else 1 }.toIntArray()
}
