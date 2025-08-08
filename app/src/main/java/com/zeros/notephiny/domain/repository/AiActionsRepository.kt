package com.zeros.notephiny.domain.repository

import android.content.Context
import android.util.Log
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.sqrt

@Singleton
class AiActionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class ActionCandidate(
        val text: String,
        val confidence: Float,         // 0f..1f
        val matchedKeyword: String?,   // which trigger matched (if any)
        val source: String             // "keyword", "semantic", or "hybrid"
    )

    // ---- 1) Triggers & prototypes (tweak these lists to improve recall) ----
    private val triggerWords = listOf(
        "send", "buy", "schedule", "prepare", "fix", "call", "email",
        "follow", "review", "submit", "book", "plan", "organize", "meet",
        "complete", "finish", "write", "check", "update", "contact",
        "remind", "approve", "assign", "report", "track", "discuss",
        "note", "clean", "build", "deploy", "test", "debug",
        "must", "should", "need to", "have to", "ought to"
    )

    // short prototype action phrases - used for semantic matching
    private val protoActions = listOf(
        "send an email", "buy groceries", "schedule a meeting", "call someone",
        "write a report", "fix bug", "review code", "book a flight", "prepare slides",
        "follow up", "submit report", "test feature"
    )

    // ---- 2) Small LRU cache for sentence embeddings (so we don't embed repeatedly) ----
    private val embeddingCache = object : LinkedHashMap<String, FloatArray>(128, 0.75f, true) {
        private val MAX = 256
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FloatArray>?): Boolean {
            return size > MAX
        }
    }

    // stored precomputed prototype embeddings (normalized)
    @Volatile
    private var protoEmbeddings: List<FloatArray>? = null

    // Optionally warm-up trigger/proto embeddings in background
    init {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                warmUpEmbeddings()
            } catch (t: Throwable) {
                Log.w("AI_ACTIONS", "Warmup failed: ${t.message}")
            }
        }
    }

    private suspend fun warmUpEmbeddings() = withContext(Dispatchers.Default) {
        // compute proto embeddings (normalized) and store
        protoEmbeddings = protoActions.map { text ->
            val raw = embedTextSync(text)
            normalize(raw)
            raw
        }
        // (optional) also compute trigger word embeddings if you want per-keyword similarity
        Log.d("AI_ACTIONS", "Proto embeddings warmed up: ${protoEmbeddings?.size}")
    }

    // ---- 3) Public API: returns rich candidates (keeps backward-compatibility via helper) ----
    suspend fun extractActionCandidates(text: String, minConfidence: Float = 0.30f): List<ActionCandidate> =
        withContext(Dispatchers.Default) {

            Log.d("AI_ACTIONS", ">>> extractActionCandidates text length=${text.length}")

            if (text.isBlank()) return@withContext emptyList()

            val cleaned = removeQuotedBlocks(text)
            val sentences = preprocessSentences(cleaned)

            val protoEmbs = protoEmbeddings // may be null if warmup not done yet

            val candidates = mutableListOf<ActionCandidate>()

            for (sentence in sentences) {
                val lowered = sentence.lowercase(Locale.getDefault())

                // 1) Keyword detection
                var matchedKeyword: String? = null
                for (keyword in triggerWords) {
                    val regex = Regex("\\b${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                    if (regex.containsMatchIn(lowered)) {
                        matchedKeyword = keyword
                        break
                    }
                }

                // 2) Semantic similarity (compare against prototypes)
                val sentEmb = getOrComputeEmbedding(sentence)
                val semSimScaled = protoEmbs?.let { protos ->
                    // compute maximum cosine with any prototype
                    var max = -1f
                    for (p in protos) {
                        // both p and sentEmb are normalized -> cosine is dot product
                        val d = dot(p, sentEmb)
                        if (d > max) max = d
                    }
                    // max might be -1..1; map to 0..1 for easier weights
                    ((max.coerceIn(-1f, 1f) + 1f) / 2f)
                } ?: 0f

                // 3) Compose a hybrid confidence:
                // If keyword present -> some baseline, but semantic similarity moves the score strongly.
                val keywordScore = if (matchedKeyword != null) 1f else 0f
                val finalScore = (semSimScaled * 0.75f) + (keywordScore * 0.25f)
                // You can tune the 0.75/0.25 weights to prefer semantic or keyword behavior.

                if (finalScore >= minConfidence) {
                    // extract a compact clause around the keyword if we have one,
                    // otherwise return the whole sentence (trim to reasonable length)
                    val extracted = if (matchedKeyword != null) {
                        extractClauseAroundKeyword(sentence, matchedKeyword)
                    } else {
                        sentence.trim().take(250)
                    }

                    val source = when {
                        matchedKeyword != null && semSimScaled > 0.5f -> "hybrid"
                        matchedKeyword != null -> "keyword"
                        else -> "semantic"
                    }

                    candidates.add(
                        ActionCandidate(
                            text = extracted.trim(),
                            confidence = finalScore.coerceIn(0f, 1f),
                            matchedKeyword = matchedKeyword,
                            source = source
                        )
                    )
                }
            }

            // ---- 4) dedupe & keep best confidence per normalized text ----
            val bestByText = HashMap<String, ActionCandidate>()
            for (c in candidates) {
                val key = c.text.lowercase(Locale.getDefault()).replace(Regex("\\s+"), " ").trim()
                val existing = bestByText[key]
                if (existing == null || c.confidence > existing.confidence) {
                    bestByText[key] = c
                }
            }

            val out = bestByText.values
                .sortedByDescending { it.confidence }
                .take(12) // keep top-N
                .toList()

            Log.d("AI_ACTIONS", "Candidates found: ${out.size} -> ${out.map { it.text }}")
            out
        }

    // Backwards-compatible helper used by your UI code that expects List<String>
    suspend fun extractActionsFromText(
        text: String,
        minConfidence: Float = 0.30f
    ): List<String> = withContext(Dispatchers.Default) {
        val candidates = extractActionCandidates(text, minConfidence)
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }

        // Convert matches to ranges
        val matches = mutableListOf<IntRange>()
        candidates.forEach { phrase ->
            val regex = Regex("\\b${Regex.escape(phrase)}\\b", RegexOption.IGNORE_CASE)
            regex.findAll(text).forEach { match ->
                matches.add(match.range)
            }
        }

        // Merge overlapping ranges
        val mergedRanges = matches.sortedBy { it.first }.fold(mutableListOf<IntRange>()) { acc, range ->
            if (acc.isEmpty() || acc.last().last < range.first) {
                acc.add(range)
            } else {
                acc[acc.lastIndex] = acc.last().first..maxOf(acc.last().last, range.last)
            }
            acc
        }

        // Extract the final clean phrases
        mergedRanges.map { range -> text.substring(range).trim() }
    }



    // ---------------------- Helpers ----------------------

    private fun removeQuotedBlocks(text: String): String {
        // remove double-quoted and single-quoted blocks (simple approach)
        return text.replace(Regex("\"[^\"]*\"|'[^']*'|“[^”]*”"), " ")
    }

    private fun preprocessSentences(text: String): List<String> {
        // split on punctuation and newlines, filter out tiny fragments
        return text.split(Regex("[\\n\\.\\?!]"))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 6 && it.split(Regex("\\s+")).size >= 3 }
    }

    private fun extractClauseAroundKeyword(sentence: String, keyword: String): String {
        val lowered = sentence.lowercase(Locale.getDefault())
        val idx = lowered.indexOf(keyword.lowercase(Locale.getDefault()))
        if (idx < 0) return sentence.trim().take(250)

        // extend to nearest punctuation/comma boundaries
        val leftPunct = listOf('.', ',', ';', ':', '—', '(', '[', '\n')
        val rightPunct = listOf('.', ',', ';', ':', '—', ')', ']', '\n')

        val start = (sentence.lastIndexOfAny(leftPunct.toCharArray(), idx).let { if (it == -1) 0 else it + 1 }).coerceAtLeast(0)
        val end = (sentence.indexOfAny(rightPunct.toCharArray(), idx).let { if (it == -1) sentence.length else it }).coerceAtMost(sentence.length)

        return sentence.substring(start, end).trim().take(250)
    }

    // compute-or-get embedding, normalized and cached
    private fun getOrComputeEmbedding(text: String): FloatArray {
        val key = text.hashCode().toString()  // simple key — use better if you persist
        synchronized(embeddingCache) {
            embeddingCache[key]?.let { return it }
        }

        val raw = embedTextSync(text) // compute (sync); assumed to be relatively fast
        normalize(raw)
        synchronized(embeddingCache) {
            embeddingCache[key] = raw
        }
        return raw
    }

    // Assumes OnnxEmbedder.embed(text, context) -> FloatArray or List<Float>
    private fun embedTextSync(text: String): FloatArray {
        // Keep this small and synchronous — OnnxEmbedder should be a fast local call.
        // If your embed method is suspend, adapt this accordingly.
        val raw = OnnxEmbedder.embed(text, context) // adjust if signature differs
        return when (raw) {
            is FloatArray -> raw
            is List<*> -> (raw as List<Number>).map { it.toFloat() }.toFloatArray()
            else -> throw IllegalStateException("Unexpected embedding type: ${raw?.javaClass}")
        }
    }

    // numeric helpers
    private fun dot(a: FloatArray, b: FloatArray): Float {
        val n = minOf(a.size, b.size)
        var s = 0f
        var i = 0
        while (i < n) {
            s += a[i] * b[i]
            i++
        }
        return s
    }

    private fun normalize(v: FloatArray) {
        var s = 0f
        var i = 0
        while (i < v.size) { s += v[i] * v[i]; i++ }
        val norm = sqrt(s)
        if (norm > 1e-8f) {
            i = 0
            while (i < v.size) {
                v[i] = v[i] / norm
                i++
            }
        }
    }
}

