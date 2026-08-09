package com.aurora.data.repository

import com.aurora.data.model.Download
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryDownloadRepositoryTest {

    @Test
    fun `add assigns incremental ids`() = runBlocking {
        val repo = InMemoryDownloadRepository()
        val first = repo.add(Download(url = "https://a.com/file.zip"))
        val second = repo.add(Download(url = "https://b.com/file2.zip"))
        assertEquals(1L, first)
        assertEquals(2L, second)
    }

    @Test
    fun `get returns stored download`() = runBlocking {
        val repo = InMemoryDownloadRepository()
        val id = repo.add(Download(url = "https://a.com/f.zip", fileName = "f.zip"))
        val d = repo.get(id)
        assertEquals("f.zip", d!!.fileName)
        assertEquals(Download.STATUS_PENDING, d.status)
    }

    @Test
    fun `update replaces fields`() = runBlocking {
        val repo = InMemoryDownloadRepository()
        val id = repo.add(Download(url = "https://a.com/f.zip"))
        repo.update(Download(id = id, url = "https://a.com/f.zip", downloadedBytes = 500, status = Download.STATUS_COMPLETED))
        val d = repo.get(id)
        assertEquals(500L, d!!.downloadedBytes)
        assertEquals(Download.STATUS_COMPLETED, d.status)
    }

    @Test
    fun `getAll and remove work`() = runBlocking {
        val repo = InMemoryDownloadRepository()
        val id = repo.add(Download(url = "https://a.com/1"))
        repo.add(Download(url = "https://a.com/2"))
        assertEquals(2, repo.getAll().size)
        repo.remove(id)
        assertEquals(1, repo.getAll().size)
        assertNull(repo.get(id))
    }

    @Test
    fun `status constants are distinct`() {
        val statuses = setOf(
            Download.STATUS_PENDING,
            Download.STATUS_DOWNLOADING,
            Download.STATUS_COMPLETED,
            Download.STATUS_FAILED,
            Download.STATUS_PAUSED
        )
        assertEquals(5, statuses.size)
        assertTrue(Download.STATUS_DOWNLOADING in statuses)
    }
}