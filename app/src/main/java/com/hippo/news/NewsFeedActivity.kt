package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
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
    private lateinit var utils: HippoUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)

        // Setup utils
        utils = HippoUtils()

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
        for (storyJson in results) {

            // Check for empty story json, which means its an invalid item and should be ignored
            if (storyJson.length() > 0) {
                // Story Type i.e. Story, Job, Poll, Poll-opt etc.
                val storyType =
                    if (storyJson.has(HackerNewsFetcher.JSON_TYPE))
                        storyJson.getString(HackerNewsFetcher.JSON_TYPE)
                    else
                        HackerNewsFetcher.JSON_NONE

                // Only parse story types for now, we aren't interested in polls or jobs
                // Ignore non-story type
                if (storyType == HackerNewsFetcher.JSON_TYPE_STORY) {
                    //Log.e("NewsFeed", "Creating news story: ${story.toString(4)}")

                    // Story score [Optional]
                    val storyScore =
                        if (storyJson.has(HackerNewsFetcher.JSON_SCORE))
                            storyJson.getInt(HackerNewsFetcher.JSON_SCORE)
                        else
                            0

                    // Descendants [Optional], non-required field might be missing
                    val descendantJson =
                        if (storyJson.has(HackerNewsFetcher.JSON_DESCENDANTS))
                            storyJson.getInt(HackerNewsFetcher.JSON_DESCENDANTS)
                        else
                            0

                    // Url [Optional], non-required field might be missing
                    val urlJson =
                        if (storyJson.has(HackerNewsFetcher.JSON_URL))
                            storyJson.getString(HackerNewsFetcher.JSON_URL)
                        else
                            HackerNewsFetcher.JSON_NONE

                    Log.e("test", "story url: $urlJson")

                    // Ensure a valid URL
                    // Ensure a valid Unique ID
                    // Ensure a valid story type
                    if (urlJson != HackerNewsFetcher.JSON_NONE) {
                        // todo add optional checks for unique id, by, time, title, type
                        // todo - move to a background thread / co-routine
                        // EXPENSIVE.. wow
                        val imgSrc = extractImagesFromStoryUrl(urlJson)
                        Log.e("test", "imgSrc as str: " + imgSrc.toString())

                        // Create a new story entry
                        val newStory = Story(
                            storyJson.getInt(HackerNewsFetcher.JSON_UNIQUE_ID),
                            storyJson.getString(HackerNewsFetcher.JSON_BY),
                            descendantJson,
                            storyJson.getInt(HackerNewsFetcher.JSON_UNIQUE_ID),
                            storyScore,
                            storyJson.getLong(HackerNewsFetcher.JSON_TIME),
                            storyJson.getString(HackerNewsFetcher.JSON_TITLE),
                            storyJson.getString(HackerNewsFetcher.JSON_TYPE),
                            urlJson,
                            HackerNewsFetcher.HACKER_NEWS_SOURCE,
                            imgSrc.toString()
                        )

                        // INSERT
                        // Insert new story into db
                        storiesViewModel.insert(newStory)

                    }
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

    private fun extractImagesFromStoryUrl(url: String): List<String>? {
        // Scrape all images out of the url using jSoup
        val allUrlImages: Elements? =
            utils.extractImagesFromUrl(url) // Expensive WOW...

        // results container filtered
        val httpImages = ArrayList<String>()

        if (allUrlImages != null) {
            for (img in allUrlImages) {
                //Log.e("test", "img found in url: $img")
                // extract attributes i.e. src from the current image element
                // then check for the actual src attribute
                // then we need to check for http to make sure its a valid remote image, not
                // a local path, since they have shown up
                val attributes = img.attributes()

                if (attributes.hasKey("src")) {
                    val srcElement = attributes.get("src")

                    // Allow ONLY http images
                    // Local ones are removed.. we can't show those or load them async here
                    if (srcElement.contains("http") || srcElement.contains("https")) {
                        Log.e("test", "found HTTP img: $img")
                        httpImages.add(srcElement)
                    }
                }
            }
        }

        return httpImages
    }
}
