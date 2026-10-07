package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookEntity
import com.example.data.BookRepository
import com.example.data.BookWithStats
import com.example.data.ChapterEntity
import com.example.data.QuickQuoteEntity
import com.example.data.ReaderPaperTheme
import com.example.data.WritingTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab(val route: String, val label: String) {
    PUSTAKA("pustaka", "Pustaka"),
    EDITOR("tulis", "Ruang Tulis"),
    KUTIPAN("kutipan", "Kutipan & Ide"),
    JEJAK("jejak", "Jejak & Ekspor")
}

enum class EditorViewMode(val label: String) {
    VISUAL("Editor Kaya"),
    MARKDOWN("Markdown"),
    BACA("Mode Baca")
}

@OptIn(ExperimentalCoroutinesApi::class)
class BukuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository =
        BookRepository(AppDatabase.getInstance(application).bookDao())

    private val _currentTab = MutableStateFlow(MainNavTab.PUSTAKA)
    val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

    private val _selectedChapterId = MutableStateFlow<Long?>(null)
    val selectedChapterId: StateFlow<Long?> = _selectedChapterId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _chapterFilter = MutableStateFlow("Semua") // Semua, Draf, Selesai, Ditandai
    val chapterFilter: StateFlow<String> = _chapterFilter.asStateFlow()

    // Reader & Editor preferences
    private val _editorMode = MutableStateFlow(EditorViewMode.VISUAL)
    val editorMode: StateFlow<EditorViewMode> = _editorMode.asStateFlow()

    private val _paperTheme = MutableStateFlow(ReaderPaperTheme.KREM)
    val paperTheme: StateFlow<ReaderPaperTheme> = _paperTheme.asStateFlow()

    private val _readerFontSizeSp = MutableStateFlow(17f)
    val readerFontSizeSp: StateFlow<Float> = _readerFontSizeSp.asStateFlow()

    private val _useSerifInReader = MutableStateFlow(true)
    val useSerifInReader: StateFlow<Boolean> = _useSerifInReader.asStateFlow()

    // Exported Markdown Preview Dialog state
    private val _exportedMarkdownDialog = MutableStateFlow<Pair<String, String>?>(null)
    val exportedMarkdownDialog: StateFlow<Pair<String, String>?> = _exportedMarkdownDialog.asStateFlow()

    // Status message banner / snackbar feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val booksWithStats: StateFlow<List<BookWithStats>> = repository.booksWithStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBooksWithStats: StateFlow<List<BookWithStats>> = combine(
        booksWithStats,
        allChapters,
        _searchQuery,
        _selectedCategory
    ) { books, chapters, query, category ->
        val trimmedQuery = query.trim().lowercase()
        books.filter { item ->
            val matchesCategory = category == "Semua" || item.book.category.equals(category, ignoreCase = true)
            if (!matchesCategory) return@filter false
            if (trimmedQuery.isEmpty()) return@filter true

            val bookMatches = item.book.title.lowercase().contains(trimmedQuery) ||
                item.book.subtitle.lowercase().contains(trimmedQuery) ||
                item.book.category.lowercase().contains(trimmedQuery)
            val chapterMatches = chapters.any { ch ->
                ch.bookId == item.book.id && (
                    ch.title.lowercase().contains(trimmedQuery) ||
                        ch.contentMarkdown.lowercase().contains(trimmedQuery)
                    )
            }
            bookMatches || chapterMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBook: StateFlow<BookEntity?> = _selectedBookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getBookByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentBookChapters: StateFlow<List<ChapterEntity>> = _selectedBookId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getChaptersForBook(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCurrentBookChapters: StateFlow<List<ChapterEntity>> = combine(
        currentBookChapters,
        _chapterFilter
    ) { chapters, filter ->
        when (filter) {
            "Draf" -> chapters.filter { it.status == "Draf" || it.status == "Revisi" }
            "Selesai" -> chapters.filter { it.status == "Selesai" }
            "Ditandai" -> chapters.filter { it.isBookmarked }
            else -> chapters
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentChapter: StateFlow<ChapterEntity?> = _selectedChapterId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getChapterByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allQuotes: StateFlow<List<QuickQuoteEntity>> = repository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun selectTab(tab: MainNavTab) {
        if (tab == MainNavTab.EDITOR) {
            // If no chapter is selected yet, pick the most recently edited chapter
            val activeChapterId = _selectedChapterId.value ?: allChapters.value.firstOrNull()?.id
            val activeBookId = _selectedBookId.value ?: allChapters.value.firstOrNull()?.bookId
                ?: booksWithStats.value.firstOrNull()?.book?.id
            _selectedBookId.value = activeBookId
            _selectedChapterId.value = activeChapterId
        }
        _currentTab.value = tab
    }

    fun openBookDetail(bookId: Long) {
        _selectedBookId.value = bookId
        _selectedChapterId.value = null
        _currentTab.value = MainNavTab.PUSTAKA
    }

    fun closeBookDetail() {
        _selectedBookId.value = null
        _selectedChapterId.value = null
    }

    fun openChapterEditor(bookId: Long, chapterId: Long, mode: EditorViewMode = EditorViewMode.VISUAL) {
        _selectedBookId.value = bookId
        _selectedChapterId.value = chapterId
        _editorMode.value = mode
        _currentTab.value = MainNavTab.EDITOR
    }

    fun navigateBackFromEditorToBook() {
        _currentTab.value = MainNavTab.PUSTAKA
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setChapterFilter(filter: String) {
        _chapterFilter.value = filter
    }

    fun setEditorMode(mode: EditorViewMode) {
        _editorMode.value = mode
    }

    fun setPaperTheme(theme: ReaderPaperTheme) {
        _paperTheme.value = theme
    }

    fun adjustReaderFontSize(delta: Float) {
        _readerFontSizeSp.value = (_readerFontSizeSp.value + delta).coerceIn(14f, 26f)
    }

    fun toggleReaderFontStyle() {
        _useSerifInReader.value = !_useSerifInReader.value
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private fun postMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun createNewBook(
        title: String,
        subtitle: String,
        author: String,
        category: String,
        coverColorKey: String,
        targetWordCount: Int,
        openImmediately: Boolean = true
    ) {
        viewModelScope.launch {
            val newId = repository.createBook(
                title = title,
                subtitle = subtitle,
                author = author,
                category = category,
                coverColorKey = coverColorKey,
                targetWordCount = targetWordCount
            )
            postMessage("Buku \"${title.ifBlank { "Buku Baru" }}\" berhasil dibuat")
            if (openImmediately) {
                openBookDetail(newId)
            }
        }
    }

    fun updateBookDetails(
        book: BookEntity,
        title: String,
        subtitle: String,
        author: String,
        category: String,
        coverColorKey: String,
        targetWordCount: Int
    ) {
        viewModelScope.launch {
            repository.updateBook(
                book.copy(
                    title = title.trim().ifEmpty { book.title },
                    subtitle = subtitle.trim(),
                    author = author.trim().ifEmpty { "Saya Sendiri" },
                    category = category.trim().ifEmpty { "Jurnal & Refleksi" },
                    coverColorKey = coverColorKey,
                    targetWordCount = targetWordCount.coerceAtLeast(500)
                )
            )
            postMessage("Detail buku diperbarui")
        }
    }

    fun togglePinBook(book: BookEntity) {
        viewModelScope.launch {
            repository.togglePinBook(book)
            postMessage(if (!book.isPinned) "Buku disematkan di atas" else "Sematan buku dilepas")
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            if (_selectedBookId.value == bookId) {
                _selectedBookId.value = null
                _selectedChapterId.value = null
            }
            repository.deleteBook(bookId)
            postMessage("Buku telah dihapus")
        }
    }

    fun createNewChapter(
        bookId: Long,
        title: String,
        contentMarkdown: String = "",
        moodTag: String = "Tenang",
        openEditorImmediately: Boolean = true
    ) {
        viewModelScope.launch {
            val chapterId = repository.createChapter(
                bookId = bookId,
                title = title,
                contentMarkdown = contentMarkdown,
                moodTag = moodTag
            )
            postMessage("Bab baru ditambahkan")
            if (openEditorImmediately) {
                openChapterEditor(bookId, chapterId, EditorViewMode.VISUAL)
            }
        }
    }

    fun createChapterFromTemplate(bookId: Long, template: WritingTemplate) {
        viewModelScope.launch {
            val chapterId = repository.createChapter(
                bookId = bookId,
                title = template.title,
                contentMarkdown = template.markdownContent,
                moodTag = template.defaultMood
            )
            postMessage("Templat \"${template.title}\" diterapkan")
            openChapterEditor(bookId, chapterId, EditorViewMode.VISUAL)
        }
    }

    fun saveChapterChanges(
        chapter: ChapterEntity,
        newTitle: String,
        newContentMarkdown: String,
        newMoodTag: String,
        newStatus: String,
        newIsBookmarked: Boolean,
        showToast: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateChapter(
                chapter = chapter,
                newTitle = newTitle,
                newContentMarkdown = newContentMarkdown,
                newMoodTag = newMoodTag,
                newStatus = newStatus,
                newIsBookmarked = newIsBookmarked
            )
            if (showToast) {
                postMessage("Bab tersimpan otomatis")
            }
        }
    }

    fun toggleBookmark(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.toggleChapterBookmark(chapter)
        }
    }

    fun moveChapter(bookId: Long, chapter: ChapterEntity, moveUp: Boolean) {
        viewModelScope.launch {
            repository.moveChapterOrder(bookId, chapter, moveUp)
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            if (_selectedChapterId.value == chapter.id) {
                _selectedChapterId.value = null
                _currentTab.value = MainNavTab.PUSTAKA
            }
            repository.deleteChapter(chapter)
            postMessage("Bab dihapus")
        }
    }

    fun openAdjacentChapter(next: Boolean) {
        val chapters = currentBookChapters.value.sortedBy { it.chapterNumber }
        val currentId = _selectedChapterId.value ?: return
        val index = chapters.indexOfFirst { it.id == currentId }
        if (index == -1) return
        val targetIndex = if (next) index + 1 else index - 1
        if (targetIndex in chapters.indices) {
            _selectedChapterId.value = chapters[targetIndex].id
        }
    }

    fun addQuickQuote(
        bookTitle: String,
        quoteText: String,
        reflectionNote: String,
        tag: String
    ) {
        viewModelScope.launch {
            repository.addQuote(bookTitle, quoteText, reflectionNote, tag)
            postMessage("Kutipan & gagasan disimpan")
        }
    }

    fun toggleFavoriteQuote(quote: QuickQuoteEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteQuote(quote)
        }
    }

    fun deleteQuote(quoteId: Long) {
        viewModelScope.launch {
            repository.deleteQuote(quoteId)
            postMessage("Kutipan dihapus")
        }
    }

    fun convertQuoteToChapter(quote: QuickQuoteEntity, targetBookId: Long) {
        viewModelScope.launch {
            val md = buildString {
                appendLine("# Refleksi: ${quote.tag}")
                appendLine()
                appendLine("> \"${quote.quoteText}\"")
                appendLine()
                if (quote.reflectionNote.isNotBlank()) {
                    appendLine("## Catatan Awal")
                    appendLine(quote.reflectionNote)
                    appendLine()
                }
                appendLine("## Pengembangan Gagasan")
                appendLine("Tuliskan kelanjutan perenungan Anda di sini...")
            }
            val chId = repository.createChapter(
                bookId = targetBookId,
                title = "Refleksi: ${quote.quoteText.take(28)}...",
                contentMarkdown = md,
                moodTag = "Reflektif"
            )
            postMessage("Kutipan dijadikan bab baru")
            openChapterEditor(targetBookId, chId, EditorViewMode.VISUAL)
        }
    }

    fun requestExportBookMarkdown(bookId: Long, bookTitle: String) {
        viewModelScope.launch {
            val md = repository.exportBookToMarkdown(bookId)
            if (md.isNotBlank()) {
                _exportedMarkdownDialog.value = bookTitle to md
            }
        }
    }

    fun dismissExportDialog() {
        _exportedMarkdownDialog.value = null
    }

    fun importBookFromMarkdown(title: String, rawMarkdown: String, coverColorKey: String) {
        viewModelScope.launch {
            val newBookId = repository.importBookFromMarkdown(
                fallbackTitle = title,
                rawMarkdown = rawMarkdown,
                coverColorKey = coverColorKey
            )
            postMessage("Dokumen Markdown berhasil diimpor menjadi buku!")
            openBookDetail(newBookId)
        }
    }
}
