package com.example.drawingapp

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlin.getValue
import com.example.drawingapp.storage.ImageDao
import com.example.drawingapp.storage.ImageEntity
import com.example.drawingapp.storage.ImageDatabase
import io.ktor.client.HttpClient
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*


///Singleton so only one instance is created
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

    //establish repository when called
    val repository by lazy { ImageRepository(scope, db.imageDao()) }

    private val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation){
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}