package com.hippo.data

import androidx.lifecycle.LiveData
import com.hippo.dao.StoryDao

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class NewsRepository(private val storyDao: StoryDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val newStories: LiveData<List<Story>> = storyDao.getNewestStories()
    val topStories: LiveData<List<Story>> = storyDao.getTopStories()

    suspend fun insert(story: Story) {
        storyDao.insert(story)
    }

    suspend fun deleteAll() {
        storyDao.deleteAll()
    }
}