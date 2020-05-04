package com.hippo.network

import android.util.Log
import com.hippo.news.BuildConfig
import okhttp3.*
import okio.IOException
import org.json.JSONArray

class HackerNewsFetcher : NewsFetcher() {

    public override fun fetchNewsIds() {

        val request = Request.Builder()
            .url(BuildConfig.URL_HACKER_NEWS_TOP)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    /*println("headers:")
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }*/

                    if (response.body != null) {
                        val storiesAsJson = response.body!!.string()
                        Log.e("Test", "stories Json: $storiesAsJson")
                        // Convert the body to a String
                        // Convert the String into a List using comma separators
                        val bestStoriesIds = JSONArray(storiesAsJson)
                        var index = 0
                        while (index < bestStoriesIds.length()) {
                            val curId = bestStoriesIds[index]
                            //Log.e("Test", "Cur ID: $curId")
                            index++
                        }
                    }
                }
            }
        })
    }
}