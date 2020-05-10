package com.hippo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hippo.data.NewsRepository
import com.hippo.data.Story
import com.hippo.data.database.NewsRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NewsRepository

    // Using LiveData and caching what getBestStories() returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val newStories: LiveData<List<Story>>
    val topStories: LiveData<List<Story>>

    init {
        val storiesDao = NewsRoomDatabase.getDatabase(application, viewModelScope).storyDao()
        repository = NewsRepository(storiesDao)
        newStories = repository.newStories
        topStories = repository.topStories
    }

    /**
     * Launching a new co-routine to insert a story in a non-blocking way
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