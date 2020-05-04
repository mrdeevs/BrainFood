package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hippo.network.HackerNewsFetcher

class NewsFeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        // todo - testing okhttp async GET of top stories
        val newsFetch = HackerNewsFetcher()
        newsFetch.fetchNews()
    }
}
