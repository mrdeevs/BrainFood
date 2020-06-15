package com.hippo.data

import androidx.lifecycle.LiveData
import com.hippo.dao.SavedDao
import com.hippo.dao.StoryDao
import com.hippo.data.entities.Story

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class SavedRepository(private val savedDao: SavedDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val savedStories: LiveData<List<Story>> = savedDao.getSavedStories()

    suspend fun insert(story: Story) {
        savedDao.insert(story)
    }

    suspend fun deleteAll() {
        savedDao.deleteAll()
    }
}