package com.example.drawingapp

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.example.drawingapp.storage.ImageDao
import com.example.drawingapp.storage.ImageEntity
import com.example.drawingapp.storage.ImageDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.io.use
import android.content.Context

/*
This class defines the repository used for interfacing with the database.

Using the ImageDao, it can access all images, save an image to the database, and delete an image.
 */
class ImageRepository (
    private val scope: CoroutineScope,
    private val dao: ImageDao,
)
{
    val allImages : Flow<List<ImageEntity>> = dao.getAllImages()



    /*
    Clears all drawings from the db.
     */
    fun clearDB(){
        scope.launch { dao.clearAll();}
    }

    /*
    Saves images. Actual drawing gets saved to the directory as a PNG, filename/path is
    saved in the database.
     */
    fun saveImage(context: Context, bitmap: Bitmap, nameOfFile: String, onSaved: (String) -> Unit){
        scope.launch(Dispatchers.IO) {
            val filename = "drawing_app_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, filename)
            file.outputStream().buffered().use { image: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, image)
            }
            //dao.insertFile(ImageEntity(filepath = file.absolutePath))
            dao.insertFile(ImageEntity(filepath = file.absolutePath, nameOfFile))
            onSaved(file.absolutePath)
        }
    }

    /*
    Deletes an image path from the database.
     */
    fun deleteImage(id :Int){
        scope.launch {
            dao.deleteFile(id)
        }
    }
}