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
import com.hippo.viewmodel.StoryViewModel
import org.json.JSONObject

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

    override fun onNewsAvailable(results: List<JSONObject>) {
        Log.e("NewsFeed", "onNewsAvailable count: ${results.size}")
        // For each story found
        // Create a new constructor of Story data class
        // Insert it into the database underneath
        for (story in results) {
            if (story.length() > 0) {
                // Story Type i.e. Story, Job, Poll, Poll-opt etc.
                val storyScore =
                    if (story.has(HackerNewsFetcher.JSON_SCORE))
                        story.getInt(HackerNewsFetcher.JSON_SCORE)
                    else
                        0

                // Story Type i.e. Story, Job, Poll, Poll-opt etc.
                val storyType =
                    if (story.has(HackerNewsFetcher.JSON_TYPE))
                        story.getString(HackerNewsFetcher.JSON_TYPE)
                    else
                        HackerNewsFetcher.JSON_NONE

                // Only parse story types for now, we aren't interested in polls or jobs
                // Ignore non-story type
                if (storyType == HackerNewsFetcher.JSON_TYPE_STORY) {
                    //Log.e("NewsFeed", "Creating news story: ${story.toString(4)}")
                    // Descendants [Optional], non-required field might be missing
                    val descendantJson =
                        if (story.has(HackerNewsFetcher.JSON_DESCENDANTS))
                            story.getInt(HackerNewsFetcher.JSON_DESCENDANTS)
                        else
                            0

                    // Url [Optional], non-required field might be missing
                    val urlJson =
                        if (story.has(HackerNewsFetcher.JSON_URL))
                            story.getString(HackerNewsFetcher.JSON_URL)
                        else
                            HackerNewsFetcher.JSON_NONE

                    // Create a new story entry
                    val newStory = Story(
                        story.getInt(HackerNewsFetcher.JSON_UNIQUE_ID),
                        story.getString(HackerNewsFetcher.JSON_BY),
                        descendantJson,
                        story.getInt(HackerNewsFetcher.JSON_UNIQUE_ID),
                        storyScore,
                        story.getLong(HackerNewsFetcher.JSON_TIME),
                        story.getString(HackerNewsFetcher.JSON_TITLE),
                        story.getString(HackerNewsFetcher.JSON_TYPE),
                        urlJson,
                        HackerNewsFetcher.HACKER_NEWS_SOURCE
                    )

                    // Insert new story into db
                    storiesViewModel.insert(newStory)

                } // End check for ONLY story types
            } // End check for empty story results
        } // End for each over results

        // Turn off the indeterminate spinner
        // on the UI thread, and show the Recycler view list
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
            findViewById<RecyclerView>(R.id.news_recycler_view).visibility = View.VISIBLE
        }
    }
}
