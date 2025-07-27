package com.zeros.notephiny.ai.embedder


import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.util.Log
import com.zeros.notephiny.ai.tokenizer.BertTokenizer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer
import javax.inject.Inject

class OnnxEmbedder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val tokenizer = BertTokenizer(context)

    init {
        val modelInputStream = context.assets.open("minilm.onnx")
        val tempModelFile = File.createTempFile("minilm", ".onnx", context.cacheDir)
        modelInputStream.use { input ->
            FileOutputStream(tempModelFile).use { output ->
                input.copyTo(output)
            }
        }
        session = ortEnvironment.createSession(tempModelFile.absolutePath)
    }

    fun embed(text: String): FloatArray {
        val tokens = tokenizer.tokenize(text)

        val inputIds = tokens.map { it.toLong() }.toLongArray()
        val attentionMask = LongArray(inputIds.size) { 1L }
        val tokenTypeIds = LongArray(inputIds.size) { 0L }

        val shape = longArrayOf(1, inputIds.size.toLong())

        val inputIdsTensor = OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(inputIds), shape)
        val attentionMaskTensor = OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(attentionMask), shape)
        val tokenTypeIdsTensor = OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(tokenTypeIds), shape)

        val inputMap = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attentionMaskTensor,
            "token_type_ids" to tokenTypeIdsTensor
        )

        val results = session.run(inputMap)
        val output = results[0].value as Array<Array<FloatArray>>
        return output[0][0] // CLS token
    }
}

