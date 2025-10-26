package com.example.drawingapp.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/*
Implements Room Dao, defining data access from database.
 */
@Dao
interface ImageDao {

    /*
    Inserts a reference (ImageEntity) to the db
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file : ImageEntity)

    /*
    Gets all image entities from the db
     */
    @Query("SELECT * FROM images ORDER BY id desc")
    fun getAllImages(): Flow<List<ImageEntity>>

    // Temporary only for debugging
    @Query("DELETE FROM images")
    suspend fun clearAll()

    /*
    Delete's a specified drawing from the db
     */
    @Query("DELETE FROM images WHERE id = :imageId")
    suspend fun deleteFile(imageId: Int)

    /*
    Gets the file names from the db.
     */
    @Query("SELECT fileName FROM images ORDER BY id ASC")
    fun getFileNames(): Flow<List<String>>
}