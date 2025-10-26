package com.example.drawingapp.storage
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
DB schema. Entities have a filepath (location), filename (what the user names
the drawing), and uses an id as the primary key.
 */
@Entity(tableName = "images")
data class ImageEntity(
    val filepath: String,
    val fileName:String = "",
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0)