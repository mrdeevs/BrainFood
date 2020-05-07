package com.hippo.network

import android.util.Log
import com.hippo.data.Story
import com.hippo.utils.HippoUtils
import okhttp3.*
import org.json.JSONObject
import org.jsoup.select.Elements

open class NewsFetcher(listener: NewsListener) {

    // Callback for when the story data is ready from api
    interface NewsListener {
        fun onNewsAvailable(results: List<Story>)
    }

    protected val mCallback = listener
    protected val mClient = OkHttpClient()
    protected val mUtils = HippoUtils()

    protected open fun fetchNews() {}

    protected open fun convertStoryJsonToStories(results: List<JSONObject>): List<Story> {
        return ArrayList()
    }

    /**
     * Takes a https remote URL and returns a list of all image paths (http only, local ignored)
     * that were found in the DOM model / html
     * */
    protected fun extractImagesFromStoryUrl(url: String): List<String>? {
        // Scrape all images out of the url using jSoup
        val allUrlImages: Elements? =
            mUtils.extractImagesFromUrl(url) // Expensive WOW...

        // results container filtered
        val httpImages = ArrayList<String>()

        if (allUrlImages != null) {
            for (img in allUrlImages) {
                Log.e("NewsFetcher", "img found in url: $img")
                // extract attributes i.e. src from the current image element
                // then check for the actual src attribute
                // then we need to check for http to make sure its a valid remote image, not
                // a local path, since they have shown up
                val attributes = img.attributes()

                if (attributes.hasKey("src")) {
                    val srcElement = attributes.get("src")

                    // Allow ONLY http images
                    // Local ones are removed.. we can't show those or load them async here
                    if (srcElement.contains("http") || srcElement.contains("https")) {
                        //Log.e("test", "found HTTP img: $img")
                        httpImages.add(srcElement)
                        break // todo - we need to find the BEST image, not just the first one.....
                    }
                }
            }
        }

        return httpImages
    }
}