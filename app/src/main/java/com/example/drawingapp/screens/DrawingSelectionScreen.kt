package com.example.drawingapp.screens

import android.R.attr.bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingApp
import com.example.drawingapp.ImageRepository
import java.util.concurrent.TimeoutException
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.room.util.TableInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.shareImageFile


@Composable
fun DrawingSelectionScreen(navController: NavHostController) {
    val repo = (LocalContext.current.applicationContext as DrawingApp).repository
    //repo.clearDB()  //Temporary for debugging
    Column(modifier  = Modifier
        .fillMaxSize()
        .padding(12.dp),
        verticalArrangement = Arrangement.Bottom) {
        Button(
            onClick = { navController.navigate("draw/new") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New Drawing")
        }
        DrawingList(
            repo,
            onTimeout = {
                navController.navigate("file_select")
            },
            navController

        )
    }
}

@Composable
fun DrawingList(repo: ImageRepository, onTimeout: () -> Unit, navController: NavHostController){
    val drawings by repo.allImages.collectAsState(initial = emptyList())
    val context = LocalContext.current   // <— ADD THIS

    LazyColumn (modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
        items(drawings) { image ->
            val bitmap = BitmapFactory.decodeFile(image.filepath)
            Row {
                Button(onClick = {
                    val encodedPath = Uri.encode(image.filepath)
                    navController.navigate("draw/$encodedPath")
                }){
                    Text(image.fileName)
                }

                // ---- ADD THIS BLOCK (Export button under the name) ----
                Button(onClick = {
                    shareImageFile(context, image.filepath)
                }, modifier = Modifier.padding(start = 8.dp)) {
                    Text("Export")
                }
                // -------------------------------------------------------

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = image.fileName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}