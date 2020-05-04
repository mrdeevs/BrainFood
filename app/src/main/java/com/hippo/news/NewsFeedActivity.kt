package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.hippo.network.HackerNewsFetcher
import com.hippo.network.NewsFetcher
import org.json.JSONObject

class NewsFeedActivity : AppCompatActivity(), NewsFetcher.NewsListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        // Make a network request to acquire all of the
        // top stories on hacker news, and break it down into list format
        val newsFetch = HackerNewsFetcher(this)
        newsFetch.fetchNews()
    }

    override fun onNewsAvailable(results: List<JSONObject>) {
        Log.e("NewsFeed", "onNewsAvailable count: ${results.size}")

        // Turn off the indeterminate spinner
        runOnUiThread(Runnable {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
        })
    }
}
