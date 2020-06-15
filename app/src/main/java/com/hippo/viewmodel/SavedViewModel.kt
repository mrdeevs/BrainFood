package com.hippo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hippo.data.SavedRepository
import com.hippo.data.entities.Story
import com.hippo.data.database.NewsRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SavedRepository

    // Keeps track of saved stories in the DB
    val savedStories: LiveData<List<Story>>

    init {
        val savedDao = NewsRoomDatabase.getDatabase(application, viewModelScope).savedStoryDao()
        repository = SavedRepository(savedDao)
        savedStories = repository.savedStories
    }

    /**
     * Launching a new co-routine to insert a story to the main DB in a non-blocking way
     */
    fun insert(story: Story) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(story)
    }

    /**
     * Launching a new co-routine to clear the database and delete all entries
     */
    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}