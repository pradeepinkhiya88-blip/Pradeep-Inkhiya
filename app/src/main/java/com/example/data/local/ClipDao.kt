package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM saved_projects ORDER BY createdAt DESC")
    fun getAllProjectsFlow(): Flow<List<SavedProjectEntity>>

    @Query("SELECT * FROM saved_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): SavedProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: SavedProjectEntity)

    @Update
    suspend fun update(project: SavedProjectEntity)

    @Delete
    suspend fun delete(project: SavedProjectEntity)

    @Query("DELETE FROM saved_projects WHERE id = :id")
    suspend fun deleteById(id: String)
}
