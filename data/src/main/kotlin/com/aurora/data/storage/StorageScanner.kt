package com.aurora.data.storage

import android.content.Context
import android.os.Environment
import java.io.File

data class StorageVolumeInfo(
    val downloadPath: File,
    val isRemovable: Boolean,
    val displayName: String,
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val isDefault: Boolean = false
)

object StorageScanner {

    fun getAvailableStorageVolumes(context: Context): List<StorageVolumeInfo> {
        val volumes = mutableListOf<StorageVolumeInfo>()
        val externalDirs = context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)
            ?: return volumes

        externalDirs.forEachIndexed { index, dir ->
            if (dir == null) return@forEachIndexed
            val isPrimary = index == 0
            val isRemovable = !isPrimary && Environment.isExternalStorageRemovable(dir)

            val displayName = when {
                isPrimary -> "Internal TV Storage"
                else -> {
                    val segments = dir.absolutePath.split(File.separator)
                    val storageIdx = segments.indexOf("storage")
                    if (storageIdx >= 0 && storageIdx + 1 < segments.size) {
                        "USB Drive (${segments[storageIdx + 1]})"
                    } else {
                        "USB Drive $index"
                    }
                }
            }

            val totalBytes = try {
                val stat = android.os.StatFs(dir.absolutePath)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                blockSize * totalBlocks
            } catch (_: Exception) { 0L }

            val availableBytes = try {
                val stat = android.os.StatFs(dir.absolutePath)
                val blockSize = stat.blockSizeLong
                val availableBlocks = stat.availableBlocksLong
                blockSize * availableBlocks
            } catch (_: Exception) { 0L }

            volumes.add(
                StorageVolumeInfo(
                    downloadPath = dir,
                    isRemovable = isRemovable,
                    displayName = displayName,
                    totalBytes = totalBytes,
                    availableBytes = availableBytes,
                    isDefault = isPrimary
                )
            )
        }

        return volumes.distinctBy { it.downloadPath.absolutePath }
    }

    fun formatSize(bytes: Long): String = when {
        bytes >= 1_000_000_000_000 -> "%.1f TB".format(bytes / 1_000_000_000_000.0)
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.0f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}