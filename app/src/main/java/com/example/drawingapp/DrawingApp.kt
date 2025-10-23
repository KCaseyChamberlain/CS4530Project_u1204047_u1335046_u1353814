package com.example.drawingapp

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlin.getValue
import com.example.drawingapp.storage.ImageDao
import com.example.drawingapp.storage.ImageEntity
import com.example.drawingapp.storage.ImageDatabase
import kotlinx.coroutines.SupervisorJob

///Singleton so only one repository is created
class DrawingApp: Application() {
    val scope =CoroutineScope(SupervisorJob())
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ImageDatabase::class.java,
            "drawing_DB"
        ).build()
        //).fallbackToDestructiveMigration().build()
    }
    val repository by lazy { ImageRepository(scope, db.imageDao()) }
}