package com.zeros.notephiny.ai.embedder


import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer
import javax.inject.Inject
import kotlin.math.sqrt

import com.zeros.notephiny.ai.tokenizer.BertTokenizer


object OnnxEmbedder {
    private var ortEnvironment: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var modelFile: File? = null


    fun initialize(context: Context) {
        if (ortEnvironment != null && session != null) return // already initialized

        ortEnvironment = OrtEnvironment.getEnvironment()

        val inputStream = context.assets.open("minilm.onnx")
        modelFile = File(context.cacheDir, "minilm.onnx")
        inputStream.use { input ->
            FileOutputStream(modelFile!!).use { output ->
                input.copyTo(output)
            }
        }

        session = ortEnvironment!!.createSession(modelFile!!.absolutePath)
    }

    fun embed(text: String, context: Context): FloatArray {
        val env = ortEnvironment ?: throw IllegalStateException("OnnxEmbedder not initialized")
        val sess = session ?: throw IllegalStateException("OnnxEmbedder not initialized")

        val tokenizer = BertTokenizer(context)

        // Get tokenized input (already padded to maxLen)
        val inputIds: IntArray = tokenizer.tokenize(text)
        val attentionMask: IntArray = tokenizer.attentionMask(inputIds)
        val tokenTypeIds: IntArray = IntArray(inputIds.size) { 0 } // all zeros for single sentence

        // Convert to LongArray for ONNX
        val inputIdsLong = inputIds.map { it.toLong() }.toLongArray()
        val attentionMaskLong = attentionMask.map { it.toLong() }.toLongArray()
        val tokenTypeIdsLong = tokenTypeIds.map { it.toLong() }.toLongArray()
        val shape = longArrayOf(1, inputIdsLong.size.toLong()) // [1, maxLen]

        // Prepare tensors and run inference
        OnnxTensor.createTensor(env, LongBuffer.wrap(inputIdsLong), shape).use { inputIdsTensor ->
            OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMaskLong), shape).use { attentionMaskTensor ->
                OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIdsLong), shape).use { tokenTypeIdsTensor ->

                    val inputMap = mapOf(
                        "input_ids" to inputIdsTensor,
                        "attention_mask" to attentionMaskTensor,
                        "token_type_ids" to tokenTypeIdsTensor
                    )

                    sess.run(inputMap).use { results ->
                        val output = results[0].value as Array<Array<FloatArray>>
                        val embedding = output[0][0] // Use [CLS] token's embedding

                        return embedding
                    }
                }
            }
        }
    }



    fun cosineSimilarity(a: List<Float>, b: List<Float>): Double {
        if (a.size != b.size) return 0.0

        val dotProduct = a.zip(b).sumOf { (x, y) -> x.toDouble() * y.toDouble() }
        val normA = sqrt(a.sumOf { it.toDouble() * it.toDouble() })
        val normB = sqrt(b.sumOf { it.toDouble() * it.toDouble() })

        return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (normA * normB)
    }

    fun close() {
        session?.close()
        ortEnvironment?.close()
        modelFile?.delete()

        session = null
        ortEnvironment = null
        modelFile = null
    }
}

