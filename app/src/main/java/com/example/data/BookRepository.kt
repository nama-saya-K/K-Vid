package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.max

class BookRepository(private val dao: BookDao) {

    val allBooks: Flow<List<BookEntity>> = dao.getAllBooks()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allQuotes: Flow<List<QuickQuoteEntity>> = dao.getAllQuotes()

    val booksWithStats: Flow<List<BookWithStats>> = combine(
        dao.getAllBooks(),
        dao.getAllChapters()
    ) { books, chapters ->
        val chaptersByBook = chapters.groupBy { it.bookId }
        books.map { book ->
            val bookChapters = chaptersByBook[book.id].orEmpty()
            BookWithStats(
                book = book,
                chapterCount = bookChapters.size,
                totalWords = bookChapters.sumOf { it.wordCount },
                bookmarkedCount = bookChapters.count { it.isBookmarked },
                completedChapters = bookChapters.count { it.status == "Selesai" }
            )
        }
    }

    fun getBookByIdFlow(bookId: Long): Flow<BookEntity?> = dao.getBookByIdFlow(bookId)

    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>> =
        dao.getChaptersForBook(bookId)

    fun getChapterByIdFlow(chapterId: Long): Flow<ChapterEntity?> =
        dao.getChapterByIdFlow(chapterId)

    suspend fun createBook(
        title: String,
        subtitle: String,
        author: String,
        category: String,
        coverColorKey: String,
        targetWordCount: Int,
        firstChapterTitle: String = "Bab 1: Permulaan",
        firstChapterMarkdown: String = "# Bab 1: Permulaan\n\nMulai tuliskan kisah, gagasan, atau catatan pribadi Anda di sini..."
    ): Long {
        val now = System.currentTimeMillis()
        val bookId = dao.insertBook(
            BookEntity(
                title = title.trim().ifEmpty { "Buku Tanpa Judul" },
                subtitle = subtitle.trim().ifEmpty { "Catatan & Refleksi Pribadi" },
                author = author.trim().ifEmpty { "Saya Sendiri" },
                category = category.trim().ifEmpty { "Jurnal & Refleksi" },
                coverColorKey = coverColorKey,
                targetWordCount = max(500, targetWordCount),
                createdAt = now,
                updatedAt = now
            )
        )
        dao.insertChapter(
            ChapterEntity(
                bookId = bookId,
                chapterNumber = 1,
                title = firstChapterTitle,
                contentMarkdown = firstChapterMarkdown,
                moodTag = "Tenang",
                status = "Draf",
                wordCount = countWords(firstChapterMarkdown),
                updatedAt = now
            )
        )
        return bookId
    }

    suspend fun updateBook(book: BookEntity) {
        dao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePinBook(book: BookEntity) {
        dao.updateBook(
            book.copy(
                isPinned = !book.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBook(bookId: Long) {
        dao.deleteBookById(bookId)
    }

    suspend fun createChapter(
        bookId: Long,
        title: String,
        contentMarkdown: String,
        moodTag: String = "Tenang",
        status: String = "Draf"
    ): Long {
        val existing = dao.getChaptersForBookOnce(bookId)
        val nextNum = (existing.maxOfOrNull { it.chapterNumber } ?: 0) + 1
        val now = System.currentTimeMillis()
        val cleanTitle = title.trim().ifEmpty { "Bab $nextNum" }
        val finalMarkdown = contentMarkdown.ifBlank {
            "# $cleanTitle\n\nTuliskan isi bab di sini..."
        }
        val chapterId = dao.insertChapter(
            ChapterEntity(
                bookId = bookId,
                chapterNumber = nextNum,
                title = cleanTitle,
                contentMarkdown = finalMarkdown,
                moodTag = moodTag,
                status = status,
                wordCount = countWords(finalMarkdown),
                updatedAt = now
            )
        )
        touchBookTimestamp(bookId, now)
        return chapterId
    }

    suspend fun updateChapter(
        chapter: ChapterEntity,
        newTitle: String,
        newContentMarkdown: String,
        newMoodTag: String,
        newStatus: String,
        newIsBookmarked: Boolean
    ) {
        val now = System.currentTimeMillis()
        val updated = chapter.copy(
            title = newTitle.trim().ifEmpty { chapter.title },
            contentMarkdown = newContentMarkdown,
            moodTag = newMoodTag,
            status = newStatus,
            isBookmarked = newIsBookmarked,
            wordCount = countWords(newContentMarkdown),
            updatedAt = now
        )
        dao.updateChapter(updated)
        touchBookTimestamp(chapter.bookId, now)
    }

    suspend fun toggleChapterBookmark(chapter: ChapterEntity) {
        dao.updateChapter(
            chapter.copy(
                isBookmarked = !chapter.isBookmarked,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun moveChapterOrder(bookId: Long, chapter: ChapterEntity, moveUp: Boolean) {
        val chapters = dao.getChaptersForBookOnce(bookId).sortedBy { it.chapterNumber }
        val index = chapters.indexOfFirst { it.id == chapter.id }
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex !in chapters.indices) return
        val neighbor = chapters[targetIndex]
        dao.updateChapter(chapter.copy(chapterNumber = neighbor.chapterNumber))
        dao.updateChapter(neighbor.copy(chapterNumber = chapter.chapterNumber))
    }

    suspend fun deleteChapter(chapter: ChapterEntity) {
        dao.deleteChapterById(chapter.id)
        val remaining = dao.getChaptersForBookOnce(chapter.bookId).sortedBy { it.chapterNumber }
        remaining.forEachIndexed { idx, item ->
            if (item.chapterNumber != idx + 1) {
                dao.updateChapter(item.copy(chapterNumber = idx + 1))
            }
        }
        touchBookTimestamp(chapter.bookId, System.currentTimeMillis())
    }

    suspend fun addQuote(
        bookTitle: String,
        quoteText: String,
        reflectionNote: String,
        tag: String
    ) {
        if (quoteText.isBlank()) return
        dao.insertQuote(
            QuickQuoteEntity(
                bookTitle = bookTitle.trim().ifEmpty { "Catatan Lepas" },
                quoteText = quoteText.trim(),
                reflectionNote = reflectionNote.trim(),
                tag = tag.trim().ifEmpty { "Gagasan" },
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavoriteQuote(quote: QuickQuoteEntity) {
        dao.updateQuote(quote.copy(isFavorite = !quote.isFavorite))
    }

    suspend fun deleteQuote(quoteId: Long) {
        dao.deleteQuoteById(quoteId)
    }

    private suspend fun touchBookTimestamp(bookId: Long, timestamp: Long) {
        val book = dao.getBookById(bookId) ?: return
        dao.updateBook(book.copy(updatedAt = timestamp))
    }

    /**
     * Exports a complete Book and all its Chapters into a clean, portable Markdown (.md) document
     * compatible with the original Buku-untuk-Saya Markdown-Tiptap Bridge format.
     */
    suspend fun exportBookToMarkdown(bookId: Long): String {
        val book = dao.getBookById(bookId) ?: return ""
        val chapters = dao.getChaptersForBookOnce(bookId).sortedBy { it.chapterNumber }
        return buildString {
            appendLine("---")
            appendLine("title: \"${book.title}\"")
            appendLine("subtitle: \"${book.subtitle}\"")
            appendLine("author: \"${book.author}\"")
            appendLine("category: \"${book.category}\"")
            appendLine("---")
            appendLine()
            appendLine("# ${book.title}")
            if (book.subtitle.isNotBlank()) {
                appendLine("*${book.subtitle}* — Oleh **${book.author}**")
            }
            appendLine()
            chapters.forEachIndexed { idx, ch ->
                if (idx > 0) {
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
                appendLine("## Bab ${ch.chapterNumber}: ${ch.title}")
                appendLine()
                appendLine(ch.contentMarkdown.trim())
            }
        }
    }

    /**
     * Imports a Markdown document and bridges it into a Book and Chapters.
     */
    suspend fun importBookFromMarkdown(
        fallbackTitle: String,
        rawMarkdown: String,
        coverColorKey: String = "EMERALD"
    ): Long {
        val lines = rawMarkdown.lines()
        var detectedTitle = fallbackTitle.trim()
        var detectedSubtitle = "Diimpor dari dokumen Markdown"
        val chapterSections = mutableListOf<Pair<String, StringBuilder>>()
        var currentChapterTitle = "Bab 1: Catatan Utama"
        var currentBuffer = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("# ") && detectedTitle.isEmpty()) {
                detectedTitle = trimmed.removePrefix("# ").trim()
            } else if (trimmed.startsWith("## ")) {
                if (currentBuffer.isNotBlank()) {
                    chapterSections.add(currentChapterTitle to currentBuffer)
                }
                currentChapterTitle = trimmed.removePrefix("## ").trim()
                    .replace(Regex("^Bab\\s+\\d+:\\s*", RegexOption.IGNORE_CASE), "")
                    .ifEmpty { "Bab ${chapterSections.size + 1}" }
                currentBuffer = StringBuilder()
            } else {
                currentBuffer.appendLine(line)
            }
        }
        if (currentBuffer.isNotBlank() || chapterSections.isEmpty()) {
            chapterSections.add(currentChapterTitle to currentBuffer)
        }

        val finalBookTitle = detectedTitle.ifEmpty { "Buku Impor Baru" }
        val now = System.currentTimeMillis()
        val bookId = dao.insertBook(
            BookEntity(
                title = finalBookTitle,
                subtitle = detectedSubtitle,
                author = "Saya Sendiri",
                category = "Impor Markdown",
                coverColorKey = coverColorKey,
                targetWordCount = 3000,
                createdAt = now,
                updatedAt = now
            )
        )

        chapterSections.forEachIndexed { index, (chTitle, chContent) ->
            val text = chContent.toString().trim().ifEmpty { "# $chTitle\n\n(Bab kosong)" }
            dao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    chapterNumber = index + 1,
                    title = chTitle,
                    contentMarkdown = text,
                    moodTag = "Reflektif",
                    status = "Draf",
                    wordCount = countWords(text),
                    updatedAt = now
                )
            )
        }
        return bookId
    }

    suspend fun ensureSeedData() {
        if (dao.getBookCount() > 0) return
        val now = System.currentTimeMillis()

        // Book 1: Buku untuk Saya (Core Personal Book)
        val book1Id = dao.insertBook(
            BookEntity(
                title = "Buku untuk Saya",
                subtitle = "Kumpulan Surat, Refleksi, dan Catatan Hening untuk Diri Sendiri",
                author = "Saya Sendiri",
                category = "Jurnal & Refleksi",
                coverColorKey = "ESPRESSO",
                targetWordCount = 2500,
                isPinned = true,
                createdAt = now - 86_400_000L * 5,
                updatedAt = now - 3_600_000L
            )
        )

        val ch1Content = """
# Prolog: Mengapa Saya Menulis Buku Ini?

> "Ada buku-buku yang ditulis untuk dunia, dan ada buku yang ditulis semata-mata agar kita tidak kehilangan diri sendiri."

Selamat datang di **Buku untuk Saya**. Ruang ini adalah tempat bernaung bagi pemikiran, perenungan, draf cerita, maupun catatan harian yang ingin Anda rawat dalam format buku yang rapi dan tenang.

## Apa yang Bisa Dilakukan di Sini?
- **Menulis Bab demi Bab:** Susun pemikiran panjang menjadi bab-bab terstruktur layaknya buku pribadi.
- **Jembatan Editor Visual & Markdown:** Gunakan bilah alat format cepat untuk membuat teks **tebal**, *miring*, ~~coret~~, ==sorotan warna==, kutipan blok, atau daftar periksa interaktif.
- **Mode Baca Buku:** Nikmati hasil tulisan Anda di atas kertas digital hangat (*Krem*, *Sepia*, *Putih*, atau *Malam Teduh*) tanpa gangguan.
- **Ekspor & Impor Markdown (`.md`):** Tulisan Anda sepenuhnya milik Anda—ekspor kapan saja ke format Markdown standar.

---

## Janji Kecil Saat Menulis
- [x] Membuka halaman pertama dan memulai catatan jujur
- [x] Menulis tanpa menghakimi kalimat pertama
- [ ] Menambahkan satu bab refleksi baru pekan ini
- [ ] Membaca ulang bab lama saat membutuhkan ketenangan
        """.trimIndent()

        val ch2Content = """
# Bab 2: Seni Berjalan Pelan di Dunia yang Bising

Sering kali kita merasa tertinggal hanya karena melihat langkah orang lain yang tampak berlari. Padahal, setiap orang memiliki musim tumbuhnya masing-masing.

> "Pohon jati tidak tumbuh dalam semalam, namun akarnya tahu persis ke mana harus mencari air."

## Tiga Pengingat Saat Pikiran Penuh
1. **Berhenti Sejenak:** Tarik napas dalam-dalam sebelum bereaksi terhadap kabar yang mendesak.
2. **Pilah yang Penting:** Tidak semua kebisingan layak mendapat ruang di kepala kita.
3. **Tulis untuk Mengurai:** Ketika kepala terasa penuh, pindahkan benang kusut itu ke atas kertas.

### Catatan Sore Ini
Hari ini saya belajar bahwa memilih tenang bukanlah bentuk menyerah, melainkan cara menjaga ==kejernihan batin== untuk hal-hal yang sungguh berarti.
        """.trimIndent()

        val ch3Content = """
# Bab 3: Panduan Singkat Format Markdown & Editor Kaya

Aplikasi **Buku untuk Saya** menggabungkan kemudahan editor visual dengan fleksibilitas Markdown:

## Contoh Format Teks
- Gunakan `**kata**` untuk **menebalkan gagasan utama**.
- Gunakan `*kata*` untuk *penekanan lembut atau istilah asing*.
- Gunakan `==kata==` untuk ==menyoroti kalimat paling penting== di halaman Anda.
- Gunakan `> kutipan` untuk membuat kotak kutipan reflektif.

```
// Bahkan Anda dapat menyimpan potongan kode atau formula:
kebahagiaan = syukur + kehadiran_penuh - perbandingan_sosial
```

## Daftar Target Menulis
- [x] Mengenal bilah alat format di bagian bawah editor
- [x] Mencoba fitur pratinjau langsung dan Mode Baca
- [ ] Mencoba fitur Ekspor Buku ke Markdown
        """.trimIndent()

        dao.insertChapter(
            ChapterEntity(
                bookId = book1Id,
                chapterNumber = 1,
                title = "Prolog: Mengapa Saya Menulis Buku Ini?",
                contentMarkdown = ch1Content,
                moodTag = "Reflektif",
                status = "Selesai",
                isBookmarked = true,
                wordCount = countWords(ch1Content),
                updatedAt = now - 86_400_000L * 3
            )
        )
        dao.insertChapter(
            ChapterEntity(
                bookId = book1Id,
                chapterNumber = 2,
                title = "Seni Berjalan Pelan di Dunia yang Bising",
                contentMarkdown = ch2Content,
                moodTag = "Tenang",
                status = "Selesai",
                isBookmarked = true,
                wordCount = countWords(ch2Content),
                updatedAt = now - 86_400_000L * 1
            )
        )
        dao.insertChapter(
            ChapterEntity(
                bookId = book1Id,
                chapterNumber = 3,
                title = "Panduan Format Markdown & Editor Kaya",
                contentMarkdown = ch3Content,
                moodTag = "Kreatif",
                status = "Draf",
                isBookmarked = false,
                wordCount = countWords(ch3Content),
                updatedAt = now - 3_600_000L
            )
        )

        // Book 2: Antologi Ide & Esai Senja
        val book2Id = dao.insertBook(
            BookEntity(
                title = "Antologi Esai & Kopi Sore",
                subtitle = "Draf cerita pendek, pengamatan keseharian, dan sketsa pemikiran",
                author = "Saya Sendiri",
                category = "Esai & Sastra",
                coverColorKey = "TERRACOTTA",
                targetWordCount = 4000,
                isPinned = false,
                createdAt = now - 86_400_000L * 4,
                updatedAt = now - 7_200_000L
            )
        )

        val b2ch1 = """
# Meja Sudut dan Hujan Bulan Oktober

Di sudut kedai kecil yang menghadap jalan basah, secangkir kopi hitam selalu punya cerita tentang waktu yang melambat.

> "Hujan tidak pernah terburu-buru sampai ke bumi; manusia saja yang selalu cemas akan jadwalnya."

## Sketsa Pengamatan
- Suara rintik air di atap seng yang berirama tetap
- Aroma tanah basah (*petrichor*) yang membangkitkan ingatan masa kecil
- Halaman buku catatan yang mulai terisi kalimat-kalimat jujur
        """.trimIndent()

        dao.insertChapter(
            ChapterEntity(
                bookId = book2Id,
                chapterNumber = 1,
                title = "Meja Sudut dan Hujan Bulan Oktober",
                contentMarkdown = b2ch1,
                moodTag = "Puitis",
                status = "Draf",
                isBookmarked = false,
                wordCount = countWords(b2ch1),
                updatedAt = now - 7_200_000L
            )
        )

        // Seed Quick Quotes
        dao.insertQuote(
            QuickQuoteEntity(
                bookTitle = "Buku untuk Saya",
                quoteText = "Menulis adalah cara kita mengabadikan percakapan paling jujur dengan diri sendiri sebelum waktu mengaburkannya.",
                reflectionNote = "Ditulis saat merancang halaman pembuka jurnal tahun ini.",
                tag = "Filosofi",
                isFavorite = true,
                createdAt = now - 86_400_000L * 2
            )
        )
        dao.insertQuote(
            QuickQuoteEntity(
                bookTitle = "Antologi Esai & Kopi Sore",
                quoteText = "Satu paragraf jujur yang ditulis hari ini lebih berharga daripada seribu rencana buku yang hanya tersimpan di angan.",
                reflectionNote = "Pengingat ketika mengalami kebuntuan menulis (writer's block).",
                tag = "Motivasi Menulis",
                isFavorite = true,
                createdAt = now - 86_400_000L
            )
        )
    }

    companion object {
        fun countWords(text: String): Int {
            val cleaned = text
                .replace(Regex("[#*>`_~\\-=\\[\\]()]"), " ")
                .trim()
            if (cleaned.isEmpty()) return 0
            return cleaned.split(Regex("\\s+")).count { it.isNotBlank() }
        }

        fun estimateReadingMinutes(wordCount: Int): Int {
            return max(1, (wordCount + 179) / 180)
        }
    }
}
