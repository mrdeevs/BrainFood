package com.hippo.network

import android.util.Log
import com.hippo.news.BuildConfig
import okhttp3.*
import okio.IOException
import org.json.JSONArray

class HackerNewsFetcher : NewsFetcher() {

    public override fun fetchNews(callback: NewsListener) {
        // Builds a GET request to the top stories of
        // hacker news
        var storyIds: JSONArray?
        val request = Request.Builder()
            .url(BuildConfig.URL_HACKER_NEWS_TOP)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                fetchNewsItems(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    if (response.body != null) {
                        val storiesAsJson = response.body!!.string()
                        // Log.e("Test", "stories Json: $storiesAsJson")
                        // Convert the body to a String
                        // Convert the String into a List using comma separators
                        storyIds = JSONArray(storiesAsJson)
                        fetchNewsItems(storyIds)
                    }
                }
            }
        }) // Finish the network call for each story ID
    }

    private fun fetchNewsItems(storyIds: JSONArray?) {
        // Builds a GET request for each top story ID
        // to acquire data for each individual story
        if (storyIds != null) {
            var index = 0
            while (index < storyIds.length()) {
                val curId = storyIds[index]
                // Log.e("Test", "Cur ID: $curId")
                index++

                val itemsRequest = Request.Builder()
                    .url(BuildConfig.URL_HACKER_NEWS_ITEM.replace("id", curId.toString()))
                    .build()

                // Make a network request to get the payload for EACH
                // story in the list...EXPENSIVE
                client.newCall(itemsRequest).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            if (response.body != null) {
                                val itemJson = response.body!!.string()
                                Log.e("Test", "item Json: $itemJson")
                            }
                        }
                    }
                }) // Finish the network call for each story ID
            }
        } else {
            // todo - graceful error handling back up
        }
    }
}