package com.hippo.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hippo.adapter.NewsListAdapter
import com.hippo.data.Story
import com.hippo.network.HackerNewsFetcher
import com.hippo.network.NewsFetcher
import com.hippo.viewmodel.StoryViewModel

class NewsFeedActivity : AppCompatActivity(), NewsFetcher.NewsListener {

    private enum class FeedFilter {
        Newest,
        Top,
        Trending
    }

    private lateinit var storiesViewModel: StoryViewModel
    private lateinit var newsFetcher: HackerNewsFetcher
    private var isLoading: Boolean = false
    private var lastStoryIndex = -1 // -1 is important here
    private var feedFilter = FeedFilter.Newest

    companion object {
        const val LOADING_INTERVAL_COUNT = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)
        setSupportActionBar(findViewById(R.id.news_toolbar))

        newsFetcher = HackerNewsFetcher(this)

        // List of stories
        // Adapter and layout manager
        // Add a scroll listener here
        val recyclerView = findViewById<RecyclerView>(R.id.news_recycler_view)
        val adapter = NewsListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addOnScrollListener(NewsScrollListener())

        // Stories view model init
        // Setup an observer to listen for data changes in the view model backing data
        storiesViewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
        storiesViewModel.allStories.observe(this, Observer { stories ->
            // Update the cached copy of the words in the adapter.
            stories?.let { adapter.setStories(it) }
        })

        // Make a network request to acquire all of the
        // top stories from various news outlets, and break it down into list format
        fetchNextNewsRange(0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_news_feed, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(menu != null) {
            val filterModes = FeedFilter.values()
            for (i in filterModes.indices) {
                menu.children.elementAt(i).isChecked = feedFilter == filterModes[i]
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // todo - clear the existing recycler view content
        // todo - fetch different news via different API endpoint
        // todo - show progress bar
        // todo -
        R.id.action_sort_new -> {
            // User chose to sort by newest stories
            feedFilter = FeedFilter.Newest
            item.isChecked = !item.isChecked
            true
        }

        R.id.action_sort_top -> {
            // User chose to sort by top story articles
            feedFilter = FeedFilter.Top
            item.isChecked = !item.isChecked
            true
        }

        R.id.action_sort_trending -> {
            // User chose to sort by trending articles
            feedFilter = FeedFilter.Trending
            item.isChecked = !item.isChecked
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNewsAvailable(results: List<Story>) {
        Log.e("NewsFeedActivity", "onNewsAvailable count: ${results.size}")

        for (newStory in results) {
            // INSERT
            // Insert new story into db
            storiesViewModel.insert(newStory)
        }

        isLoading = false

        // Turn off the indeterminate spinner
        // on the UI thread, and show the Recycler view list
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
            findViewById<RecyclerView>(R.id.news_recycler_view).visibility = View.VISIBLE
        }
    }

    private fun fetchNextNewsRange(first: Int) {
        lastStoryIndex += LOADING_INTERVAL_COUNT
        newsFetcher.fetchNews(first, lastStoryIndex)
        isLoading = true
    }

    private inner class NewsScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            //Log.e(this.javaClass.simpleName, "onSCrolled: dx: $dx dy: $dy")
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            when (newState) {
                // Dragging: moving
                //RecyclerView.SCROLL_STATE_DRAGGING ->
                //Log.e(this.javaClass.simpleName, "Dragging")

                // Idle: Resting
                RecyclerView.SCROLL_STATE_IDLE -> {
                    Log.e(this.javaClass.simpleName, "Idle")

                    // Check if we're at the bottom..
                    if (!recyclerView.canScrollVertically(1) && !isLoading) {
                        Log.e(this.javaClass.simpleName, "Bottom reached! About to load more...")

                        // Fetch the next range of stories
                        runOnUiThread {
                            findViewById<ProgressBar>(R.id.news_feed_progress).visibility =
                                View.VISIBLE
                        }

                        fetchNextNewsRange(lastStoryIndex + 1)

                    } else {
                        Log.e(this.javaClass.simpleName, "Already loading or not at bottom yet...")
                    }
                }

                // Settling: About to stop moving soon
                //RecyclerView.SCROLL_STATE_SETTLING ->
                //Log.e(this.javaClass.simpleName, "Settling")
            }
        }
    }
}
