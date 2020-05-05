package com.hippo.network

import okhttp3.*
import org.json.JSONObject

open class NewsRepository(listener: NewsListener) {
    interface NewsListener { fun onNewsAvailable(results : List<JSONObject>) }

    protected val mCallback = listener;
    protected val mClient = OkHttpClient()
    protected open fun fetchNews() {}
}