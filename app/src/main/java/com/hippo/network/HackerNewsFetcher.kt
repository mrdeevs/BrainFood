package com.hippo.network

import com.hippo.news.BuildConfig
import okhttp3.*
import okio.IOException

class HackerNewsFetcher : NewsFetcher() {

    override fun fetchNews() {

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

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }
}