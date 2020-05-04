package com.hippo.network

import com.squareup.moshi.Moshi
import okhttp3.*

open class NewsFetcher {
    interface NewsListener { fun onNewsAvailable() }
    protected val client = OkHttpClient()
    protected val moshi = Moshi.Builder().build()
    protected open fun fetchNews(callback: NewsListener) {}
}