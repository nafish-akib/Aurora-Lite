package com.aurora.browser.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.aurora.data.DataService
import com.aurora.data.model.Download
import com.aurora.data.preferences.SessionPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class ActiveDownload(
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val mimeType: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = Download.STATUS_PENDING,
    val speed: String = "",
    val filePath: String? = null
)

data class DownloadFileType(val label: String, val icon: String)

class DownloadManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _active = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val active: StateFlow<List<ActiveDownload>> = _active.asStateFlow()

    private val activeJobs = mutableMapOf<Long, Job>()
    private val activeConnections = mutableMapOf<Long, HttpURLConnection>()
    private val partialFiles = mutableMapOf<Long, File>()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val pausedByNetwork = mutableSetOf<Long>()
    private var networkReceiver: BroadcastReceiver? = null

    private val downloadDir: File
        get() {
            val prefs = SessionPreferences(context)
            val path = prefs.activeDownloadPath()
            val dir = File(path)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    init {
        createNotificationChannel()
        registerNetworkReceiver()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress and completion notifications"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun registerNetworkReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (isNetworkAvailable()) resumePausedByNetwork()
            }
        }
        networkReceiver = receiver
        context.registerReceiver(receiver, filter)
    }

    fun destroy() {
        networkReceiver?.let { try { context.unregisterReceiver(it) } catch (_: Exception) {} }
        networkReceiver = null
        scope.cancel()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun resumePausedByNetwork() {
        val snapshot = pausedByNetwork.toSet()
        pausedByNetwork.clear()
        snapshot.forEach { id ->
            val dl = _active.value.find { it.id == id }
            if (dl != null && dl.status == Download.STATUS_PAUSED) {
                resumeDownload(id)
            }
        }
    }

    private fun onActiveDownloadStarted() {
        DownloadForegroundService.start(context)
    }

    private fun onActiveDownloadCompleted() {
        val hasActive = _active.value.any { it.status == Download.STATUS_DOWNLOADING || it.status == Download.STATUS_PENDING }
        if (!hasActive) {
            DownloadForegroundService.stop(context)
        }
    }

    fun startDownload(url: String, fileName: String, mimeType: String = ""): DownloadResult {
        val safeName = sanitiseFileName(fileName)
        val targetFile = File(downloadDir, safeName)

        return if (targetFile.exists()) {
            DownloadResult.Duplicate(fileName, targetFile.absolutePath)
        } else {
            val localId = System.currentTimeMillis()
            val type = detectFileType(fileName, mimeType)
            val active = ActiveDownload(id = localId, url = url, fileName = fileName, mimeType = type.label, status = Download.STATUS_DOWNLOADING)
            addActive(active)
            onActiveDownloadStarted()
            showNotification(localId, fileName, type, 0, Download.STATUS_DOWNLOADING)
            scope.launch {
                val record = Download(id = localId, url = url, fileName = fileName, mimeType = mimeType, status = Download.STATUS_PENDING, timestamp = localId)
                DataService.downloads.add(record)
                downloadFile(active)
            }
            DownloadResult.Started(localId)
        }
    }

    fun registerCompletedDownload(url: String, fileName: String, mimeType: String, filePath: String) {
        val safeName = sanitiseFileName(fileName)
        val file = File(filePath)
        val fileSize = if (file.exists()) file.length() else 0L
        val localId = System.currentTimeMillis()
        val type = detectFileType(fileName, mimeType)
        val active = ActiveDownload(
            id = localId, url = url, fileName = fileName, mimeType = type.label,
            totalBytes = fileSize, downloadedBytes = fileSize, status = Download.STATUS_COMPLETED,
            filePath = filePath
        )
        addActive(active)
        showCompletedNotification(localId, fileName, file)
        val record = Download(
            id = localId, url = url, fileName = fileName, mimeType = mimeType,
            status = Download.STATUS_COMPLETED, timestamp = localId
        )
        kotlinx.coroutines.runBlocking { DataService.downloads.add(record) }
    }

    fun overwriteAndDownload(url: String, fileName: String, mimeType: String = ""): Long {
        val safeName = sanitiseFileName(fileName)
        val targetFile = File(downloadDir, safeName)
        targetFile.delete()

        val localId = System.currentTimeMillis()
        val type = detectFileType(fileName, mimeType)
        val active = ActiveDownload(id = localId, url = url, fileName = fileName, mimeType = type.label, status = Download.STATUS_DOWNLOADING)
        addActive(active)
        showNotification(localId, fileName, type, 0, Download.STATUS_DOWNLOADING)
        scope.launch {
            val record = Download(id = localId, url = url, fileName = fileName, mimeType = mimeType, status = Download.STATUS_PENDING, timestamp = localId)
            DataService.downloads.add(record)
            downloadFile(active)
        }
        return localId
    }

    fun pauseDownload(id: Long) {
        activeConnections[id]?.disconnect()
        activeConnections.remove(id)
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        updateActiveStatus(id, Download.STATUS_PAUSED)
        scope.launch { persistDownload(id, Download.STATUS_PAUSED) }
        cancelNotification(id)
    }

    fun resumeDownload(id: Long) {
        val current = _active.value.find { it.id == id } ?: return
        if (!isNetworkAvailable()) {
            pausedByNetwork.add(id)
            updateActiveStatus(id, Download.STATUS_PAUSED)
            return
        }
        updateActiveStatus(id, Download.STATUS_DOWNLOADING)
        showNotification(id, current.fileName, detectFileType(current.fileName, current.mimeType), current.downloadedBytes, Download.STATUS_DOWNLOADING)
        val job = scope.launch { downloadFile(current.copy(status = Download.STATUS_DOWNLOADING)) }
        activeJobs[id] = job
    }

    fun cancelDownload(id: Long) {
        activeConnections[id]?.disconnect()
        activeConnections.remove(id)
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        partialFiles[id]?.delete()
        partialFiles.remove(id)
        pausedByNetwork.remove(id)
        removeActive(id)
        cancelNotification(id)
        scope.launch { DataService.downloads.remove(id) }
        onActiveDownloadCompleted()
    }

    fun getHistory(onResult: (List<Download>) -> Unit) {
        scope.launch { onResult(DataService.downloads.getAll()) }
    }

    fun clearHistory() {
        scope.launch {
            val all = DataService.downloads.getAll()
            all.forEach { d ->
                if (d.status == Download.STATUS_COMPLETED || d.status == Download.STATUS_FAILED) {
                    DataService.downloads.remove(d.id)
                }
            }
        }
    }

    fun deleteFromHistory(id: Long, deleteFile: Boolean = false) {
        scope.launch {
            if (deleteFile) {
                val download = DataService.downloads.get(id)
                if (download != null) {
                    val file = File(downloadDir, sanitiseFileName(download.fileName))
                    try { file.delete() } catch (_: Exception) { }
                }
            }
            DataService.downloads.remove(id)
        }
    }

    fun retryDownload(url: String, fileName: String, mimeType: String): Long {
        cancelDownload(
            _active.value.find { it.url == url }?.id ?: return startDownload(url, fileName, mimeType).let {
                (it as? DownloadResult.Started)?.id ?: -1
            }
        )
        return startDownload(url, fileName, mimeType).let {
            (it as? DownloadResult.Started)?.id ?: -1
        }
    }

    fun openFile(context: Context, id: Long, fileName: String, mimeType: String = "") {
        scope.launch {
            try {
                val file = File(downloadDir, sanitiseFileName(fileName))
                if (!file.exists()) {
                    Log.w("AuroraDownload", "File not found: ${file.absolutePath}")
                    return@launch
                }
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else {
                    Uri.fromFile(file)
                }
                val extension = file.extension.lowercase()
                val mime = mimeType.ifEmpty {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("AuroraDownload", "Failed to open file: $fileName", e)
            }
        }
    }

    fun fileTypeFor(fileName: String, mimeType: String = ""): DownloadFileType = detectFileType(fileName, mimeType)

    companion object {
        const val CHANNEL_ID = "aurora_downloads"
        private const val NOTIFICATION_BASE_ID = 5000

        fun detectFileType(fileName: String, mimeType: String = ""): DownloadFileType {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            val mime = mimeType.ifEmpty {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
            }
            return when {
                mime.startsWith("video/") || extension in VIDEO_EXTS -> DownloadFileType("Video", "\uD83C\uDFAC")
                mime.startsWith("audio/") || extension in AUDIO_EXTS -> DownloadFileType("Audio", "\uD83C\uDFB5")
                mime == "application/pdf" || extension == "pdf" -> DownloadFileType("PDF", "\uD83D\uDCC4")
                mime.startsWith("image/") || extension in IMAGE_EXTS -> DownloadFileType("Image", "\uD83D\uDDBC")
                mime in ARCHIVE_MIMES || extension in ARCHIVE_EXTS -> DownloadFileType("Archive", "\uD83D\uDCE6")
                mime.startsWith("text/") && !mime.contains("html") -> DownloadFileType("Document", "\uD83D\uDCC4")
                extension in DOC_EXTS -> DownloadFileType("Document", "\uD83D\uDCC4")
                extension in EXEC_EXTS || extension == "apk" -> DownloadFileType("App", "\uD83D\uDCF1")
                else -> DownloadFileType("File", "\uD83D\uDCE5")
            }
        }

        private val VIDEO_EXTS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp")
        private val AUDIO_EXTS = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus")
        private val IMAGE_EXTS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff")
        private val ARCHIVE_EXTS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
        private val ARCHIVE_MIMES = setOf("application/zip", "application/x-rar-compressed", "application/x-7z-compressed", "application/gzip", "application/x-tar")
        private val DOC_EXTS = setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp")
        private val EXEC_EXTS = setOf("exe", "msi", "apk", "dmg", "pkg")
    }

    private suspend fun downloadFile(download: ActiveDownload) {
        var totalRead = download.downloadedBytes
        val file = partialFiles[download.id] ?: File(downloadDir, sanitiseFileName(download.fileName))

        try {
            if (!isNetworkAvailable()) {
                pausedByNetwork.add(download.id)
                updateActive(download.id) { it.copy(status = Download.STATUS_PAUSED, speed = "Waiting for network") }
                cancelNotification(download.id)
                return
            }

            val connection = openConnection(download.url, totalRead)
            activeConnections[download.id] = connection

            val contentLength = connection.contentLengthLong
            val totalSize = if (contentLength > 0) contentLength + totalRead else download.totalBytes
            val effectiveTotal = if (totalSize > 0) totalSize else (totalRead + 1024 * 1024)

            if (connection.responseCode != HttpURLConnection.HTTP_OK && connection.responseCode != HttpURLConnection.HTTP_PARTIAL) {
                updateActive(download.id) { it.copy(status = Download.STATUS_FAILED, totalBytes = effectiveTotal) }
                updateNotification(download.id, download.fileName, 0, effectiveTotal, Download.STATUS_FAILED)
                persistDownload(download.id, Download.STATUS_FAILED, totalRead, effectiveTotal)
                onActiveDownloadCompleted()
                return
            }

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(file, totalRead > 0)
            partialFiles[download.id] = file

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var lastReported = System.currentTimeMillis()
            var lastNotified = System.currentTimeMillis()
            var lastReportedBytes = totalRead

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                try { currentCoroutineContext().ensureActive() } catch (e: kotlinx.coroutines.CancellationException) { outputStream.close(); inputStream.close(); throw e }
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastReported > 200) {
                    val elapsed = (now - lastReported).toFloat() / 1000f
                    val deltaBytes = totalRead - lastReportedBytes
                    val speedBps = if (elapsed > 0) (deltaBytes / elapsed).toLong() else 0L
                    lastReported = now
                    lastReportedBytes = totalRead

                    updateActive(download.id) {
                        it.copy(totalBytes = effectiveTotal, downloadedBytes = totalRead,
                            status = Download.STATUS_DOWNLOADING, speed = formatSpeed(speedBps), filePath = file.absolutePath)
                    }
                }

                if (now - lastNotified > 1000) {
                    lastNotified = now
                    updateNotification(download.id, download.fileName, totalRead, effectiveTotal, Download.STATUS_DOWNLOADING)
                }
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()
            activeConnections.remove(download.id)
            partialFiles.remove(download.id)

            updateActive(download.id) {
                it.copy(downloadedBytes = totalRead, totalBytes = totalRead,
                    status = Download.STATUS_COMPLETED, speed = "", filePath = file.absolutePath)
            }
            showCompletedNotification(download.id, download.fileName, file)
            persistDownload(download.id, Download.STATUS_COMPLETED, totalRead, totalRead)
            onActiveDownloadCompleted()

        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: java.net.SocketTimeoutException) {
            pausedByNetwork.add(download.id)
            updateActive(download.id) { it.copy(status = Download.STATUS_PAUSED, speed = "Network timeout") }
            cancelNotification(download.id)
            persistDownload(download.id, Download.STATUS_PAUSED, totalRead, download.totalBytes)
        } catch (e: java.io.IOException) {
            if (!isNetworkAvailable()) {
                pausedByNetwork.add(download.id)
                updateActive(download.id) { it.copy(status = Download.STATUS_PAUSED, speed = "Waiting for network") }
                cancelNotification(download.id)
                persistDownload(download.id, Download.STATUS_PAUSED, totalRead, download.totalBytes)
            } else {
                updateActive(download.id) { it.copy(status = Download.STATUS_FAILED, filePath = file.absolutePath) }
                updateNotification(download.id, download.fileName, totalRead, download.totalBytes, Download.STATUS_FAILED)
                persistDownload(download.id, Download.STATUS_FAILED, totalRead, download.totalBytes)
                onActiveDownloadCompleted()
            }
        }
    }

    private fun openConnection(urlStr: String, offset: Long): HttpURLConnection {
        val connection = URL(urlStr).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true
        if (offset > 0) {
            connection.setRequestProperty("Range", "bytes=$offset-")
        }
        return connection
    }

    private fun showNotification(id: Long, fileName: String, type: DownloadFileType, current: Long, status: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(if (status == Download.STATUS_DOWNLOADING) "Downloading $fileName" else fileName)
            .setContentText("${type.icon} ${type.label}")
            .setOngoing(status == Download.STATUS_DOWNLOADING)
            .setAutoCancel(status == Download.STATUS_COMPLETED)
            .setProgress(100, 0, status == Download.STATUS_DOWNLOADING)
        notificationManager.notify(NOTIFICATION_BASE_ID + id.toInt().and(0xFFFF), builder.build())
    }

    private fun updateNotification(id: Long, fileName: String, current: Long, total: Long, status: String) {
        val progress = if (total > 0) ((current.toFloat() / total) * 100).toInt().coerceIn(0, 99) else 0
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $fileName")
            .setContentText("$progress% - ${formatFileSizeShort(current)} / ${formatFileSizeShort(total)}")
            .setOngoing(true)
            .setProgress(100, progress, false)
        notificationManager.notify(NOTIFICATION_BASE_ID + id.toInt().and(0xFFFF), builder.build())
    }

    private fun showCompletedNotification(id: Long, fileName: String, file: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val pending = PendingIntent.getActivity(context, id.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download complete")
            .setContentText(fileName)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setProgress(0, 0, false)
        notificationManager.notify(NOTIFICATION_BASE_ID + id.toInt().and(0xFFFF), builder.build())
    }

    private fun cancelNotification(id: Long) {
        notificationManager.cancel(NOTIFICATION_BASE_ID + id.toInt().and(0xFFFF))
    }

    private fun addActive(dl: ActiveDownload) { _active.value = _active.value + dl }
    private fun updateActive(id: Long, transform: (ActiveDownload) -> ActiveDownload) { _active.value = _active.value.map { if (it.id == id) transform(it) else it } }
    private fun updateActiveStatus(id: Long, status: String) { _active.value = _active.value.map { if (it.id == id) it.copy(status = status) else it } }
    private fun removeActive(id: Long) { _active.value = _active.value.filter { it.id != id } }

    private suspend fun persistDownload(id: Long, status: String, downloaded: Long = -1, total: Long = -1) {
        val all = DataService.downloads.getAll()
        val existing = all.find { it.id == id }
        if (existing != null) {
            DataService.downloads.update(existing.copy(
                status = status,
                downloadedBytes = if (downloaded >= 0) downloaded else existing.downloadedBytes,
                totalBytes = if (total >= 0) total else existing.totalBytes
            ))
        }
    }

    private fun sanitiseFileName(name: String): String = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(200)
    private fun formatSpeed(bytesPerSec: Long): String = when {
        bytesPerSec >= 1_000_000 -> "%.1f MB/s".format(bytesPerSec / 1_000_000.0)
        bytesPerSec >= 1_000 -> "%.0f KB/s".format(bytesPerSec / 1_000.0)
        bytesPerSec > 0 -> "$bytesPerSec B/s"
        else -> ""
    }
    private fun formatFileSizeShort(bytes: Long): String = when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}

sealed class DownloadResult {
    data class Started(val id: Long) : DownloadResult()
    data class Duplicate(val fileName: String, val existingPath: String) : DownloadResult()
}
