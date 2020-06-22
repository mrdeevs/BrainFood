package com.hippo.data

import com.hippo.dao.SavedDao
import com.hippo.data.entities.SavedStory

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class SavedRepository(private val savedDao: SavedDao) {

    suspend fun insert(story: SavedStory) {
        savedDao.insert(story)
    }

    suspend fun deleteAll() {
        savedDao.deleteAll()
    }

    suspend fun getAllSaved(): List<SavedStory> {
        return savedDao.getSavedStories()
    }
}