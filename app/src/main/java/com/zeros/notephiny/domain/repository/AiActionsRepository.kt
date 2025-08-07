package com.zeros.notephiny.domain.repository

import android.content.Context
import android.util.Log
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiActionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // 1. Broad set of trigger keywords (verbs, duties, suggestions)
    private val triggerWords = listOf(
        "send", "buy", "schedule", "prepare", "fix", "call", "email",
        "follow", "review", "submit", "book", "plan", "organize", "meet",
        "complete", "finish", "write", "check", "update", "contact",
        "remind", "approve", "assign", "report", "track", "discuss",
        "note", "clean", "build", "deploy", "test", "debug",
        "must", "should", "need to", "have to", "ought to"
    )

    // 2. Embed trigger words once (lazily)
    private val triggerEmbeddings: List<List<Float>> by lazy {
        triggerWords.map { OnnxEmbedder.embed(it, context).toList() }
    }

    // 3. Extract action-like phrases based on embedding similarity
    suspend fun extractActionsFromText(text: String): List<String> = withContext(Dispatchers.Default) {
        Log.d("AI_ACTIONS", ">>> Received text: $text")

        val sentences = preprocessSentences(text)
        val matchedPhrases = mutableListOf<String>()

        for (sentence in sentences) {
            Log.d("AI_ACTIONS", "Checking sentence: $sentence")

            val lowered = sentence.lowercase()
            triggerWords.forEach { keyword ->
                val regex = Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE)
                val match = regex.find(lowered)
                if (match != null) {
                    Log.d("AI_ACTIONS", "Matched keyword: $keyword in $sentence")

                    val words = sentence.split(" ")
                    val keywordIndex = words.indexOfFirst { it.contains(keyword, ignoreCase = true) }
                    if (keywordIndex != -1) {
                        val start = (keywordIndex - 4).coerceAtLeast(0)
                        val end = (keywordIndex + 5).coerceAtMost(words.size)
                        val window = words.subList(start, end).joinToString(" ")
                        matchedPhrases.add(window)
                    }
                }
            }
        }

        Log.d("AI_ACTIONS", "Extracted phrases: $matchedPhrases")

        matchedPhrases.distinct()
    }


    private fun preprocessSentences(text: String): List<String> {
        return text.split(Regex("[.?!\n]"))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 5 && it.split(" ").size >= 3 }
    }


}



