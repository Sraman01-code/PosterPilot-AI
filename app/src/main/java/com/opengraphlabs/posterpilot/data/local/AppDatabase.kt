package com.opengraphlabs.posterpilot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.opengraphlabs.posterpilot.data.local.history.ExportedPosterDao
import com.opengraphlabs.posterpilot.data.local.history.ExportedPosterEntity

@Database(
    entities = [ExportedPosterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exportedPosterDao(): ExportedPosterDao
}

object AppDatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "posterpilot.db"
            ).build().also { instance = it }
        }
}
