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
        val recyclerView = findViewById<RecyclerView>(R.id.news_recycler_view)
        //val adapter = StoriesListAdapter(this)
        //recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Stories view model setup
        storiesViewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
//        storiesViewModel.allStories.observe(this, Observer { words ->
//            // Update the cached copy of the words in the adapter.
//            words?.let { adapter.setStories(it) }
//        })

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

            // Story Type i.e. Story, Job, Poll, Pollopt etc.
            val storyType =
                if (story.has(HackerNewsFetcher.JSON_TYPE))
                    story.getString(HackerNewsFetcher.JSON_TYPE)
                else
                    HackerNewsFetcher.JSON_NONE

            // Only parse story types for now, we aren't interested in polls or jobs
            if (storyType == HackerNewsFetcher.JSON_TYPE_STORY) {
                Log.e("NewsFeed", "Creating news story: ${story.toString(4)}")

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
                    story.getInt(HackerNewsFetcher.JSON_SCORE),
                    story.getLong(HackerNewsFetcher.JSON_TIME),
                    story.getString(HackerNewsFetcher.JSON_TITLE),
                    story.getString(HackerNewsFetcher.JSON_TYPE),
                    urlJson
                )

                // Insert into DB
                storiesViewModel.insert(newStory)

            } else {
                // Ignore non-stories for now
                Log.e("NewsTest", "Non-story type found: $storyType")
            }
        }

        // Turn off the indeterminate spinner
        // on the UI thread
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
        }
    }
}
