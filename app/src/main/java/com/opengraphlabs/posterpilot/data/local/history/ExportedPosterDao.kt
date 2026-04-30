package com.opengraphlabs.posterpilot.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExportedPosterDao {
    @Insert
    suspend fun insert(entity: ExportedPosterEntity)

    @Query("SELECT * FROM exported_posters ORDER BY createdAt DESC")
    fun observeRecent(): Flow<List<ExportedPosterEntity>>
}
