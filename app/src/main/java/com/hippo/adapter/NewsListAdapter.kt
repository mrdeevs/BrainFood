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
    RecyclerView.Adapter<NewsListAdapter.WordViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var stories = emptyList<Story>() // Cached copy of words

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordItemView: TextView = itemView.findViewById(R.id.story_item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recycler_story_item, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = stories[position]
        holder.wordItemView.text = current.title
    }

    internal fun setStories(updateStories: List<Story>) {
        this.stories = updateStories
        notifyDataSetChanged()
    }

    override fun getItemCount() = stories.size
}