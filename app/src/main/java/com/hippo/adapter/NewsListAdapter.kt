package com.hippo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hippo.data.Story
import com.hippo.news.NewsPreviewActivity
import com.hippo.news.R

class NewsListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<NewsListAdapter.StoryViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var stories = emptyList<Story>() // Cached copy of words

    inner class StoryViewHolder(storyView: View) : RecyclerView.ViewHolder(storyView),
        View.OnClickListener {

        val storyTitleTextView: TextView = storyView.findViewById(R.id.story_item_title)
        val storyDescTextView: TextView = storyView.findViewById(R.id.story_item_description)
        val storyImageView: ImageView = storyView.findViewById(R.id.story_item_image)

        init {
            // View holders handle their own events
            storyView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            // Open the preview activity
            // to view the story in full
            val context = itemView.context
            val showPhotoIntent = Intent(context, NewsPreviewActivity::class.java)
            showPhotoIntent.putExtra(NewsPreviewActivity.KEY_URL, stories[adapterPosition].url)
            context.startActivity(showPhotoIntent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemView = inflater.inflate(R.layout.recycler_story_item, parent, false)
        return StoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val current = stories[position]

        // Set the title
        // Set the description text
        holder.storyTitleTextView.text = current.title
        holder.storyDescTextView.text = current.url

        // Async load the image URL into image view
        // Only load non-empty valid urls
        if (!current.image.isNullOrEmpty()) {
            Glide.with(holder.storyImageView.context)
                .load(current.image)
                .centerCrop()
                .into(holder.storyImageView)
        } else {
            Glide.with(holder.storyImageView.context).clear(holder.storyImageView)
        }
    }

    internal fun setStories(updateStories: List<Story>) {
        this.stories = updateStories
        notifyDataSetChanged()
    }

    override fun getItemCount() = stories.size
}