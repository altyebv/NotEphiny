package com.zeros.notephiny.ai.tokenizer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import com.zeros.notephiny.ai.embedder.OnnxEmbedder


class BertTokenizer(context: Context) {

    private val vocab: Map<String, Int>
    private val unknownToken = "[UNK]"
    private val maxLen = 256

    init {
        val inputStream = context.assets.open("vocab.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        vocab = reader.lineSequence()
            .withIndex()
            .associate { it.value to it.index }
        reader.close()
    }

    fun tokenize(text: String): IntArray {
        val tokens = text
            .lowercase()
            .replace(Regex("[^\\w\\s]"), "")
            .split(" ")
            .map { token -> vocab[token] ?: vocab[unknownToken] ?: 100 }
            .take(maxLen)

        val padded = IntArray(maxLen) { 0 }
        for (i in tokens.indices) {
            padded[i] = tokens[i]
        }

        return padded
    }

    fun attentionMask(tokens: IntArray): IntArray =
        tokens.map { if (it == 0) 0 else 1 }.toIntArray()
}