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
import com.hippo.data.entities.SavedStory
import com.hippo.data.entities.Story
import com.hippo.news.NewsPreviewActivity
import com.hippo.news.R
import java.util.*
import kotlin.collections.ArrayList

class NewsListAdapter internal constructor(context: Context,
                                           storyViewListener: StoryViewHolderListener) :
    RecyclerView.Adapter<NewsListAdapter.StoryViewHolder>() {

    interface StoryViewHolderListener {
        fun onStorySaveClicked(result: Story)
        fun onStoryShareClicked(result: Story)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var stories = emptyList<Story>() // Cached copy of words
    private var storyListener = storyViewListener

    inner class StoryViewHolder(storyView: View) : RecyclerView.ViewHolder(storyView),
        View.OnClickListener {

        val storyTitleText: TextView = storyView.findViewById(R.id.story_item_title)
        val storyDescText: TextView = storyView.findViewById(R.id.story_item_description)
        val storyAuthorDateText: TextView = storyView.findViewById(R.id.story_item_author_date)
        val storyImage: ImageView = storyView.findViewById(R.id.story_item_image)
        val saveIcon: ImageView = storyView.findViewById(R.id.story_item_save)
        val shareIcon: ImageView = storyView.findViewById(R.id.story_item_share)

        init {
            // View holders handle their own events
            storyImage.setOnClickListener(this)
            storyTitleText.setOnClickListener(this)
            saveIcon.setOnClickListener(this)
            shareIcon.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.story_item_image, R.id.story_item_title -> {
                    // Story Clicked for viewing
                    // Open the preview activity
                    // to view the story in full
                    val context = itemView.context
                    val showPhotoIntent = Intent(context, NewsPreviewActivity::class.java)
                    showPhotoIntent.putExtra(
                        NewsPreviewActivity.KEY_URL,
                        stories[adapterPosition].url
                    )
                    context.startActivity(showPhotoIntent)
                }

                // Save
                R.id.story_item_save -> {
                    storyListener.onStorySaveClicked(stories[adapterPosition])
                }

                // Share
                R.id.story_item_share -> {
                    storyListener.onStoryShareClicked(stories[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemView = inflater.inflate(R.layout.news_story_item, parent, false)
        return StoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val curStory = stories[position]

        // Set the title
        // Set the description text
        holder.storyTitleText.text = curStory.title
        holder.storyDescText.text = curStory.url

        // Author | Timestamp
        // Convert time delta from milliseconds to seconds to hours ago
        val curTime = System.currentTimeMillis()
        val timeDeltaDate = Date(curTime - curStory.time)
        // Formatting
        holder.storyAuthorDateText.text =
            holder.itemView.context.getString(
                R.string.story_author_and_date,
                curStory.by,
                timeDeltaDate.toString()
            )

        // Async load the image URL into image view
        // Only load non-empty valid urls
        Glide.with(holder.storyImage.context).clear(holder.storyImage)

        if (!curStory.image.isNullOrEmpty()) {
            // Turn on the image, we found a url
            holder.storyImage.visibility = View.VISIBLE
            Glide.with(holder.storyImage.context)
                .load(curStory.image)
                .fitCenter()
                .into(holder.storyImage)
        } else {
            // Clear the resource
            // Hide the image view so the UI doesn't take up space
            //Glide.with(holder.storyImage.context).clear(holder.storyImage)
            holder.storyImage.visibility = View.GONE
        }
    }

    internal fun setStories(updateStories: List<Story>) {
        this.stories = updateStories
        notifyDataSetChanged()
    }

    internal fun setSavedStories(updateStories: List<SavedStory>) {
        if (updateStories.isNotEmpty()) {
            val convertedList = ArrayList<Story>()
            for (story in updateStories) {
                convertedList.add(
                    Story(
                        story.storyId,
                        story.by, story.descendants,
                        story.id, story.score,
                        story.time, story.title,
                        story.type, story.url,
                        story.source,
                        story.image,
                        0
                    )
                )
            }

            // update
            setStories(convertedList)
        }
    }

    override fun getItemCount() = stories.size
}