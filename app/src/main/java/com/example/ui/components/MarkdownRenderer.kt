package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily

/**
 * Renders Markdown content as rich, literary Jetpack Compose blocks with interactive task checkboxes,
 * inline formatting (**bold**, *italic*, ~~strikethrough~~, ==highlight==, `code`),
 * blockquotes, code blocks, headings, and lists.
 */
@Composable
fun MarkdownRenderer(
    markdown: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    baseFontSizeSp: Float = 16f,
    useSerifBody: Boolean = true,
    onToggleTaskAtLine: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lines = markdown.lines()
    val bodyFont = if (useSerifBody) LoraFontFamily else PlusJakartaSansFontFamily
    val baseSize: TextUnit = baseFontSizeSp.sp
    val lineHeight: TextUnit = (baseFontSizeSp * 1.65f).sp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var i = 0
        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()

            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    i++
                }

                // Code block ```
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // skip closing ```
                    Surface(
                        color = accentColor.copy(alpha = 0.09f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = accentColor.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = codeLines.joinToString("\n").ifEmpty { "// Kode kosong" },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = (baseFontSizeSp - 2f).coerceAtLeast(12f).sp,
                            lineHeight = (baseFontSizeSp * 1.4f).sp,
                            color = textColor,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Horizontal Divider
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = secondaryColor.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "  ✦  ",
                            color = accentColor,
                            fontSize = 14.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = secondaryColor.copy(alpha = 0.3f)
                        )
                    }
                    i++
                }

                // Heading 1
                trimmed.startsWith("# ") -> {
                    val content = trimmed.removePrefix("# ").trim()
                    Text(
                        text = parseInlineMarkdown(content, accentColor),
                        style = TextStyle(
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseFontSizeSp + 8f).sp,
                            lineHeight = (baseFontSizeSp + 16f).sp,
                            color = textColor
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                    i++
                }

                // Heading 2
                trimmed.startsWith("## ") -> {
                    val content = trimmed.removePrefix("## ").trim()
                    Text(
                        text = parseInlineMarkdown(content, accentColor),
                        style = TextStyle(
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (baseFontSizeSp + 4f).sp,
                            lineHeight = (baseFontSizeSp + 12f).sp,
                            color = textColor
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                    i++
                }

                // Heading 3
                trimmed.startsWith("### ") -> {
                    val content = trimmed.removePrefix("### ").trim()
                    Text(
                        text = parseInlineMarkdown(content, accentColor),
                        style = TextStyle(
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (baseFontSizeSp + 2f).sp,
                            lineHeight = (baseFontSizeSp + 8f).sp,
                            color = accentColor
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    i++
                }

                // Blockquote
                trimmed.startsWith(">") -> {
                    val quoteLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        quoteLines.add(lines[i].trim().removePrefix(">").trim())
                        i++
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                            .background(accentColor.copy(alpha = 0.08f))
                            .padding(end = 14.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(accentColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = accentColor.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(quoteLines.joinToString("\n"), accentColor),
                            style = TextStyle(
                                fontFamily = LoraFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = baseSize,
                                lineHeight = lineHeight,
                                color = textColor.copy(alpha = 0.92f)
                            )
                        )
                    }
                }

                // Task list item (- [ ] or - [x])
                trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                    val isChecked = trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")
                    val itemText = trimmed.substring(6).trim()
                    val lineIndex = i
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = onToggleTaskAtLine != null) {
                                onToggleTaskAtLine?.invoke(lineIndex)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (isChecked) "Tugas selesai" else "Tugas belum selesai",
                            tint = if (isChecked) accentColor else secondaryColor,
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(itemText, accentColor),
                            style = TextStyle(
                                fontFamily = bodyFont,
                                fontSize = baseSize,
                                lineHeight = lineHeight,
                                color = if (isChecked) secondaryColor else textColor,
                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                    }
                    i++
                }

                // Unordered bullet list
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val itemText = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseFontSizeSp + 2f).sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(itemText, accentColor),
                            style = TextStyle(
                                fontFamily = bodyFont,
                                fontSize = baseSize,
                                lineHeight = lineHeight,
                                color = textColor
                            )
                        )
                    }
                    i++
                }

                // Ordered numbered list
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIndex = trimmed.indexOf('.')
                    val numPrefix = trimmed.substring(0, dotIndex + 1)
                    val itemText = trimmed.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = numPrefix,
                            color = accentColor,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = baseSize,
                            modifier = Modifier.padding(end = 10.dp, top = 1.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(itemText, accentColor),
                            style = TextStyle(
                                fontFamily = bodyFont,
                                fontSize = baseSize,
                                lineHeight = lineHeight,
                                color = textColor
                            )
                        )
                    }
                    i++
                }

                // Standard paragraph
                else -> {
                    Text(
                        text = parseInlineMarkdown(rawLine, accentColor),
                        style = TextStyle(
                            fontFamily = bodyFont,
                            fontSize = baseSize,
                            lineHeight = lineHeight,
                            color = textColor
                        )
                    )
                    i++
                }
            }
        }
    }
}

/**
 * Parses inline Markdown spans:
 * - **bold**
 * - *italic*
 * - ~~strikethrough~~
 * - ==highlight==
 * - `inline code`
 */
fun parseInlineMarkdown(text: String, accentColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var idx = 0
        val len = text.length
        while (idx < len) {
            when {
                // Bold **...**
                text.startsWith("**", idx) -> {
                    val end = text.indexOf("**", idx + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(idx + 2, end))
                        }
                        idx = end + 2
                    } else {
                        append("**")
                        idx += 2
                    }
                }
                // Highlight ==...==
                text.startsWith("==", idx) -> {
                    val end = text.indexOf("==", idx + 2)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                background = accentColor.copy(alpha = 0.24f),
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(" ${text.substring(idx + 2, end)} ")
                        }
                        idx = end + 2
                    } else {
                        append("==")
                        idx += 2
                    }
                }
                // Strikethrough ~~...~~
                text.startsWith("~~", idx) -> {
                    val end = text.indexOf("~~", idx + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(idx + 2, end))
                        }
                        idx = end + 2
                    } else {
                        append("~~")
                        idx += 2
                    }
                }
                // Inline code `...`
                text.startsWith("`", idx) -> {
                    val end = text.indexOf("`", idx + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                background = accentColor.copy(alpha = 0.14f),
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(" ${text.substring(idx + 1, end)} ")
                        }
                        idx = end + 1
                    } else {
                        append("`")
                        idx += 1
                    }
                }
                // Italic *...*
                text.startsWith("*", idx) -> {
                    val end = text.indexOf("*", idx + 1)
                    if (end != -1 && end > idx + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(idx + 1, end))
                        }
                        idx = end + 1
                    } else {
                        append("*")
                        idx += 1
                    }
                }
                else -> {
                    append(text[idx])
                    idx++
                }
            }
        }
    }
}

/**
 * Toggles a `- [ ]` or `- [x]` checkbox at [lineIndex] in a Markdown string.
 */
fun toggleMarkdownCheckboxAtLine(markdown: String, lineIndex: Int): String {
    val lines = markdown.lines().toMutableList()
    if (lineIndex !in lines.indices) return markdown
    val current = lines[lineIndex]
    lines[lineIndex] = when {
        current.contains("- [ ] ") -> current.replaceFirst("- [ ] ", "- [x] ")
        current.contains("- [x] ") -> current.replaceFirst("- [x] ", "- [ ] ")
        current.contains("- [X] ") -> current.replaceFirst("- [X] ", "- [ ] ")
        else -> current
    }
    return lines.joinToString("\n")
}
