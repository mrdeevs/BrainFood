package com.hippo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey var storyId: Int,
    var by: String?,
    var descendants: Int,
    @ColumnInfo(name = "unique_id") var id: Int,
    var score: Int,
    var time: Long,
    var title: String?,
    var type: String?,
    var url: String?,
    var source: String?
    // @ColumnInfo(name = "comment_ids") var kids: JSONArray?, // todo add comment support
)