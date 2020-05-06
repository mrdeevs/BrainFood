package com.hippo.news

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class NewsPreviewActivity : AppCompatActivity() {

    private var urlProgress: ProgressBar? = null
    private var previewWebView: WebView? = null

    companion object {
        const val KEY_URL = "keyurl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_preview)

        urlProgress = findViewById(R.id.url_progress)
        previewWebView = findViewById(R.id.preview_webview)

        if (intent != null && intent.getStringExtra(KEY_URL) != null) {
            val storyUrl = intent.getStringExtra(KEY_URL)
            val storyWebView: WebView = findViewById(R.id.preview_webview)

            // Create a new custom story web view to listen on events
            // Load the story url
            storyWebView.webViewClient = StoryWebViewClient()
            storyWebView.loadUrl(storyUrl)

        } else {
            // todo show a failed to load UI instead..try again later UI
        }
    }

    inner class StoryWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showWebView(false)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            showWebView(true)
        }

        private fun showWebView(show: Boolean) {
            // Show the web view based on flag
            previewWebView!!.visibility =
                if (show)
                    View.VISIBLE
                else
                    View.INVISIBLE

            // Show the inverse of web view with the progress bar
            urlProgress!!.visibility =
                if (!show)
                    View.VISIBLE
                else
                    View.GONE
        }
    }
}