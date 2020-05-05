package com.hippo.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hippo.data.Story

class UserProfileViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val storyId: Int =
        savedStateHandle["storyId"] ?: throw IllegalArgumentException("missing story id")

    val story: Story = TODO()
}