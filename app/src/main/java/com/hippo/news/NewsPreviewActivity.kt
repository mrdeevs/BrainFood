package com.hippo.news

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class NewsPreviewActivity : AppCompatActivity() {

    companion object {
        const val KEY_URL = "keyurl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_preview)

        if(intent != null && intent.getStringExtra(KEY_URL) != null) {
            val storyUrl = intent.getStringExtra(KEY_URL)
            Log.e("test", "preview found url: $storyUrl")
        }
    }
}