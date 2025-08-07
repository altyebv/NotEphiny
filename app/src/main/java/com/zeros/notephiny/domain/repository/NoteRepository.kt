package com.zeros.notephiny.domain.repository

import android.content.Context
import android.util.Log
import com.zeros.notephiny.data.local.NoteDao
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.data.model.CategoryCount
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

class NoteRepository @Inject constructor(
     val dao: NoteDao,
    @ApplicationContext  val context: Context
) {

    fun getDefaultCategories(): List<String> = DefaultCategories

    fun getAppCategories(): List<String> = AppCategories

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    suspend fun insertNote(note: Note) = dao.insertNote(note)


    suspend fun getNoteCountsByCategory(): List<CategoryCount> {
        return dao.getNoteCountsByCategory()
    }

    suspend fun deleteNoteById(noteId: Int) = dao.deleteNoteById(noteId)

    suspend fun getAllCategories(): List<String> = dao.getAllCategories()

    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)


    suspend fun togglePinById(noteId: Int, pinned: Boolean) {
        dao.updatePin(noteId, pinned)
    }

    suspend fun togglePin(note: Note) {
        val updated = note.copy(isPinned = !note.isPinned)
        dao.insertNote(updated) // OnConflict.REPLACE makes this work
    }


    suspend fun getSimilarNotesForNoteId(noteId: Int, topK: Int = 5): List<Note> {
        val currentNote = dao.getNoteById(noteId) ?: return emptyList()
        return getSimilarNotes(currentNote, topK)
    }


    suspend fun getSimilarNotes(
        currentNote: Note,
        topK: Int = 5
    ): List<Note> {
        val targetEmbedding = currentNote.embedding ?: run {
            Log.w("SimilarNotes", "Current note has no embedding. ID=${currentNote.id}")
            return emptyList()
        }

        val candidates = dao.getNotesWithEmbeddingsExcluding(currentNote.id ?: -1)
        if (candidates.isEmpty()) {
            Log.w("SimilarNotes", "No candidate notes found. ID=${currentNote.id}")
            return emptyList()
        }

        Log.d("SimilarNotes", "Checking ${candidates.size} candidates for note ID=${currentNote.id}")

        val scoredNotes = candidates.mapNotNull { note ->
            note.embedding?.let { emb ->
                val semanticScore = OnnxEmbedder.cosineSimilarity(targetEmbedding, emb).toFloat()
                val keywordScore = keywordOverlap(currentNote.content, note.content)
                val finalScore = (0.8f * semanticScore) + (0.2f * keywordScore)

                Log.v("SimilarNotes",
                    "Note ${note.id}: semantic=$semanticScore, keyword=$keywordScore, final=$finalScore"
                )

                note.copy(similarity = finalScore)
            }
        }

        if (scoredNotes.isEmpty()) {
            Log.w("SimilarNotes", "No scored candidates for note ID=${currentNote.id}")
            return emptyList()
        }

        // Dynamic threshold: mean - (stdDev * 0.25), clamped to [0, 1]
        val mean = scoredNotes.map { it.similarity ?: 0f }.average().toFloat()
        val stdDev = kotlin.math.sqrt(
            scoredNotes.map { ((it.similarity ?: 0f) - mean).pow(2) }.average()
        ).toFloat()
        val threshold = (mean - stdDev * 0.25f).coerceIn(0f, 1f)

        Log.d("SimilarNotes", "Mean=$mean, StdDev=$stdDev, Threshold=$threshold")

        val result = scoredNotes
            .sortedByDescending { it.similarity }
            .filterIndexed { index, note ->
                index == 0 || (note.similarity ?: 0f) >= threshold
            }
            .take(topK)

        Log.d("SimilarNotes", "Returning ${result.size} similar notes: ${result.map { it.id }}")

        return result
    }

    private fun keywordOverlap(text1: String, text2: String): Float {
        val words1 = tokenize(text1)
        val words2 = tokenize(text2)
        if (words1.isEmpty() || words2.isEmpty()) return 0f

        val intersection = words1.intersect(words2).size.toFloat()
        val union = words1.union(words2).size.toFloat()
        return intersection / union
    }

    private fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split("\\W+".toRegex())
            .filter { it.isNotBlank() }
            .toSet()



    suspend fun saveNoteWithEmbedding(
        id: Int? = null,
        title: String,
        content: String,
        category: String,
        color: Int,
        createdAt: Long? = null,
        isPinned: Boolean = false,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        val text = "$title $content $category"
        val embedding = OnnxEmbedder.embed(text, context).toList()
        Log.d("NoteRepo", "Embedding vector length: ${embedding.size}")

        val note = Note(
            id = id,
            title = title,
            content = content,
            category = category,
            embedding = embedding,
            color = color,
            createdAt = createdAt ?: System.currentTimeMillis(),
            updatedAt = updatedAt,
            isPinned = isPinned,
        )
        dao.insertNote(note)
    }
}