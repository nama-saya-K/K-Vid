package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ui.theme.CoverEmerald
import com.example.ui.theme.CoverEspresso
import com.example.ui.theme.CoverNavy
import com.example.ui.theme.CoverOchre
import com.example.ui.theme.CoverPlum
import com.example.ui.theme.CoverTerracotta

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val author: String = "Saya Sendiri",
    val category: String = "Jurnal & Refleksi",
    val coverColorKey: String = "ESPRESSO",
    val targetWordCount: Int = 5000,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterNumber: Int,
    val title: String,
    val contentMarkdown: String,
    val moodTag: String = "Tenang",
    val status: String = "Draf", // "Draf", "Revisi", "Selesai"
    val isBookmarked: Boolean = false,
    val wordCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_quotes")
data class QuickQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookTitle: String,
    val quoteText: String,
    val reflectionNote: String,
    val tag: String = "Gagasan",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookWithStats(
    val book: BookEntity,
    val chapterCount: Int,
    val totalWords: Int,
    val bookmarkedCount: Int,
    val completedChapters: Int
)

enum class CoverThemeOption(
    val key: String,
    val label: String,
    val primaryColor: Color,
    val accentColor: Color
) {
    ESPRESSO("ESPRESSO", "Kopi Senja", CoverEspresso, Color(0xFFD9A76A)),
    TERRACOTTA("TERRACOTTA", "Senja Terakota", CoverTerracotta, Color(0xFFF3C6A8)),
    EMERALD("EMERALD", "Tinta Zamrud", CoverEmerald, Color(0xFFA7D7B8)),
    NAVY("NAVY", "Samudra Malam", CoverNavy, Color(0xFFA8C5E6)),
    PLUM("PLUM", "Anggur Klasik", CoverPlum, Color(0xFFE3B5D3)),
    OCHRE("OCHRE", "Kayu Jati Emas", CoverOchre, Color(0xFFF5DCA8));

    companion object {
        fun fromKey(key: String): CoverThemeOption =
            entries.find { it.key == key } ?: ESPRESSO
    }
}

enum class ReaderPaperTheme(
    val key: String,
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    val secondaryColor: Color,
    val quoteAccentColor: Color
) {
    KREM(
        "KREM",
        "Kertas Krem",
        Color(0xFFFBF6EC),
        Color(0xFF261B13),
        Color(0xFF695545),
        Color(0xFFA6632D)
    ),
    SEPIA(
        "SEPIA",
        "Sepia Klasik",
        Color(0xFFF1E3CD),
        Color(0xFF2D1E10),
        Color(0xFF6B4F37),
        Color(0xFF8C4820)
    ),
    PUTIH(
        "PUTIH",
        "Putih Bersih",
        Color(0xFFFAFAFA),
        Color(0xFF1E1E1E),
        Color(0xFF616161),
        Color(0xFF4A6B53)
    ),
    MALAM(
        "MALAM",
        "Malam Teduh",
        Color(0xFF171310),
        Color(0xFFEDE2D5),
        Color(0xFFA69686),
        Color(0xFFD9A066)
    )
}

data class WritingTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultMood: String,
    val markdownContent: String
)

val DefaultWritingTemplates = listOf(
    WritingTemplate(
        id = "refleksi_harian",
        title = "Refleksi Harian & Syukur",
        subtitle = "Merenungkan kejadian hari ini dengan tenang",
        defaultMood = "Bersyukur",
        markdownContent = """
# Refleksi Hari Ini

> "Menulis adalah cara terbaik untuk berbicara tanpa disela, dan mendengar suara hati sendiri."

## Tiga Hal yang Saya Syukuri
- [x] Momen kecil yang memberi ketenangan pagi ini
- [ ] Pelajaran berharga dari percakapan hari ini
- [ ] Kesehatan dan kesempatan untuk terus bertumbuh

## Apa yang Berjalan dengan Baik?
Tuliskan cerita atau momen yang membuat hati terasa hangat hari ini. Anda dapat menggunakan teks **tebal**, *miring*, atau ==sorotan penting==.

## Hal yang Ingin Dilepaskan
Apa beban pikiran yang tidak perlu dibawa ke hari esok?

---

### Catatan untuk Diri Sendiri Besok
1. Fokus pada satu langkah kecil yang bermakna.
2. Jaga jeda dan napas di tengah kesibukan.
        """.trimIndent()
    ),
    WritingTemplate(
        id = "surat_masa_depan",
        title = "Surat untuk Diri di Masa Depan",
        subtitle = "Pesan jujur untuk dibaca kembali nanti",
        defaultMood = "Toh Harapan",
        markdownContent = """
# Halo, Diri Saya di Masa Depan

Jika kamu membuka kembali halaman ini, ingatlah bagaimana rasanya berada di titik perjalanan ini.

## Keadaan Saat Ini
Saat menulis bab ini, hal yang sedang paling sering kupikirkan adalah...

> "Jangan takut berjalan pelan; takutlah jika hanya berhenti di tempat."

## Impian & Janji yang Sedang Dijaga
- [ ] Menyelesaikan karya tulis pribadi ini hingga tuntas
- [ ] Merawat kebiasaan membaca dan menulis setiap pekan
- [ ] Lebih ramah dan sabar terhadap proses diri sendiri

## Pertanyaan untukmu Saat Membaca Ini
Apakah hal yang dulu kamu khawatirkan ternyata bisa dilewati dengan baik?
        """.trimIndent()
    ),
    WritingTemplate(
        id = "kerangka_cerita",
        title = "Bab Narasi / Esai Pribadi",
        subtitle = "Struktur bab untuk buku nonfiksi, memoar, atau fiksi",
        defaultMood = "Kreatif",
        markdownContent = """
# Judul Bab: Jejak Langkah Pertama

## 1. Pembuka & Suasana
Gambarkan latar suasana, waktu, atau pertanyaan pemantik yang membuka bab ini.

## 2. Konflik / Peristiwa Utama
Tuliskan gagasan inti atau peristiwa yang mengubah cara pandang:

- **Latar Belakang:** Mengapa peristiwa ini penting?
- **Titik Balik:** Apa yang terjadi di luar dugaan?
- **Makna Baru:** Apa kesadaran yang muncul setelahnya?

> "Setiap bab dalam hidup layak dicatat agar tidak hilang bersama waktu."

---

## 3. Penutup Bab
Simpulkan dengan satu kalimat reflektif yang menghubungkan bab ini dengan bab berikutnya.
        """.trimIndent()
    ),
    WritingTemplate(
        id = "catatan_bacaan",
        title = "Catatan Buku & Ringkasan Ide",
        subtitle = "Merangkum intisari buku yang sedang dibaca",
        defaultMood = "Fokus",
        markdownContent = """
# Catatan Bacaan & Intisari

## Identitas Karya
- **Judul / Topik:** 
- **gagasan Utama:** 

## Kutipan Paling Membekas
> "Buku yang baik tidak hanya memberi jawaban, tetapi mengubah kualitas pertanyaan kita."

## Poin-Poin Penting
- Gagasan pertama yang mengubah perspektif saya
- Hubungan ide ini dengan pengalaman pribadi
- Langkah nyata yang ingin saya terapkan:
  - [ ] Mencoba kebiasaan baru selama 7 hari
  - [ ] Mendiskusikan ide ini dalam tulisan berikutnya
        """.trimIndent()
    )
)
