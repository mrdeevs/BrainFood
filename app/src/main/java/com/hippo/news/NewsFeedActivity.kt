package com.hippo.news

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hippo.adapter.NewsListAdapter
import com.hippo.data.Story
import com.hippo.network.HackerNewsFetcher
import com.hippo.network.NewsFetcher
import com.hippo.viewmodel.StoryViewModel

class NewsFeedActivity : AppCompatActivity(), NewsFetcher.NewsListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var storiesViewModel: StoryViewModel
    private lateinit var newsFetcher: HackerNewsFetcher
    private lateinit var filterPopup: PopupMenu
    private lateinit var newsAdapter: NewsListAdapter
    private var isLoading: Boolean = false
    private var storyDataIndex: Int = STARTING_STORY_INDEX
    private var feedCategory = NewsFetcher.NewsCategory.Newest

    companion object {
        const val LOADING_INTERVAL_COUNT = 15
        const val STARTING_STORY_INDEX = -1
        const val PREF_STORY_DATA_INDEX = "StoryDataIndex"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_main)
        setSupportActionBar(findViewById(R.id.news_toolbar))

        // Filter news popup
        filterPopup = PopupMenu(this, findViewById(R.id.action_filter))
        val inflater: MenuInflater = filterPopup.menuInflater
        inflater.inflate(R.menu.menu_filter_feed, filterPopup.menu)
        filterPopup.setOnMenuItemClickListener(this)

        // Hacker news API fetcher
        newsFetcher = HackerNewsFetcher(this)

        // List of stories
        // Adapter and layout manager
        // Add a scroll listener here
        val recyclerView = findViewById<RecyclerView>(R.id.news_recycler_view)
        newsAdapter = NewsListAdapter(this)
        recyclerView.adapter = newsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addOnScrollListener(NewsScrollListener())

        // Stories view model init
        // Setup an observer to listen for data changes in the view model backing data
        storiesViewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
        // Update who we listen to for db results
        updateViewModelObserversFromCategory()

        // Remember the last data index, so that we can allow our cached data to
        // already be loaded on fresh app restarts
        storyDataIndex = this.getPreferences(Context.MODE_PRIVATE)
            .getInt(PREF_STORY_DATA_INDEX, STARTING_STORY_INDEX)

        // Log.e(this.localClassName, "lastStoryIndex updated (onCreate): $storyDataIndex")
        // Only necessary on the first run, otherwise we'll use cached data
        if (storyDataIndex == STARTING_STORY_INDEX) {
            //Log.e(this.localClassName, "loading fetchNextNewsRange in onCreate()")
            // Make a network request to acquire all of the
            // top stories from various news outlets, and break it down into list format
            fetchNextNewsRange(0, hideList = true, clearDb = false)

        } else {
            showNewsList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_news_feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Filter by newest, top or trending
        R.id.action_filter -> {
            if (!isLoading) {
                // User chose to sort, show the options
                filterPopup.show()
            } else {
                // We're in the middle of refreshing the feed, show a message
                Toast.makeText(applicationContext, "Currently loading, try again later", Toast.LENGTH_SHORT).show()
            }
            true
        }

        R.id.action_settings -> {
            // User wants to open settings
            // todo open settings activity
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Popup menu callback for selecting a filtering mode for the news feed
     * */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item != null) {
            // Clear any existing checked items so we can update
            // based on state
            for (i in 0 until filterPopup.menu.size()) {
                filterPopup.menu.getItem(i).isChecked = false
            }

            // Update the checked item
            when (item.itemId) {
                R.id.action_filter_newest, R.id.action_filter_best -> {

                    item.isChecked = true

                    // Filter the list for newest, clear the db and fetch new
                    if (item.itemId == R.id.action_filter_newest && feedCategory != NewsFetcher.NewsCategory.Newest) {
                        // Fetch the next range of stories
                        // Update category endpoints to Newest
                        // Update who we listen to for db results
                        switchFeedCategory(NewsFetcher.NewsCategory.Newest)

                    } else if (item.itemId == R.id.action_filter_best && feedCategory != NewsFetcher.NewsCategory.Best) {
                        // Update category endpoints to Top
                        switchFeedCategory(NewsFetcher.NewsCategory.Best)
                    }

                    return true
                }
            }
        }

        return false
    }

    override fun onNewsAvailable(results: List<Story>) {
        Log.e("NewsFeedActivity", "onNewsAvailable count: ${results.size}")

        for (newStory in results) {
            // INSERT
            // Insert new story into db
            storiesViewModel.insert(newStory)
        }

        isLoading = false

        // Show the Recycler view list
        showNewsList()
    }

    private fun setDataIndex(dataIndex: Int) {
        // Update existing member val
        storyDataIndex = dataIndex

        // Match the backing pref
        this.getPreferences(Context.MODE_PRIVATE).edit()
            .putInt(PREF_STORY_DATA_INDEX, storyDataIndex).apply()
    }

    /**
     * When changing feeds from i.e. new stories to best/trending stories,
     * we need to change the feed type, reset the data index for retrieving stories,
     * and update which observers listen to the database and update the view model
     * */
    private fun switchFeedCategory(category: NewsFetcher.NewsCategory) {
        feedCategory = category
        setDataIndex(STARTING_STORY_INDEX)
        fetchNextNewsRange(storyDataIndex + 1, hideList = true, clearDb = true)
        updateViewModelObserversFromCategory()
    }

    private fun updateViewModelObserversFromCategory() {
        // Clear off the old observers
        storiesViewModel.newStories.removeObservers(this)
        storiesViewModel.bestStories.removeObservers(this)

        // Setup observer for the new data
        when (feedCategory) {
            NewsFetcher.NewsCategory.Newest -> {
                // Observe the newest stories
                storiesViewModel.newStories.observe(this, Observer { stories ->
                    // Update the cached copy of the words in the adapter.
                    stories?.let { newsAdapter.setStories(it) }
                })
            }

            NewsFetcher.NewsCategory.Best -> {
                // Observe the top stories
                storiesViewModel.bestStories.observe(this, Observer { stories ->
                    // Update the cached copy of the words in the adapter.
                    stories?.let { newsAdapter.setStories(it) }
                })
            }
        }
    }

    /**
     * Turn off the indeterminate spinner on the UI thread, and show the Recycler view list
     * */
    private fun showNewsList() {
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.GONE
            findViewById<RecyclerView>(R.id.news_recycler_view).visibility = View.VISIBLE
        }
    }

    private fun showLoading(hideList: Boolean) {
        runOnUiThread {
            findViewById<ProgressBar>(R.id.news_feed_progress).visibility = View.VISIBLE
            if (hideList)
                findViewById<RecyclerView>(R.id.news_recycler_view).visibility = View.GONE
        }
    }

    /**
     * Make a network request to acquire all of the top stories from various
     * news outlets, and break it down into list format
     * */
    private fun fetchNextNewsRange(first: Int, hideList: Boolean, clearDb: Boolean) {
        //lastStoryIndex += LOADING_INTERVAL_COUNT
        setDataIndex(storyDataIndex + LOADING_INTERVAL_COUNT)
        newsFetcher.fetchNews(first, storyDataIndex, feedCategory)
        isLoading = true

        // Show the loading bar
        showLoading(hideList)

        // Clear the db
        if (clearDb) {
            storiesViewModel.deleteAll()
        }
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
                // RecyclerView.SCROLL_STATE_DRAGGING ->
                // Log.e(this.javaClass.simpleName, "Dragging")
                // Idle: Resting
                RecyclerView.SCROLL_STATE_IDLE -> {
                    //Log.e(this.javaClass.simpleName, "Idle")

                    // Check if we're at the bottom..
                    if (!recyclerView.canScrollVertically(1) && !isLoading) {
                        Log.e(this.javaClass.simpleName, "Bottom reached! About to load more...")

                        // Fetch the next range of stories
                        fetchNextNewsRange(storyDataIndex + 1, false, false)
                    }
                }
                // Settling: About to stop moving soon
                // RecyclerView.SCROLL_STATE_SETTLING ->
                // Log.e(this.javaClass.simpleName, "Settling")
            }
        }
    }
}
