package com.aurora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.aurora.data.model.BookmarkFolder

@Entity(
    tableName = "bookmark_folders",
    indices = [Index(value = ["name"], unique = true)]
)
data class BookmarkFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "0")
    val order: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): BookmarkFolder = BookmarkFolder(id = id, name = name, order = order)
}
