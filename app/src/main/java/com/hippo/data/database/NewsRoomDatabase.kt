package com.hippo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hippo.dao.StoryDao
import com.hippo.data.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Story class
@Database(entities = [Story::class], version = 1, exportSchema = false)
abstract class NewsRoomDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao

    /**
     * Used to override the onOpen callback when our database has been created
     * and can be populated using a news network fetch
     * */
    private class NewsRoomDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // todo - launch the news fetcher here, and then in the listener
                    // todo - callback, we'll pass the results to populateDatabase
                    populateDatabase(database.storyDao())
                }
            }
        }

        suspend fun populateDatabase(storiesDao: StoryDao) {
            // Delete all old news here.
            storiesDao.deleteAll()

            // Add sample words.
//            var word = Word("Hello")
//            wordDao.insert(word)
//            word = Word("World!")
//            wordDao.insert(word)

            // TODO: Add your own words!
            // todo
            // todo
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NewsRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NewsRoomDatabase {
            val tempInstance = INSTANCE

            // Return instance if we already have one
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                // Create a database instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsRoomDatabase::class.java,
                    "news_database"
                )
                    .addCallback(NewsRoomDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}