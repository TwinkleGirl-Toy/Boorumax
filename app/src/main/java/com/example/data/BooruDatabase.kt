package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BooruDao {
    // Booru Profiles
    @Query("SELECT * FROM booru_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BooruProfileEntity>>

    @Query("SELECT * FROM booru_profiles ORDER BY id ASC")
    suspend fun getAllProfilesSnapshot(): List<BooruProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BooruProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: BooruProfileEntity)

    @Query("UPDATE booru_profiles SET isActive = CASE WHEN id = :profileId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(profileId: Int)

    @Query("DELETE FROM booru_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Int)

    // Bookmarked Posts
    @Query("SELECT * FROM bookmarked_posts ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedPostEntity>>

    @Query("SELECT * FROM bookmarked_posts ORDER BY savedAt DESC")
    suspend fun getAllBookmarksSnapshot(): List<BookmarkedPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedPostEntity)

    @Query("DELETE FROM bookmarked_posts WHERE uniqueKey = :uniqueKey")
    suspend fun deleteBookmark(uniqueKey: String)

    // Saved & Blacklisted Tags
    @Query("SELECT * FROM saved_tags WHERE isBlacklisted = 0 ORDER BY createdAt DESC")
    fun getFavoriteTags(): Flow<List<SavedTagEntity>>

    @Query("SELECT * FROM saved_tags WHERE isBlacklisted = 1 ORDER BY createdAt DESC")
    fun getBlacklistedTags(): Flow<List<SavedTagEntity>>

    @Query("SELECT * FROM saved_tags ORDER BY createdAt DESC")
    suspend fun getAllSavedTagsSnapshot(): List<SavedTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedTag(tag: SavedTagEntity)

    @Query("DELETE FROM saved_tags WHERE id = :id")
    suspend fun deleteSavedTag(id: Int)

    @Query("DELETE FROM saved_tags WHERE tagQuery = :tagQuery AND isBlacklisted = :isBlacklisted")
    suspend fun deleteSavedTagByName(tagQuery: String, isBlacklisted: Boolean)
}

@Database(
    entities = [
        BooruProfileEntity::class,
        BookmarkedPostEntity::class,
        SavedTagEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BooruDatabase : RoomDatabase() {
    abstract fun booruDao(): BooruDao

    companion object {
        @Volatile
        private var INSTANCE: BooruDatabase? = null

        fun getDatabase(context: Context): BooruDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BooruDatabase::class.java,
                    "boorusama_plus_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
