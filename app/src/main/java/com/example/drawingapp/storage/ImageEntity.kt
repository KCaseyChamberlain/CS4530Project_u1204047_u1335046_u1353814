package com.example.drawingapp.storage
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    val filepath: String,
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0)