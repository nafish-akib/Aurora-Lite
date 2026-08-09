package com.aurora.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryHistoryRepositoryTest {

    @Test
    fun `new entries get sequential ids`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "A", "", null, false)
        repo.addEntry("https://b.com", "B", "", null, false)
        val all = repo.getAll()
        assertEquals(2, all.size)
        assertEquals(1L, all.first { it.url == "https://a.com" }.id)
        assertEquals(2L, all.first { it.url == "https://b.com" }.id)
    }

    @Test
    fun `revisiting same url increments visit count and keeps title`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "First Title", "", null, false)
        Thread.sleep(5)
        repo.addEntry("https://a.com", "", "", null, false)
        val entry = repo.getEntryByUrl("https://a.com")
        assertEquals(2, entry!!.visitCount)
        assertEquals("First Title", entry.title)
        assertEquals(1, repo.getAll().size)
    }

    @Test
    fun `getRecent returns most recent first and respects limit`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "A", "", null, false)
        sleep(5)
        repo.addEntry("https://b.com", "B", "", null, false)
        sleep(5)
        repo.addEntry("https://c.com", "C", "", null, false)
        val recent = repo.getRecent(2)
        assertEquals(listOf("https://c.com", "https://b.com"), recent.map { it.url })
    }

    @Test
    fun `search matches url or title ignoring case`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://example.com/page", "Aurora Browser News", "", null, false)
        repo.addEntry("https://other.org", "Kotlin", "", null, false)
        val byTitle = repo.search("aurora")
        assertEquals(1, byTitle.size)
        val byUrl = repo.search("example")
        assertEquals(1, byUrl.size)
        val none = repo.search("zzz")
        assertTrue(none.isEmpty())
    }

    @Test
    fun `deleteEntry removes only that entry`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "A", "", null, false)
        repo.addEntry("https://b.com", "B", "", null, false)
        val id = repo.getEntryByUrl("https://a.com")!!.id
        repo.deleteEntry(id)
        assertNull(repo.getEntryByUrl("https://a.com"))
        assertEquals(1, repo.getAll().size)
    }

    @Test
    fun `clearLastHour removes recent entries`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "A", "", null, false)
        repo.addEntry("https://b.com", "B", "", null, false)
        repo.clearLastHour()
        assertTrue(repo.getAll().isEmpty())
    }

    @Test
    fun `clear removes everything`() = runBlocking {
        val repo = InMemoryHistoryRepository()
        repo.addEntry("https://a.com", "A", "", null, false)
        repo.clear()
        assertTrue(repo.getAll().isEmpty())
    }

    private suspend fun sleep(ms: Long) = kotlinx.coroutines.delay(ms)
}