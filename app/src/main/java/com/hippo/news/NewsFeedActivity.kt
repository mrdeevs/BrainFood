package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hippo.network.HackerNewsFetcher
import com.hippo.network.NewsFetcher
import org.json.JSONObject

class NewsFeedActivity : AppCompatActivity(), NewsFetcher.NewsListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        // todo - testing okhttp async GET of top stories
        val newsFetch = HackerNewsFetcher(this)
        newsFetch.fetchNews()
    }

    override fun onNewsAvailable(results: List<JSONObject>) {
        Log.e("NewsFeed", "onNewsAvailable count: ${results.size}")
    }
}
