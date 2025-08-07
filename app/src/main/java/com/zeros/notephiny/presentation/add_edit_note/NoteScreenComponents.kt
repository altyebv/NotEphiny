package com.zeros.notephiny.presentation.add_edit_note

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.zeros.notephiny.data.model.Note
import kotlinx.coroutines.delay

@Composable
fun RelatedNotesSection(
    relatedNotes: List<Note>,
    onNoteClick: (Note) -> Unit // Pass a callback for navigation
) {
    if (relatedNotes.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Related Notes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(relatedNotes) { note ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(160.dp)
                        .clickable { onNoteClick(note) }, // Handle navigation
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = note.title.ifBlank { "Untitled" },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        val similarity = note.similarity?.coerceIn(0f, 1f) ?: 0f
                        val similarityPercent = (similarity * 100).toInt()

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = similarity,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = when {
                                    similarity > 0.75f -> MaterialTheme.colorScheme.primary
                                    similarity > 0.4f -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Similarity: $similarityPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HighlightedActionsText(
    text: String,
    actions: List<String>,
    style: TextStyle,
    onActionClick: (String, Offset) -> Unit
) {
    val scanCharCount = remember { mutableStateOf(0) }
    var showRealHighlights by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Animate the fake scan (character-based)
    LaunchedEffect(text, actions) {
        showRealHighlights = false
        scanCharCount.value = 0
        for (i in 0..text.length) {
            scanCharCount.value = i
            delay(15) // Faster scan
        }
        delay(300) // Let it linger
        showRealHighlights = true
    }

    val annotatedText = remember(text, actions, scanCharCount.value, showRealHighlights) {
        buildAnnotatedString {
            if (showRealHighlights) {
                // ✅ Original matching logic
                var currentIndex = 0
                val matches = mutableListOf<Pair<IntRange, String>>()

                actions.distinct().forEach { action ->
                    val cleanedAction = action.trim()
                    val regex = Regex(Regex.escape(cleanedAction), RegexOption.IGNORE_CASE)
                    regex.findAll(text).forEach { match ->
                        val range = match.range
                        if (matches.none { it.first.overlaps(range) }) {
                            matches.add(range to match.value)
                        }
                    }
                }

                matches.sortBy { it.first.first }

                for ((range, matchedText) in matches) {
                    if (currentIndex < range.first) {
                        append(text.substring(currentIndex, range.first))
                    }

                    pushStringAnnotation(tag = "ACTION", annotation = matchedText)
                    withStyle(
                        SpanStyle(
                            background = Color(0xFFFFF9C4),
                            color = Color(0xFF222222),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(matchedText)
                    }
                    pop()

                    currentIndex = range.last + 1
                }

                if (currentIndex < text.length) {
                    append(text.substring(currentIndex))
                }
            } else {
                // 🔹 Fake highlighter (character-based)
                for (i in text.indices) {
                    val char = text[i].toString()
                    if (i < scanCharCount.value) {
                        withStyle(
                            SpanStyle(
                                background = Color(0xFFFFF9C4).copy(alpha = 0.3f),
                                color = Color.Black
                            )
                        ) {
                            append(char)
                        }
                    } else {
                        append(char)
                    }
                }
            }
        }
    }

    BasicText(
        text = annotatedText,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(annotatedText) {
                detectTapGestures { offsetPos ->
                    if (!showRealHighlights) return@detectTapGestures
                    textLayoutResult?.let { layout ->
                        val offset = layout.getOffsetForPosition(offsetPos)
                        annotatedText.getStringAnnotations("ACTION", offset, offset)
                            .firstOrNull()?.let { annotation ->
                                val rect = layout.getBoundingBox(offset)
                                val pos = Offset(rect.left, rect.top)
                                onActionClick(annotation.item, pos)
                            }
                    }
                }
            },
        onTextLayout = { textLayoutResult = it }
    )
}

private fun IntRange.overlaps(other: IntRange): Boolean {
    return this.first <= other.last && other.first <= this.last
}

@Composable
fun ActionPopupMenu(
    visible: Boolean,
    position: Offset,
    actionText: String,
    onAddTodo: () -> Unit,
    onDismiss: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // to absorb outside clicks
            .clickable(onClick = { onDismiss() }) // dismiss on any outside tap
    ) {
        if (!visible) return
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(x = 0, y = 64) // Fixed distance from top
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add To-Do",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable {
                            onAddTodo()
                        }
                    )
                    Divider(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable {
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}


