package com.hippo.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_stories")
data class SavedStory(
    @PrimaryKey var storyId: Int,
    var by: String?,
    var descendants: Int,
    @ColumnInfo(name = "unique_id") var id: Int,
    var score: Int,
    var time: Long,
    var title: String?,
    var type: String?,
    var url: String?,
    var source: String?,
    var image: String?
)