package com.hippo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hippo.data.entities.SavedStory

@Dao
interface SavedDao {
    @Query("SELECT * from saved_stories")
    suspend fun getSavedStories(): List<SavedStory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: SavedStory)

    @Query("DELETE FROM saved_stories")
    suspend fun deleteAll()
}