package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun getBookByIdFlow(bookId: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: Long): BookEntity?

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long)

    // Chapters
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC, id ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY updatedAt DESC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun getChapterByIdFlow(chapterId: Long): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC, id ASC")
    suspend fun getChaptersForBookOnce(bookId: Long): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: Long)

    // Quick Quotes & Ideas
    @Query("SELECT * FROM quick_quotes ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllQuotes(): Flow<List<QuickQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuickQuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: QuickQuoteEntity)

    @Query("DELETE FROM quick_quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: Long)
}
