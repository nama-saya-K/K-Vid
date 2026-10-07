package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookRepository
import com.example.data.BookWithStats
import com.example.data.CoverThemeOption
import com.example.data.QuickQuoteEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LoraFontFamily

@Composable
fun QuotesScreen(
    quotes: List<QuickQuoteEntity>,
    booksWithStats: List<BookWithStats>,
    onAddQuote: (bookTitle: String, quoteText: String, reflection: String, tag: String) -> Unit,
    onToggleFavorite: (QuickQuoteEntity) -> Unit,
    onDeleteQuote: (Long) -> Unit,
    onConvertQuoteToChapter: (QuickQuoteEntity, Long) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var onlyFavorites by rememberSaveable { mutableStateOf(false) }
    var quoteToConvert by remember { mutableStateOf<QuickQuoteEntity?>(null) }

    val displayedQuotes = remember(quotes, onlyFavorites) {
        if (onlyFavorites) quotes.filter { it.isFavorite } else quotes
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quotes_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kotak Kutipan & Ide Kilat",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Tangkap kalimat indah, penggalan buku, atau ide dadakan sebelum terlupa. Anda juga dapat mengubah kutipan menjadi bab baru dalam satu ketukan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_quote_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Kutipan / Ide")
                        }
                        FilterChip(
                            selected = onlyFavorites,
                            onClick = { onlyFavorites = !onlyFavorites },
                            label = { Text("Hanya Favorit") }
                        )
                    }
                }
            }
        }

        if (displayedQuotes.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Belum Ada Kutipan Tersimpan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Simpan kalimat reflektif atau ide bab berikutnya di sini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedQuotes, key = { it.id }) { quote ->
                QuoteCard(
                    quote = quote,
                    onToggleFavorite = { onToggleFavorite(quote) },
                    onDelete = { onDeleteQuote(quote.id) },
                    onConvertToChapter = { quoteToConvert = quote }
                )
            }
        }
    }

    if (showAddDialog) {
        AddQuoteDialog(
            bookTitles = booksWithStats.map { it.book.title },
            onDismiss = { showAddDialog = false },
            onConfirm = { bookTitle, quoteText, reflection, tag ->
                onAddQuote(bookTitle, quoteText, reflection, tag)
                showAddDialog = false
            }
        )
    }

    quoteToConvert?.let { selectedQuote ->
        AlertDialog(
            onDismissRequest = { quoteToConvert = null },
            title = { Text("Jadikan Bab Baru di Buku") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih buku tujuan untuk mengembangkan kutipan ini menjadi satu bab utuh:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    booksWithStats.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onConvertQuoteToChapter(selectedQuote, item.book.id)
                                    quoteToConvert = null
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.book.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${item.chapterCount} bab",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { quoteToConvert = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun QuoteCard(
    quote: QuickQuoteEntity,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onConvertToChapter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quote_card_${quote.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${quote.tag} • ${quote.bookTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritkan kutipan",
                            tint = if (quote.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus kutipan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "“${quote.quoteText}”",
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (quote.reflectionNote.isNotBlank()) {
                Text(
                    text = quote.reflectionNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onConvertToChapter,
                    modifier = Modifier.testTag("convert_quote_btn_${quote.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kembangkan Jadi Bab")
                }
            }
        }
    }
}

@Composable
private fun AddQuoteDialog(
    bookTitles: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (bookTitle: String, quoteText: String, reflection: String, tag: String) -> Unit
) {
    var quoteText by rememberSaveable { mutableStateOf("") }
    var reflection by rememberSaveable { mutableStateOf("") }
    var bookTitle by rememberSaveable {
        mutableStateOf(bookTitles.firstOrNull() ?: "Buku untuk Saya")
    }
    var tag by rememberSaveable { mutableStateOf("Filosofi") }
    val tags = listOf("Filosofi", "Gagasan", "Motivasi Menulis", "Puisi", "Renungan")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simpan Kutipan & Gagasan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Isi Kutipan atau Kalimat Ide") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quote_text_input")
                )
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("Catatan Refleksi (Opsional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    label = { Text("Sumber / Buku Terkait") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { itemTag ->
                        FilterChip(
                            selected = tag == itemTag,
                            onClick = { tag = itemTag },
                            label = { Text(itemTag) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (quoteText.isNotBlank()) {
                        onConfirm(bookTitle, quoteText, reflection, tag)
                    }
                },
                modifier = Modifier.testTag("confirm_save_quote_btn")
            ) {
                Text("Simpan")
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
fun StatsAndExportScreen(
    booksWithStats: List<BookWithStats>,
    quotesCount: Int,
    onExportBook: (Long, String) -> Unit,
    onOpenBook: (Long) -> Unit,
    onImportMarkdown: (String, String, String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    var showImportDialog by rememberSaveable { mutableStateOf(false) }

    val totalBooks = booksWithStats.size
    val totalChapters = booksWithStats.sumOf { it.chapterCount }
    val completedChapters = booksWithStats.sumOf { it.completedChapters }
    val totalWords = booksWithStats.sumOf { it.totalWords }
    val totalReadMinutes = BookRepository.estimateReadingMinutes(totalWords)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Jejak Menulis & Pusat Markdown",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Pantau perkembangan karya tulis Anda dan ekspor buku kapan saja ke dokumen Markdown (.md) portabel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricBox(
                            value = "$totalWords",
                            label = "Total Kata",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            value = "$totalChapters",
                            label = "Total Bab",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            value = "$completedChapters",
                            label = "Bab Selesai",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricBox(
                            value = "$totalBooks",
                            label = "Buku Aktif",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            value = "$quotesCount",
                            label = "Kutipan & Ide",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricBox(
                            value = "~$totalReadMinutes mnt",
                            label = "Durasi Baca",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FilledTonalButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stats_import_markdown_btn")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Impor Buku dari Dokumen Markdown (.md)")
                    }
                }
            }
        }

        item {
            Text(
                text = "Ekspor & Progres Setiap Buku",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(booksWithStats, key = { it.book.id }) { item ->
            val theme = CoverThemeOption.fromKey(item.book.coverColorKey)
            val progress = (item.totalWords.toFloat() / item.book.targetWordCount.coerceAtLeast(1)).coerceIn(0f, 1f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBook(item.book.id) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                            )
                            Text(
                                text = item.book.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onExportBook(item.book.id, item.book.title) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("export_btn_${item.book.id}")
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ekspor .md", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Text(
                        text = "${item.chapterCount} bab (${item.completedChapters} selesai) • ${item.totalWords} / ${item.book.targetWordCount} kata (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = theme.primaryColor
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        ImportMarkdownDialog(
            onDismiss = { showImportDialog = false },
            onImport = { title, md, colorKey ->
                onImportMarkdown(title, md, colorKey)
                showImportDialog = false
            }
        )
    }
}

@Composable
private fun StatMetricBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MarkdownExportPreviewDialog(
    bookTitle: String,
    markdownContent: String,
    onDismiss: () -> Unit,
    onCopiedFeedback: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ekspor Markdown: $bookTitle")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Dokumen ini kompatibel penuh dengan format Markdown (.md) Buku untuk Saya:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Text(
                        text = markdownContent,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("$bookTitle.md", markdownContent)
                        )
                        onCopiedFeedback()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("copy_exported_md_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin")
                }

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(Intent.EXTRA_TITLE, "$bookTitle.md")
                            putExtra(Intent.EXTRA_SUBJECT, bookTitle)
                            putExtra(Intent.EXTRA_TEXT, markdownContent)
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Bagikan Buku Markdown")
                        )
                    },
                    modifier = Modifier.testTag("share_exported_md_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
