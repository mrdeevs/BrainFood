package com.hippo.network

import android.util.Log
import com.hippo.news.BuildConfig
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class HackerNewsFetcher(listener: NewsListener) : NewsFetcher(listener) {

    companion object {
        const val JSON_BY = "by"
        const val JSON_DESCENDANTS = "descendants"
        const val JSON_UNIQUE_ID = "id"
        const val JSON_SCORE = "score"
        const val JSON_TIME = "time"
        const val JSON_TITLE = "title"
        const val JSON_TYPE = "type"
        const val JSON_URL = "url"
        const val JSON_TYPE_STORY = "story"
        const val JSON_TYPE_JOB = "job"
        const val JSON_TYPE_COMMENT = "comment"
        const val JSON_TYPE_POLL = "poll"
        const val JSON_TYPE_POLL_OPT = "pollopt"
        const val JSON_NONE = "none"

        // Class constants
        const val HACKER_NEWS_SOURCE = "hacker"
    }

    public override fun fetchNews() {
        // Builds a GET request to the top stories of
        // hacker news
        var storyIds: JSONArray?

        val request = Request.Builder()
            .url(BuildConfig.URL_HACKER_NEWS_BEST)
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
                        //Log.e("HackerNewsRepository", "story Ids: $storyIds")
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
                        mCallback.onNewsAvailable(ArrayList())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful)
                                throw IOException("Unexpected code $response")

                            try {
                                if (response.body != null) {
                                    val itemJson = JSONObject(response.body!!.string())
                                    results.add(itemJson)
                                    //Log.e("Test", "item Json: $itemJson")
                                } else {
                                    // Response body was null or invalid, handle error
                                    // Create an empty JSON object for results that go wrong or are invalid
                                    results.add(JSONObject())
                                }
                            } catch (e: Exception) {
                                // Create an empty JSON object for results that go wrong or are invalid
                                results.add(JSONObject())
                            }

                            //Log.e("test", "result size: " + results.size + " story id length: " + storyIds.length())
                            // Check if all stories have been fetched
                            if (results.size == storyIds.length()) {
                                //Log.e("Test", "Counts match at: ${results.size}")
                                // return the full result list
                                mCallback.onNewsAvailable(results)
                            }
                        }
                    }
                })
            }
        } else {
            // EMPTY results
            // could also mean no internet / couldn't find a network
            // otherwise it will be empty from initialization
            mCallback.onNewsAvailable(ArrayList())
        }
    }
}