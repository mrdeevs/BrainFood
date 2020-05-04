package com.hippo.network

import com.squareup.moshi.Moshi
import okhttp3.*
import org.json.JSONObject

open class NewsFetcher(listener: NewsListener) {
    interface NewsListener { fun onNewsAvailable(results : List<JSONObject>) }

    protected val mCallback = listener;
    protected val mClient = OkHttpClient()
    protected val moshi = Moshi.Builder().build()
    protected open fun fetchNews() {}
}