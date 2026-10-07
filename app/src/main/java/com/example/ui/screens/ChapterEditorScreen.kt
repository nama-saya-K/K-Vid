package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookEntity
import com.example.data.BookRepository
import com.example.data.ChapterEntity
import com.example.data.ReaderPaperTheme
import com.example.ui.EditorViewMode
import com.example.ui.components.MarkdownRenderer
import com.example.ui.components.toggleMarkdownCheckboxAtLine
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily

@Composable
fun ChapterEditorScreen(
    book: BookEntity?,
    chapter: ChapterEntity?,
    bookChapters: List<ChapterEntity>,
    editorMode: EditorViewMode,
    paperTheme: ReaderPaperTheme,
    readerFontSizeSp: Float,
    useSerifInReader: Boolean,
    onEditorModeChange: (EditorViewMode) -> Unit,
    onPaperThemeChange: (ReaderPaperTheme) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onToggleReaderFontStyle: () -> Unit,
    onSaveChapter: (
        chapter: ChapterEntity,
        newTitle: String,
        newContent: String,
        newMood: String,
        newStatus: String,
        newBookmarked: Boolean,
        showToast: Boolean
    ) -> Unit,
    onSelectChapter: (Long) -> Unit,
    onNavigatePrevNext: (Boolean) -> Unit,
    onSaveSelectionAsQuote: (String) -> Unit,
    onBackToBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToBook)

    if (book == null || chapter == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih Buku atau Bab untuk Mulai Menulis",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = onBackToBook) {
                    Text("Buka Pustaka Buku")
                }
            }
        }
        return
    }

    var titleText by remember(chapter.id) { mutableStateOf(chapter.title) }
    var textFieldValue by remember(chapter.id) {
        mutableStateOf(TextFieldValue(text = chapter.contentMarkdown))
    }
    var moodTag by remember(chapter.id) { mutableStateOf(chapter.moodTag) }
    var status by remember(chapter.id) { mutableStateOf(chapter.status) }
    var isBookmarked by remember(chapter.id) { mutableStateOf(chapter.isBookmarked) }
    var showLivePreviewInVisual by rememberSaveable { mutableStateOf(true) }

    // Keep state synced if checkbox is toggled or external update arrives
    LaunchedEffect(chapter.id) {
        titleText = chapter.title
        textFieldValue = TextFieldValue(text = chapter.contentMarkdown)
        moodTag = chapter.moodTag
        status = chapter.status
        isBookmarked = chapter.isBookmarked
    }

    val liveWordCount = remember(textFieldValue.text) {
        BookRepository.countWords(textFieldValue.text)
    }
    val liveCharCount = textFieldValue.text.length
    val liveReadMinutes = BookRepository.estimateReadingMinutes(liveWordCount)

    val sortedChapters = remember(bookChapters) { bookChapters.sortedBy { it.chapterNumber } }
    val currentChapterIdx = sortedChapters.indexOfFirst { it.id == chapter.id }
    val hasPrevChapter = currentChapterIdx > 0
    val hasNextChapter = currentChapterIdx != -1 && currentChapterIdx < sortedChapters.lastIndex

    fun persistNow(showToast: Boolean = false) {
        onSaveChapter(
            chapter,
            titleText,
            textFieldValue.text,
            moodTag,
            status,
            isBookmarked,
            showToast
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chapter_editor_screen")
    ) {
        // Top Editorial Bar
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                persistNow(showToast = false)
                                onBackToBook()
                            },
                            modifier = Modifier.testTag("editor_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke daftar bab"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Bab ${chapter.chapterNumber}: $titleText",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Status Cycle Chip
                        Surface(
                            color = when (status) {
                                "Selesai" -> MaterialTheme.colorScheme.tertiaryContainer
                                "Revisi" -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    status = when (status) {
                                        "Draf" -> "Revisi"
                                        "Revisi" -> "Selesai"
                                        else -> "Draf"
                                    }
                                    persistNow(showToast = false)
                                }
                                .testTag("chapter_status_badge")
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                persistNow(showToast = false)
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Tandai Bab",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { persistNow(showToast = true) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("save_chapter_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Simpan",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Mode Switcher: Editor Kaya (Tiptap Visual) | Markdown | Mode Baca
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EditorViewMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = editorMode == mode,
                            onClick = {
                                persistNow(showToast = false)
                                onEditorModeChange(mode)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = EditorViewMode.entries.size
                            ),
                            modifier = Modifier.testTag("mode_tab_${mode.name}")
                        ) {
                            Text(mode.label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Main Content Area according to Mode
        when (editorMode) {
            EditorViewMode.VISUAL -> {
                VisualBridgeEditorContent(
                    titleText = titleText,
                    onTitleChange = {
                        titleText = it
                        persistNow(showToast = false)
                    },
                    textFieldValue = textFieldValue,
                    onTextFieldValueChange = {
                        textFieldValue = it
                        persistNow(showToast = false)
                    },
                    showLivePreview = showLivePreviewInVisual,
                    onToggleLivePreview = { showLivePreviewInVisual = !showLivePreviewInVisual },
                    liveWordCount = liveWordCount,
                    liveCharCount = liveCharCount,
                    liveReadMinutes = liveReadMinutes,
                    onSaveQuote = { quote ->
                        onSaveSelectionAsQuote(quote)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            EditorViewMode.MARKDOWN -> {
                RawMarkdownEditorContent(
                    titleText = titleText,
                    onTitleChange = {
                        titleText = it
                        persistNow(showToast = false)
                    },
                    textFieldValue = textFieldValue,
                    onTextFieldValueChange = {
                        textFieldValue = it
                        persistNow(showToast = false)
                    },
                    liveWordCount = liveWordCount,
                    liveCharCount = liveCharCount,
                    liveReadMinutes = liveReadMinutes,
                    modifier = Modifier.weight(1f)
                )
            }

            EditorViewMode.BACA -> {
                BookReaderModeContent(
                    book = book,
                    chapterNumber = chapter.chapterNumber,
                    chapterTitle = titleText,
                    markdown = textFieldValue.text,
                    moodTag = moodTag,
                    status = status,
                    wordCount = liveWordCount,
                    readMinutes = liveReadMinutes,
                    paperTheme = paperTheme,
                    fontSizeSp = readerFontSizeSp,
                    useSerif = useSerifInReader,
                    hasPrevChapter = hasPrevChapter,
                    hasNextChapter = hasNextChapter,
                    bookChapters = sortedChapters,
                    currentChapterId = chapter.id,
                    onPaperThemeChange = onPaperThemeChange,
                    onAdjustFontSize = onAdjustFontSize,
                    onToggleFontStyle = onToggleReaderFontStyle,
                    onToggleCheckboxAtLine = { lineIndex ->
                        val updatedMd = toggleMarkdownCheckboxAtLine(textFieldValue.text, lineIndex)
                        textFieldValue = textFieldValue.copy(text = updatedMd)
                        persistNow(showToast = false)
                    },
                    onSelectChapter = { targetId ->
                        persistNow(showToast = false)
                        onSelectChapter(targetId)
                    },
                    onPrevChapter = {
                        persistNow(showToast = false)
                        onNavigatePrevNext(false)
                    },
                    onNextChapter = {
                        persistNow(showToast = false)
                        onNavigatePrevNext(true)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VisualBridgeEditorContent(
    titleText: String,
    onTitleChange: (String) -> Unit,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    showLivePreview: Boolean,
    onToggleLivePreview: () -> Unit,
    liveWordCount: Int,
    liveCharCount: Int,
    liveReadMinutes: Int,
    onSaveQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Rich Text Tiptap-Bridge Formatting Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FormatToolbarButton(
                        label = "H1",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "# "))
                        }
                    )
                }
                item {
                    FormatToolbarButton(
                        label = "H2",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "## "))
                        }
                    )
                }
                item {
                    FormatToolbarButton(
                        label = "H3",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "### "))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.FormatBold,
                        contentDescription = "Tebal",
                        onClick = {
                            onTextFieldValueChange(wrapSelectionWith(textFieldValue, "**", "**", "teks tebal"))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.FormatItalic,
                        contentDescription = "Miring",
                        onClick = {
                            onTextFieldValueChange(wrapSelectionWith(textFieldValue, "*", "*", "teks miring"))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.Highlight,
                        contentDescription = "Sorotan",
                        onClick = {
                            onTextFieldValueChange(wrapSelectionWith(textFieldValue, "==", "==", "sorotan penting"))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.FormatStrikethrough,
                        contentDescription = "Coret",
                        onClick = {
                            onTextFieldValueChange(wrapSelectionWith(textFieldValue, "~~", "~~", "dicoret"))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.FormatQuote,
                        contentDescription = "Kutipan",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "> "))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Daftar Poin",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "- "))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.CheckBox,
                        contentDescription = "Daftar Tugas",
                        onClick = {
                            onTextFieldValueChange(insertLinePrefix(textFieldValue, "- [ ] "))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.Code,
                        contentDescription = "Kode",
                        onClick = {
                            onTextFieldValueChange(wrapSelectionWith(textFieldValue, "`", "`", "kode"))
                        }
                    )
                }
                item {
                    FormatToolbarIconButton(
                        icon = Icons.Default.HorizontalRule,
                        contentDescription = "Garis Pemisah",
                        onClick = {
                            onTextFieldValueChange(insertBlockSnippet(textFieldValue, "\n---\n"))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = showLivePreview,
                        onClick = onToggleLivePreview,
                        label = { Text("Pratinjau Kertas") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Chapter Title Input
            OutlinedTextField(
                value = titleText,
                onValueChange = onTitleChange,
                label = { Text("Judul Bab") },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chapter_title_editor_input")
            )

            // Main Rich-Markdown Bridge TextField
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = onTextFieldValueChange,
                label = { Text("Isi Tulisan (Mendukung Format Visual & Markdown)") },
                textStyle = TextStyle(
                    fontFamily = LoraFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                minLines = if (showLivePreview) 8 else 15,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chapter_body_editor_input")
            )

            // Quick Quote Extraction Action if user selected text or wants to save snippet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$liveWordCount kata • $liveCharCount karakter • ~$liveReadMinutes mnt baca",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val selectedSnippet = remember(textFieldValue.selection, textFieldValue.text) {
                    val min = textFieldValue.selection.min.coerceAtLeast(0)
                    val max = textFieldValue.selection.max.coerceAtMost(textFieldValue.text.length)
                    if (max > min) textFieldValue.text.substring(min, max).trim() else ""
                }

                if (selectedSnippet.isNotBlank()) {
                    TextButton(onClick = { onSaveQuote(selectedSnippet) }) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan Teks Terpilih ke Kutipan")
                    }
                }
            }

            // Live Visual Paper Preview (with interactive checkboxes!)
            if (showLivePreview) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_preview_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✦ Pratinjau Halaman Buku (Interaktif)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ketuk kotak tugas untuk mencentang",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                        MarkdownRenderer(
                            markdown = textFieldValue.text,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            accentColor = MaterialTheme.colorScheme.primary,
                            baseFontSizeSp = 16f,
                            useSerifBody = true,
                            onToggleTaskAtLine = { lineIdx ->
                                val updated = toggleMarkdownCheckboxAtLine(textFieldValue.text, lineIdx)
                                onTextFieldValueChange(textFieldValue.copy(text = updated))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun RawMarkdownEditorContent(
    titleText: String,
    onTitleChange: (String) -> Unit,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    liveWordCount: Int,
    liveCharCount: Int,
    liveReadMinutes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mode Markdown Murni (.md)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${textFieldValue.text.lines().size} baris • $liveWordCount kata • ~$liveReadMinutes mnt",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = titleText,
            onValueChange = onTitleChange,
            label = { Text("Judul Bab") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
            textStyle = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("raw_markdown_input"),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun BookReaderModeContent(
    book: BookEntity,
    chapterNumber: Int,
    chapterTitle: String,
    markdown: String,
    moodTag: String,
    status: String,
    wordCount: Int,
    readMinutes: Int,
    paperTheme: ReaderPaperTheme,
    fontSizeSp: Float,
    useSerif: Boolean,
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
    bookChapters: List<ChapterEntity>,
    currentChapterId: Long,
    onPaperThemeChange: (ReaderPaperTheme) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onToggleFontStyle: () -> Unit,
    onToggleCheckboxAtLine: (Int) -> Unit,
    onSelectChapter: (Long) -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(paperTheme.bgColor)
            .testTag("book_reader_view")
    ) {
        // Reader Appearance Controls Bar
        Surface(
            color = paperTheme.bgColor,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Paper Theme Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReaderPaperTheme.entries.forEach { theme ->
                            val isSelected = paperTheme == theme
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) paperTheme.quoteAccentColor.copy(alpha = 0.18f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) paperTheme.quoteAccentColor else paperTheme.secondaryColor.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { onPaperThemeChange(theme) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(theme.bgColor)
                                        .border(1.dp, theme.textColor.copy(alpha = 0.4f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = theme.label.removePrefix("Kertas "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = paperTheme.textColor
                                )
                            }
                        }
                    }

                    // Font Size & Serif Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(onClick = { onAdjustFontSize(-1.5f) }) {
                            Text("A-", color = paperTheme.textColor, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { onAdjustFontSize(1.5f) }) {
                            Text("A+", color = paperTheme.textColor, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onToggleFontStyle) {
                            Text(
                                text = if (useSerif) "Serif" else "Sans",
                                color = paperTheme.quoteAccentColor,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Quick Chapter Jumper Strip
                if (bookChapters.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(bookChapters.size) { index ->
                            val ch = bookChapters[index]
                            val selected = ch.id == currentChapterId
                            Surface(
                                color = if (selected) paperTheme.quoteAccentColor else paperTheme.secondaryColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onSelectChapter(ch.id) }
                            ) {
                                Text(
                                    text = "Bab ${ch.chapterNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else paperTheme.textColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = paperTheme.secondaryColor.copy(alpha = 0.2f))

        // Literary Book Page Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Running Header (Book Title & Chapter Meta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${book.title.uppercase()} — BAB $chapterNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = paperTheme.secondaryColor,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$moodTag • ~$readMinutes mnt baca",
                    style = MaterialTheme.typography.labelSmall,
                    color = paperTheme.quoteAccentColor
                )
            }

            // Chapter Main Title
            Text(
                text = chapterTitle,
                style = TextStyle(
                    fontFamily = LoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSizeSp + 8f).sp,
                    lineHeight = (fontSizeSp + 16f).sp,
                    color = paperTheme.textColor
                )
            )

            HorizontalDivider(
                color = paperTheme.quoteAccentColor.copy(alpha = 0.35f),
                modifier = Modifier.width(72.dp)
            )

            // Rendered Markdown Content
            MarkdownRenderer(
                markdown = markdown,
                textColor = paperTheme.textColor,
                secondaryColor = paperTheme.secondaryColor,
                accentColor = paperTheme.quoteAccentColor,
                baseFontSizeSp = fontSizeSp,
                useSerifBody = useSerif,
                onToggleTaskAtLine = onToggleCheckboxAtLine
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Page Footer & Prev/Next Chapter Navigation
            HorizontalDivider(color = paperTheme.secondaryColor.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 72.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrevChapter,
                    enabled = hasPrevChapter
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bab Sebelumnya")
                }

                Text(
                    text = "— $chapterNumber —",
                    fontFamily = LoraFontFamily,
                    color = paperTheme.secondaryColor
                )

                OutlinedButton(
                    onClick = onNextChapter,
                    enabled = hasNextChapter
                ) {
                    Text("Bab Berikutnya")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatToolbarButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FormatToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun wrapSelectionWith(
    value: TextFieldValue,
    prefix: String,
    suffix: String,
    placeholder: String
): TextFieldValue {
    val text = value.text
    val start = value.selection.min.coerceIn(0, text.length)
    val end = value.selection.max.coerceIn(0, text.length)
    val selected = if (end > start) text.substring(start, end) else placeholder
    val replacement = "$prefix$selected$suffix"
    val newText = text.replaceRange(start, end, replacement)
    val newCursor = start + replacement.length
    return value.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}

private fun insertLinePrefix(
    value: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.min.coerceIn(0, text.length)
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let {
        if (it == -1) 0 else it + 1
    }
    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    return value.copy(
        text = newText,
        selection = TextRange(cursor + prefix.length)
    )
}

private fun insertBlockSnippet(
    value: TextFieldValue,
    snippet: String
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.max.coerceIn(0, text.length)
    val newText = text.substring(0, cursor) + snippet + text.substring(cursor)
    return value.copy(
        text = newText,
        selection = TextRange(cursor + snippet.length)
    )
}
