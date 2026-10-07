package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BookRepository
import com.example.ui.components.toggleMarkdownCheckboxAtLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Buku untuk Saya", appName)
    }

    @Test
    fun `markdown word count and interactive checkbox toggle work`() {
        val sample = "# Judul Bab\n- [ ] Menulis catatan jujur hari ini"
        val words = BookRepository.countWords(sample)
        assertTrue(words >= 6)

        val toggled = toggleMarkdownCheckboxAtLine(sample, 1)
        assertTrue(toggled.contains("- [x] Menulis catatan jujur hari ini"))
    }
}
