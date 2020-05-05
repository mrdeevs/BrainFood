package com.hippo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity
data class Story (
    @PrimaryKey var storyId: Int,
    var by: String?,
    var descendants: Int,
    var id: Int,
    var kids: JSONArray?,
    var score: Int,
    var time: Long,
    var title: String?,
    var type: String?,
    var url: String
)