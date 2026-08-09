package com.aurora.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HistoryEntity::class, BookmarkEntity::class, BookmarkFolderEntity::class, DownloadEntity::class, WebsiteEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao
    abstract fun websiteDao(): WebsiteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `url` TEXT NOT NULL, `title` TEXT NOT NULL DEFAULT '', `order` INTEGER NOT NULL DEFAULT 0, `addedAt` INTEGER NOT NULL DEFAULT 0, `faviconUrl` TEXT NOT NULL DEFAULT '')")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bookmarks_url` ON `bookmarks` (`url`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `downloads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `url` TEXT NOT NULL, `fileName` TEXT NOT NULL DEFAULT '', `mimeType` TEXT NOT NULL DEFAULT '', `totalBytes` INTEGER NOT NULL DEFAULT 0, `downloadedBytes` INTEGER NOT NULL DEFAULT 0, `status` TEXT NOT NULL DEFAULT '', `timestamp` INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `websites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `url` TEXT NOT NULL, `title` TEXT NOT NULL DEFAULT '', `faviconUri` TEXT, `thumbnailUri` TEXT, `dominantColor` INTEGER NOT NULL DEFAULT -15066598, `themeColor` INTEGER, `lastVisited` INTEGER NOT NULL DEFAULT 0, `visitCount` INTEGER NOT NULL DEFAULT 0, `scrollPosition` INTEGER NOT NULL DEFAULT 0, `pageLanguage` TEXT, `isSecure` INTEGER NOT NULL DEFAULT 0, `certificateIssuer` TEXT NOT NULL DEFAULT '', `isVerified` INTEGER NOT NULL DEFAULT 0, `isBookmarked` INTEGER NOT NULL DEFAULT 0, `isPinned` INTEGER NOT NULL DEFAULT 0, `openTabCount` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_websites_url` ON `websites` (`url`)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bookmarks` ADD COLUMN `folderId` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmark_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bookmark_folders_name` ON `bookmark_folders` (`name`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aurora.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build().also { instance = it }
            }
        }
    }
}
