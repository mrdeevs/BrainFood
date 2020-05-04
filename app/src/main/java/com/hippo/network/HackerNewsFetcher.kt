package com.hippo.network

import android.util.Log
import com.hippo.news.BuildConfig
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class HackerNewsFetcher(listener: NewsListener) : NewsFetcher(listener) {

    public override fun fetchNews() {
        // Builds a GET request to the top stories of
        // hacker news
        var storyIds: JSONArray?
        val request = Request.Builder()
            .url(BuildConfig.URL_HACKER_NEWS_TOP)
            .build()

        mClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                fetchNewsItems(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    if (response.body != null) {
                        val storiesAsJson = response.body!!.string()
                        // Convert the body to a String
                        // Convert the String into a List using comma separators
                        storyIds = JSONArray(storiesAsJson)
                        //Log.e("Test", "Story Ids length: ${storyIds!!.length()}")
                        fetchNewsItems(storyIds)
                    }
                }
            }
        })
    }

    private fun fetchNewsItems(storyIds: JSONArray?) {
        // Builds a GET request for each top story ID
        // to acquire data for each individual story
        val results: ArrayList<JSONObject> = ArrayList()

        if (storyIds != null && storyIds.length() > 0) {
            var index = 0
            while (index < storyIds.length()) {
                val curId = storyIds[index]
                index++

                // Request for a news article
                val itemsRequest = Request.Builder()
                    .url(BuildConfig.URL_HACKER_NEWS_ITEM.replace("id", curId.toString()))
                    .build()

                // Make a network request to get the payload for EACH
                // story in the list...EXPENSIVE
                mClient.newCall(itemsRequest).enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            if (response.body != null) {
                                val itemJson = JSONObject(response.body!!.string())
                                val itemJsonStr = itemJson.toString()
                                //Log.e("Test", "item Json: $itemJsonStr")
                                results.add(itemJson)
                            } else {
                                Log.e("Test", "Found an empty body")
                            }

                            if (results.size == storyIds.length()) {
                                //Log.e("Test", "Counts MATCH at: ${results.size}")
                                // FULL RESULTS
                                // return the full result list
                                mCallback.onNewsAvailable(results)
                            }
                        }
                    }
                })
            }

        } else {
            // todo could also mean no internet
            // EMPTY
            // otherwise it will be empty from initialization
            mCallback.onNewsAvailable(results)
        }
    }
}