package com.aurora.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryFavoriteRepositoryTest {

    @Test
    fun `add returns id and getByUrl finds it`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val id = repo.add("https://example.com", "Example", 0L)
        assertTrue(id > 0)
        assertEquals("Example", repo.getByUrl("https://example.com")?.title)
        assertTrue(repo.isFavorite("https://example.com"))
    }

    @Test
    fun `adding same url twice returns same id`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val first = repo.add("https://example.com", "First", 0L)
        val second = repo.add("https://example.com", "Second", 0L)
        assertEquals(first, second)
        assertEquals(1, repo.getAll().size)
    }

    @Test
    fun `remove deletes bookmark`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val id = repo.add("https://example.com", "X", 0L)
        repo.remove(id)
        assertFalse(repo.isFavorite("https://example.com"))
        assertTrue(repo.getAll().isEmpty())
    }

    @Test
    fun `getAll preserves insertion order`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        repo.add("https://a.com", "A", 0L)
        repo.add("https://b.com", "B", 0L)
        assertEquals(listOf("https://a.com", "https://b.com"), repo.getAll().map { it.url })
    }

    @Test
    fun `folders dedupe by name ignoring case`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val first = repo.addFolder("Work")
        val second = repo.addFolder("  work  ")
        assertEquals(first, second)
    }

    @Test
    fun `blank folder name falls back to New Folder`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val id = repo.addFolder("   ")
        assertTrue(id > 0)
        assertEquals("New Folder", repo.getFolders().first { it.id == id }.name)
    }

    @Test
    fun `getFolders returns Unsorted plus real and synthetic folders`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val folderId = repo.addFolder("Reading")
        repo.add("https://x.com", "X", folderId)
        repo.add("https://y.com", "Y", 99L)
        val folders = repo.getFolders()
        assertTrue(folders.any { it.name == "Unsorted" })
        assertTrue(folders.any { it.name == "Reading" })
        assertTrue(folders.any { it.name == "Folder 99" })
    }

    @Test
    fun `removeFolder reassigns its bookmarks to unsorted`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val folderId = repo.addFolder("Temp")
        val bookmarkId = repo.add("https://example.com", "X", folderId)
        repo.removeFolder(folderId)
        assertEquals(0L, repo.getAll().first { it.id == bookmarkId }.folderId)
    }

    @Test
    fun `unsorted folder cannot be removed`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        repo.add("https://example.com", "X", 0L)
        repo.removeFolder(0L)
        assertEquals(1, repo.getAll().size)
    }

    @Test
    fun `moveToFolder updates folder assignment`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val issue = repo.add("https://example.com", "X", 0L)
        val folderId = repo.addFolder("Work")
        repo.moveToFolder(issue, folderId)
        assertEquals(folderId, repo.getByUrl("https://example.com")?.folderId)
    }

    @Test
    fun `getByFolder filters and sorts`() = runBlocking {
        val repo = InMemoryFavoriteRepository()
        val folderId = repo.addFolder("Work")
        repo.add("https://a.com", "A", 0L)
        repo.add("https://b.com", "B", folderId)
        repo.add("https://c.com", "C", folderId)
        assertEquals(listOf("https://b.com", "https://c.com"), repo.getByFolder(folderId).map { it.url })
        assertNull(repo.getByUrl("https://nope.com"))
    }
}