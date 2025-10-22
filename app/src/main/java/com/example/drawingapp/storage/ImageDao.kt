package com.example.drawingapp.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file : ImageEntity)

    @Query("SELECT * FROM images ORDER BY id desc")
    fun getAllImages(): Flow<List<ImageEntity>>
}