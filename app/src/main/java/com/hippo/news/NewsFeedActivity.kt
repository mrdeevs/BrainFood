package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hippo.adapter.NewsListAdapter
import com.hippo.data.Story
import com.hippo.network.HackerNewsFetcher
import com.hippo.network.NewsFetcher
import com.hippo.utils.HippoUtils
import com.hippo.viewmodel.StoryViewModel
import org.json.JSONObject
import org.jsoup.select.Elements

class NewsFeedActivity : AppCompatActivity(), NewsFetcher.NewsListener {

    private lateinit var storiesViewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        // List of stories
        // Adapter and layout manager
        val recyclerView = findViewById<RecyclerView>(R.id.news_recycler_view)
        val adapter = NewsListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Stories view model init
        // Setup an observer to listen for data changes in the view model backing data
        storiesViewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
        storiesViewModel.allStories.observe(this, Observer { stories ->
            // Update the cached copy of the words in the adapter.
            stories?.let { adapter.setStories(it) }
        })

        // Make a network request to acquire all of the
        // top stories from various news outlets, and break it down into list format
        val newsFetch = HackerNewsFetcher(this)
        newsFetch.fetchNews()
    }

    override fun onNewsAvailable(results: List<Story>) {
        Log.e("NewsFeedActivity", "onNewsAvailable count: ${results.size}")

        for (newStory in results) {
            // INSERT
            // Insert new story into db
            storiesViewModel.insert(newStory)
        }

        // Turn off the indeterminate spinner
        // on the UI thread, and show the Recycler view list
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
            findViewById<RecyclerView>(R.id.news_recycler_view).visibility = View.VISIBLE
        }
    }
}
