package com.hippo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hippo.data.Story
import com.hippo.news.R

class NewsListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<NewsListAdapter.StoryViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var stories = emptyList<Story>() // Cached copy of words

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyTitleTextView: TextView = itemView.findViewById(R.id.story_item_title)
        val storyDescTextView: TextView = itemView.findViewById(R.id.story_item_description)
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
        //holder.storyDescTextView.text = current.
    }

    internal fun setStories(updateStories: List<Story>) {
        this.stories = updateStories
        notifyDataSetChanged()
    }

    override fun getItemCount() = stories.size
}