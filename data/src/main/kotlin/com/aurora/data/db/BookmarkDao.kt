package com.aurora.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM bookmarks ORDER BY `order` ASC")
    suspend fun getAll(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE folderId = :folderId ORDER BY `order` ASC")
    suspend fun getByFolder(folderId: Long): List<BookmarkEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    suspend fun exists(url: String): Boolean

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): BookmarkEntity?

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    @Query("UPDATE bookmarks SET folderId = :folderId WHERE id = :id")
    suspend fun moveToFolder(id: Long, folderId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(entity: BookmarkFolderEntity): Long

    @Query("SELECT * FROM bookmark_folders ORDER BY `order` ASC, name ASC")
    suspend fun getFolders(): List<BookmarkFolderEntity>

    @Query("SELECT * FROM bookmark_folders WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): BookmarkFolderEntity?

    @Query("DELETE FROM bookmark_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Query("UPDATE bookmarks SET folderId = 0 WHERE folderId = :folderId")
    suspend fun moveFolderBookmarksToRoot(folderId: Long)
}
