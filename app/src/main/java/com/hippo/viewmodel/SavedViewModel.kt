package com.hippo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hippo.data.SavedRepository
import com.hippo.data.entities.Story
import com.hippo.data.database.NewsRoomDatabase
import com.hippo.data.entities.SavedStory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SavedRepository

    init {
        val savedDao = NewsRoomDatabase.getDatabase(application, viewModelScope).savedStoryDao()
        repository = SavedRepository(savedDao)
    }

    /**
     * Launching a new co-routine to insert a story to the main DB in a non-blocking way
     */
    fun insert(story: SavedStory) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(story)
    }

    /**
     * Launching a new co-routine to clear the database and delete all entries
     */
    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    /**
     * Launching a new co-routine to GET all saved database entries
     */
    fun getAllSaved() = viewModelScope.launch(Dispatchers.IO) {
        val saved: List<SavedStory> = repository.getAllSaved()
        Log.e("TAG", "saved size: " + saved.size)
    }
}