package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BookEntity
import com.example.data.BookRepository
import com.example.data.ChapterEntity
import com.example.data.CoverThemeOption
import com.example.data.DefaultWritingTemplates
import com.example.data.WritingTemplate
import com.example.ui.EditorViewMode

private val ChapterFilterOptions = listOf("Semua", "Draf", "Selesai", "Ditandai")

@Composable
fun BookDetailScreen(
    book: BookEntity,
    allChapters: List<ChapterEntity>,
    filteredChapters: List<ChapterEntity>,
    chapterFilter: String,
    onFilterChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenChapter: (Long, EditorViewMode) -> Unit,
    onCreateChapter: (title: String, mood: String) -> Unit,
    onCreateFromTemplate: (WritingTemplate) -> Unit,
    onToggleBookmark: (ChapterEntity) -> Unit,
    onMoveChapter: (ChapterEntity, Boolean) -> Unit,
    onDeleteChapter: (ChapterEntity) -> Unit,
    onExportBookMarkdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showNewChapterDialog by rememberSaveable { mutableStateOf(false) }
    var showTemplateDialog by rememberSaveable { mutableStateOf(false) }
    var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }

    val coverTheme = CoverThemeOption.fromKey(book.coverColorKey)
    val totalWords = allChapters.sumOf { it.wordCount }
    val progress = (totalWords.toFloat() / book.targetWordCount.coerceAtLeast(1)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("book_detail_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Book Cover Header Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = coverTheme.primaryColor),
                modifier = Modifier.fillMaxWidth()
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
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("back_to_library_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Pustaka",
                                tint = Color(0xFFFFF8F0)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pustaka Buku", color = Color(0xFFFFF8F0))
                        }

                        FilledTonalButton(
                            onClick = onExportBookMarkdown,
                            modifier = Modifier.testTag("export_book_md_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.IosShare,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor .md")
                        }
                    }

                    Surface(
                        color = coverTheme.accentColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${book.category} • Oleh ${book.author}",
                            style = MaterialTheme.typography.labelMedium,
                            color = coverTheme.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFFFF8F0)
                    )

                    if (book.subtitle.isNotBlank()) {
                        Text(
                            text = book.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEADACA)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress toward book word goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${allChapters.size} Bab • $totalWords / ${book.targetWordCount} kata",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFF8F0)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% • ~${BookRepository.estimateReadingMinutes(totalWords)} mnt baca",
                            style = MaterialTheme.typography.labelMedium,
                            color = coverTheme.accentColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = coverTheme.accentColor,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNewChapterDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_chapter_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bab Baru")
                        }

                        FilledTonalButton(
                            onClick = { showTemplateDialog = true },
                            modifier = Modifier.testTag("use_template_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMosaic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Templat")
                        }
                    }
                }
            }
        }

        // Filter Chips for Table of Contents
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Isi (${filteredChapters.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(ChapterFilterOptions) { _, filter ->
                        FilterChip(
                            selected = chapterFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter) }
                        )
                    }
                }
            }
        }

        if (filteredChapters.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Belum Ada Bab pada Filter Ini",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tambahkan bab baru atau gunakan templat reflektif untuk mulai menulis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(filteredChapters, key = { _, ch -> ch.id }) { idx, chapter ->
                ChapterItemCard(
                    chapter = chapter,
                    canMoveUp = idx > 0 && chapterFilter == "Semua",
                    canMoveDown = idx < filteredChapters.lastIndex && chapterFilter == "Semua",
                    onEditClick = { onOpenChapter(chapter.id, EditorViewMode.VISUAL) },
                    onReadClick = { onOpenChapter(chapter.id, EditorViewMode.BACA) },
                    onBookmarkToggle = { onToggleBookmark(chapter) },
                    onMoveUp = { onMoveChapter(chapter, true) },
                    onMoveDown = { onMoveChapter(chapter, false) },
                    onDelete = { chapterToDelete = chapter }
                )
            }
        }
    }

    if (showNewChapterDialog) {
        NewChapterDialog(
            nextNumber = allChapters.size + 1,
            onDismiss = { showNewChapterDialog = false },
            onConfirm = { title, mood ->
                onCreateChapter(title, mood)
                showNewChapterDialog = false
            }
        )
    }

    if (showTemplateDialog) {
        WritingTemplateDialog(
            onDismiss = { showTemplateDialog = false },
            onSelectTemplate = { template ->
                onCreateFromTemplate(template)
                showTemplateDialog = false
            }
        )
    }

    chapterToDelete?.let { ch ->
        AlertDialog(
            onDismissRequest = { chapterToDelete = null },
            title = { Text("Hapus Bab Ini?") },
            text = { Text("Bab \"${ch.title}\" akan dihapus dari buku ini.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChapter(ch)
                        chapterToDelete = null
                    }
                ) {
                    Text("Hapus Bab")
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ChapterItemCard(
    chapter: ChapterEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEditClick: () -> Unit,
    onReadClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val previewSnippet = remember(chapter.contentMarkdown) {
        chapter.contentMarkdown
            .lines()
            .filterNot { it.trim().startsWith("#") || it.trim().isEmpty() }
            .joinToString(" ")
            .replace(Regex("[*_~`>\\[\\]]"), "")
            .take(140)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .testTag("chapter_card_${chapter.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        color = if (chapter.status == "Selesai") {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${chapter.status} • ${chapter.moodTag}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Naikkan urutan bab",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (canMoveDown) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Turunkan urutan bab",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (chapter.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai bab",
                            tint = if (chapter.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus bab",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleLarge
            )

            if (previewSnippet.isNotBlank()) {
                Text(
                    text = previewSnippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${chapter.wordCount} kata • ~${BookRepository.estimateReadingMinutes(chapter.wordCount)} mnt baca",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onReadClick,
                        modifier = Modifier.testTag("read_chapter_btn_${chapter.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Baca")
                    }
                    FilledTonalButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("edit_chapter_btn_${chapter.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tulis")
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChapterDialog(
    nextNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (title: String, mood: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedMood by rememberSaveable { mutableStateOf("Tenang") }
    val moods = listOf("Tenang", "Reflektif", "Bersyukur", "Kreatif", "Puitis", "Fokus")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Bab ke-$nextNumber") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Bab") },
                    placeholder = { Text("Mis. Catatan Malam Hujan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chapter_title_input")
                )

                Text("Suasana Hati Bab", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(moods) { _, mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title.ifBlank { "Bab $nextNumber" }, selectedMood)
                },
                modifier = Modifier.testTag("confirm_new_chapter_btn")
            ) {
                Text("Mulai Menulis")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun WritingTemplateDialog(
    onDismiss: () -> Unit,
    onSelectTemplate: (WritingTemplate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Templat Bab") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Mulai menulis lebih cepat dengan struktur halaman yang sudah disiapkan:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DefaultWritingTemplates.forEach { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(template) }
                            .testTag("template_item_${template.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = template.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
