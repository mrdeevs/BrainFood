package com.hippo.network

import okhttp3.*

open class NewsFetcher {
    open val client = OkHttpClient();
    open fun fetchNews() {}
}