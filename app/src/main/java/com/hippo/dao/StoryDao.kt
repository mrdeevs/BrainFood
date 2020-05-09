package com.hippo.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hippo.data.Story

@Dao
interface StoryDao {

    @Query("SELECT * from stories ORDER BY time DESC")
    fun getBestStories(): LiveData<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: Story)

    @Query("DELETE FROM stories")
    suspend fun deleteAll()
}