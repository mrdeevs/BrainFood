package com.hippo.network

import android.util.Log
import com.hippo.data.Story
import com.hippo.utils.HippoUtils
import okhttp3.*
import org.json.JSONObject
import org.jsoup.select.Elements
import java.lang.Exception

open class NewsFetcher(listener: NewsListener) {

    companion object {
        const val MIN_PREVIEW_WIDTH = 650
        const val MIN_PREVIEW_HEIGHT = 900
        const val ATTR_WIDTH = "width"
        const val ATTR_HEIGHT = "height"
        const val ATTR_SRC = "src"
    }

    enum class NewsCategory {
        Newest,
        Best
    }

    // Callback for when the story data is ready from api
    interface NewsListener {
        fun onNewsAvailable(results: List<Story>)
    }

    protected val mCallback = listener
    protected val mClient = OkHttpClient()
    private val mUtils = HippoUtils()

    protected open fun fetchNews(firstStoryIndex: Int, lastStoryIndex: Int, category: NewsCategory) {}

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

        val httpImages = ArrayList<String>()

        if (allUrlImages != null) {
            var maxArea = 0
            for (img in allUrlImages) {
                //Log.e("NewsFetcher", "img found in url: $img")
                // extract attributes i.e. src from the current image element
                // then check for the actual src attribute
                // then we need to check for http to make sure its a valid remote image, not
                // a local path, since they have shown up
                val attributes = img.attributes()

                // Found an image source
                if (attributes.hasKey(ATTR_SRC)) {
                    val srcImgUrlPath = attributes.get(ATTR_SRC)

                    // Allow ONLY http images
                    // Local ones are removed.. we can't show those or load them async here
                    if (srcImgUrlPath.contains("http") || srcImgUrlPath.contains("https")) {
                        var imgWidth: Int
                        var imgHeight: Int

                        // Add this url path to the end of the list
                        httpImages.add(srcImgUrlPath)

                        // Check for width and height
                        // And check for a valid value for both
                        if (attributes.hasDeclaredValueForKey(ATTR_WIDTH)
                            && attributes.hasDeclaredValueForKey(ATTR_HEIGHT)) {
                            // Need a try here because some numbers can be
                            // incorrectly formatted i.e. 'auto' or '230px'..
                            // Ignore those images
                            try {
                                // Parse width and height
                                imgWidth = attributes.get(ATTR_WIDTH).toInt()
                                imgHeight = attributes.get(ATTR_HEIGHT).toInt()

                                // Calculate area
                                // Make sure its at least 250 x 250 in area size to guarantee decent
                                // preview in the main story list
                                val area = imgWidth * imgHeight
                                if (area > maxArea && area >= (MIN_PREVIEW_WIDTH * MIN_PREVIEW_HEIGHT)) {

                                    maxArea = area
                                    // Remove the current http image from the list (from the end)
                                    // And add it to the beginning, that way when we retrieve the first
                                    // item for display later.. it'll always be the best one
                                    if (httpImages.size > 1) {
                                        // ONLY do the remove-and-insert-front if the http
                                        // list has at least 2 items..otherwise it's already been
                                        // inserted at the front
                                        httpImages.remove(srcImgUrlPath)
                                        httpImages.add(0, srcImgUrlPath)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "NewsFetcher",
                                    "Exception occurred while parsing image dimensions. e: "
                                            + e.message
                                )
                            }
                        }
                    }
                }
            }
        }

        return httpImages
    }
}