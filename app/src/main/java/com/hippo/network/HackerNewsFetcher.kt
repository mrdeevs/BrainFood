package com.hippo.network

import android.util.Log
import com.hippo.data.Story
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
        const val JSON_NONE = "none"

        // Class constants for various news sources / API
        const val HACKER_NEWS_SOURCE = "hacker"
    }

    /**
     * Attempts to pull a range of news stories from the API
     * */
    public override fun fetchNews(firstStoryIndex: Int, lastStoryIndex: Int) {
        // Builds a GET request to the top stories of
        // hacker news
        var storyIds: JSONArray?
        val request = Request.Builder()
            .url(BuildConfig.URL_HACKER_NEWS_NEW)
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

                        // Ensure list of story ids is valid and not empty
                        if (storyIds != null && storyIds!!.length() > 0) {
                            // Ensure range checking on the first and last index
                            // Also make sure last > first
                            if (firstStoryIndex >= 0 && firstStoryIndex < storyIds!!.length()
                                && lastStoryIndex >= 0 && lastStoryIndex < storyIds!!.length()
                                && lastStoryIndex >= firstStoryIndex
                            ) {
                                // Filter down to the specified range:
                                // [firstStoryIndex, lastStoryIndex]
                                val filteredRange = JSONArray()
                                for (i in firstStoryIndex..lastStoryIndex) {
                                    filteredRange.put(storyIds!![i])
                                }

                                Log.e(this.javaClass.simpleName, "story ids valid, start index: $firstStoryIndex, last index: $lastStoryIndex")
                                fetchNewsItems(filteredRange)

                            } else {
                                // Indicates that an invalid range was passed in
                                // and therefore we can't parse. Return empty
                                fetchNewsItems(null)
                            }
                        } else {
                            // story IDs is empty or null, return an empty set
                            fetchNewsItems(null)
                        }
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
                                // Converts each JSONObject into a readable story
                                mCallback.onNewsAvailable(convertStoryJsonToStories(results))
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

    /**
     * Takes a valid response of List<JSONObject> that contains the News data
     * and converts it into our database friendly (Room) class of a Story for each
     * */
    override fun convertStoryJsonToStories(results: List<JSONObject>): List<Story> {
        val output: ArrayList<Story> = ArrayList()
        // For each story found
        // Create a new constructor of Story data class
        // Insert it into the database underneath
        var index = 0

        for (storyJson in results) {
            // Check for empty story json, which means its an invalid item and should be ignored
            if (storyJson.length() > 0) {

                // Story Type i.e. Story, Job, Poll, Poll-opt etc.
                val storyType =
                    if (storyJson.has(JSON_TYPE))
                        storyJson.getString(JSON_TYPE)
                    else
                        JSON_NONE

                // Only parse story types for now, we aren't interested in polls or jobs
                // Ignore non-story type
                if (storyType == JSON_TYPE_STORY) {
                    //Log.e("NewsFeed", "Creating news story: ${story.toString(4)}")

                    // Story score [Optional]
                    val storyScore =
                        if (storyJson.has(JSON_SCORE))
                            storyJson.getInt(JSON_SCORE)
                        else
                            0

                    // Descendants [Optional], non-required field might be missing
                    val descendantJson =
                        if (storyJson.has(JSON_DESCENDANTS))
                            storyJson.getInt(JSON_DESCENDANTS)
                        else
                            0

                    // Url [Optional], non-required field might be missing
                    val urlJson =
                        if (storyJson.has(JSON_URL))
                            storyJson.getString(JSON_URL)
                        else
                            JSON_NONE

                    // Ensure a valid URL
                    // Ensure a valid Unique ID
                    // Ensure a valid story type
                    if (urlJson != JSON_NONE) {
                        // todo add optional checks for unique id, by, time, title, type
                        // todo - move to a background thread / co-routine

                        // img. url path
                        // EXPENSIVE.. wow
                        val imgSrc = extractImagesFromStoryUrl(urlJson)
                        //Log.e("test", "imgSrc as str: " + imgSrc.toString())

                        // Create a new story entry
                        val newStory = Story(
                            storyJson.getInt(JSON_UNIQUE_ID),
                            storyJson.getString(JSON_BY),
                            descendantJson,
                            storyJson.getInt(JSON_UNIQUE_ID),
                            storyScore,
                            storyJson.getLong(JSON_TIME),
                            storyJson.getString(JSON_TITLE),
                            storyJson.getString(JSON_TYPE),
                            urlJson,
                            HACKER_NEWS_SOURCE,

                            // Check for valid image
                            // This list is sorted by area, taking the first image here [0]
                            // will return the best header image / preview automatically
                            if (imgSrc != null && imgSrc.isNotEmpty())
                                imgSrc[0]
                            else
                                ""
                        )

                        // Add it to output
                        output.add(newStory)
                    }
                } // End check for ONLY story types
            } // End check for empty story results

            index++

        } // End for each over results

        // Return the converted type list
        return output
    }
}