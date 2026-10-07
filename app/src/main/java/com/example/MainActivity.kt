package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BukuViewModel
import com.example.ui.MainNavTab
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.ChapterEditorScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MarkdownExportPreviewDialog
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.StatsAndExportScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BukuUntukSayaApp()
            }
        }
    }
}

@Composable
fun BukuUntukSayaApp(
    viewModel: BukuViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val booksWithStats by viewModel.booksWithStats.collectAsStateWithLifecycle()
    val filteredBooks by viewModel.filteredBooksWithStats.collectAsStateWithLifecycle()
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val currentBook by viewModel.currentBook.collectAsStateWithLifecycle()
    val currentBookChapters by viewModel.currentBookChapters.collectAsStateWithLifecycle()
    val filteredBookChapters by viewModel.filteredCurrentBookChapters.collectAsStateWithLifecycle()
    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val chapterFilter by viewModel.chapterFilter.collectAsStateWithLifecycle()
    val editorMode by viewModel.editorMode.collectAsStateWithLifecycle()
    val paperTheme by viewModel.paperTheme.collectAsStateWithLifecycle()
    val readerFontSizeSp by viewModel.readerFontSizeSp.collectAsStateWithLifecycle()
    val useSerifInReader by viewModel.useSerifInReader.collectAsStateWithLifecycle()
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle()
    val exportedMarkdownDialog by viewModel.exportedMarkdownDialog.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val totalWordsAllBooks = remember(booksWithStats) {
        booksWithStats.sumOf { it.totalWords }
    }
    val totalChaptersAllBooks = remember(booksWithStats) {
        booksWithStats.sumOf { it.chapterCount }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!isExpanded) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (tab == MainNavTab.PUSTAKA && currentTab == MainNavTab.PUSTAKA) {
                                        viewModel.closeBookDetail()
                                    } else {
                                        viewModel.selectTab(tab)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpanded) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("side_nav_rail")
                    ) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (tab == MainNavTab.PUSTAKA && currentTab == MainNavTab.PUSTAKA) {
                                        viewModel.closeBookDetail()
                                    } else {
                                        viewModel.selectTab(tab)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        MainNavTab.PUSTAKA -> {
                            val activeBook = currentBook
                            if (activeBook != null) {
                                BookDetailScreen(
                                    book = activeBook,
                                    allChapters = currentBookChapters,
                                    filteredChapters = filteredBookChapters,
                                    chapterFilter = chapterFilter,
                                    onFilterChange = viewModel::setChapterFilter,
                                    onBack = viewModel::closeBookDetail,
                                    onOpenChapter = { chapterId, mode ->
                                        viewModel.openChapterEditor(activeBook.id, chapterId, mode)
                                    },
                                    onCreateChapter = { title, mood ->
                                        viewModel.createNewChapter(
                                            bookId = activeBook.id,
                                            title = title,
                                            moodTag = mood,
                                            openEditorImmediately = true
                                        )
                                    },
                                    onCreateFromTemplate = { template ->
                                        viewModel.createChapterFromTemplate(activeBook.id, template)
                                    },
                                    onToggleBookmark = viewModel::toggleBookmark,
                                    onMoveChapter = { ch, moveUp ->
                                        viewModel.moveChapter(activeBook.id, ch, moveUp)
                                    },
                                    onDeleteChapter = viewModel::deleteChapter,
                                    onExportBookMarkdown = {
                                        viewModel.requestExportBookMarkdown(activeBook.id, activeBook.title)
                                    }
                                )
                            } else {
                                LibraryScreen(
                                    booksWithStats = filteredBooks,
                                    allBooksCount = booksWithStats.size,
                                    totalWordsAllBooks = totalWordsAllBooks,
                                    totalChaptersAllBooks = totalChaptersAllBooks,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    onSearchQueryChange = viewModel::setSearchQuery,
                                    onCategorySelect = viewModel::setSelectedCategory,
                                    onOpenBook = viewModel::openBookDetail,
                                    onContinueWriting = {
                                        val latestChapter = allChapters.firstOrNull()
                                        if (latestChapter != null) {
                                            viewModel.openChapterEditor(
                                                latestChapter.bookId,
                                                latestChapter.id
                                            )
                                        } else {
                                            viewModel.selectTab(MainNavTab.EDITOR)
                                        }
                                    },
                                    onCreateBook = { title, subtitle, author, category, colorKey, targetWords ->
                                        viewModel.createNewBook(
                                            title = title,
                                            subtitle = subtitle,
                                            author = author,
                                            category = category,
                                            coverColorKey = colorKey,
                                            targetWordCount = targetWords
                                        )
                                    },
                                    onUpdateBook = viewModel::updateBookDetails,
                                    onTogglePinBook = viewModel::togglePinBook,
                                    onDeleteBook = viewModel::deleteBook,
                                    onImportMarkdown = viewModel::importBookFromMarkdown
                                )
                            }
                        }

                        MainNavTab.EDITOR -> {
                            ChapterEditorScreen(
                                book = currentBook,
                                chapter = currentChapter,
                                bookChapters = currentBookChapters,
                                editorMode = editorMode,
                                paperTheme = paperTheme,
                                readerFontSizeSp = readerFontSizeSp,
                                useSerifInReader = useSerifInReader,
                                onEditorModeChange = viewModel::setEditorMode,
                                onPaperThemeChange = viewModel::setPaperTheme,
                                onAdjustFontSize = viewModel::adjustReaderFontSize,
                                onToggleReaderFontStyle = viewModel::toggleReaderFontStyle,
                                onSaveChapter = viewModel::saveChapterChanges,
                                onSelectChapter = { targetChapterId ->
                                    val bookId = currentBook?.id ?: return@ChapterEditorScreen
                                    viewModel.openChapterEditor(bookId, targetChapterId, editorMode)
                                },
                                onNavigatePrevNext = viewModel::openAdjacentChapter,
                                onSaveSelectionAsQuote = { snippet ->
                                    viewModel.addQuickQuote(
                                        bookTitle = currentBook?.title ?: "Buku untuk Saya",
                                        quoteText = snippet,
                                        reflectionNote = "Diambil dari Bab: ${currentChapter?.title ?: ""}",
                                        tag = "Kutipan Bab"
                                    )
                                },
                                onBackToBook = viewModel::navigateBackFromEditorToBook
                            )
                        }

                        MainNavTab.KUTIPAN -> {
                            QuotesScreen(
                                quotes = allQuotes,
                                booksWithStats = booksWithStats,
                                onAddQuote = viewModel::addQuickQuote,
                                onToggleFavorite = viewModel::toggleFavoriteQuote,
                                onDeleteQuote = viewModel::deleteQuote,
                                onConvertQuoteToChapter = viewModel::convertQuoteToChapter,
                                onBackToHome = { viewModel.selectTab(MainNavTab.PUSTAKA) }
                            )
                        }

                        MainNavTab.JEJAK -> {
                            StatsAndExportScreen(
                                booksWithStats = booksWithStats,
                                quotesCount = allQuotes.size,
                                onExportBook = viewModel::requestExportBookMarkdown,
                                onOpenBook = viewModel::openBookDetail,
                                onImportMarkdown = viewModel::importBookFromMarkdown,
                                onBackToHome = { viewModel.selectTab(MainNavTab.PUSTAKA) }
                            )
                        }
                    }
                }
            }
        }
    }

    exportedMarkdownDialog?.let { (bookTitle, mdContent) ->
        MarkdownExportPreviewDialog(
            bookTitle = bookTitle,
            markdownContent = mdContent,
            onDismiss = viewModel::dismissExportDialog,
            onCopiedFeedback = {
                // Handled by system clipboard & dialog dismissal
            }
        )
    }
}

private fun tabIcon(tab: MainNavTab, selected: Boolean): ImageVector {
    return when (tab) {
        MainNavTab.PUSTAKA ->
            if (selected) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook
        MainNavTab.EDITOR ->
            if (selected) Icons.Filled.EditNote else Icons.Outlined.EditNote
        MainNavTab.KUTIPAN ->
            if (selected) Icons.Filled.FormatQuote else Icons.Outlined.FormatQuote
        MainNavTab.JEJAK ->
            if (selected) Icons.Filled.Analytics else Icons.Outlined.Analytics
    }
}
