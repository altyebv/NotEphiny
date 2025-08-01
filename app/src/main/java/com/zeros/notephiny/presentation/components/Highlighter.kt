package com.zeros.notephiny.presentation.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun HighlightedText(text: String, query: String, style: TextStyle) {
    if (query.isBlank()) {
        Text(text = text, style = style)
        return
    }

//    val highlightStyle = SpanStyle(
//        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
//    )
    val highlightStyle = SpanStyle(
        background = Color(0xFFD1C4E9).copy(alpha = 0.5f) // Soft violet
    )

    val annotatedText = remember(text, query, highlightStyle) {
        buildAnnotatedString {
            val regex = Regex("(?i)${Regex.escape(query)}")
            var lastIndex = 0

            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1

                append(text.substring(lastIndex, start))

                withStyle(highlightStyle) {
                    append(text.substring(start, end))
                }

                lastIndex = end
            }

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    Text(text = annotatedText, style = style)
}






