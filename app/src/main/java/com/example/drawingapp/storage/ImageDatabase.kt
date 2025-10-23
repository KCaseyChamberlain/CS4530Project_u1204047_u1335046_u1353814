package com.example.drawingapp.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageEntity::class], version = 2, exportSchema = false)
abstract class ImageDatabase: RoomDatabase() {
    abstract fun imageDao(): ImageDao
}