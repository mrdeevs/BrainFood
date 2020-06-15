package com.hippo.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hippo.data.entities.Story

@Dao
interface SavedDao {
    @Query("SELECT * from saved_stories ORDER BY naturalIndex ASC")
    fun getSavedStories(): LiveData<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: Story)

    @Query("DELETE FROM saved_stories")
    suspend fun deleteAll()
}