package com.hippo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Exception


class BrainUtils {
    /**
     * Takes a url and extracts all the images out of its HTML
     * WARNING: This is very expensive and takes time in the bakground to walk
     * the DOM of a given HTML page and parse it for images tags. This should be done in the
     * background via ro-routine or something
     * */
    fun extractImagesFromUrl(url: String): Elements? {
        val doc: Document
        var images: Elements?
        try {
            // Get all images from the url HTML
            doc = Jsoup.connect(url).get()
            images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]")
//            for (image in images) {
//                System.out.println(
//                    """ src : ${image.attr("src")}
//                        """.trimIndent() )
//                System.out.println("height : " + image.attr("height"))
//                System.out.println("width : " + image.attr("width"))
//                System.out.println("alt : " + image.attr("alt"))
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            images = null
        }

        // Return the results
        return images
    }

    fun internetAvailable(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
    }
}