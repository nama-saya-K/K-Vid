package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BookEntity
import com.example.data.BookRepository
import com.example.data.BookWithStats
import com.example.data.CoverThemeOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BookCategories = listOf(
    "Semua",
    "Jurnal & Refleksi",
    "Esai & Sastra",
    "Catatan Bacaan",
    "Memoar & Cerita",
    "Impor Markdown"
)

@Composable
fun LibraryScreen(
    booksWithStats: List<BookWithStats>,
    allBooksCount: Int,
    totalWordsAllBooks: Int,
    totalChaptersAllBooks: Int,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onOpenBook: (Long) -> Unit,
    onContinueWriting: () -> Unit,
    onCreateBook: (String, String, String, String, String, Int) -> Unit,
    onUpdateBook: (BookEntity, String, String, String, String, String, Int) -> Unit,
    onTogglePinBook: (BookEntity) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onImportMarkdown: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateBookDialog by rememberSaveable { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<BookEntity?>(null) }
    var deletingBook by remember { mutableStateOf<BookEntity?>(null) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Hero Card
        item {
            HeroBannerCard(
                totalBooks = allBooksCount,
                totalChapters = totalChaptersAllBooks,
                totalWords = totalWordsAllBooks,
                onNewBookClick = { showCreateBookDialog = true },
                onContinueClick = onContinueWriting,
                onImportClick = { showImportDialog = true }
            )
        }

        // Search & Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Cari judul buku, bab, atau isi tulisan…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus pencarian"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BookCategories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            label = { Text(category) },
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }
        }

        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rak Buku Pribadi (${booksWithStats.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = { showCreateBookDialog = true },
                    modifier = Modifier.testTag("header_add_book_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buku Baru")
                }
            }
        }

        if (booksWithStats.isEmpty()) {
            item {
                EmptyLibraryCard(
                    isSearching = searchQuery.isNotBlank() || selectedCategory != "Semua",
                    onResetFilter = {
                        onSearchQueryChange("")
                        onCategorySelect("Semua")
                    },
                    onCreateBook = { showCreateBookDialog = true }
                )
            }
        } else {
            items(booksWithStats, key = { it.book.id }) { item ->
                BookShelfCard(
                    item = item,
                    onClick = { onOpenBook(item.book.id) },
                    onPinClick = { onTogglePinBook(item.book) },
                    onEditClick = { editingBook = item.book },
                    onDeleteClick = { deletingBook = item.book }
                )
            }
        }
    }

    if (showCreateBookDialog) {
        BookFormDialog(
            initialBook = null,
            onDismiss = { showCreateBookDialog = false },
            onConfirm = { title, subtitle, author, category, colorKey, targetWords ->
                onCreateBook(title, subtitle, author, category, colorKey, targetWords)
                showCreateBookDialog = false
            }
        )
    }

    editingBook?.let { bookToEdit ->
        BookFormDialog(
            initialBook = bookToEdit,
            onDismiss = { editingBook = null },
            onConfirm = { title, subtitle, author, category, colorKey, targetWords ->
                onUpdateBook(bookToEdit, title, subtitle, author, category, colorKey, targetWords)
                editingBook = null
            }
        )
    }

    deletingBook?.let { bookToDelete ->
        AlertDialog(
            onDismissRequest = { deletingBook = null },
            title = { Text("Hapus Buku?") },
            text = {
                Text("Buku \"${bookToDelete.title}\" beserta seluruh bab di dalamnya akan dihapus secara permanen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBook(bookToDelete.id)
                        deletingBook = null
                    },
                    modifier = Modifier.testTag("confirm_delete_book_btn")
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBook = null }) {
                    Text("Batal")
                }
            }
        )
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
private fun HeroBannerCard(
    totalBooks: Int,
    totalChapters: Int,
    totalWords: Int,
    onNewBookClick: () -> Unit,
    onContinueClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_buku),
                contentDescription = "Ilustrasi meja menulis Buku untuk Saya",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x991C110A),
                                Color(0xD924160E),
                                Color(0xF21E120B)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFD9A76A).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "✦ Ruang Menulis Pribadi",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFF7DEC0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Buku untuk Saya",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFFFF8F0)
                )
                Text(
                    text = "Rangkai bab kehidupan, jurnal hening, dan catatan Markdown yang sepenuhnya milik Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEADACA),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Quick Stats Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroStatPill(label = "Buku", value = "$totalBooks")
                    HeroStatPill(label = "Bab", value = "$totalChapters")
                    HeroStatPill(label = "Kata", value = "$totalWords")
                    HeroStatPill(
                        label = "Baca",
                        value = "${BookRepository.estimateReadingMinutes(totalWords)} mnt"
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_continue_writing_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Editor")
                    }

                    FilledTonalButton(
                        onClick = onNewBookClick,
                        modifier = Modifier.testTag("hero_new_book_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buku Baru")
                    }

                    IconButton(
                        onClick = onImportClick,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.14f), CircleShape)
                            .testTag("hero_import_md_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Impor Markdown",
                            tint = Color(0xFFFFF8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatPill(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF7DEC0)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFD5C1B0)
        )
    }
}

@Composable
private fun BookShelfCard(
    item: BookWithStats,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val book = item.book
    val theme = CoverThemeOption.fromKey(book.coverColorKey)
    val progress = (item.totalWords.toFloat() / book.targetWordCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hardcover Book Visual Thumbnail with Spine
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .height(106.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                    .background(theme.primaryColor)
            ) {
                // Book Spine Crease
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(10.dp)
                        .background(Color.Black.copy(alpha = 0.25f))
                )
                // Gold / Accent Frame inside Cover
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(theme.accentColor.copy(alpha = 0.7f))
                    )
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFF8F0),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Book Metadata & Progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = book.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPinClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (book.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Sematkan buku",
                                tint = if (book.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit detail buku",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus buku",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.subtitle.isNotBlank()) {
                    Text(
                        text = book.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.chapterCount} Bab • ${item.totalWords} kata",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateFormatter.format(Date(book.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = theme.primaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard(
    isSearching: Boolean,
    onResetFilter: () -> Unit,
    onCreateBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (isSearching) "Tidak Ada Buku yang Cocok" else "Rak Buku Masih Kosong",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = if (isSearching) {
                    "Coba kata kunci lain atau tampilkan semua kategori buku."
                } else {
                    "Mulai tulis buku pertama Anda untuk menyimpan refleksi, ide, dan draf tulisan."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSearching) {
                OutlinedButton(onClick = onResetFilter) {
                    Text("Reset Pencarian")
                }
            } else {
                Button(onClick = onCreateBook) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Buku Pertama")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookFormDialog(
    initialBook: BookEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        subtitle: String,
        author: String,
        category: String,
        coverColorKey: String,
        targetWordCount: Int
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialBook?.title ?: "") }
    var subtitle by rememberSaveable { mutableStateOf(initialBook?.subtitle ?: "") }
    var author by rememberSaveable { mutableStateOf(initialBook?.author ?: "Saya Sendiri") }
    var category by rememberSaveable { mutableStateOf(initialBook?.category ?: "Jurnal & Refleksi") }
    var coverColorKey by rememberSaveable { mutableStateOf(initialBook?.coverColorKey ?: "ESPRESSO") }
    var targetWordsText by rememberSaveable {
        mutableStateOf((initialBook?.targetWordCount ?: 3000).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialBook == null) "Buat Buku Baru" else "Edit Detail Buku")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Buku") },
                    placeholder = { Text("Mis. Catatan Hening untuk Diri") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_input")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subjudul / Deskripsi Singkat") },
                    placeholder = { Text("Mis. Kumpulan refleksi dan surat pribadi") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_subtitle_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Penulis") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetWordsText,
                        onValueChange = { targetWordsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Kata") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.85f)
                    )
                }

                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BookCategories.drop(1).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "Warna Sampul Buku",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CoverThemeOption.entries.forEach { option ->
                        val selected = coverColorKey == option.key
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(option.primaryColor)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) option.accentColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { coverColorKey = option.key },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = option.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetWordsText.toIntOrNull() ?: 3000
                    onConfirm(title, subtitle, author, category, coverColorKey, target)
                },
                modifier = Modifier.testTag("save_book_dialog_btn")
            ) {
                Text("Simpan Buku")
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
fun ImportMarkdownDialog(
    onDismiss: () -> Unit,
    onImport: (title: String, rawMarkdown: String, coverColorKey: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var rawMarkdown by rememberSaveable {
        mutableStateOf(
            """
# Buku Catatan Impor Saya

## Bab 1: Gagasan Pembuka
Tuliskan atau tempelkan dokumen Markdown (`.md`) Anda di sini.
Gunakan `## Judul Bab` untuk memisahkan bab secara otomatis!

## Bab 2: Langkah Selanjutnya
- [x] Mengimpor catatan Markdown
- [ ] Melanjutkan menulis di mode visual
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Impor Dokumen Markdown (.md)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tempelkan teks Markdown Anda. Heading `# ` akan menjadi judul buku, dan setiap `## ` akan otomatis dibagi menjadi bab terpisah.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Buku (Opsional)") },
                    placeholder = { Text("Otomatis dari # Judul jika dikosongkan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rawMarkdown,
                    onValueChange = { rawMarkdown = it },
                    label = { Text("Isi Markdown") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_markdown_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawMarkdown.isNotBlank()) {
                        onImport(title, rawMarkdown, "EMERALD")
                    }
                },
                modifier = Modifier.testTag("confirm_import_md_btn")
            ) {
                Text("Impor Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
